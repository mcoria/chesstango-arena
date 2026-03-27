package net.chesstango.uci.gui;

/**
 * @author Mauricio Coria
 */
public class NoResponseException extends RuntimeException {
    public NoResponseException(String message) {
        super(message);
    }
}
