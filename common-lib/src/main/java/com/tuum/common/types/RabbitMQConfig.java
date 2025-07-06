package com.tuum.common.types;

/**
 * RabbitMQ Configuration Constants for Exchange, Queues, and Routing Keys
 */
public enum RabbitMQConfig {

    // === Exchange ===
    TUUM_BANKING_EXCHANGE("tuum.banking"),

    ACCOUNTS_EVENTS_QUEUE("accounts-events-queue"),
    ACCOUNTS_ERRORS_QUEUE("accounts-errors-queue"),
    ACCOUNTS_NOTIFICATIONS_QUEUE("accounts-notifications-queue"),

    TRANSACTIONS_EVENTS_QUEUE("transactions-events-queue"),
    TRANSACTIONS_ERRORS_QUEUE("transactions-errors-queue"),
    TRANSACTIONS_NOTIFICATIONS_QUEUE("transactions-notifications-queue"),

    ACCOUNTS_EVENTS_ROUTING_KEY("accounts.events.*"),
    ACCOUNTS_ERRORS_ROUTING_KEY("accounts.errors.*"),
    ACCOUNTS_NOTIFICATIONS_ROUTING_KEY("accounts.notifications.*"),

    TRANSACTIONS_EVENTS_ROUTING_KEY("transactions.events.*"),
    TRANSACTIONS_ERRORS_ROUTING_KEY("transactions.errors.*"),
    TRANSACTIONS_NOTIFICATIONS_ROUTING_KEY("transactions.notifications.*"),

    ACCOUNTS_CREATED_ROUTING_KEY("accounts.events.created"),
    ACCOUNTS_PROCESSED_ROUTING_KEY("accounts.notifications.processed"),
    ACCOUNTS_ERROR_ROUTING_KEY("accounts.errors.processing"),

    TRANSACTIONS_CREATED_ROUTING_KEY("transactions.events.created"),
    TRANSACTIONS_PROCESSED_ROUTING_KEY("transactions.notifications.processed"),
    TRANSACTIONS_ERROR_ROUTING_KEY("transactions.errors.processing");

    private final String value;

    RabbitMQConfig(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
