package com.solacesystems.poc;

import com.solacesystems.poc.conn.Serializer;
import com.solacesystems.poc.conn.SolaceConnector;
import com.solacesystems.solclientj.core.event.FlowEventCallback;
import com.solacesystems.solclientj.core.event.MessageCallback;
import com.solacesystems.solclientj.core.event.SessionEventCallback;
import com.solacesystems.solclientj.core.handle.*;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

public class FunctionalTest {

    //@Test
    public void pubSubTest() {
        ClientOrder order = new ClientOrder(555);
        order.setIsBuy(true);
        order.setQuantity(54.321);
        order.setPrice(12.345);
        order.setInstrument("MSFT");

        ByteBuffer sendBuffer = ByteBuffer.allocate(ClientOrder.SERIALIZED_SIZE);
        final ByteBuffer recvBuffer = ByteBuffer.allocate(ClientOrder.SERIALIZED_SIZE);

        final AtomicInteger received = new AtomicInteger(0);
        int expected = 10;
        SolaceConnector conn = new SolaceConnector();
        conn.ConnectSession("192.168.56.102", "poc_vpn", "test", "test", "app1_inst1",
                new SessionEventCallback() {
                    public void onEvent(SessionHandle sessionHandle) {
                        System.out.println("Session: " + sessionHandle.getSessionEvent());
                    }
                });
        FlowHandle flow = conn.BindQueue("fntest",
                new MessageCallback() {
                    public void onMessage(Handle handle) {
                        MessageSupport ms = (MessageSupport) handle;
                        MessageHandle msg = ms.getRxMessage();
                        recvBuffer.clear();
                        msg.getBinaryAttachment(recvBuffer);
                        System.out.println("MESSAGE LENGTH: " + recvBuffer.limit()
                                            + " POSITION: " + recvBuffer.position());
                        ClientOrder output = Serializer.DeserializeClientOrder(recvBuffer);
                        System.out.println("OUTPUT: " + output);
                        received.incrementAndGet();
                    }
                },
                new FlowEventCallback() {
                    public void onEvent(FlowHandle flowHandle) {
                        System.out.println("Flow event: " + flowHandle.getFlowEvent());
                    }
                });
        flow.start();
        for(int i = 0; i < expected; i++) {
            sendBuffer.clear();
            Serializer.SerializeClientOrder(sendBuffer, order);
            conn.SendOutput("monitor/state", sendBuffer);
        }
        while(received.get() < expected) {
            try {
                Thread.sleep(1000);
            }
            catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("DONE");
    }
}
