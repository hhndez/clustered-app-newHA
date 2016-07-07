package com.solacesystems.poc;
import com.solacesystems.poc.conn.SolaceConnector;
import com.solacesystems.poc.conn.Serializer;
import com.solacesystems.solclientj.core.event.SessionEvent;
import com.solacesystems.solclientj.core.event.SessionEventCallback;
import com.solacesystems.solclientj.core.handle.SessionHandle;

import java.nio.ByteBuffer;
import java.util.Random;

public class MockOrderGateway {
    public static void main(String[] args)
    {

        if (args.length < 6)
        {
            System.out.println("USAGE: SamplePublisher <HOST> <VPN> <USER> <PASS> <PUB-TOPIC> <STARTID>");
            return;
        }
        new MockOrderGateway(args[0], args[1], args[2], args[3], args[4], args[5])
                .run();
    }

    private MockOrderGateway(String host, String vpn, String username, String password, String topic, String startId)
    {
        _startOrderId = Integer.parseInt(startId);
        _outTopic = topic;
        _connector = new SolaceConnector();
        _connector.ConnectSession(host, vpn, username, password, "MockOrderGW2", new SessionEventCallback() {
            public void onEvent(SessionHandle sessionHandle) {
                handleSessionEvent(sessionHandle.getSessionEvent());
            }
        });
    }

    private void handleSessionEvent(SessionEvent event)
    {
        // System.out.println("Session event: " + event);
    }

    private void run()
    {
        boolean running = true;
        int orderId = _startOrderId;
        while (running)
        {
            try {
                Thread.sleep(1000);
            } catch(InterruptedException e) {
                e.printStackTrace();
                running = false;
            }
            sendNextOrder(orderId++);
        }
    }

    private ClientOrder nextOrder(int oid) {
        ClientOrder order = new ClientOrder(oid);
        order.setIsBuy(_rand.nextBoolean());
        order.setQuantity(_rand.nextDouble() % 1000);
        order.setPrice(_rand.nextDouble() * 50);
        order.setInstrument("MSFT");
        return order;
    }
    private void sendNextOrder(int oid)
    {
        ClientOrder order = nextOrder(oid);
        System.out.println("Sending msg: " + order);
        _connector.SendOutput(_outTopic, Serializer.SerializeClientOrder(_outbuf, order));
    }

    private final Random _rand = new Random();
    private final int _startOrderId;
    private final String _outTopic;
    private final ByteBuffer _outbuf = ByteBuffer.allocate(ClientOrder.SERIALIZED_SIZE);
    private final SolaceConnector _connector;
}
