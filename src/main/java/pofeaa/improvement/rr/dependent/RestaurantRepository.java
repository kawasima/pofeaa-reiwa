package pofeaa.improvement.rr.dependent;

import org.jooq.DSLContext;
import org.jooq.Record;
import pofeaa.combination.domain.model.Identity;

import java.util.List;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class RestaurantRepository {
    private final DSLContext ctx;

    public RestaurantRepository(DSLContext ctx) {
        this.ctx = ctx;
    }

    public Restaurant find(Identity id) {
        List<Table> tables = findTablesByRestaurantId(id);
        return ctx.select()
                .from("restaurants")
                .where("id = ?", id)
                .fetchOne()
                .map(record -> new Restaurant(
                        Identity.of(record.getValue("id", Long.class)),
                        record.getValue("name", String.class),
                        tables
                ));
    }

    private List<Table> findTablesByRestaurantId(Identity restaurantId) {
        return ctx.select()
                .from("tables")
                .where("restaurant_id = ?", restaurantId)
                .fetch()
                .map(record -> new Table(
                        Identity.of(record.getValue("id", Long.class)),
                        new Restaurant(restaurantId, null, null), // Placeholder for restaurant
                        record.getValue("table_number", Integer.class),
                        record.getValue("number_of_seats", Integer.class)
                ));
    }

    public void save(Restaurant restaurant) {
        if (restaurant.id().isUndecided()) {
            Record record = ctx.insertInto(table("restaurants"))
                    .set(field("name"), restaurant.name())
                    .returning(field("id"))
                    .fetchOne();
            restaurant.id().decide(record.get(field("id", Long.class)));
        } else {
            ctx.update(table("restaurants"))
                    .set(field("name"), restaurant.name())
                    .where(field("id").eq(restaurant.id().asLong()))
                    .execute();
        }
    }
}
