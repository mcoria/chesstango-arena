module net.chesstango.tools {

    requires net.chesstango.board;
    requires net.chesstango.engine;
    requires net.chesstango.evaluation;
    requires net.chesstango.gardel;
    requires net.chesstango.piazzolla;
    requires net.chesstango.search;
    requires net.chesstango.uci.engine;
    requires net.chesstango.uci.gui;
    requires net.chesstango.arena.core;
    requires net.chesstango.arena.worker;
    requires net.chesstango.mbeans;


    requires org.slf4j;
    requires org.apache.commons.pool2;
    requires org.apache.commons.cli;


    requires static lombok;
    requires com.rabbitmq.client;
}