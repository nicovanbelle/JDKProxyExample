package org.example.domain.account;

import org.example.annotation.Cacheable;
import org.example.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class AccountServiceImpl implements AccountService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountServiceImpl.class);

    @Override
    @Transactional
    public BigDecimal credit(AccountId accountId, BigDecimal amount) {
        LOGGER.info("Crediting account #{} for an amount of {}", accountId.accountId(), amount);
        return BigDecimal.TEN;
    }

    @Override
    @Transactional
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
    @Transactional
    public void internalTargetCall() {
        LOGGER.info("This should run in a transaction! But the proxy isn't aware..");
    }

    @Cacheable
    @Transactional
    public Account get(AccountId accountId) {
        LOGGER.info("Fetching account with id {}", accountId);
        return new Account(accountId, new AccountOwner("Jane", "Doe"));
    }
}
