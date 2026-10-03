package org.tovasha.ych.script;

public class ContinueException extends RuntimeException {
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
