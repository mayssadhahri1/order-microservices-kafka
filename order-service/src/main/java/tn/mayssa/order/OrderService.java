package tn.mayssa.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tn.mayssa.order.Events.OrderCreated;
import tn.mayssa.order.Events.OrderStatusChanged;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orders;
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper json;

    public OrderService(OrderRepository orders, KafkaTemplate<String, String> kafka, ObjectMapper json) {
        this.orders = orders;
        this.kafka = kafka;
        this.json = json;
    }

    @Transactional
    public Order create(String customerEmail, String productCode, int quantity) {
        Order order = orders.save(new Order(customerEmail, productCode.toUpperCase(), quantity));
        var event = new OrderCreated(order.getId(), order.getCustomerEmail(), order.getProductCode(), order.getQuantity());

        // On publie seulement après la validation de la transaction,
        // pour ne jamais annoncer une commande qui n'existe pas en base
        afterCommit(() -> publish(OrderServiceApplication.ORDERS_CREATED, order.getId(), event));
        return order;
    }

    public Optional<Order> find(Long id) {
        return orders.findById(id);
    }

    public List<Order> latest() {
        return orders.findTop50ByOrderByCreatedAtDesc();
    }

    @KafkaListener(topics = OrderServiceApplication.ORDERS_STATUS, groupId = "order-service")
    @Transactional
    public void onStatusChanged(String message) throws JsonProcessingException {
        OrderStatusChanged event = json.readValue(message, OrderStatusChanged.class);
        orders.findById(event.orderId()).ifPresentOrElse(order -> {
            boolean changed = order.resolve(Order.Status.valueOf(event.status()), event.reason());
            log.info("Commande {} → {} (modifiée : {})", order.getId(), event.status(), changed);
        }, () -> log.warn("Statut reçu pour une commande inconnue : {}", event.orderId()));
    }

    private void publish(String topic, Long key, Object event) {
        try {
            // La clé = id de commande : tous les messages d'une commande vont dans la même partition (ordre garanti)
            kafka.send(topic, String.valueOf(key), json.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Événement non sérialisable", e);
        }
    }

    private static void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }
}
