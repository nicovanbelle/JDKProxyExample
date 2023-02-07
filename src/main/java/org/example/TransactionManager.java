package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionManager.class);

    public void start() {
        LOGGER.info("---> Starting Transaction");
    }

    public void commit() {
        LOGGER.info("---> Commit Transaction");
    }

    public void rollback() {
        LOGGER.info("---> Rollback Transaction");
    }

}
