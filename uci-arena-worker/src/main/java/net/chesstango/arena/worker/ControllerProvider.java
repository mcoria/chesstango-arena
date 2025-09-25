package net.chesstango.arena.worker;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.uci.gui.Controller;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Mauricio Coria
 */
@Slf4j
class ControllerProvider implements AutoCloseable {

    private final Path catalogDirectory;

    /**
     * Termina actuando como un pool de controles.
     */
    private final Map<String, Controller> controllers = new HashMap<>();

    private ControllerProvider(Path catalogDirectory) {
        this.catalogDirectory = catalogDirectory;
    }

    static ControllerProvider create(String catalogDirectoryString) {
        Path catalogDirectory = Path.of(catalogDirectoryString);
        if (!catalogDirectory.toFile().exists()) {
            throw new RuntimeException("Catalog directory not found: " + catalogDirectoryString);
        }
        return new ControllerProvider(catalogDirectory);
    }

    public Controller getController(String engineName) {
        return controllers.computeIfAbsent(engineName, this::openController);
    }

    @Override
    public void close() {
        controllers.values().forEach(controller -> {
            log.info("Closing engine: {}", controller.getEngineName());
            controller.stopEngine();
        });
    }

    private Controller openController(String engine) {
        Controller controller = null;
        log.info("Opening engine: {}", engine);
        if (engine.startsWith("file:")) {
            Path enginePath = catalogDirectory.resolve(String.format("%s.json", engine.substring(5)));
            if (!enginePath.toFile().exists()) {
                throw new RuntimeException("Engine not found: " + engine);
            }
            controller = ControllerFactory.createProxyController(enginePath);
        } else if (engine.startsWith("class:")) {
            Supplier<Controller> controllerSupplier = instantiateSupplier(engine.substring(6));
            controller = controllerSupplier.get();
        } else {
            throw new RuntimeException("Invalid engine name: " + engine);
        }
        controller.startEngine();
        return controller;
    }

    private Supplier<Controller> instantiateSupplier(String className) {
        try {
            Class<?> clazz = Class.forName(String.format("%s.%s", "net.chesstango.tools.worker.match.factories", className));
            if (!Supplier.class.isAssignableFrom(clazz)) {
                throw new RuntimeException("Class must implement Supplier<Controller>: " + className);
            }
            @SuppressWarnings("unchecked")
            Supplier<Controller> supplier = (Supplier<Controller>) clazz.getDeclaredConstructor().newInstance();
            return supplier;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to instantiate controller supplier: " + className, e);
        }
    }
}
