package pofeaa.original.behavior.lazyload;

import java.util.List;

public class Supplier {
    private final Long id;
    private final String name;
    private final ValueHolder<List<Product>> products;

    public Supplier(Long id, String name, ProductsLoader productsLoader) {
        this.id = id;
        this.name = name;
        this.products = new ValueHolderImpl<>(productsLoader);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Product> getProducts() {
        return products.getValue();
    }
}
