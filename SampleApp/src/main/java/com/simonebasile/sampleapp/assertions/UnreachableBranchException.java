package com.simonebasile.sampleapp.assertions;

public class UnreachableBranchException extends RuntimeException {
    public UnreachableBranchException() {
        super("Unreachable branch reached?!");
    }
}
