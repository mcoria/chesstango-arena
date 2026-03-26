package net.chesstango.uci.gui;

import net.chesstango.uci.proxy.ProxyConfig;

/**
 * @author Mauricio Coria
 */
public class SpikeProxy extends ProxyConfig {
    private SpikeProxy(){
        this.setName("SPIKE");
        this.setDirectory("C:\\java\\projects\\chess\\chess-utils\\tools\\arena_3.5.1\\Engines\\Spike");
        this.setCommand("C:\\java\\projects\\chess\\chess-utils\\tools\\arena_3.5.1\\Engines\\Spike\\Spike1.4.exe");
    }

    public static final SpikeProxy INSTANCE = new SpikeProxy();
}
