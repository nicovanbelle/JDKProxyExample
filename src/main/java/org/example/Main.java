package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        RewardRepository myProxy = TransactionalProxy.newInstance(new StubRewardRepositoryImpl());
        AccountId accountId = new AccountId(465345);

        try {

            // Annotated so will run in a transaction and will be committed
            BigDecimal credit = myProxy.credit(accountId, BigDecimal.valueOf(60));

            // Not annotated so will not run in a transaction
            BigDecimal balance = myProxy.getBalance(accountId);

            // Annotated but throws exception so there will be a rollback
            BigDecimal debit = myProxy.debit(accountId, BigDecimal.valueOf(40));

            // Show how Transaction is not started when not passing the proxy boundary
            myProxy.generateReports(accountId);

        } catch (Exception e) {
            LOGGER.error("{}", e.getCause().getCause().getMessage());
        }
    }
}
