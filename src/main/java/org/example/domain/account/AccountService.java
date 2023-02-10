package org.example.domain.account;

import java.math.BigDecimal;

public interface AccountService {
    BigDecimal credit(AccountId accountId, BigDecimal amount);

    BigDecimal debit(AccountId accountId, BigDecimal amount);

    BigDecimal getBalance(AccountId accountId);

    void generateReports(AccountId accountId);

    void internalTargetCall();

    Account get(AccountId accountId);
}
