package pofeaa.original.metadata.metadatamapping;

import java.lang.reflect.Field;

public class ColumnMap {
    private String columnName;
    private String fieldName;
    private Field field;
    private DataMap dataMap;

    public ColumnMap(String columnName, String fieldName, DataMap dataMap) {
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
}
