package com.example.app.bluetooth;

import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

@Getter
@ToString
public class BleMessage implements Serializable {

    private byte[] message;
    private long intervalN;

    public BleMessage(byte[] message, long intervalN) {
        this.message = message;
        this.intervalN = intervalN;
    }

    public byte[] toByteArray() {
        return SerializationUtils.serialize(this);
    }

    public static BleMessage fromByteArray(byte[] data) {
        return (BleMessage) SerializationUtils.deserialize(data);
    }
}
