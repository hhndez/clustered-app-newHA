package com.solacesystems.poc.model;

/**
 * A ClusterEventListener is updated by the ClusterModel for every state
 * change related to the application cluster. Relevant state changes include:
 *
 * - HAState change:  from Backup to Active or vice versa
 * - SeqState change: what the application's state is with respect to the state-queue stream;
 *                    e.g. recovering, up-to-date, etc.
 * - State message:   when the LastValueQueue of the cluster has been read to provide
 *                    last known state of the application cluster
 * - Input message:   when a new input message is read by the ClusterConnector
 *
 * @param <InputType> -- input message type
 * @param <OutputType>-- output message type
 */
public interface ClusterEventListener<InputType, OutputType> {

    /**
     * HA State changes can include: Disconnected, Connected, Backup, Active
     *
     * @param oldState -- previous HA State
     * @param newState -- new HA State
     */
    void OnHAStateChange(HAState oldState, HAState newState);

    /**
     * Sequence State changes can include: Connected, Bound, Recovering, RecoveringFromFlow, UpToDate
     *
     * @param oldState -- previous Sequence State
     * @param newState -- new Sequence State
     */
    void OnSeqStateChange(SeqState oldState, SeqState newState);

    /**
     * Called when an output message from the state-queue was read for recovery purposes
     *
     * @param state -- last output value from the member's state queue
     */
    void OnStateMessage(OutputType state);

    /**
     * This is an important method called by the ClusterConnector when it calculates that
     * the cluster instance is up-to-date with the input stream, so every input requires
     * an updated state output.
     *
     * @param input -- the input message driving a potential application state change
     * @return new output state reflecting the input
     */
    OutputType UpdateApplicationState(InputType input);
}
