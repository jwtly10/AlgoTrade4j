package dev.jwtly10.liveapi.model.broker;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import dev.jwtly10.core.model.Broker;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "broker_accounts_tb")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrokerAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "broker_name", nullable = false)
    private String brokerName;

    @Column(name = "broker_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private Broker brokerType;

    @Column(name = "broker_env", nullable = false)
    @Enumerated(EnumType.STRING)
    private BrokerEnv brokerEnv;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "initial_balance", nullable = false)
    private Integer initialBalance;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Add the one-to-one relationship to Mt5Credentials
    @OneToOne(mappedBy = "brokerAccount", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Mt5Credentials mt5Credentials;
}