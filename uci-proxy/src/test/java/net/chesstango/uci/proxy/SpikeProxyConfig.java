package net.chesstango.uci.proxy;

/**
 * @author Mauricio Coria
 */
public class SpikeProxyConfig extends ProxyConfig {
    private SpikeProxyConfig(){
        this.setName("SPIKE");
        this.setDirectory("C:\\java\\projects\\chess\\chess-utils\\tools\\arena_3.5.1\\Engines\\Spike");
        this.setCommand("C:\\java\\projects\\chess\\chess-utils\\tools\\arena_3.5.1\\Engines\\Spike\\Spike1.4.exe");
    }

    public static final SpikeProxyConfig INSTANCE = new SpikeProxyConfig();
}
