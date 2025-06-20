package pofeaa.original.metadata.metadatamapping;

import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

import java.util.List;

public class DataMap<T> {
    private final Class<? extends T> domainClass;
    private final String tableName;
    private final List<ColumnMap<T>> columnMaps;

    public DataMap(Class<? extends T> domainClass, String tableName) {
        this.domainClass = domainClass;
        this.tableName = tableName;
        columnMaps = new java.util.ArrayList<>();
    }

    public Class<?> getDomainClass() {
        return domainClass;
    }

    public Field<?>[] columnList() {
        return columnMaps.stream()
                .map(ColumnMap::getColumnName)
                .map(DSL::field)
                .toArray(Field[]::new);
    }

    public List<ColumnMap<T>> getColumns() {
        return columnMaps;
    }

    public Table<?> getTable() {
        return DSL.table(tableName);
    }

    public void addColumn(String columnName, String type, String fieldName) {
        columnMaps.add(new ColumnMap<>(columnName, fieldName, this));
    }
}
