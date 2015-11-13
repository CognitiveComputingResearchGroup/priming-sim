/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myagent.SMS.EE;

import edu.memphis.ccrg.lida.framework.tasks.FrameworkTask;

/**
 * KF: the Kalman filter
 * A Kalman filter is implemented to estimate a specific actuators states
 *
 * @author Daqi
 */
public interface KF extends FrameworkTask{

    @Override
    public void init();

    /*
     * recieve motor command that drives the estimation
     */
    public void recieveMotorCommand(Object o);

    /*
     * recieve new measurement(s), which is supposed to be combined with
     * prior knowledge of the statess
     */
    public void recieveMeasturement(Object o);

    /*
     * output the current (newest) estimated state
     */
    public Object estimatedSensoryData();

    /*
     * To run the Kalman filter
     */
    public void execute();

}
