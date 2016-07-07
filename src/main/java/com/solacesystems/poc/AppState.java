package com.solacesystems.poc;

import com.solacesystems.poc.model.Ordered;

/**
 * Example application state with a sequence number
 */
public class AppState implements Ordered {
    public AppState(String instrument) {
        _instrument = instrument;
    }

    public void addOrder(ClientOrder order) {
        _sequenceId = order.getSequenceId();
    }

    public int getSequenceId() {
        return _sequenceId;
    }
    public void setSequenceId(int sid) {
        _sequenceId = sid;
    }

    public String getInstrument() { return _instrument; }

    @Override
    public String toString() {
        return "AppState{" +
                "='" + _instrument + '\'' +
                ", seqID=" + _sequenceId +
                '}';
    }

    public static final int SERIALIZED_SIZE = 4 + 16;

    final private String _instrument;
    private int _sequenceId;
}
