package tn.mayssa.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    private OrderRepository repository;
    private KafkaTemplate<String, String> kafka;
    private OrderService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        repository = mock(OrderRepository.class);
        kafka = mock(KafkaTemplate.class);
        service = new OrderService(repository, kafka, new ObjectMapper());
        when(repository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createSavesOrderAndPublishesEvent() {
        Order order = service.create("client@test.tn", "laptop-01", 3);

        assertThat(order.getProductCode()).isEqualTo("LAPTOP-01");
        verify(kafka).send(eq(OrderServiceApplication.ORDERS_CREATED), any(), contains("\"quantity\":3"));
    }

    @Test
    void statusMessageUpdatesOrder() throws Exception {
        Order order = new Order("client@test.tn", "LAPTOP-01", 1);
        when(repository.findById(7L)).thenReturn(Optional.of(order));

        service.onStatusChanged("""
                {"orderId":7,"customerEmail":"client@test.tn","status":"CONFIRMED","reason":null}
                """);

        assertThat(order.getStatus()).isEqualTo(Order.Status.CONFIRMED);
    }
}
