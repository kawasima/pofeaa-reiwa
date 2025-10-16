package pofeaa.original.behavior.lazyload;

public class SupplierLoader implements ValueLoader<Supplier> {
    private final Long id;
    private final SupplierMapper supplierMapper;

    public SupplierLoader(Long id, SupplierMapper supplierMapper) {
        this.id = id;
        this.supplierMapper = supplierMapper;
    }

    @Override
    public Supplier load() {
        return supplierMapper.findById(id);
    }
}
