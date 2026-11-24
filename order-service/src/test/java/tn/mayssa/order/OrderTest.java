package tn.mayssa.order;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTest {

    @Test
    void newOrderIsPending() {
        Order order = new Order("client@test.tn", "LAPTOP-01", 2);
        assertThat(order.getStatus()).isEqualTo(Order.Status.PENDING);
    }

    @Test
    void pendingOrderCanBeConfirmed() {
        Order order = new Order("client@test.tn", "LAPTOP-01", 2);
        assertThat(order.resolve(Order.Status.CONFIRMED, null)).isTrue();
        assertThat(order.getStatus()).isEqualTo(Order.Status.CONFIRMED);
        assertThat(order.getUpdatedAt()).isNotNull();
    }

    @Test
    void resolvedOrderCannotChangeAgain() {
        // Protège contre un message Kafka reçu deux fois
        Order order = new Order("client@test.tn", "LAPTOP-01", 2);
        order.resolve(Order.Status.REJECTED, "Stock insuffisant");
        assertThat(order.resolve(Order.Status.CONFIRMED, null)).isFalse();
        assertThat(order.getStatus()).isEqualTo(Order.Status.REJECTED);
        assertThat(order.getReason()).isEqualTo("Stock insuffisant");
    }
}
