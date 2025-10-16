package pofeaa.original.behavior.lazyload;

import java.util.List;

public class ProductsLoader implements ValueLoader<List<Product>> {
    private final Long id;
    private final ProductMapper productMapper;

    public ProductsLoader(Long id, ProductMapper mapper) {
        this.id = id;
        this.productMapper = mapper;
    }

    @Override
    public List<Product> load() {
        return productMapper.findForSupplier(id);
    }
}
