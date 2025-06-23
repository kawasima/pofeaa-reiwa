package pofeaa.combination.domain.repository;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import pofeaa.combination.domain.model.Account;
import pofeaa.combination.domain.model.Identity;
import pofeaa.original.base.money.Money;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
public class SendMoneyController {
    private final AccountRepository accountRepository;

    public SendMoneyController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @PostMapping("/send/{sourceAccountId}/{targetAccountId}/{amount}")
    public boolean sendMoney(@PathVariable("sourceAccountId") Long sourceAccountId,
                          @PathVariable("targetAccountId") Long targetAccountId,
                          @PathVariable("amount") Long amount) {
        Money money = Money.dollars(BigDecimal.valueOf(amount));
        LocalDateTime baselineDate = LocalDateTime.now().minusDays(10);
        Account sourceAccount = accountRepository.getAccount(Identity.of(sourceAccountId), baselineDate);
        Account targetAccount = accountRepository.getAccount(Identity.of(targetAccountId), baselineDate);

        accountRepository.lockAccount(sourceAccount.getId());
        if (!sourceAccount.withdraw(money, targetAccount.getId())) {
            accountRepository.releaseAccount(sourceAccount.getId());
            return false;
        }

        accountRepository.lockAccount(targetAccount.getId());
        if (!targetAccount.deposit(money, sourceAccount.getId())) {
            accountRepository.releaseAccount(sourceAccount.getId());
            accountRepository.releaseAccount(targetAccount.getId());
            return false;
        }


        accountRepository.releaseAccount(sourceAccount.getId());
        accountRepository.releaseAccount(targetAccount.getId());
        
        return true;
    }
}
