package pofeaa.original.behavior.unitofwork;

public interface DataMapper<T> {
    void insert(T domainObject);
    void update(T domainObject);
    void delete(T domainObject);
}
