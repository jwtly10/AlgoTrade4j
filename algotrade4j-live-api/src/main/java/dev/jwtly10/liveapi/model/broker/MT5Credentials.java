package dev.jwtly10.liveapi.model.broker;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jwtly10.liveapi.security.AttributeEncryptor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "mt5_credentials_tb")
@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamicUpdate
public class MT5Credentials {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = AttributeEncryptor.class)
    @Column(name = "password", nullable = true)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // Prevent retuning password in responses
    private String password;

    @Column(name = "server", nullable = false)
    private String server;

    @Column(name = "path", nullable = false)
    private String path;

    @Column(name = "timezone", nullable = false)
    @Enumerated(EnumType.STRING)
    private Timezone timezone;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "broker_id", nullable = false)
    @JsonBackReference
    private BrokerAccount brokerAccount;

    /**
     * Set the password, encrypting it before saving
     * This override ensure we do not re-encrypt the password if it is the same unnecessarily
     *
     * @param password the password to set
     */
    public void setPassword(String password) {
        if (password != null && !password.equals(this.password)) {
            this.password = password;
        }
    }
}