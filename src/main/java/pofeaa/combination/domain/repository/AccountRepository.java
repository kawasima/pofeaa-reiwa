package pofeaa.combination.domain.repository;

import pofeaa.combination.domain.model.Account;
import pofeaa.combination.domain.model.Identity;

import java.time.LocalDateTime;

public interface AccountRepository {
    Account getAccount(Identity id, LocalDateTime baselineDate);

    void lockAccount(Identity id);
    void releaseAccount(Identity id);
}
