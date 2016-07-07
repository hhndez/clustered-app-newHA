package com.solacesystems.poc.model;

/**
 * Represents the application cluster member state with respect to the input stream sequence.
 */
public enum SeqState {
    /**
     * The instance is in initial state, before connecting, binding, or reading
     */
    INIT,
    /**
     * The instance is connected to the cluster but has not yet bound to queues
     */
    CONNECTED,
    /**
     * The instance is connected to the cluster and bound to input and state queues
     */
    BOUND,
    /**
     * The instance is connected to the cluster and reading from the state queue while waiting for the ACTIVE indication
     */
    RECOVERING,
    /**
     * The instance is connected to the cluster and it's application state is caught up with the state-queue flow.
     */
    UP_TO_DATE
}
