package tn.mayssa.inventory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tn.mayssa.inventory.InventoryService.OrderCreated;
import tn.mayssa.inventory.InventoryService.OrderStatusChanged;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InventoryServiceTest {

    private ProductRepository products;
    private ProcessedOrderRepository processed;
    private InventoryService service;
    private Product laptop;

    @BeforeEach
    void setUp() {
        products = mock(ProductRepository.class);
        processed = mock(ProcessedOrderRepository.class);
        service = new InventoryService(products, processed);
        laptop = new Product("LAPTOP-01", "Ordinateur", 5);
        when(products.findById("LAPTOP-01")).thenReturn(Optional.of(laptop));
    }

    @Test
    void confirmsOrderAndDecreasesStock() {
        OrderStatusChanged result = service.reserve(new OrderCreated(1L, "a@b.tn", "LAPTOP-01", 2)).orElseThrow();

        assertThat(result.status()).isEqualTo("CONFIRMED");
        assertThat(laptop.getStock()).isEqualTo(3);
    }

    @Test
    void rejectsOrderWhenStockIsInsufficient() {
        OrderStatusChanged result = service.reserve(new OrderCreated(2L, "a@b.tn", "LAPTOP-01", 9)).orElseThrow();

        assertThat(result.status()).isEqualTo("REJECTED");
        assertThat(result.reason()).contains("Stock insuffisant");
        assertThat(laptop.getStock()).isEqualTo(5);
    }

    @Test
    void rejectsUnknownProduct() {
        when(products.findById("NOPE")).thenReturn(Optional.empty());
        OrderStatusChanged result = service.reserve(new OrderCreated(3L, "a@b.tn", "NOPE", 1)).orElseThrow();

        assertThat(result.status()).isEqualTo("REJECTED");
    }

    @Test
    void ignoresDuplicateMessages() {
        when(processed.existsById(4L)).thenReturn(true);

        assertThat(service.reserve(new OrderCreated(4L, "a@b.tn", "LAPTOP-01", 1))).isEmpty();
        assertThat(laptop.getStock()).isEqualTo(5);
        verify(processed, never()).save(any());
    }
}
