package tn.mayssa.inventory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class InventoryServiceApplication {

    public static final String ORDERS_CREATED = "orders.created";
    public static final String ORDERS_STATUS = "orders.status";

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }

    // Stock initial de démonstration
    @Bean
    CommandLineRunner seed(ProductRepository products) {
        return args -> {
            if (products.count() == 0) {
                products.save(new Product("LAPTOP-01", "Ordinateur portable 15\"", 10));
                products.save(new Product("PHONE-02", "Smartphone 128 Go", 25));
                products.save(new Product("SCREEN-03", "Écran 27 pouces", 3));
            }
        };
    }
}
