package org.example;

import org.example.domain.account.Account;
import org.example.domain.account.AccountId;
import org.example.domain.account.AccountService;
import org.example.domain.account.AccountServiceImpl;
import org.example.proxy.CachingProxy;
import org.example.proxy.TransactionalProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException {
        AccountService transactionalProxy = TransactionalProxy.newInstance(new AccountServiceImpl());
        AccountId accountId = new AccountId(465345);


        // Annotated so will run in a transaction and will be committed
        transactionalProxy.credit(accountId, BigDecimal.valueOf(60));

        LOGGER.info("--------------------------------");

        // Not annotated so will not run in a transaction
        transactionalProxy.getBalance(accountId);

        LOGGER.info("--------------------------------");

        // Annotated but throws exception so there will be a rollback
        try {
            transactionalProxy.debit(accountId, BigDecimal.valueOf(40));
        } catch (Exception e) {
            LOGGER.error("{}", e.getCause().getCause().getMessage());
        }

        LOGGER.info("--------------------------------");

        // Show how Transaction is not started when not passing the proxy boundary
        transactionalProxy.generateReports(accountId);

        LOGGER.info("--------------------------------");

        // A bean can be wrapped in proxy's multiple times. Proxy in proxy
        AccountService transactionalCachingProxy = CachingProxy.newInstance(transactionalProxy);

        // Fetch the same account twice. One should result from cache
        Account account1 = transactionalCachingProxy.get(accountId);

        LOGGER.info("--------------------------------");

        // As a result of the method not being invoked due to the caching, also no transactions will be started
        Account account2 = transactionalCachingProxy.get(accountId);

        LOGGER.info("Both objects have the same reference because of caching: {}", account1 == account2);
    }
}
