package pofeaa.original.metadata.metadatamapping;

import org.jooq.DSLContext;

import java.util.Set;

import static org.jooq.impl.DSL.field;

public class PersonMapper extends AbstractMapper<Person>{
    private static final DataMap<Person> DATAMAP;
    static {
        DATAMAP = new DataMap<>(Person.class, "persons");
        DATAMAP.addColumn("id", "Long", "id");
        DATAMAP.addColumn("first_name", "String", "firstName");
        DATAMAP.addColumn("last_name", "String", "lastName");
        DATAMAP.addColumn("number_of_dependents", "Integer", "numberOfDependents");
    }

    protected PersonMapper(DSLContext ctx) {
        super(DATAMAP, ctx);
    }

    public Set<Person> findLastNamesLike(String pattern) {
        System.out.println(ctx.select(dataMap.columnList())
                .from(dataMap.getTable())
                .where(field("last_name").like(pattern))
                .getSQL());
        return ctx.select(dataMap.columnList())
                .from(dataMap.getTable())
                .where(field("last_name").like("%" + pattern + "%"))
                .fetchSet(this::load);
    }
}
