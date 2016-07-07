package com.solacesystems.poc;

import com.solacesystems.poc.conn.Serializer;
import com.solacesystems.poc.model.ClusteredAppSerializer;
import com.solacesystems.solclientj.core.handle.MessageHandle;

import java.nio.ByteBuffer;

public class SampleAppSerializer implements ClusteredAppSerializer<ClientOrder, AppState> {
    public ClientOrder DeserializeInput(MessageHandle msg) {
        _inmsgbuf.clear();
        msg.getBinaryAttachment(_inmsgbuf);
        return Serializer.DeserializeClientOrder(_inmsgbuf);
    }

    public AppState DeserializeOutput(MessageHandle msg) {
        _lvqmsgbuf.clear();
        msg.getBinaryAttachment(_lvqmsgbuf);
        return Serializer.DeserializeAppState(_lvqmsgbuf);
    }

    public ByteBuffer SerializeOutput(AppState output) {
        Serializer.SerializeAppState(_outmsgbuf, output);
        return _outmsgbuf;
    }

    private final ByteBuffer _lvqmsgbuf = ByteBuffer.allocate(AppState.SERIALIZED_SIZE);
    private final ByteBuffer _inmsgbuf  = ByteBuffer.allocate(ClientOrder.SERIALIZED_SIZE);
    private final ByteBuffer _outmsgbuf = ByteBuffer.allocate(AppState.SERIALIZED_SIZE);
}
