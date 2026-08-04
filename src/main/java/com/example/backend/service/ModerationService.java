package com.example.backend.service;

import com.example.backend.dto.ModerationActionRequestDto;
import com.example.backend.dto.ReportRequestDto;
import com.example.backend.dto.ReportResponseDto;
import com.example.backend.entity.*;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ModerationService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final DecisionRepository decisionRepository;
    private final CommentRepository commentRepository;
    private final CommunityRepository communityRepository;
    private final DecisionService decisionService;
    private final UserService userService;
    private final CommunityService communityService;
    private final NotificationRepository notificationRepository;

    public ReportResponseDto createReport(ReportRequestDto request, User reporter) {
        String cleanType = request.getTargetType().toUpperCase().trim();
        String title = request.getTargetTitle();
        User reportedUser = null;

        if (request.getReportedUserId() != null) {
            reportedUser = userRepository.findById(request.getReportedUserId()).orElse(null);
        }

        if (request.getTargetId() != null) {
            if ("BOARD".equals(cleanType) || "DECISION".equals(cleanType) || "POLL".equals(cleanType)) {
                Decision d = decisionRepository.findById(request.getTargetId()).orElse(null);
                if (d != null) {
                    if (title == null || title.isBlank()) title = d.getTitle();
                    if (reportedUser == null) reportedUser = d.getUser();
                }
            } else if ("COMMENT".equals(cleanType)) {
                Comment c = commentRepository.findById(request.getTargetId()).orElse(null);
                if (c != null) {
                    if (title == null || title.isBlank()) {
                        title = c.getCommentText() != null && c.getCommentText().length() > 60
                                ? c.getCommentText().substring(0, 60) + "…"
                                : c.getCommentText();
                    }
                    if (reportedUser == null) reportedUser = c.getUser();
                }
            } else if ("COMMUNITY".equals(cleanType)) {
                Community com = communityRepository.findById(request.getTargetId()).orElse(null);
                if (com != null) {
                    if (title == null || title.isBlank()) title = com.getName();
                    if (reportedUser == null) reportedUser = com.getModerator();
                }
            } else if ("USER".equals(cleanType)) {
                User u = userRepository.findById(request.getTargetId()).orElse(null);
                if (u != null) {
                    if (title == null || title.isBlank()) title = u.getActualUsername();
                    reportedUser = u;
                }
            }
        }

        Report report = Report.builder()
                .reporter(reporter)
                .targetType(cleanType)
                .targetId(request.getTargetId())
                .targetTitle(title != null ? title : "Reported Item #" + request.getTargetId())
                .reportedUser(reportedUser)
                .reason(request.getReason())
                .details(request.getDetails())
                .status("PENDING")
                .actionTaken("NONE")
                .build();

        Report saved = reportRepository.save(report);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ReportResponseDto> getReports(String status, String type) {
        List<Report> reports = reportRepository.findAllByOrderByCreatedAtDesc();

        return reports.stream()
                .filter(r -> {
                    if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
                        return status.equalsIgnoreCase(r.getStatus());
                    }
                    return true;
                })
                .filter(r -> {
                    if (type != null && !type.isBlank() && !"ALL".equalsIgnoreCase(type)) {
                        return type.equalsIgnoreCase(r.getTargetType());
                    }
                    return true;
                })
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReportResponseDto getReportById(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));
        return mapToDto(report);
    }

    public ReportResponseDto dismissReport(Long id, User moderator, String notes) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));

        report.setStatus("DISMISSED");
        report.setActionTaken("DISMISSED");
        report.setModerator(moderator);
        report.setModeratorNotes(notes);
        report.setResolvedAt(LocalDateTime.now());

        Report saved = reportRepository.save(report);
        return mapToDto(saved);
    }

    public ReportResponseDto executeAction(Long id, ModerationActionRequestDto request, User moderator) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));

        String action = request.getAction() != null ? request.getAction().toUpperCase().trim() : "NONE";

        switch (action) {
            case "DISMISS":
                report.setStatus("DISMISSED");
                report.setActionTaken("DISMISSED");
                break;

            case "WARN":
                User targetUser = resolveReportedUser(report);
                if (targetUser == null) {
                    throw new BadRequestException("Cannot issue warning: Author or reported user could not be found for this item.");
                }
                report.setReportedUser(targetUser);

                String warnMsg = (request.getWarningMessage() != null && !request.getWarningMessage().isBlank())
                        ? request.getWarningMessage()
                        : "Your content ('" + report.getTargetTitle() + "') was flagged for: " + report.getReason() + ". Please adhere to community guidelines.";

                if (warnMsg.length() > 490) {
                    warnMsg = warnMsg.substring(0, 487) + "...";
                }

                Notification notification = Notification.builder()
                        .receiver(targetUser)
                        .sender(moderator)
                        .type(NotificationType.WARNING)
                        .title("Moderation Warning")
                        .message(warnMsg)
                        .read(false)
                        .createdAt(LocalDateTime.now())
                        .build();
                notificationRepository.save(notification);

                report.setStatus("ACTION_TAKEN");
                report.setActionTaken("WARNED");
                break;

            case "DELETE_CONTENT":
                if (report.getTargetId() != null) {
                    String type = report.getTargetType() != null ? report.getTargetType().toUpperCase() : "";
                    if ("BOARD".equals(type) || "DECISION".equals(type) || "POLL".equals(type)) {
                        try {
                            decisionService.deleteDecision(report.getTargetId(), moderator);
                        } catch (Exception ignored) {}
                    } else if ("COMMENT".equals(type)) {
                        try {
                            commentRepository.deleteById(report.getTargetId());
                        } catch (Exception ignored) {}
                    } else if ("COMMUNITY".equals(type)) {
                        try {
                            communityService.deleteCommunity(report.getTargetId(), moderator);
                        } catch (Exception ignored) {}
                    }
                }
                report.setStatus("ACTION_TAKEN");
                report.setActionTaken("CONTENT_DELETED");
                break;

            case "SUSPEND_USER":
                User suspendUser = resolveReportedUser(report);
                if (suspendUser == null) {
                    throw new BadRequestException("Cannot suspend user: User associated with this report could not be found.");
                }
                report.setReportedUser(suspendUser);
                userService.updateUserStatus(suspendUser.getId(), "SUSPENDED");
                report.setStatus("ACTION_TAKEN");
                report.setActionTaken("USER_SUSPENDED");
                break;

            case "BAN_USER":
                User banUser = resolveReportedUser(report);
                if (banUser == null) {
                    throw new BadRequestException("Cannot ban user: User associated with this report could not be found.");
                }
                report.setReportedUser(banUser);
                userService.updateUserStatus(banUser.getId(), "BANNED");
                report.setStatus("ACTION_TAKEN");
                report.setActionTaken("USER_BANNED");
                break;

            default:
                throw new BadRequestException("Unknown moderation action: " + action);
        }

        report.setModerator(moderator);
        report.setModeratorNotes(request.getModeratorNotes());
        report.setResolvedAt(LocalDateTime.now());

        Report saved = reportRepository.save(report);
        return mapToDto(saved);
    }

    private User resolveReportedUser(Report report) {
        if (report.getReportedUser() != null) {
            return report.getReportedUser();
        }
        if (report.getTargetId() == null) {
            return null;
        }
        String cleanType = report.getTargetType() != null ? report.getTargetType().toUpperCase().trim() : "";
        if ("BOARD".equals(cleanType) || "DECISION".equals(cleanType) || "POLL".equals(cleanType)) {
            return decisionRepository.findById(report.getTargetId()).map(Decision::getUser).orElse(null);
        } else if ("COMMENT".equals(cleanType)) {
            return commentRepository.findById(report.getTargetId()).map(Comment::getUser).orElse(null);
        } else if ("COMMUNITY".equals(cleanType)) {
            return communityRepository.findById(report.getTargetId()).map(Community::getModerator).orElse(null);
        } else if ("USER".equals(cleanType)) {
            return userRepository.findById(report.getTargetId()).orElse(null);
        }
        return null;
    }

    private ReportResponseDto mapToDto(Report r) {
        return ReportResponseDto.builder()
                .id(r.getId())
                .targetType(r.getTargetType())
                .targetId(r.getTargetId())
                .targetTitle(r.getTargetTitle())
                .reporterId(r.getReporter() != null ? r.getReporter().getId() : null)
                .reporterUsername(r.getReporter() != null ? r.getReporter().getActualUsername() : null)
                .reporterEmail(r.getReporter() != null ? r.getReporter().getEmail() : null)
                .reportedUserId(r.getReportedUser() != null ? r.getReportedUser().getId() : null)
                .reportedUsername(r.getReportedUser() != null ? r.getReportedUser().getActualUsername() : null)
                .reportedEmail(r.getReportedUser() != null ? r.getReportedUser().getEmail() : null)
                .reason(r.getReason())
                .details(r.getDetails())
                .status(r.getStatus())
                .actionTaken(r.getActionTaken())
                .moderatorNotes(r.getModeratorNotes())
                .moderatorId(r.getModerator() != null ? r.getModerator().getId() : null)
                .moderatorUsername(r.getModerator() != null ? r.getModerator().getActualUsername() : null)
                .createdAt(r.getCreatedAt())
                .resolvedAt(r.getResolvedAt())
                .build();
    }
}
