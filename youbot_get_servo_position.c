/*
 * File:          youbot.c
 * Date:          24th May 2011
 * Description:   Starts with a predefined behaviors and then
 *                read the user keyboard inputs to actuate the
 *                robot
 * Author:        fabien.rohrer@cyberbotics.com
 * Modifications: 
 */

#include <webots/robot.h>

#include "../../lib/base.h"
#include "../../lib/arm.h"
#include "../../lib/gripper.h"

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#define TIME_STEP 32

static WbDeviceTag finger1, finger2;


int main(int argc, char **argv)
{
  
  double left_pos, right_pos;
  
  wb_robot_init();
  
  finger1 = wb_robot_get_device("finger1");
  finger2 = wb_robot_get_device("finger2");
  
  wb_servo_enable_position(finger1, TIME_STEP);
  wb_servo_enable_position(finger2, TIME_STEP);
  
  
  left_pos = wb_servo_get_position(finger1);

  right_pos = wb_servo_get_position(finger2);

  printf("The start grippers' positions are: %lf and %lf\n", left_pos, right_pos);

  wb_servo_set_position(finger1, 0.024);
  
  wb_servo_set_position(finger2, 0.018);

  wb_robot_step(TIME_STEP*100);
  
  left_pos = 0.812;
  
  right_pos = 0.125;

  left_pos = wb_servo_get_position(finger1);
  
  right_pos = wb_servo_get_position(finger2);

  printf("The end grippers' positions are: %lf and %lf\n", left_pos, right_pos);
  
  //wb_servo_set_velocity(finger1, 0.03);
  //wb_servo_set_velocity(finger2, 0.03);

  
  wb_robot_cleanup();
  
  return 0;
}
