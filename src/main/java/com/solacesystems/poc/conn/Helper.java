package com.solacesystems.poc.conn;

import com.solacesystems.poc.model.HAState;
import com.solacesystems.poc.model.SeqState;
import com.solacesystems.solclientj.core.handle.Handle;

public class Helper {
    public static void destroyHandle(Handle handle) {
        try {
            if (handle != null && handle.isBound()) {
                handle.destroy();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
