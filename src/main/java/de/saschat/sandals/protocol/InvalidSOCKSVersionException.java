package de.saschat.sandals.protocol;

public class InvalidSOCKSVersionException extends RuntimeException {
    public InvalidSOCKSVersionException(int a, int b) {
        super("Expected version: " + b + ", got: " + a);
    }
}
