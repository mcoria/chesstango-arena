package net.chesstango.uci.arena.gui;

import net.chesstango.engine.Config;
import net.chesstango.engine.Tango;
import net.chesstango.evaluation.Evaluator;
import net.chesstango.search.DefaultSearch;
import net.chesstango.search.Search;
import net.chesstango.uci.engine.UciTango;
import net.chesstango.uci.gui.Controller;
import net.chesstango.uci.gui.ControllerProxy;
import net.chesstango.uci.gui.ControllerTango;
import net.chesstango.uci.proxy.ProxyConfigLoader;
import net.chesstango.uci.proxy.UciProxy;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Mauricio Coria
 */
public class ControllerFactory {
    public static Controller createProxyController(String proxyName, Consumer<UciProxy> fnProxySetup) {
        UciProxy proxy = new UciProxy(ProxyConfigLoader.loadEngineConfig(proxyName));
        if (fnProxySetup != null) {
            fnProxySetup.accept(proxy);
        }
        return new ControllerProxy(proxy);
    }

    /**
     * Tango without any customization
     *
     * @return
     */
    public static Controller createTangoController() {
        return new ControllerTango(new UciTango());
    }

    /**
     * Tango with search customization
     *
     * @return
     */
    public static Controller createTangoControllerWithSearch(Supplier<Search> searchMoveSupplier) {
        Search search = searchMoveSupplier.get();

        Function<Config, Tango> tangoFactory = config -> {
            config.setSearch(search);
            return Tango.open(config);
        };

        return new ControllerTango(new UciTango(tangoFactory))
                .overrideEngineName(search.getClass().getSimpleName());
    }

    /**
     * Tango with evaluator customization
     *
     * @return
     */
    public static Controller createTangoControllerWithEvaluator(Supplier<Evaluator> gameEvaluatorSupplier) {
        Evaluator evaluator = gameEvaluatorSupplier.get();

        Function<Config, Tango> tangoFactory = config -> {
            config.setSearch(new DefaultSearch(evaluator));
            return Tango.open(config);
        };

        return new ControllerTango(new UciTango(tangoFactory))
                .overrideEngineName(evaluator.getClass().getSimpleName());
    }

}
