package net.chesstango.arena.master.common;

import net.chesstango.uci.gui.Controller;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.util.function.Supplier;

/**
 * @author Mauricio Coria
 */
public class ControllerPoolFactory extends BasePooledObjectFactory<Controller> {

    private final Supplier<Controller> fnCreateEngineController;

    public ControllerPoolFactory(Supplier<Controller> fnCreateEngineController) {
        this.fnCreateEngineController = fnCreateEngineController;
    }

    @Override
    public Controller create() {

        Controller controller = fnCreateEngineController.get();

        controller.startEngine();

        return controller;
    }

    @Override
    public PooledObject<Controller> wrap(Controller controller) {
        return new DefaultPooledObject<>(controller);
    }

    @Override
    public void destroyObject(PooledObject<Controller> pooledController) {
        Controller controller = pooledController.getObject();

        controller.stopEngine();
    }

    @Override
    public void activateObject(PooledObject<Controller> pooledController) throws Exception {
        pooledController.getObject().send_ReqIsReady();
    }

}
