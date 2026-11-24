package tn.mayssa.inventory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class InventoryService {

    public record OrderCreated(Long orderId, String customerEmail, String productCode, int quantity) {
    }

    public record OrderStatusChanged(Long orderId, String customerEmail, String status, String reason) {
    }

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final ProductRepository products;
    private final ProcessedOrderRepository processed;

    public InventoryService(ProductRepository products, ProcessedOrderRepository processed) {
        this.products = products;
        this.processed = processed;
    }

    /**
     * Réserve le stock d'une commande.
     * Retourne le résultat à publier, ou vide si la commande a déjà été traitée.
     */
    @Transactional
    public Optional<OrderStatusChanged> reserve(OrderCreated order) {
        if (processed.existsById(order.orderId())) {
            log.info("Commande {} déjà traitée, message ignoré", order.orderId());
            return Optional.empty();
        }
        processed.save(new ProcessedOrder(order.orderId()));

        Optional<Product> product = products.findById(order.productCode());
        OrderStatusChanged result;
        if (product.isEmpty()) {
            result = rejected(order, "Produit inconnu : " + order.productCode());
        } else if (!product.get().canReserve(order.quantity())) {
            result = rejected(order, "Stock insuffisant (disponible : " + product.get().getStock() + ")");
        } else {
            product.get().reserve(order.quantity());
            result = new OrderStatusChanged(order.orderId(), order.customerEmail(), "CONFIRMED", null);
        }
        log.info("Commande {} → {}", order.orderId(), result.status());
        return Optional.of(result);
    }

    private static OrderStatusChanged rejected(OrderCreated order, String reason) {
        return new OrderStatusChanged(order.orderId(), order.customerEmail(), "REJECTED", reason);
    }
}
