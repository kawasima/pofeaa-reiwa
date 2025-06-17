package pofeaa.original.behavior.unitofwork;

import java.util.ArrayList;
import java.util.List;

public class UnitOfWork {
    private final MapperRegistry mapperRegistry;

    private final List<Object> newObjects = new ArrayList<>();
    private final List<Object> dirtyObjects = new ArrayList<>();
    private final List<Object> removedObjects = new ArrayList<>();

    public UnitOfWork(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

    public void registerNew(Object object) {
        newObjects.add(object);
    }

    public void registerDirty(Object object) {
        if (!dirtyObjects.contains(object)) {
            dirtyObjects.add(object);
        }
    }

    public void registerRemoved(Object object) {
        if (newObjects.remove(object)) {
            return;
        }
        dirtyObjects.remove(object);
        if (!removedObjects.contains(object)) {
            removedObjects.add(object);
        }
    }

    public void commit() {
        insertNew();
        updateDirty();
        deleteRemoved();
        clear();
    }

    private void clear() {
        newObjects.clear();
        dirtyObjects.clear();
        removedObjects.clear();
    }

    private void insertNew() {
        newObjects.forEach(this::insertObject);
    }

    private void updateDirty() {
        dirtyObjects.forEach(this::updateObject);
    }

    private void deleteRemoved() {
        removedObjects.forEach(this::deleteObject);
    }

    @SuppressWarnings("unchecked")
    private <T> void insertObject(T object) {
        DataMapper<T> mapper = (DataMapper<T>) mapperRegistry.getMapper(object.getClass());
        mapper.insert(object);
    }

    @SuppressWarnings("unchecked")
    private <T> void updateObject(T object) {
        DataMapper<T> mapper = (DataMapper<T>) mapperRegistry.getMapper(object.getClass());
        mapper.update(object);
    }

    @SuppressWarnings("unchecked")
    private <T> void deleteObject(T object) {
        DataMapper<T> mapper = (DataMapper<T>) mapperRegistry.getMapper(object.getClass());
        mapper.delete(object);
    }
}
