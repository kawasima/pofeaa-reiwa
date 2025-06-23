/**
 * Domain-driven repository pattern implementation demonstrating the combination of 
 * Domain Model and Data Mapper patterns from Patterns of Enterprise Application Architecture.
 * 
 * <h2>Architecture Overview</h2>
 * This package implements a clean architecture approach where:
 * <ul>
 *   <li>Repository interfaces define domain behaviors using domain objects as inputs/outputs</li>
 *   <li>Domain objects encapsulate business logic and maintain their invariants</li>
 *   <li>Data mappers handle object-relational mapping using jOOQ</li>
 *   <li>Controllers orchestrate use cases while maintaining transaction boundaries</li>
 * </ul>
 * 
 * <h2>Key Components</h2>
 * 
 * <h3>Domain Objects</h3>
 * <ul>
 *   <li>{@link pofeaa.combination.domain.model.Account} - Rich domain object representing a bank account with business rules</li>
 *   <li>{@link pofeaa.combination.domain.model.Activity} - Immutable value object representing a financial transaction</li>
 *   <li>{@link pofeaa.combination.domain.model.ActivityWindow} - Collection of activities with domain operations</li>
 *   <li>{@link pofeaa.combination.domain.model.Identity} - Type-safe identifier that can be undecided (for new objects)</li>
 * </ul>
 * 
 * <h3>Repository Pattern</h3>
 * <ul>
 *   <li>{@link pofeaa.combination.domain.repository.AccountRepository} - Interface defining domain-oriented data access</li>
 *   <li>{@link pofeaa.combination.domain.repository.AccountRepositoryImpl} - Implementation using data mappers</li>
 * </ul>
 * 
 * <h3>Data Mapper Pattern</h3>
 * <ul>
 *   <li>{@link pofeaa.combination.domain.repository.AccountMapper} - Maps between Account domain objects and database records</li>
 *   <li>{@link pofeaa.combination.domain.repository.ActivityMapper} - Maps between Activity domain objects and database records</li>
 * </ul>
 * 
 * <h3>Application Services</h3>
 * <ul>
 *   <li>{@link pofeaa.combination.domain.repository.SendMoneyController} - Orchestrates money transfer use case with proper locking</li>
 * </ul>
 * 
 * <h2>Design Principles</h2>
 * 
 * <h3>Domain-Driven Design</h3>
 * <ul>
 *   <li>Repository methods use domain language and operate on domain objects</li>
 *   <li>Business logic is encapsulated within domain objects</li>
 *   <li>Domain objects maintain their invariants and validate business rules</li>
 *   <li>Immutable value objects prevent unintended state changes</li>
 * </ul>
 * 
 * <h3>Separation of Concerns</h3>
 * <ul>
 *   <li>Domain layer is independent of persistence technology</li>
 *   <li>Data mappers isolate SQL/database concerns from domain logic</li>
 *   <li>Controllers handle coordination but delegate business rules to domain objects</li>
 * </ul>
 * 
 * <h3>Testability</h3>
 * <ul>
 *   <li>Repository interfaces enable easy mocking for unit tests</li>
 *   <li>Domain objects can be tested independently of database</li>
 *   <li>Data mappers can be tested with integration tests</li>
 * </ul>
 * 
 * <h2>Patterns Demonstrated</h2>
 * <ul>
 *   <li><strong>Repository</strong> - Encapsulates data access logic behind domain-oriented interface</li>
 *   <li><strong>Data Mapper</strong> - Separates domain objects from database schema</li>
 *   <li><strong>Domain Model</strong> - Rich objects with behavior, not anemic data structures</li>
 *   <li><strong>Value Object</strong> - Immutable objects identified by their values</li>
 *   <li><strong>Identity Object</strong> - Type-safe identifiers with lifecycle management</li>
 *   <li><strong>Unit of Work</strong> - Implicit through account locking mechanisms</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Repository provides domain-oriented interface
 * AccountRepository repository = new AccountRepositoryImpl(accountMapper);
 * 
 * // Domain objects encapsulate business logic
 * Account sourceAccount = repository.getAccount(Identity.of(1L), baselineDate);
 * Account targetAccount = repository.getAccount(Identity.of(2L), baselineDate);
 * 
 * // Business operations are performed on domain objects
 * Money transferAmount = Money.dollars(BigDecimal.valueOf(100));
 * boolean success = sourceAccount.withdraw(transferAmount, targetAccount.getId());
 * if (success) {
 *     targetAccount.deposit(transferAmount, sourceAccount.getId());
 * }
 * }</pre>
 * 
 * @see pofeaa.original.base.money.Money
 * @see pofeaa.original.behavior.unitofwork
 * @author Martin Fowler (original patterns)
 */
package pofeaa.combination.domain.repository;