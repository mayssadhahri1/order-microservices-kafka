package tn.mayssa.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "orders")
public class Order {

    public enum Status { PENDING, CONFIRMED, REJECTED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerEmail;

    @Column(nullable = false)
    private String productCode;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    private String reason;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt;

    protected Order() {
    }

    public Order(String customerEmail, String productCode, int quantity) {
        this.customerEmail = customerEmail;
        this.productCode = productCode;
        this.quantity = quantity;
    }

    /** Une commande ne change de statut qu'une seule fois (PENDING → CONFIRMED ou REJECTED). */
    public boolean resolve(Status newStatus, String reason) {
        if (status != Status.PENDING || newStatus == Status.PENDING) {
            return false;
        }
        this.status = newStatus;
        this.reason = reason;
        this.updatedAt = Instant.now();
        return true;
    }

    public Long getId() { return id; }
    public String getCustomerEmail() { return customerEmail; }
    public String getProductCode() { return productCode; }
    public int getQuantity() { return quantity; }
    public Status getStatus() { return status; }
    public String getReason() { return reason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
