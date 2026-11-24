package tn.mayssa.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tn.mayssa.notification.NotificationService.OrderStatusChanged;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceTest {

    @Test
    void confirmedMessage() {
        String msg = NotificationService.buildMessage(new OrderStatusChanged(12L, "a@b.tn", "CONFIRMED", null));
        assertThat(msg).contains("n°12").contains("confirmée");
    }

    @Test
    void rejectedMessageContainsReason() {
        String msg = NotificationService.buildMessage(new OrderStatusChanged(13L, "a@b.tn", "REJECTED", "Stock insuffisant"));
        assertThat(msg).contains("Stock insuffisant");
    }

    @Test
    void kafkaMessageIsStoredAsNotification() throws Exception {
        NotificationRepository repo = mock(NotificationRepository.class);
        when(repo.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));
        NotificationService service = new NotificationService(repo, new ObjectMapper());

        service.onStatusChanged("{\"orderId\":5,\"customerEmail\":\"client@test.tn\",\"status\":\"CONFIRMED\",\"reason\":null}");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().getRecipient()).isEqualTo("client@test.tn");
        assertThat(captor.getValue().getOrderId()).isEqualTo(5L);
    }
}
