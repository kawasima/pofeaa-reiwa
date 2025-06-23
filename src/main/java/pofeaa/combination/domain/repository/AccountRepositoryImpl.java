package pofeaa.combination.domain.repository;

import pofeaa.combination.domain.model.Account;
import pofeaa.combination.domain.model.Identity;

import java.time.LocalDateTime;

public class AccountRepositoryImpl implements AccountRepository {
    private final AccountMapper accountMapper;

    public AccountRepositoryImpl(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    @Override
    public Account getAccount(Identity id, LocalDateTime baselineDate) {
        if (id == null || id.isUndecided()) {
            throw new IllegalArgumentException("Account ID must be decided");
        }
        return accountMapper.find(id.asLong());
    }

    @Override
    public void lockAccount(Identity id) {
        // Implementation to lock the account
    }

    @Override
    public void releaseAccount(Identity id) {
        // Implementation to release the account lock
    }

    public void saveActivities(Account account) {
        account.getActivityWindow().getActivities().forEach(activity -> {
            accountMapper.getActivityMapper().insert(activity);
        });
    }
}
