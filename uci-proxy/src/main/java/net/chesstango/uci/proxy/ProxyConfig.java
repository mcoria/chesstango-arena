package net.chesstango.uci.proxy;

import lombok.Getter;
import lombok.Setter;
import net.chesstango.goyeneche.requests.ReqSetOption;
import net.chesstango.goyeneche.requests.UCIRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Mauricio Coria
 */

@Setter
@Getter
public class ProxyConfig {
    private String name;

    private String directory;

    private String exe;

    private List<String> params;

    private List<UciOption> options;

    @Getter
    @Setter
    public static class UciOption {
        private String name;
        private String value;
    }


    public ProcessBuilder processBuilder() {
        List<String> commandAndArguments = new ArrayList<>();

        commandAndArguments.add(getExe());

        if (getParams() != null) {
            commandAndArguments.addAll(getParams());
        }

        ProcessBuilder processBuilder = new ProcessBuilder(commandAndArguments);
        processBuilder.directory(new File(getDirectory()));

        return processBuilder;
    }

    public List<ReqSetOption> uciOptionCommands() {
        if (getOptions() != null) {
            return options.stream()
                    .map(option -> UCIRequest.setOption(option.getName(), option.getValue()))
                    .toList();
        }
        return Collections.emptyList();
    }
}

