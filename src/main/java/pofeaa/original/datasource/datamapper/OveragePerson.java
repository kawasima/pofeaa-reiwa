package pofeaa.original.datasource.datamapper;

import am.ik.yavi.arguments.Arguments5Validator;
import am.ik.yavi.validator.Yavi;
import pofeaa.original.base.money.Money;
import pofeaa.original.datasource.PersonName;

import java.util.List;

public final class OveragePerson extends Person {
    private List<Person> dependents;
    private Money annualIncome;

    static Arguments5Validator<Identity, PersonName, Integer, Money, List<Person>, OveragePerson> validator = Yavi.arguments()
            .<Identity>_object("id", b -> b.notNull())
            .<PersonName>_object("name", b -> b.notNull())
            ._integer("age", b -> b.greaterThanOrEqual(18))
            .<Money>_object("annualIncome", b -> b.notNull())
            .<List<Person>>_object("dependents", b -> b.notNull())
            .apply(OveragePerson::new);

    private OveragePerson(Identity id, PersonName name, int age, Money annualIncome, List<Person> dependents) {
        super(id, name, age);
        this.annualIncome = annualIncome;
        this.dependents = dependents;
    }

    public static OveragePerson of(Identity id, PersonName name, int age, Money annualIncome, List<Person> dependents) {
        return validator.validated(id, name, age, annualIncome, dependents);
    }

    public Money getAnnualIncome() {
        return annualIncome;
    }

    public List<Person> getDependents() {
        return dependents;
    }
}
