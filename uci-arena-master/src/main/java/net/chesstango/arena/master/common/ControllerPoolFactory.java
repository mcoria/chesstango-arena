package net.chesstango.arena.master.common;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.uci.gui.Controller;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.util.function.Supplier;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class ControllerPoolFactory extends BasePooledObjectFactory<Controller> {

    private final Supplier<Controller> fnCreateEngineController;

    public ControllerPoolFactory(Supplier<Controller> fnCreateEngineController) {
        this.fnCreateEngineController = fnCreateEngineController;
    }

    @Override
    public Controller create() {

        Controller controller = fnCreateEngineController.get();

        controller.init();

        return controller;
    }

    @Override
    public PooledObject<Controller> wrap(Controller controller) {
        return new DefaultPooledObject<>(controller);
    }

    @Override
    public void activateObject(PooledObject<Controller> pooledController) {
        pooledController.getObject().send_ReqIsReady();
    }

    @Override
    public void destroyObject(PooledObject<Controller> pooledController) {
        Controller controller = pooledController.getObject();
        try {
            controller.close();
        } catch (Exception e) {
            log.error("Error closing controller", e);
        }
    }

}
