package tn.mayssa.inventory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tn.mayssa.inventory.InventoryService.OrderCreated;
import tn.mayssa.inventory.InventoryService.OrderStatusChanged;

/**
 * Reçoit les nouvelles commandes depuis Kafka.
 * Classe séparée du service : l'appel à reserve() passe ainsi par le proxy Spring
 * et s'exécute bien dans une transaction. Le résultat n'est publié qu'après la validation.
 */
@Component
public class OrderCreatedListener {

    private final InventoryService inventory;
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper json;

    public OrderCreatedListener(InventoryService inventory, KafkaTemplate<String, String> kafka, ObjectMapper json) {
        this.inventory = inventory;
        this.kafka = kafka;
        this.json = json;
    }

    @KafkaListener(topics = InventoryServiceApplication.ORDERS_CREATED, groupId = "inventory-service")
    public void onOrderCreated(String message) throws JsonProcessingException {
        OrderCreated order = json.readValue(message, OrderCreated.class);
        inventory.reserve(order).ifPresent(this::publish);
    }

    private void publish(OrderStatusChanged event) {
        try {
            kafka.send(InventoryServiceApplication.ORDERS_STATUS, String.valueOf(event.orderId()), json.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Événement non sérialisable", e);
        }
    }
}
