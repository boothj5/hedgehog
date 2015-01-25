package com.boothj5.hedgehog;

public class HegdehogRuntimeException extends RuntimeException {
    public HegdehogRuntimeException(String message) {
        super(message);
    }
    public HegdehogRuntimeException(Exception e) {
        super(e);
    }
}
