package com.solacesystems.poc;

import com.solacesystems.poc.model.Ordered;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Example client order type with a sequence number.
 */
public class ClientOrder implements Ordered {
    public ClientOrder(int seqId) {
        this.seqId = seqId;
        this._df.setRoundingMode(RoundingMode.CEILING);
    }

    public int getSequenceId() {
        return seqId;
    }

    public boolean isBuy() {
        return buyOrSell;
    }

    public void setIsBuy(boolean buyOrSell) {
        this.buyOrSell = buyOrSell;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public String toStringBrief() {
        return "ClientOrder{" +
                "seqId=" + seqId +
                ", " + (buyOrSell ? 'B' : 'S') +
                "=>'" + instrument + '\'' +
                '}';
    }
    @Override
    public String toString() {
        return "ClientOrder{" +
                "seqId=" + seqId +
                ", buyOrSell=" + (buyOrSell ? 'B' : 'S') +
                ", quantity=" + _df.format(quantity) +
                ", price=" + _df.format(price) +
                ", instrument='" + instrument + '\'' +
                '}';
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientOrder that = (ClientOrder) o;

        return seqId == that.seqId;

    }

    @Override
    public int hashCode() {
        return seqId;
    }

    public static final int SERIALIZED_SIZE = 4 + 1 + 8 + 8 + 16;

    private final int seqId;
    private boolean buyOrSell;
    private double quantity;
    private double price;
    private String instrument;

    final private DecimalFormat _df = new DecimalFormat("#.####");
}
