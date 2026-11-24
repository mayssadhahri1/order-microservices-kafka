package tn.mayssa.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public record OrderStatusChanged(Long orderId, String customerEmail, String status, String reason) {
    }

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notifications;
    private final ObjectMapper json;

    public NotificationService(NotificationRepository notifications, ObjectMapper json) {
        this.notifications = notifications;
        this.json = json;
    }

    // Groupe différent de order-service : chaque service reçoit sa propre copie du message
    @KafkaListener(topics = "orders.status", groupId = "notification-service")
    public void onStatusChanged(String message) throws JsonProcessingException {
        OrderStatusChanged event = json.readValue(message, OrderStatusChanged.class);
        Notification saved = notifications.save(new Notification(event.orderId(), event.customerEmail(), buildMessage(event)));
        // En production, on enverrait ici un email ou un SMS
        log.info("Notification envoyée à {} : {}", saved.getRecipient(), saved.getMessage());
    }

    static String buildMessage(OrderStatusChanged event) {
        if ("CONFIRMED".equals(event.status())) {
            return "Bonne nouvelle : votre commande n°" + event.orderId() + " est confirmée et sera bientôt expédiée.";
        }
        return "Votre commande n°" + event.orderId() + " n'a pas pu être validée. Motif : " + event.reason() + ".";
    }
}
