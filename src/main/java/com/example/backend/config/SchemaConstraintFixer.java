package com.example.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SchemaConstraintFixer {

    private static final Logger log = LoggerFactory.getLogger(SchemaConstraintFixer.class);
    private final JdbcTemplate jdbcTemplate;

    public SchemaConstraintFixer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void dropOutdatedEnumCheckConstraints() {
        try {
            jdbcTemplate.execute("ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_type_check");
            log.info("Successfully dropped outdated notifications_type_check constraint.");
        } catch (Exception e) {
            log.warn("Could not drop notifications_type_check constraint: {}", e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE reports DROP CONSTRAINT IF EXISTS reports_target_type_check");
            jdbcTemplate.execute("ALTER TABLE reports DROP CONSTRAINT IF EXISTS reports_status_check");
        } catch (Exception ignored) {}
    }
}
