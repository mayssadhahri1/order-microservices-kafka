package tn.mayssa.inventory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    public record RestockRequest(@Min(1) int quantity) {
    }

    private final ProductRepository products;

    public ProductController(ProductRepository products) {
        this.products = products;
    }

    @GetMapping
    public List<Product> all() {
        return products.findAll();
    }

    @PostMapping("/{code}/restock")
    @Transactional
    public ResponseEntity<Product> restock(@PathVariable String code, @Valid @RequestBody RestockRequest req) {
        return products.findById(code.toUpperCase())
                .map(p -> {
                    p.restock(req.quantity());
                    return ResponseEntity.ok(p);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
