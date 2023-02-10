package org.example.domain.account;

public record AccountId(long accountId) {
    @Override
    public String toString() {
        return "#" + accountId;
    }
}
