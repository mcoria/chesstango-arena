module net.chesstango.arena.worker {
    exports net.chesstango.arena.worker;

    requires net.chesstango.arena.core;
    requires net.chesstango.uci.gui;
    requires net.chesstango.uci.engine;
    requires net.chesstango.uci.proxy;

    requires net.chesstango.gardel;
    requires net.chesstango.engine;
    requires net.chesstango.evaluation;
    requires net.chesstango.search;

    requires com.rabbitmq.client;

    requires org.slf4j;
    requires static lombok;
}