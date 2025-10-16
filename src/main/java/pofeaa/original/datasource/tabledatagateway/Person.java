package pofeaa.original.datasource.tabledatagateway;

import java.math.BigInteger;

/**
 * Type safe representation of a record set.
 */
public class Person {
    private Long id;
    private String firstName;
    private String lastName;
    private Integer age;
    private BigInteger annualIncome;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public BigInteger getAnnualIncome() {
        return annualIncome;
    }

    public void setAnnualIncome(BigInteger annualIncome) {
        this.annualIncome = annualIncome;
    }
}
