package dev.jwtly10.liveservice.model;

import dev.jwtly10.marketdata.common.Broker;
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
    @Enumerated(EnumType.STRING)
    private Broker brokerName;

    @Column(name = "broker_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private BrokerType brokerType;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(name = "initial_balance", nullable = false)
    private Integer initialBalance;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}