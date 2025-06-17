package pofeaa.original.metadata.metadatamapping;

import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

import java.util.List;

public class DataMap {
    private Class<?> domainClass;
    private String tableName;
    private List<ColumnMap> columnMaps;

    public Class<?> getDomainClass() {
        return domainClass;
    }

    public Field<?>[] columnList() {
        return columnMaps.stream()
                .map(ColumnMap::getColumnName)
                .map(DSL::field)
                .toArray(Field[]::new);
    }

    public Table<?> getTable() {
        return DSL.table(tableName);
    }

    public void addColumn(String columnName, String type, String fieldName) {

    }
}
