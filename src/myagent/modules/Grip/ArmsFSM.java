/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myagent.modules.Grip;

import myagent.modules.FSM;

/**
 *
 * @author Daqi
 */
public interface ArmsFSM extends FSM{

    // This method is supposed to be implemented and used for all FSMs controlling Arms position,
    // so that to do hand moving
    public void recieveArmsPositions(double arm2, double arm3, double arm4);

}
