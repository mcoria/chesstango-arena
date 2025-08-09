package net.chesstango.uci.arena.gui;

import net.chesstango.engine.Config;
import net.chesstango.engine.Tango;
import net.chesstango.evaluation.Evaluator;
import net.chesstango.goyeneche.requests.ReqSetOption;
import net.chesstango.search.Search;
import net.chesstango.uci.engine.UciTango;
import net.chesstango.uci.gui.Controller;
import net.chesstango.uci.gui.ControllerProxy;
import net.chesstango.uci.gui.ControllerTango;
import net.chesstango.uci.proxy.ProxyConfig;
import net.chesstango.uci.proxy.ProxyConfigLoader;
import net.chesstango.uci.proxy.UciProxy;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Mauricio Coria
 */
public class ControllerFactory {
    public static Controller createProxyController(String proxyName) {
        ProxyConfig config = ProxyConfigLoader.loadEngineConfig(proxyName);
        UciProxy proxy = new UciProxy(config);
        return new ControllerProxy(proxy)
                .setOptionsCommands(config.uciOptionCommands());
    }

    public static Controller createTangoController() {
        Function<Config, Tango> tangoFactory = config -> {
            config.setSyncSearch(true);
            return Tango.open(config);
        };

        return new ControllerTango(new UciTango(tangoFactory));
    }

    public static Controller createTangoControllerCustomConfig(Consumer<Config> configConsumer) {
        Function<Config, Tango> tangoFactory = config -> {
            config.setSyncSearch(true);
            configConsumer.accept(config);
            return Tango.open(config);
        };

        return new ControllerTango(new UciTango(tangoFactory));
    }


    public static Controller createTangoControllerWithSearch(Supplier<Search> searchMoveSupplier) {
        Search search = searchMoveSupplier.get();

        Function<Config, Tango> tangoFactory = config -> {
            config.setSyncSearch(true);
            config.setSearch(search);
            return Tango.open(config);
        };

        return new ControllerTango(new UciTango(tangoFactory))
                .overrideEngineName(search.getClass().getSimpleName());
    }


    public static Controller createTangoControllerWithEvaluator(Supplier<Evaluator> evaluatorSupplier) {
        Evaluator evaluator = evaluatorSupplier.get();

        Function<Config, Tango> tangoFactory = config -> {
            config.setSyncSearch(true);
            config.setSearch(Search.getInstance(evaluator));
            return Tango.open(config);
        };

        return new ControllerTango(new UciTango(tangoFactory))
                .overrideEngineName(evaluator.getClass().getSimpleName());
    }

}
