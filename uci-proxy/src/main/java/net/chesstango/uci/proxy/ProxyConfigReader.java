package net.chesstango.uci.proxy;


import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Mauricio Coria
 */
public class ProxyConfigReader {

    public static ProxyConfig readConfig(Path configFile) throws IOException {
        try (InputStream inputStream = Files.newInputStream(configFile)) {
            return readConfig(inputStream);
        }
    }

    protected static ProxyConfig readConfig(InputStream inputStream) throws IOException {
        try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {

            ObjectMapper objectMapper = JsonMapper.builder().build();

            return objectMapper.readValue(inputStreamReader, ProxyConfig.class);
        }
    }
}
