package pofeaa.original.base.recordset;

import am.ik.yavi.arguments.Arguments2Validator;
import am.ik.yavi.arguments.ArgumentsValidators;
import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.StringValidatorBuilder;

/**
 * Domain model for a Person.
 */
public class Person {
    private final String firstName;
    private final String lastName;

    static StringValidator<String> firstNameValidator = StringValidatorBuilder.of("firstName",
                    b -> b.lessThanOrEqual(50))

            .build();
    static StringValidator<String> lastNameValidator = StringValidatorBuilder.of("lastName",
                    b -> b.lessThanOrEqual(50))

            .build();

    static Arguments2Validator<String, String, Person> validator = ArgumentsValidators.split(
            firstNameValidator, lastNameValidator
    ).apply(Person::new);

    private Person(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public static Person of(String firstName, String lastName) {
        return validator.validated(firstName, lastName);
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
