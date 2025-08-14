package engine.exceptions;

public class EngineRunException extends Exception {
    public EngineRunException(String message) { super(message); }
    public EngineRunException(String message, Throwable cause) { super(message, cause); }
}
