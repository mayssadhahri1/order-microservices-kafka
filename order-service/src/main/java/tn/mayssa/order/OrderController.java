package tn.mayssa.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    public record CreateOrderRequest(
            @NotBlank @Email String customerEmail,
            @NotBlank String productCode,
            @Min(1) @Max(100) int quantity) {
    }

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    // 202 Accepted : la commande est enregistrée, sa validation se fait en asynchrone
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Order create(@Valid @RequestBody CreateOrderRequest req) {
        return service.create(req.customerEmail(), req.productCode(), req.quantity());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> get(@PathVariable Long id) {
        return ResponseEntity.of(service.find(id));
    }

    @GetMapping
    public List<Order> latest() {
        return service.latest();
    }
}
