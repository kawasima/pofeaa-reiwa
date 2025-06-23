# Table Data Gateway Pattern Implementation

## Overview

The `AccountGateway` class has been implemented following the **Table Data Gateway pattern** from Martin Fowler's "Patterns of Enterprise Application Architecture" (PoEAA).

## Key Characteristics

### 1. **RecordSet-Oriented**
- Methods return `Result<AccountsRecord>` (jOOQ's RecordSet equivalent)
- Results can contain zero, one, or many records
- Client code works with collections of records

### 2. **One Instance Per Table**
- Single gateway instance handles all rows in the accounts table
- No per-row instances like in Row Data Gateway

### 3. **SQL Encapsulation**
- All SQL operations are hidden within gateway methods
- Uses jOOQ generated classes for type-safe queries
- Direct table and column references via `ACCOUNTS` table object

### 4. **No Business Logic**
- Pure data access layer
- Only CRUD operations and simple queries
- Business logic belongs in the client code (Transaction Scripts)

## Implementation Details

### Constructor
```java
public AccountGateway(DSLContext ctx) {
    this.ctx = ctx;
    this.accountsTable = ACCOUNTS;
}
```

### Find Operations Return RecordSets
```java
public Result<AccountsRecord> find(Long id) {
    return ctx.selectFrom(accountsTable)
            .where(accountsTable.ID.eq(id))
            .fetch();
}
```

### Batch Operations
```java
public int updateInterestRateForAllSavings(BigDecimal newRate) {
    return ctx.update(accountsTable)
            .set(accountsTable.ANNUAL_INTEREST_RATE, newRate)
            .where(accountsTable.ACCOUNT_TYPE.eq("SAVING"))
            .execute();
}
```

### Working with Records
```java
// Insert using record object
public int insert(AccountsRecord record) {
    return ctx.insertInto(accountsTable)
            .set(record)
            .execute();
}
```

## Usage Examples

### Finding Records
```java
AccountGateway gateway = new AccountGateway(ctx);

// Find single record (returns RecordSet with 0 or 1 record)
Result<AccountsRecord> result = gateway.find(1L);
if (!result.isEmpty()) {
    AccountsRecord account = result.get(0);
    // Process account...
}

// Find multiple records
Result<AccountsRecord> savingAccounts = gateway.findByAccountType("SAVING");
for (AccountsRecord record : savingAccounts) {
    // Process each record...
}
```

### Batch Operations
```java
// Update all savings accounts
int updated = gateway.updateInterestRateForAllSavings(new BigDecimal("0.03"));

// Delete accounts with zero balance
int deleted = gateway.deleteAccountsWithZeroBalance();
```

### Aggregate Queries
```java
// Count operations
int totalAccounts = gateway.count();
int savingAccounts = gateway.countByType("SAVING");

// Sum operations
BigDecimal totalUSD = gateway.sumBalancesByCurrency("USD");
```

## Comparison with Other Patterns

### vs Row Data Gateway
- Table Data Gateway: One instance for entire table, returns RecordSets
- Row Data Gateway: One instance per row, returns single objects

### vs Active Record
- Table Data Gateway: No business logic, pure data access
- Active Record: Combines data access and business logic

### vs Data Mapper
- Table Data Gateway: Works with record-like structures
- Data Mapper: Maps between database and domain objects

## Benefits

1. **Simple and Direct**: Straightforward mapping to database operations
2. **Performance**: Can leverage SQL set operations efficiently
3. **Type Safety**: Uses jOOQ generated classes for compile-time safety
4. **Testable**: Easy to mock or stub for testing

## Limitations

1. **No Object Graph**: Doesn't handle relationships automatically
2. **Procedural Style**: Works with data structures, not domain objects
3. **SQL Leakage**: Client code needs to understand table structure

## When to Use

- Simple CRUD applications
- Transaction Script architectures
- When domain model is simple or non-existent
- Performance-critical batch operations
- Reporting or data analysis scenarios