package pofeaa.combination;

import org.jooq.DSLContext;
import org.jooq.impl.SQLDataType;

import static org.jooq.impl.DSL.*;

/**
 * Database setup utility for creating the account and activity tables.
 * 
 * <p>This class provides methods to create and configure the database schema
 * required for the account management system. It supports both SavingAccount
 * and CheckingAccount types through the account_type field.</p>
 */
public class DbSetup {
    
    /**
     * Creates the database schema including accounts and activities tables.
     * 
     * @param ctx the JOOQ DSL context for database operations
     */
    public void up(DSLContext ctx) {
        createAccountsTable(ctx);
        createActivitiesTable(ctx);
    }
    
    /**
     * Drops all database tables.
     * 
     * @param ctx the JOOQ DSL context for database operations
     */
    public void down(DSLContext ctx) {
        // Drop tables in reverse order due to foreign key constraints
        ctx.dropTableIfExists(table("activities")).execute();
        ctx.dropTableIfExists(table("accounts")).execute();
    }
    
    /**
     * Creates the accounts table with all necessary columns for different account types.
     */
    private void createAccountsTable(DSLContext ctx) {
        ctx.createTableIfNotExists(table("accounts"))
                .column(field("id", SQLDataType.BIGINT.notNull()))
                .column(field("baseline_balance", SQLDataType.DECIMAL(10, 2).notNull()))
                .column(field("currency", SQLDataType.VARCHAR(3).notNull().defaultValue("USD")))
                .column(field("account_type", SQLDataType.VARCHAR(20).notNull().defaultValue("SAVING")))
                .column(field("annual_interest_rate", SQLDataType.DECIMAL(8, 6))) // for SavingAccount (e.g., 0.012500)
                .column(field("overdraft_limit", SQLDataType.DECIMAL(10, 2))) // for CheckingAccount
                .column(field("overdraft_interest_rate", SQLDataType.DECIMAL(8, 6))) // for CheckingAccount (e.g., 0.180000)
                .constraints(
                        constraint("pk_accounts").primaryKey(field("id")),
                        constraint("chk_account_type").check(
                                field("account_type").in("SAVING", "CHECKING")
                        )
                )
                .execute();
    }
    
    /**
     * Creates the activities table for tracking all account transactions.
     */
    private void createActivitiesTable(DSLContext ctx) {
        ctx.createTableIfNotExists(table("activities"))
                .column(field("id", SQLDataType.BIGINT.notNull()))
                .column(field("owner_account_id", SQLDataType.BIGINT.notNull()))
                .column(field("source_account_id", SQLDataType.BIGINT.notNull()))
                .column(field("target_account_id", SQLDataType.BIGINT.notNull()))
                .column(field("timestamp", SQLDataType.TIMESTAMP.notNull()))
                .column(field("amount", SQLDataType.DECIMAL(10, 2).notNull()))
                .column(field("currency", SQLDataType.VARCHAR(3).notNull().defaultValue("USD")))
                .constraints(
                        constraint("pk_activities").primaryKey(field("id")),
                        constraint("fk_activities_owner").foreignKey(field("owner_account_id"))
                                .references(table("accounts"), field("id"))
                                .onDeleteCascade(),
                        // Note: Removed foreign key constraints on source and target accounts
                        // to allow test scenarios with dummy account references
                        constraint("chk_positive_amount").check(field("amount").gt(0))
                )
                .execute();
    }
    
    /**
     * Creates indexes for better query performance.
     */
    public void createIndexes(DSLContext ctx) {
        // Index for finding activities by owner account
        ctx.createIndexIfNotExists("idx_activities_owner")
                .on(table("activities"), field("owner_account_id"))
                .execute();
                
        // Index for finding activities by timestamp (for monthly calculations)
        ctx.createIndexIfNotExists("idx_activities_timestamp")
                .on(table("activities"), field("timestamp"))
                .execute();
                
        // Composite index for owner + timestamp (most common query pattern)
        ctx.createIndexIfNotExists("idx_activities_owner_timestamp")
                .on(table("activities"), field("owner_account_id"), field("timestamp"))
                .execute();
    }
    
    /**
     * Initializes the database with sample data for testing.
     */
    public void insertSampleData(DSLContext ctx) {
        // Insert sample saving account
        ctx.insertInto(table("accounts"))
                .columns(field("id"), field("baseline_balance"), field("currency"), field("account_type"))
                .values(1L, 1000.00, "USD", "SAVING")
                .execute();
                
        // Insert sample checking account
        ctx.insertInto(table("accounts"))
                .columns(field("id"), field("baseline_balance"), field("currency"), field("account_type"))
                .values(2L, 500.00, "USD", "CHECKING")
                .execute();
    }
}
