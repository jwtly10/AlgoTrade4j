package dev.jwtly10.backtestapi.model.optimisation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.jwtly10.core.optimisation.OptimisationConfig;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;

@Entity
@Table(name = "optimisation_task_tb")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptimisationTask {
    @Transient
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private OptimisationConfig config;

    @Column(nullable = false, columnDefinition = "json", name = "progress_info")
    @JdbcTypeCode(SqlTypes.JSON)
    private ProgressInfo progressInfo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OptimisationState state;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at")
    @CreationTimestamp
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private ZonedDateTime updatedAt;
}