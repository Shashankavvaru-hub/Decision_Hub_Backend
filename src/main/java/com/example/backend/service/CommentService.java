package com.example.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.dto.CommentDto;
import com.example.backend.dto.CommentRequest;
import com.example.backend.entity.Comment;
import com.example.backend.entity.Decision;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.UnauthorizedActionException;
import com.example.backend.repository.CommentRepository;
import com.example.backend.repository.DecisionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final DecisionRepository decisionRepository;
//    private final NotificationService notificationService;
    
    @Transactional
    public CommentDto createComment(Long decisionId,
                                    CommentRequest request,
                                    User user) {

        Decision decision = decisionRepository.findById(decisionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Decision not found."));

        Comment comment = Comment.builder()
                .decision(decision)
                .user(user)
                .commentText(request.getCommentText())
                .parentComment(null)
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();

        comment = commentRepository.save(comment);
//        notificationService.createCommentNotification(decision, user, comment.getCommentId());

        return convertToDto(comment);
    }
    
    private CommentDto convertToDto(Comment comment) {

        return CommentDto.builder()
                .commentId(comment.getCommentId())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getActualUsername())
                .commentText(comment.getCommentText())
                .createdAt(comment.getCreatedAt())
                .replies(List.of())
                .build();
    }
    
    private CommentDto convertToDtoWithReplies(Comment comment) {

        List<CommentDto> replies = commentRepository
                .findByParentCommentCommentIdOrderByCreatedAtAsc(comment.getCommentId())
                .stream()
                .map(this::convertToDtoWithReplies)
                .toList();

        return CommentDto.builder()
                .commentId(comment.getCommentId())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getActualUsername())
                .commentText(comment.getCommentText())
                .createdAt(comment.getCreatedAt())
                .replies(replies)
                .build();
    }
    
    
    @Transactional
    public CommentDto replyToComment(Long commentId,
                                     CommentRequest request,
                                     User user) {

        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Comment not found."));

        Comment reply = Comment.builder()
                .decision(parentComment.getDecision())
                .user(user)
                .parentComment(parentComment)
                .commentText(request.getCommentText())
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();

        reply = commentRepository.save(reply);
//        notificationService.createCommentNotification(parentComment.getDecision(), user, reply.getCommentId());

        return convertToDto(reply);
    }
    
    @Transactional(readOnly = true)
    public List<CommentDto> getComments(Long decisionId) {

        decisionRepository.findById(decisionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Decision not found."));

        List<Comment> comments = commentRepository
                .findByDecisionIdAndParentCommentIsNullOrderByCreatedAtAsc(decisionId);

        return comments.stream()
                .map(this::convertToDtoWithReplies)
                .toList();
    }
    
    @Transactional
    public CommentDto updateComment(Long commentId,
                                    CommentRequest request,
                                    User user) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Comment not found."));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedActionException(
                    "You can edit only your own comments.");
        }

        comment.setCommentText(request.getCommentText());
        comment.setUpdatedAt(java.time.LocalDateTime.now());

        comment = commentRepository.save(comment);

        return convertToDtoWithReplies(comment);
    }
    
    @Transactional
    public void deleteComment(Long commentId, User user) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Comment not found."));

        boolean isOwner = comment.getUser().getId().equals(user.getId());

        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new UnauthorizedActionException(
                    "You are not authorized to delete this comment.");
        }

        commentRepository.delete(comment);
    }

}