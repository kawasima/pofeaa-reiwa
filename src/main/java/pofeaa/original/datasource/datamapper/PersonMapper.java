package pofeaa.original.datasource.datamapper;

import org.jooq.DSLContext;
import org.jooq.Record;
import pofeaa.original.base.money.Money;
import pofeaa.original.datasource.PersonName;

import java.math.BigDecimal;
import java.util.List;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class PersonMapper {
    private final DSLContext ctx;

    public PersonMapper(DSLContext ctx) {
        this.ctx = ctx;
    }

    public Person find(Identity id) {
        Record record = ctx.select()
                .from(table("persons"))
                .where(field("id").eq(id.getValue()))
                .fetchOne();
        return doLoad(record);
    }

    protected Person doLoad(Record record) {
        if (record == null) {
            return null;
        }

        Long id = record.getValue("id", Long.class);
        String firstName = record.getValue("first_name", String.class);
        String lastName = record.getValue("last_name", String.class);
        int age = record.getValue("age", Integer.class);

        if (age < 18) {
            return UnderagePerson.of(Identity.of(id), PersonName.of(firstName, lastName), age, List.of());
        } else {
            BigDecimal annualIncome = record.getValue("annual_income", BigDecimal.class);
            return OveragePerson.of(Identity.of(id), PersonName.of(firstName, lastName), age, Money.dollars(annualIncome), List.of());
        }
    }
}
