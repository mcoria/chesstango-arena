package net.chesstango.uci.proxy;

/**
 * @author Mauricio Coria
 */
public class MoraProxyConfig extends ProxyConfig {
    private MoraProxyConfig(){
        this.setName("SPIKE");
        this.setDirectory("C:\\java\\projects\\chess\\chess-utils\\engines\\Mora");
        this.setCommand("C:\\java\\projects\\chess\\chess-utils\\engines\\Mora\\MORA_1.1.0.exe");
    }

    public static final MoraProxyConfig INSTANCE = new MoraProxyConfig();
}
