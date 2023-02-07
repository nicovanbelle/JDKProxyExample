package org.example;

public record AccountId(long accountId) {
    @Override
    public String toString() {
        return "#" + accountId;
    }
}
