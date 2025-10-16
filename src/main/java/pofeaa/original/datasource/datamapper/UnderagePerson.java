package pofeaa.original.datasource.datamapper;

import am.ik.yavi.arguments.Arguments4Validator;
import am.ik.yavi.validator.Yavi;
import pofeaa.original.datasource.PersonName;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class UnderagePerson extends Person{
    private final List<OveragePerson> guardians;

    static Arguments4Validator<Identity, PersonName, Integer, List<OveragePerson>, UnderagePerson> validator = Yavi.arguments()
            .<Identity>_object("id", b -> b.notNull())
            .<PersonName>_object("name", b -> b.notNull())
            ._integer("age", b -> b.greaterThanOrEqual(18))
            .<List<OveragePerson>>_object("guardians", b -> b.notNull())
            .apply(UnderagePerson::new);

    private UnderagePerson(Identity id, PersonName name, int age, List<OveragePerson> guardians) {
        super(id, name, age);
        this.guardians = guardians;
    }

    public Optional<OveragePerson> getDelegate() {
        return guardians.stream().max(Comparator.comparing(p -> p.getAnnualIncome().amount()));
    }

    public static UnderagePerson of(Identity id, PersonName name, int age, List<OveragePerson> guardians) {
        return validator.validated(id, name, age, guardians);
    }
}
