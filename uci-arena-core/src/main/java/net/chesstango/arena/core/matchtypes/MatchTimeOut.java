package net.chesstango.arena.core.matchtypes;

import lombok.Getter;
import net.chesstango.uci.gui.Controller;

/**
 * @author Mauricio Coria
 */
public class MatchTimeOut extends RuntimeException {

    @Getter
    private final Controller controller;

    public MatchTimeOut(String message, Controller controller) {
        super(message);
        this.controller = controller;
    }
}
