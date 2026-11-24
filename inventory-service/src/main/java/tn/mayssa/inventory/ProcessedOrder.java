package tn.mayssa.inventory;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Garde la trace des commandes déjà traitées.
 * Kafka garantit « au moins une fois » : un message peut arriver deux fois,
 * cette table rend le traitement idempotent (le stock n'est pas réservé deux fois).
 */
@Entity
@Table(name = "processed_orders")
public class ProcessedOrder {

    @Id
    private Long orderId;

    private Instant processedAt = Instant.now();

    protected ProcessedOrder() {
    }

    public ProcessedOrder(Long orderId) {
        this.orderId = orderId;
    }

    public Long getOrderId() { return orderId; }
    public Instant getProcessedAt() { return processedAt; }
}
