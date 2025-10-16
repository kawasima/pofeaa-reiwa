package pofeaa.original.datasource.activerecord;

import am.ik.yavi.arguments.IntegerValidator;
import am.ik.yavi.builder.IntegerValidatorBuilder;
import org.jooq.Record;
import pofeaa.original.datasource.PersonName;

public final class OveragePerson extends Person {
    private OveragePerson(Long id, PersonName name, int age) {
        super(id, name, age);
    }

    static IntegerValidator<Integer> ageValidator = IntegerValidatorBuilder.of("age", b -> b.greaterThanOrEqual(18).lessThanOrEqual(120))
            .build();

    static OveragePerson load(Record record) {
        if (record == null) {
            return null;
        }

        Long id = record.getValue("id", Long.class);
        String firstName = record.getValue("first_name", String.class);
        String lastName = record.getValue("last_name", String.class);
        Integer age = record.getValue("age", Integer.class);

        ageValidator.validate(age);

        return new OveragePerson(id, PersonName.of(firstName, lastName), age);
    }
}
