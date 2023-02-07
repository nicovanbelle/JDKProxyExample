package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class StubRewardRepositoryImpl implements RewardRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(StubRewardRepositoryImpl.class);

    @Override
    @RunInTransaction
    public BigDecimal credit(AccountId accountId, BigDecimal amount) {
        LOGGER.info("Crediting account #{} for an amount of {}", accountId.accountId(), amount);
        return BigDecimal.TEN;
    }

    @Override
    @RunInTransaction
    public BigDecimal debit(AccountId accountId, BigDecimal amount) {
        LOGGER.info("Debiting account {} for an amount of {}", accountId, amount);
        throw new RuntimeException("Could not debit account #" + accountId);
    }

    @Override
    public BigDecimal getBalance(AccountId accountId) {
        LOGGER.info("Getting the balance of account {}", accountId);
        return BigDecimal.valueOf(100);
    }

    @Override
    public void generateReports(AccountId accountId) {
        internalTargetCall();
    }

    @Override
    @RunInTransaction
    public void internalTargetCall() {
        LOGGER.info("This should run in a transaction!");
    }
}
