package com.solacesystems.poc.conn;

import com.solacesystems.solclientj.core.*;
import com.solacesystems.solclientj.core.event.*;
import com.solacesystems.solclientj.core.handle.*;
import com.solacesystems.solclientj.core.resource.*;

import java.nio.ByteBuffer;
import java.util.logging.Level;

public class SolaceConnector {

    public SolaceConnector() throws IllegalStateException {
        int rc = Solclient.init(new String[0]);
        if (rc != SolEnum.ReturnCode.OK)
            throw new IllegalStateException("Failed to init Solace Client Library");
        Solclient.setLogLevel(Level.INFO);
        rc = Solclient.createContextForHandle(_ctx, new String[0]);
        if (rc != SolEnum.ReturnCode.OK)
            throw new IllegalStateException("Failed to allocate Solace context handle");
        rc = Solclient.createMessageForHandle(_outmsg);
        if (rc != SolEnum.ReturnCode.OK)
            throw new IllegalStateException("Failed to allocate Solace message handle");
        _outmsg.setMessageDeliveryMode(SolEnum.MessageDeliveryMode.PERSISTENT);
    }

    public void destroy() {
        Helper.destroyHandle(_outmsg);
        Helper.destroyHandle(_sess);
        Helper.destroyHandle(_ctx);
    }

    public void ConnectSession(String host, String vpn, String user, String pass, String clientName, SessionEventCallback eventHandler) throws SolclientException {

        final String[] props = new String[20];
        int i = 0;
        props[i++] = SessionHandle.PROPERTIES.HOST;     props[i++] = host;
        props[i++] = SessionHandle.PROPERTIES.VPN_NAME; props[i++] = vpn;
        props[i++] = SessionHandle.PROPERTIES.USERNAME; props[i++] = user;
        props[i++] = SessionHandle.PROPERTIES.PASSWORD; props[i++] = pass;
        props[i++] = SessionHandle.PROPERTIES.CLIENT_NAME; props[i++] = clientName;
        props[i++] = SessionHandle.PROPERTIES.CONNECT_RETRIES; props[i++] = "5";
        props[i++] = SessionHandle.PROPERTIES.CONNECT_TIMEOUT_MS; props[i++] = "1000";
        props[i++] = SessionHandle.PROPERTIES.RECONNECT_RETRIES; props[i++] = "300";
        props[i++] = SessionHandle.PROPERTIES.KEEP_ALIVE_LIMIT; props[i++] = "3";
        props[i++] = SessionHandle.PROPERTIES.KEEP_ALIVE_INT_MS; props[i] = "1000";

        int rc = _ctx.createSessionForHandle(_sess, props, new MessageCallback() {
            public void onMessage(Handle handle) {
                System.out.println("MAYDAY! SHOULD NOT BE ANY DIRECT MESSAGES!");
            }
        }, eventHandler);
        if (rc != SolEnum.ReturnCode.OK)
            throw new IllegalStateException("Failed to create Solace session handle");

        _sess.connect();

        if (!_sess.isCapable(SolEnum.CapabilityName.CAPABILITY_TRANSACTED_SESSION)) {
            throw new IllegalStateException(
                    "Required Capability is not present: Transacted session not supported.");
        }
    }

    public FlowHandle BindQueue(String name, MessageCallback msgHandler, FlowEventCallback flowEventHandler) {
        int i = 0;
        String[] props = new String[8];

        props[i++] = FlowHandle.PROPERTIES.BIND_BLOCKING;  props[i++] = SolEnum.BooleanValue.ENABLE;
        props[i++] = FlowHandle.PROPERTIES.ACKMODE;        props[i++] = SolEnum.AckMode.AUTO;
        props[i++] = FlowHandle.PROPERTIES.ACTIVE_FLOW_IND;props[i++] = SolEnum.BooleanValue.ENABLE;
        props[i++] = FlowHandle.PROPERTIES.START_STATE;    props[i]   = SolEnum.BooleanValue.DISABLE;

        Queue queue = Solclient.Allocator.newQueue(name, null);

        FlowHandle flowHandle = Solclient.Allocator.newFlowHandle();
        int rc = _sess.createFlowForHandle(flowHandle, props, queue, null, msgHandler, flowEventHandler);
        if (rc != SolEnum.ReturnCode.OK)
            throw new IllegalStateException("Failed to create Solace queue binding flow handle");

        return flowHandle;
    }

    public void SendOutput(String sendTopic, ByteBuffer payload) {
        payload.flip();
        _outmsg.setBinaryAttachment(payload);
        _outmsg.deleteApplicationMessageType();
        _outmsg.setDestination(Solclient.Allocator.newTopic(sendTopic));
        //System.out.println("Sending msg to next stage on topic: " + _outmsg.getDestination().getName());
        _sess.send(_outmsg);
    }

    public void SendSentinel(String queueName, String msgType) {
        _outmsg.setDestination(Solclient.Allocator.newQueue(queueName));
        _outmsg.setApplicationMessageType(msgType);
        _sess.send(_outmsg);
    }


    private final ContextHandle _ctx = Solclient.Allocator.newContextHandle();
    private final SessionHandle _sess = Solclient.Allocator.newSessionHandle();
    private final MessageHandle _outmsg = Solclient.Allocator.newMessageHandle();
}
