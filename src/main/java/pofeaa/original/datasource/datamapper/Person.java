package pofeaa.original.datasource.datamapper;

/**
 * Person Domain Class.
 *
 * This class is always valid.
 */
public class Person {
    private Identity id;
    private String firstName;
    private String lastName;
    private Integer numberOfDependents;

    public Person(Identity id, String firstName, String lastName, Integer numberOfDependents) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.numberOfDependents = numberOfDependents;
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

    public Integer getNumberOfDependents() {
        return numberOfDependents;
    }
}
