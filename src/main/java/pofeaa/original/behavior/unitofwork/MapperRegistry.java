package pofeaa.original.behavior.unitofwork;

import java.util.HashMap;
import java.util.Map;

public class MapperRegistry {
    private Map<Class<?>, DataMapper<?>> mappers = new HashMap<>();

    public <T> void registerMapper(Class<? extends T> clazz, DataMapper<T> mapper) {
        mappers.put(clazz, mapper);// Register the mapper with the given name
    }

    public <T> DataMapper<T> getMapper(Class<? extends T> clazz) {
        @SuppressWarnings("unchecked")
        DataMapper<T> mapper = (DataMapper<T>) mappers.get(clazz);
        if (mapper == null) {
            throw new IllegalArgumentException("No mapper registered for class: " + clazz.getName());
        }
        return mapper;
    }
}
