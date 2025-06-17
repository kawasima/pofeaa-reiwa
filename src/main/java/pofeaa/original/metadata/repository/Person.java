package pofeaa.original.metadata.repository;

import pofeaa.original.datasource.datamapper.Identity;

import java.util.List;

public class Person {
    private final Identity id;
    private final String firstName;
    private final String lastName;
    private final int numberOfDependents;

    public Person(Identity id, String firstName, String lastName, int numberOfDependents) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.numberOfDependents = numberOfDependents;
    }

    public List<Person> dependents() {
        return Registry.getPersonRepository().dependentsOf(this);
    }

    public Identity getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getNumberOfDependents() {
        return numberOfDependents;
    }
}
