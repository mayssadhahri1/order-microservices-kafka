package tn.mayssa.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false)
    private Instant sentAt = Instant.now();

    protected Notification() {
    }

    public Notification(Long orderId, String recipient, String message) {
        this.orderId = orderId;
        this.recipient = recipient;
        this.message = message;
    }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public String getRecipient() { return recipient; }
    public String getMessage() { return message; }
    public Instant getSentAt() { return sentAt; }
}
