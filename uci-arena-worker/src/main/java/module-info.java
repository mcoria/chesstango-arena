module net.chesstango.tools.worker.match {
    exports net.chesstango.tools.worker.match;

    requires net.chesstango.uci.arena;
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