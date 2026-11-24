package tn.mayssa.order;

/** Messages échangés sur Kafka (sérialisés en JSON). */
public final class Events {

    private Events() {
    }

    /** Publié sur « orders.created » quand un client passe commande. */
    public record OrderCreated(Long orderId, String customerEmail, String productCode, int quantity) {
    }

    /** Reçu sur « orders.status » quand le stock a été vérifié. */
    public record OrderStatusChanged(Long orderId, String customerEmail, String status, String reason) {
    }
}
