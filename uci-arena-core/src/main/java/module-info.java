module net.chesstango.arena.core {
    exports net.chesstango.arena.core;
    exports net.chesstango.arena.core.listeners;
    exports net.chesstango.arena.core.matchtypes;
    exports net.chesstango.arena.core.reports;

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
    requires net.chesstango.reports;
}