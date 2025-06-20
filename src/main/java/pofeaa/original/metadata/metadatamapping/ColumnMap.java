package pofeaa.original.metadata.metadatamapping;

import java.lang.reflect.Field;

public class ColumnMap<T> {
    private final String columnName;
    private final String fieldName;
    private Field field;
    private final DataMap dataMap;

    public <S> ColumnMap(String columnName, String fieldName, DataMap<S> dataMap) {
        this.columnName = columnName;
        this.fieldName = fieldName;
        this.dataMap = dataMap;
        initField();
    }

    private void initField() {
        try {
            this.field = dataMap.getDomainClass().getDeclaredField(fieldName);
            this.field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Field " + fieldName + " not found in class " + dataMap.getDomainClass().getName(), e);
        }
    }

    public String getColumnName() {
        return columnName;
    }

    public void setField(T domainObject, Object value) {
        try {
            field.set(domainObject, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot set field " + fieldName + " on class " + dataMap.getDomainClass().getName(), e);
        }
    }
}
