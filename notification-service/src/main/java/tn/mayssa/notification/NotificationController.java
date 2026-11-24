package tn.mayssa.notification;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notifications;

    public NotificationController(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    @GetMapping
    public List<Notification> list(@RequestParam(required = false) String recipient) {
        return recipient == null
                ? notifications.findTop50ByOrderBySentAtDesc()
                : notifications.findByRecipientIgnoreCaseOrderBySentAtDesc(recipient);
    }
}
