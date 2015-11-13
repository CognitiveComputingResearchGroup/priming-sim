/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myagent.SMS.EE;

import edu.memphis.ccrg.lida.framework.tasks.FrameworkTaskImpl;
import edu.memphis.ccrg.lida.framework.tasks.TaskManager;
import edu.memphis.ccrg.lida.framework.tasks.TaskSpawner;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Daqi
 */
public abstract class EEImpl implements EE{

    private static final Logger logger = Logger
                .getLogger(EEImpl.class.getCanonicalName());

    //This is the real sensory data represented with uncertainty
    protected Map<String, Object> measurements = new HashMap<String, Object>();

    //The copy of motor commands
    protected Map<String, Object> motorCommands = new HashMap<String, Object>();

    //The estimated sensroy data
    // The data's type need to be specified in concrete classes
    protected Object estimatedSensoryData = null;

    //The prior/current knowledge about the actuators states
    // The data's type need to be specified in concrete classes
    protected Object actuatorsStates = null;

    
    protected TaskSpawner taskSpawner;

    @Override
    public void load() {
        //TODO: Impl
    }

    @Override
    public void save() {
        //TODO: Impl
    }

    @Override
    public void recieveMotorCommands(Object o) {
        if (o instanceof HashMap) {
            motorCommands.putAll((HashMap) o);
        } else {
            logger.log(Level.INFO, "The copy of motor commands is not a HashMap!", TaskManager.getCurrentTick());
        }
    }
    
    @Override
    public void recieveMeasurements(Object o) {
        if (o instanceof HashMap) {
            measurements.putAll((HashMap) o);
        } else {
            logger.log(Level.INFO, "Sensed data is not a HashMap!", TaskManager.getCurrentTick());
        }
    }

    @Override
    public Object estSensoryData() {
        return estimatedSensoryData;
    }

    @Override
    public void estimation() {
        //TODO: Impl
    }

    @Override
    public void receiveTS(TaskSpawner ts){
        taskSpawner = ts;
    }
    


}
