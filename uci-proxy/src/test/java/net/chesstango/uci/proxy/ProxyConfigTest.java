package net.chesstango.uci.proxy;


import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Mauricio Coria
 */
public class ProxyConfigTest {

    @Test
    public void testReadConfig() throws IOException {
        InputStream inputStream = ProxyConfigTest.class.getClassLoader().getResourceAsStream("sample_config.json");

        ProxyConfig config = ProxyConfigReader.readConfig(inputStream);

        assertEquals("EngineName", config.getName());
    }
}
