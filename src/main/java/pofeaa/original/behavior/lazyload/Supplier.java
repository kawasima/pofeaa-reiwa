package pofeaa.original.behavior.lazyload;

import java.util.List;

public class Supplier {
    private final Long id;
    private final String name;
    private final List<Product> products;

    public Supplier(Long id, String name, List<Product> products) {
        this.id = id;
        this.name = name;
        this.products = products;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Product> getProducts() {
        if (products == null) {
        }
        return products;
    }
}
