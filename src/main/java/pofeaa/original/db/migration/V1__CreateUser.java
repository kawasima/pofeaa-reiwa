package pofeaa.original.db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import static org.jooq.impl.DSL.*;

public class V1__CreateUser extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        DSLContext dsl = DSL.using(context.getConnection());
        
        dsl.createTable("users")
            .column("id", SQLDataType.BIGINT.identity(true))
            .column("username", SQLDataType.VARCHAR(50).nullable(false))
            .column("email", SQLDataType.VARCHAR(100).nullable(false))
            .column("password_hash", SQLDataType.VARCHAR(255).nullable(false))
            .column("created_at", SQLDataType.TIMESTAMP.defaultValue(currentTimestamp()))
            .column("updated_at", SQLDataType.TIMESTAMP.defaultValue(currentTimestamp()))
            .constraints(
                primaryKey("id"),
                unique("username"),
                unique("email")
            )
            .execute();
    }
}
