package net.chesstango.arena.worker;

import net.chesstango.engine.Config;
import net.chesstango.evaluation.Evaluator;
import net.chesstango.search.Search;
import net.chesstango.uci.engine.UciTango;
import net.chesstango.uci.gui.Controller;
import net.chesstango.uci.gui.ControllerProxy;
import net.chesstango.uci.gui.ControllerTango;
import net.chesstango.uci.proxy.ProxyConfig;
import net.chesstango.uci.proxy.ProxyConfigReader;
import net.chesstango.uci.proxy.UciProxy;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Mauricio Coria
 */
public class ControllerFactory {

    public static Controller createProxyController(Path configFile) {
        try {
            ProxyConfig proxyConfig = ProxyConfigReader.readConfig(configFile);
            UciProxy proxy = new UciProxy(proxyConfig);
            return new ControllerProxy(proxy)
                    .setOptionsCommands(proxyConfig.uciOptionCommands())
                    .overrideEngineName(proxyConfig.getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Controller createTangoController() {
        Config config = Config.create()
                        .setAsyncSearch(false);

        return new ControllerTango(new UciTango(config));
    }

    public static Controller createTangoControllerCustomConfig(Consumer<Config> configConsumer) {
        Config config = Config.create()
                .setAsyncSearch(false);
        configConsumer.accept(config);

        return new ControllerTango(new UciTango(config));
    }


    public static Controller createTangoControllerWithSearch(Supplier<Search> searchMoveSupplier) {
        Search search = searchMoveSupplier.get();

        Config config = Config.create()
                .setAsyncSearch(false);
        config.setSearch(search);

        return new ControllerTango(new UciTango(config))
                .overrideEngineName(search.getClass().getSimpleName());
    }


    public static Controller createTangoControllerWithEvaluator(Supplier<Evaluator> evaluatorSupplier) {
        Evaluator evaluator = evaluatorSupplier.get();

        Config config = Config.create()
                .setAsyncSearch(false);
        config.setEvaluator(evaluator);

        return new ControllerTango(new UciTango(config))
                .overrideEngineName(evaluator.getClass().getSimpleName());
    }

}
