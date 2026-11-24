package tn.mayssa.order;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

@SpringBootApplication
public class OrderServiceApplication {

    public static final String ORDERS_CREATED = "orders.created";
    public static final String ORDERS_STATUS = "orders.status";

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

    // Les topics sont créés automatiquement au démarrage s'ils n'existent pas
    @Bean
    NewTopic ordersCreatedTopic() {
        return TopicBuilder.name(ORDERS_CREATED).partitions(3).replicas(1).build();
    }

    @Bean
    NewTopic ordersStatusTopic() {
        return TopicBuilder.name(ORDERS_STATUS).partitions(3).replicas(1).build();
    }
}
