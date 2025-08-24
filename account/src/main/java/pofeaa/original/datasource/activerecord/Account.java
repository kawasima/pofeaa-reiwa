package pofeaa.original.datasource.activerecord;

import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.sql.DataSource;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

/**
 * Active Record implementation for the Account domain model.
 * <p>
 * An account represents a financial entity with balance and transaction history.
 * This implementation follows the Active Record pattern where the object
 * encapsulates both domain logic and data access functionality.
 * </p>
 */
public class Account {
    private static final DataSource ds = new HikariDataSource();
    static {
        ((HikariDataSource) ds).setJdbcUrl("jdbc:h2:mem:account_test;DB_CLOSE_DELAY=-1");
        ((HikariDataSource) ds).setUsername("sa");
        ((HikariDataSource) ds).setPassword("");
    }

    private Long id;
    private String accountNumber;
    private String accountHolderName;
    private Double balance;

    /**
     * Constructs a new Account instance with the specified attributes.
     * 
     * @param id the unique identifier for this account, may be null for new instances
     * @param accountNumber the unique account number
     * @param accountHolderName the name of the account holder
     * @param balance the current account balance
     */
    public Account(Long id, String accountNumber, String accountHolderName, Double balance) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    /**
     * Creates and returns a JOOQ DSL context for database operations.
     * 
     * This method provides a configured context using the H2 database dialect
     * and the shared data source connection pool.
     * 
     * @return a DSLContext configured for H2 database operations
     */
    private static DSLContext getContext() {
        return DSL.using(ds, SQLDialect.H2);
    }

    /**
     * Finds and retrieves an Account from the database by their unique identifier.
     * 
     * This method implements the finder pattern of Active Record, querying the
     * accounts table for a record matching the given ID and mapping it to an
     * Account domain object.
     * 
     * @param id the unique identifier of the account to retrieve
     * @return the Account instance if found, null if no account exists with the given ID
     */
    public static Account find(Long id) {
        DSLContext ctx = getContext();
        Record record = ctx.select()
                .from(table("accounts"))
                .where(field("id").eq(id))
                .fetchOne();

        return load(record);
    }

    /**
     * Maps a database record to an Account domain model object.
     * 
     * This method performs the transformation from a Record Set (database row) to 
     * an Account instance, implementing the Active Record pattern. It extracts field 
     * values from the database record and constructs a fully initialized Account object.
     * 
     * @param record the database record containing account data with fields: id, account_number, 
     *               account_holder_name, and balance. May be null.
     * @return a new Account instance populated with data from the record, or null if the 
     *         input record is null
     */
    public static Account load(Record record) {
        if (record == null) {
            return null;
        }
        return new Account(
                record.get(field("id"), Long.class),
                record.get(field("account_number"), String.class),
                record.get("account_holder_name", String.class),
                record.get("balance", Double.class)
        );
    }

    /**
     * Persists the current state of this Account instance to the database.
     * 
     * This method updates the existing database record with the current values
     * of all mutable fields (account_number, account_holder_name, balance).
     * The update is performed based on the account's ID.
     * 
     * @throws RuntimeException if the database update fails
     */
    public void update() {
        DSLContext ctx = getContext();
        ctx.update(table("accounts"))
                .set(field("account_number"), this.accountNumber)
                .set(field("account_holder_name"), this.accountHolderName)
                .set(field("balance"), this.balance)
                .where(field("id").eq(this.id))
                .execute();
    }

    /**
     * Inserts this Account as a new record in the database.
     * 
     * This method creates a new record in the accounts table with the current
     * field values. Upon successful insertion, it updates this instance's ID
     * with the auto-generated value from the database.
     * 
     * @throws RuntimeException if the database insert fails
     */
    public void insert() {
        DSLContext ctx = getContext();
        Record result = ctx.insertInto(table("accounts"))
                .set(field("account_number"), this.accountNumber)
                .set(field("account_holder_name"), this.accountHolderName)
                .set(field("balance"), this.balance)
                .returning(field("id"))
                .fetchOne();
        
        if (result != null) {
            this.id = result.get(field("id"), Long.class);
        }
    }
}
