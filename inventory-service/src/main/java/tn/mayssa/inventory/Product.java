package tn.mayssa.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "products")
public class Product {

    @Id
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int stock;

    // Verrou optimiste : évite que deux réservations simultanées vendent le même article
    @Version
    private Long version;

    protected Product() {
    }

    public Product(String code, String name, int stock) {
        this.code = code;
        this.name = name;
        this.stock = stock;
    }

    public boolean canReserve(int quantity) {
        return quantity > 0 && stock >= quantity;
    }

    public void reserve(int quantity) {
        if (!canReserve(quantity)) {
            throw new IllegalStateException("Stock insuffisant pour " + code);
        }
        stock -= quantity;
    }

    public void restock(int quantity) {
        stock += quantity;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public int getStock() { return stock; }
}
