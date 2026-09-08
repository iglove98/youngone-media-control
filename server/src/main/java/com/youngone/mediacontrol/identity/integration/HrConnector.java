package com.youngone.mediacontrol.identity.integration;

public interface HrConnector {
    HrConnectorType type();
    HrChangePage read(String cursor, int limit);
    default void validateConnection() {}
}