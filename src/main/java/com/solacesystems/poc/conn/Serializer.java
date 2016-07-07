package com.solacesystems.poc.conn;

import com.solacesystems.poc.ClientOrder;
import com.solacesystems.poc.AppState;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

public class Serializer {
    public static ByteBuffer SerializeBool(ByteBuffer buffer, boolean b)
    {
        buffer.clear();
        return internalSerializeBool(buffer, b);
    }
    public static boolean DeserializeBool(ByteBuffer data)
    {
        data.flip();
        return internalDeserializeBool(data);
    }

    public static ByteBuffer SerializeInt(ByteBuffer buffer, int i)
    {
        buffer.clear();
        return internalSerializeInt(buffer, i);
    }
    public static int DeserializeInt(ByteBuffer data)
    {
        data.flip();
        return internalDeserializeInt(data);
    }

    public static ByteBuffer SerializeDouble(ByteBuffer buffer, double d)
    {
        buffer.clear();
        return internalSerializeDouble(buffer, d);
    }
    public static double DeserializeDouble(ByteBuffer data)
    {
        data.flip();
        return internalDeserializeDouble(data);
    }

    public static ByteBuffer SerializeString(ByteBuffer buffer, String s)
    {
        buffer.clear();
        return internalSerializeString(buffer, s);
    }
    public static String DeserializeString(ByteBuffer data) throws UnsupportedEncodingException
    {
        data.flip();
        return internalDeserializeString(data);
    }

    public static ByteBuffer SerializeClientOrder(ByteBuffer buffer, ClientOrder o)
    {
        buffer.clear();
        internalSerializeInt(buffer, o.getSequenceId());
        internalSerializeBool(buffer, o.isBuy());
        internalSerializeDouble(buffer, o.getQuantity());
        internalSerializeDouble(buffer, o.getPrice());
        internalSerializeString(buffer, o.getInstrument());
        return buffer;

    }
    public static ClientOrder DeserializeClientOrder(ByteBuffer data)
    {
        data.flip();
        ClientOrder order = new ClientOrder(internalDeserializeInt(data));
        order.setIsBuy(internalDeserializeBool(data));
        order.setQuantity(internalDeserializeDouble(data));
        order.setPrice(internalDeserializeDouble(data));
        try {
            order.setInstrument(internalDeserializeString(data));
        }
        catch(UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return order;
    }

    public static ByteBuffer SerializeAppState(ByteBuffer buffer, AppState o)
    {
        buffer.clear();
        internalSerializeInt(buffer, o.getSequenceId());
        internalSerializeString(buffer, o.getInstrument());
        return buffer;

    }
    public static AppState DeserializeAppState(ByteBuffer data)
    {
        data.flip();
        try {
            int seqId = internalDeserializeInt(data);
            AppState state = new AppState(internalDeserializeString(data));
            state.setSequenceId(seqId);
            return state;
        }
        catch(UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    ////////////////////////////////////////////////////////////
    ///////////////        INTERNAL METHODS      ///////////////
    ////////////////////////////////////////////////////////////
    public static ByteBuffer internalSerializeBool(ByteBuffer buffer, boolean b)
    {
        return internalSerializeByte(buffer, (byte) (b ? 0x01 : 0x00));
    }
    public static boolean internalDeserializeBool(ByteBuffer data)
    {
        return (byte) 01 == internalDeserializeByte(data);
    }


    private static ByteBuffer internalSerializeByte(ByteBuffer buffer, byte b)
    {
        return buffer.order(ByteOrder.LITTLE_ENDIAN)
                .put(b);
    }
    private static byte internalDeserializeByte(ByteBuffer data)
    {
        return data
                .order(ByteOrder.LITTLE_ENDIAN)
                .get();
    }

    private static ByteBuffer internalSerializeInt(ByteBuffer buffer, int i)
    {
        return buffer.order(ByteOrder.LITTLE_ENDIAN)
                .putInt(i);
    }
    private static int internalDeserializeInt(ByteBuffer data)
    {
        return data
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt();
    }

    private static ByteBuffer internalSerializeDouble(ByteBuffer buffer, double d)
    {
        return buffer.order(ByteOrder.LITTLE_ENDIAN)
                .putDouble(d);
    }
    private static double internalDeserializeDouble(ByteBuffer data)
    {
        return data
                .order(ByteOrder.LITTLE_ENDIAN)
                .getDouble();
    }

    private static ByteBuffer internalSerializeString(ByteBuffer buffer, String s) {
        if (s == null)
            return buffer.order(ByteOrder.LITTLE_ENDIAN).putInt(0);
        byte[] bytes = s.getBytes(Charset.forName("UTF-8"));
        buffer.order(ByteOrder.LITTLE_ENDIAN)
                .putInt(bytes.length)
                .put(bytes);
        return buffer;
    }
    private static String internalDeserializeString(ByteBuffer data) throws UnsupportedEncodingException {
        int len =  data
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt();
        if (len == 0)
            return null;
        byte[] sbytes = new byte[len];
        data.get(sbytes);
        return new String(sbytes, "UTF-8");
    }

}
