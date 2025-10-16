package pofeaa.original.behavior.lazyload;

public class Product {
    private final Long id;
    private final String name;
    private final ValueHolder<Supplier> supplier;

    public Product(Long id, String name, SupplierLoader supplierLoader) {
        this.id = id;
        this.name = name;
        this.supplier = new ValueHolderImpl<>(supplierLoader);

    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Supplier getSupplier() {
        return supplier.getValue();
    }
}
