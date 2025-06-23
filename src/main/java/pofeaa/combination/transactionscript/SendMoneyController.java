package pofeaa.combination.transactionscript;

import org.jooq.DSLContext;
import org.jooq.Result;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import pofeaa.combination.transactionscript.generated.tables.records.AccountsRecord;
import pofeaa.original.base.money.Money;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction Script implementation for money transfer operations.
 * 
 * This controller implements the money transfer use case using the Transaction Script pattern,
 * where all business logic is contained within the service method and uses Table Data Gateways
 * for data access.
 * 
 * Key characteristics:
 * - Business logic is procedural and contained in the service method
 * - Uses Table Data Gateway pattern for data access
 * - No domain objects with behavior - data and logic are separate
 * - Direct database operations through gateways
 */
@RestController
public class SendMoneyController {
    
    private final AccountGateway accountGateway;
    private final ActivityGateway activityGateway;
    
    public SendMoneyController(DSLContext ctx) {
        this.accountGateway = new AccountGateway(ctx);
        this.activityGateway = new ActivityGateway(ctx);
    }
    
    /**
     * Transfers money between two accounts using Transaction Script pattern.
     * 
     * The business logic is contained entirely within this method:
     * 1. Load source and target accounts
     * 2. Calculate current balances including activities
     * 3. Validate withdrawal is possible
     * 4. Create withdrawal and deposit activities
     * 5. Update account balances
     * 
     * @param sourceAccountId The ID of the account to withdraw from
     * @param targetAccountId The ID of the account to deposit to
     * @param amount The amount to transfer (in major currency units)
     * @return true if transfer was successful, false otherwise
     */
    @PostMapping("/send/{sourceAccountId}/{targetAccountId}/{amount}")
    @Transactional
    public boolean sendMoney(@PathVariable("sourceAccountId") Long sourceAccountId,
                             @PathVariable("targetAccountId") Long targetAccountId,
                             @PathVariable("amount") Long amount) {
        
        // Convert amount to Money object
        Money money = Money.dollars(BigDecimal.valueOf(amount));
        LocalDateTime baselineDate = LocalDateTime.now().minusDays(10);
        LocalDateTime now = LocalDateTime.now();
        
        // Load source account
        Result<AccountsRecord> sourceResult = accountGateway.find(sourceAccountId);
        if (sourceResult.isEmpty()) {
            return false;
        }
        AccountsRecord sourceAccount = sourceResult.getFirst();
        
        // Load target account
        Result<AccountsRecord> targetResult = accountGateway.find(targetAccountId);
        if (targetResult.isEmpty()) {
            return false;
        }
        
        // Calculate current balance for source account
        // Balance = baseline + activities after baseline date
        BigDecimal sourceBaselineBalance = sourceAccount.getBaselineBalance();
        BigDecimal sourceActivityBalance = activityGateway.calculateBalanceAfterDate(sourceAccountId, baselineDate);
        BigDecimal sourceCurrentBalance = sourceBaselineBalance.add(sourceActivityBalance);
        
        // Check if withdrawal is possible
        BigDecimal newSourceBalance = sourceCurrentBalance.subtract(money.amount());
        if (newSourceBalance.compareTo(BigDecimal.ZERO) < 0) {
            // Check if it's a checking account with overdraft
            if (!"CHECKING".equals(sourceAccount.getAccountType()) || 
                sourceAccount.getOverdraftLimit() == null ||
                newSourceBalance.abs().compareTo(sourceAccount.getOverdraftLimit()) > 0) {
                return false;
            }
        }
        
        // Create withdrawal activity for source account
        int withdrawalResult = activityGateway.insert(
            sourceAccountId,     // owner
            sourceAccountId,     // source
            targetAccountId,     // target
            now,                 // timestamp
            money.amount(),      // amount
            money.currency().getCurrencyCode()  // currency
        );
        
        if (withdrawalResult != 1) {
            return false;
        }
        
        // Create deposit activity for target account
        int depositResult = activityGateway.insert(
            targetAccountId,     // owner
            sourceAccountId,     // source
            targetAccountId,     // target
            now,                 // timestamp
            money.amount(),      // amount
            money.currency().getCurrencyCode()  // currency
        );
        
        return depositResult == 1;
    }
}
