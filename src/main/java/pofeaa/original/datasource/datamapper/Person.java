package pofeaa.original.datasource.datamapper;

import pofeaa.original.datasource.PersonName;

/**
 * Person Domain Class.
 * This class is always valid.
 */
public abstract sealed class Person permits OveragePerson, UnderagePerson {
    private final Identity id;
    private final PersonName name;
    private final int age;

    protected Person(Identity id, PersonName name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public Identity getId() {
        return id;
    }
    public PersonName getName() {
        return name;
    }
    public int getAge() {
        return age;
    }
}
