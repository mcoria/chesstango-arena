module net.chesstango.uci.arena {
    exports net.chesstango.uci.arena;
    exports net.chesstango.uci.arena.listeners;
    exports net.chesstango.uci.arena.matchtypes;

    requires net.chesstango.goyeneche;
    requires net.chesstango.gardel;
    requires net.chesstango.mbeans;
    requires net.chesstango.board;
    requires net.chesstango.engine;
    requires net.chesstango.evaluation;
    requires net.chesstango.search;
    requires net.chesstango.uci.engine;
    requires net.chesstango.uci.proxy;
    requires net.chesstango.uci.gui;

    requires java.management;
    requires org.slf4j;

    requires static lombok;
}