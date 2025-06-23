package pofeaa.original.behavior.unitofwork;

import java.util.UUID;

public interface DataMapper<T> {
    T find(UUID id);
    void insert(T domainObject);
    void update(T domainObject);
    void delete(T domainObject);
}
