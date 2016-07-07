package com.solacesystems.poc;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import com.solacesystems.poc.conn.Serializer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SerializerTest {

    @Test
    public void testSerializeAppStateRoundtrip() throws UnsupportedEncodingException {
        AppState stack = new AppState("AAPL");
        ClientOrder sell = new ClientOrder(5);
        sell.setIsBuy(false);
        sell.setQuantity(1.2345);
        sell.setPrice(5.4321);
        sell.setInstrument("AAPL");
        stack.addOrder(sell);

        ByteBuffer dest = ByteBuffer.allocate(AppState.SERIALIZED_SIZE);
        Serializer.SerializeAppState(dest, stack);
        AppState output = Serializer.DeserializeAppState(dest);
        assertEquals(sell.getSequenceId(), output.getSequenceId());
        assertEquals(sell.getInstrument(), output.getInstrument());
    }

    @Test
    public void testSerializeClientOrderRoundtrip() throws UnsupportedEncodingException {
        ClientOrder input = new ClientOrder(5);
        input.setIsBuy(false);
        input.setQuantity(1.2345);
        input.setPrice(5.4321);
        input.setInstrument("AAPL");
        ByteBuffer dest = ByteBuffer.allocate(ClientOrder.SERIALIZED_SIZE);
        Serializer.SerializeClientOrder(dest, input);
        ClientOrder output = Serializer.DeserializeClientOrder(dest);
        assertEquals(input.getSequenceId(), output.getSequenceId());
        assertEquals(input.getQuantity(), output.getQuantity(), 0.00001);
        assertEquals(input.getPrice(), output.getPrice(), 0.00001);
        assertEquals(input.getInstrument(), output.getInstrument());
    }

    @Test
    public void testSerializeEmptyClientOrderRoundtrip() throws UnsupportedEncodingException {
        ClientOrder input = new ClientOrder(5);
        ByteBuffer dest = ByteBuffer.allocate(ClientOrder.SERIALIZED_SIZE);
        Serializer.SerializeClientOrder(dest, input);
        ClientOrder output = Serializer.DeserializeClientOrder(dest);
        assertEquals(input.getSequenceId(), output.getSequenceId());
        assertEquals(input.getQuantity(), output.getQuantity(), 0.00001);
        assertEquals(input.getPrice(), output.getPrice(), 0.00001);
        assertNull(input.getInstrument());
        assertNull(output.getInstrument());
    }

    @Test
    public void testSerializeIntRoundtrip() {
        int input = 5;
        ByteBuffer dest = ByteBuffer.allocate(4);
        Serializer.SerializeInt(dest, input);
        int output = Serializer.DeserializeInt(dest);
        assertEquals(input, output);
    }

    @Test
    public void testMultipleSerializeIntRoundtrip() {
        ByteBuffer dest = ByteBuffer.allocate(4);
        for(int input = 0; input < 10; input++) {
            Serializer.SerializeInt(dest, input);
            int output = Serializer.DeserializeInt(dest);
            assertEquals(input, output);
        }
    }


    @Test
    public void testSerializeDoubleRoundtrip() {
        double input = 5.55;
        ByteBuffer dest = ByteBuffer.allocate(8);
        Serializer.SerializeDouble(dest, input);
        double output = Serializer.DeserializeDouble(dest);
        assertEquals(input, output, 0.001);
    }

    @Test
    public void testMultipleSerializeDoubleRoundtrip() {
        ByteBuffer dest = ByteBuffer.allocate(8);
        for(double input = 0.5; input < 10; input += 1.0) {
            Serializer.SerializeDouble(dest, input);
            double output = Serializer.DeserializeDouble(dest);
            assertEquals(input, output, 0.001);
        }
    }

    @Test
    public void testSerializeStringRoundtrip() throws UnsupportedEncodingException {
        String input = "five";
        ByteBuffer dest = ByteBuffer.allocate(10);
        Serializer.SerializeString(dest, input);
        String output = Serializer.DeserializeString(dest);
        assertEquals(input, output);
    }

    @Test
    public void testMultipleSerializeStringRoundtrip() throws UnsupportedEncodingException {
        ByteBuffer dest = ByteBuffer.allocate(10);
        for(Integer i = 0; i < 10; i++) {
            String input = i.toString();
            Serializer.SerializeString(dest, input);
            String output = Serializer.DeserializeString(dest);
            assertEquals(input, output);
        }
    }

    @Test
    public void testSerializeBoolRoundtrip() {
        boolean input = true;
        ByteBuffer dest = ByteBuffer.allocate(1);
        Serializer.SerializeBool(dest, input);
        boolean output = Serializer.DeserializeBool(dest);
        assertEquals(input, output);
    }

    @Test
    public void testMultipleSerializeBoolRoundtrip() {
        ByteBuffer dest = ByteBuffer.allocate(1);
        boolean input = true;
        for(int i = 0; i < 10; i++) {
            Serializer.SerializeBool(dest, input);
            boolean output = Serializer.DeserializeBool(dest);
            assertEquals(input, output);
            input = !input;
        }
    }
}
