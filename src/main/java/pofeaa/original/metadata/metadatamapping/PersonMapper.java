package pofeaa.original.metadata.metadatamapping;

import org.jooq.Record;
import pofeaa.original.datasource.datamapper.Identity;
import pofeaa.original.datasource.datamapper.Person;

public class PersonMapper extends AbstractMapper<Person>{
    @Override
    public Person load(Record record) {
        Person person =  new Person(Identity.of(record.getValue("id", Long.class)),
                                    record.getValue("first_name", String.class),
                                    record.getValue("last_name", String.class),
                                    record.getValue("number_of_dependents", Integer.class));
    }
}
