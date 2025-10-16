package pofeaa.original.datasource.activerecord;

import org.jooq.Record;
import pofeaa.original.datasource.PersonName;

import java.util.List;

import static org.jooq.impl.DSL.table;

public final class UnderagePerson extends Person{
    private List<OveragePerson> guardians;
    private UnderagePerson(Long id, PersonName name, int age, List<OveragePerson> guardians) {
        super(id, name, age);
        this.guardians = guardians;
    }

    static UnderagePerson load(Record record) {
        if (record == null) {
            return null;
        }

        Long id = record.getValue("id", Long.class);
        String firstName = record.getValue("first_name", String.class);
        String lastName = record.getValue("last_name", String.class);
        Integer age = record.getValue("age", Integer.class);

        List<OveragePerson> guardians = record.get("guardians", List.class);

        return new UnderagePerson(id, PersonName.of(firstName, lastName), age, guardians);
    }

    public void update() {
        getContext().update(table("persons"))
                .set("first_name", getName().firstName())
                .set("last_name", getName().lastName())
                .set("age", getAge())
                .where("id = ?", getId())
                .execute();
    }

    public void insert() {

    }
}
