package dev.jwtly10.api.model.optimisation;

import dev.jwtly10.core.optimisation.StrategyOutput;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "optimisation_results_tb")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptimisationResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "optimisation_task_id", nullable = false)
    private Long optimisationTaskId;

    @Column(name = "parameters", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> parameters;

    @Column(name = "output", nullable = false, columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private StrategyOutput output;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "optimisation_task_id", insertable = false, updatable = false)
    private OptimisationTask optimisationTask;
}