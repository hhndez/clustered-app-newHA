package com.solacesystems.poc.model;

import com.solacesystems.poc.conn.Helper;
import com.solacesystems.poc.conn.SolaceConnector;
import com.solacesystems.solclientj.core.SolEnum;
import com.solacesystems.solclientj.core.SolclientException;
import com.solacesystems.solclientj.core.event.*;
import com.solacesystems.solclientj.core.handle.*;

import java.nio.ByteBuffer;

/**
 * Primary clustering logic performed here. This class connects to a Solace Exclusive Queue
 * and related State-Queue to participate in a cluster of Hot/Warm event handling application
 * instances. This class tracks the following state:
 * - HA State: whether the instance is Active or Backup; Active instances are responsible for
 *             processing input and tracking application state per input and outputting
 *             that state to downstream consumers and peer state-queues. The Backup instances
 *             are responsible for processing State output via their state-queue.
 *
 * - Sequence State: where the application instance is with respect to processing the output
 *                   state from the LIVE member. For example, when an instance joins the cluster
 *                   it will simply process state-queue messages. When it receive ACTIVE flow
 *                   indication on the input-queue it empties the state-queue, changes it's state
 *                   to UP_TO_DATE before starting the input queue flow. As it processes input
 *                   the ACTIVE member publishes state messages to it's standard output topic.
 *
 * - Last known output state from the application
 *
 * - Last known input state to the application
 *
 * @param <InputType> -- input message type
 * @param <OutputType>-- output message type
 */
public class ClusterConnector<InputType, OutputType> {

    ////////////////////////////////////////////////////////////////////////
    //////////            Public Interface                         /////////
    ////////////////////////////////////////////////////////////////////////

    public ClusterConnector(ClusterModel<InputType, OutputType> model,
                            ClusteredAppSerializer<InputType, OutputType> serializer) {
        _model = model;
        _serializer = serializer;
        _connector = new SolaceConnector();
        initState();
    }

    public void Connect(String host, String vpn, String user, String pass, String clientName) throws SolclientException {
        _connector.ConnectSession(host, vpn, user, pass, clientName,
                new SessionEventCallback() {
                    public void onEvent(SessionHandle sessionHandle) {
                        onSessionEvent(sessionHandle.getSessionEvent());
                    }
                });
    }

    public void BindQueues(String inputQueue, String stateQueue) {
        // Wait until the Solace Session is UP before binding to queues
        boolean connected = false;
        _inputQueueName = inputQueue;
        _stateQueueName = stateQueue;
        while(!connected) {
            if (_model.GetHAStatus() == HAState.CONNECTED) {
                // The order of instantiation matters; lvqflow is used for active-flow-ind
                // which triggers recovering state via browser, then starts appflow
                // after recovery completes
                _stateflow = _connector.BindQueue(stateQueue,
                        new MessageCallback() {
                            public void onMessage(Handle handle) {
                                MessageSupport ms = (MessageSupport) handle;
                                onStateMessage(ms.getRxMessage());
                            }
                        },
                        new FlowEventCallback() {
                            public void onEvent(FlowHandle flowHandle) {
                                FlowEvent event = flowHandle.getFlowEvent();
                                System.out.println("STATE-QUEUE FLOW EVENT: " + event);
                            }
                        });
                _inputflow = _connector.BindQueue(inputQueue,
                        new MessageCallback() {
                            public void onMessage(Handle handle) {
                                MessageSupport ms = (MessageSupport) handle;
                                onInputMessage(ms.getRxMessage());
                            }
                        },
                        new FlowEventCallback() {
                            public void onEvent(FlowHandle flowHandle) {
                                FlowEvent event = flowHandle.getFlowEvent();
                                onInputFlowEvent(event);
                            }
                        });
                _model.SetSequenceStatus(SeqState.BOUND);
                _stateflow.start();
                connected = true;
            }
        }
    }

    public void SendSerializedOutput(String topic, ByteBuffer output) {
        // HACK: just wanted to have a nice, standalone web-gui to display these
        _connector.SendOutput(topic, output);
    }

    public void SendOutput(String topic, OutputType output) {
        // If we're the active member of the cluster, we are responsible
        // for all output but don't publish until we have new input data
        if (_model.GetHAStatus() == HAState.ACTIVE && _model.GetSequenceStatus() == SeqState.UP_TO_DATE)
        {
            _connector.SendOutput(topic, _serializer.SerializeOutput(output));
        }
    }

    public void Destroy() {
        if (_inputflow != null) {
            _inputflow.stop();
            Helper.destroyHandle(_inputflow);
        }
        if (_stateflow != null) {
            _stateflow.stop();
            Helper.destroyHandle(_stateflow);
        }
        _connector.destroy();
    }

    ////////////////////////////////////////////////////////////////////////
    //////////            Event Handlers                           /////////
    ////////////////////////////////////////////////////////////////////////

    /**
     * Invoked on the Solace session; this is used to indicate when the
     * connection is UP/Down or reconnecting
     *
     * @param event -- the Solace session connectivity event
     */
    private void onSessionEvent(SessionEvent event) {
        switch(event.getSessionEventCode()) {
            case SolEnum.SessionEventCode.UP_NOTICE:
                _model.SetHAStatus(HAState.CONNECTED);
                _model.SetSequenceStatus(SeqState.CONNECTED);
                break;
            case SolEnum.SessionEventCode.DOWN_ERROR:
                _model.SetHAStatus(HAState.DISCONNECTED);
                _model.SetSequenceStatus(SeqState.INIT);
                break;
            case SolEnum.SessionEventCode.RECONNECTING_NOTICE:
                break;
            case SolEnum.SessionEventCode.RECONNECTED_NOTICE:
                break;
            default:
                break;
        }
    }

    /**
     * Invoked on the application queue flow object when a flow event occurs
     *
     * @param event -- the flow object for the application queue
     */
    private void onInputFlowEvent(FlowEvent event) {
        System.out.println("LVQ flow event: " + event);
        switch (event.getFlowEventEnum())
        {
            case SolEnum.FlowEventCode.UP_NOTICE:
                break;
            case SolEnum.FlowEventCode.ACTIVE:
                becomeActive();
                break;
            case SolEnum.FlowEventCode.INACTIVE:
                becomeBackup();
                break;
            default:
                break;
        }
    }

    /**
     * Invoked on the appflow when an app queue message arrives
     *
     * @param msg -- new solace message from the application queue
     */
    private void onInputMessage(MessageHandle msg) {
        processInputMsg(_serializer.DeserializeInput(msg));
    }

    /**
     * Invoked on the LVQBrowser flowhandle
     *
     * @param msg -- solace msg read from the LVQ
     */
    private void onStateMessage(MessageHandle msg) {
        String msgtype = msg.getApplicationMessageType();
        if (msgtype != null && msgtype.equals(SENTINEL))
            processStateMessage(null, true);
        else
            processStateMessage(_serializer.DeserializeOutput(msg), false);

    }

    ////////////////////////////////////////////////////////////////////////
    //////////          State Transitions                          /////////
    ////////////////////////////////////////////////////////////////////////
    private void initState() {
        _model.SetLastOutput(null);
        _model.SetHAStatus(HAState.DISCONNECTED);
        _model.SetSequenceStatus(SeqState.INIT);
    }

    /**
     * Invoked on the InputFlow flowhandle when message arrives
     *
     * @param state -- a message from the state-queue read to keep up with the Active processor state
     */
    private void processStateMessage(OutputType state, boolean isSentinel) {
        if (isSentinel) {
            System.out.println("RECEIVED SENTINEL MESSAGE; CHANGING STATE TO UP_TO_DATE");
            _model.SetSequenceStatus(SeqState.UP_TO_DATE);
            _inputflow.start(); // if a msg arrives it is passed to processLastOutputMsg (below)
        }
        else {
            if (_model.GetSequenceStatus() != SeqState.UP_TO_DATE && _model.GetSequenceStatus() != SeqState.RECOVERING) {
                _model.SetSequenceStatus(SeqState.RECOVERING);
                _model.SetHAStatus(HAState.BACKUP);
            }
            _model.SetLastOutput(state);
        }
    }

    /**
     * Invoked on the inputflow when an application message arrives. If
     * the current position in the application sequence is up to dote
     * with the last output, then this function calls the application
     * listener to give it a chance to update its current application
     * state and output something representing that state.
     *
     * @param input -- new application input message
     */
    private void processInputMsg(InputType input) {
        // Construct a new app state
        _model.UpdateApplicationState(input);
    }

    /**
     * Invoked on the input when flow UP event occurs or when flow changes
     * from INACTIVE to ACTIVE This function tries to browse the message on
     * the LVQ to recover the last output state from this application
     */
    private void recoverAllState() {
        System.out.println("Recovering all state from the state queue, current sequence state is "
                + _model.GetSequenceStatus());
        _model.SetSequenceStatus(SeqState.RECOVERING);
        System.out.println("SENDING A SENTINEL MESSAGE");
        _connector.SendSentinel(_stateQueueName, SENTINEL);
    }

    /**
     * Invoked on the inputflow when flow ACTIVE event occurs
     */
    private void becomeActive()
    {
        recoverAllState();
        _model.SetHAStatus(HAState.ACTIVE);
    }

    /**
     * Invoked on the inputflow when flow INACTIVE event occurs
     */
    private void becomeBackup()
    {
        _model.SetHAStatus(HAState.BACKUP);
        _inputflow.stop();
    }

    private final static String SENTINEL = "SENTINEL";

    private final SolaceConnector _connector;
    private final ClusterModel<InputType,OutputType> _model;
    private final ClusteredAppSerializer<InputType, OutputType> _serializer;
    private String _inputQueueName;
    private String _stateQueueName;

    private FlowHandle _stateflow;
    private FlowHandle _inputflow;
}
