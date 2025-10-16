package pofeaa.original.datasource;

import am.ik.yavi.arguments.Arguments2Validator;
import am.ik.yavi.arguments.ArgumentsValidators;
import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.StringValidatorBuilder;

public class PersonName {
    private final String firstName;
    private final String lastName;

    static StringValidator<String> firstNameValidator = StringValidatorBuilder.of("firstName", b -> b.notBlank().lessThanOrEqual(50))
            .build();

    static StringValidator<String> lastNameValidator = StringValidatorBuilder.of("lastName", b -> b.notBlank().lessThanOrEqual(50))
            .build();

    static Arguments2Validator<String, String, PersonName> validator = ArgumentsValidators.split(
            firstNameValidator, lastNameValidator
    ).apply(PersonName::new);

    private PersonName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
    public static PersonName of(String firstName, String lastName) {
        return validator.validated(firstName, lastName);
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
