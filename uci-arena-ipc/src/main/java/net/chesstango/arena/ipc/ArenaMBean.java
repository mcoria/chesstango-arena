package net.chesstango.arena.ipc;

/**
 * @author Mauricio Coria
 */
public interface ArenaMBean {
    GameDescriptionInitial getCurrentGame();

    GameDescriptionInitial getGameDescriptionInitial(String gameId);

    GameDescriptionCurrent getGameDescriptionCurrent(String gameId);
}

