package org.example;

import java.math.BigDecimal;

public interface RewardRepository {
    BigDecimal credit(AccountId accountId, BigDecimal amount);

    BigDecimal debit(AccountId accountId, BigDecimal amount);

    BigDecimal getBalance(AccountId accountId);

    void generateReports(AccountId accountId);

    void internalTargetCall();
}
