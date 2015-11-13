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

static void step() {
  if (wb_robot_step(TIME_STEP) == -1) {
    wb_robot_cleanup();
    exit(EXIT_SUCCESS);
  }
}


static void display_helper_message() {
  printf("Control commands:\n");
  printf(" Arrows:       Move the robot\n");
  printf(" Page Up/Down ('A'/'Z'): Rotate the robot\n");
  printf(" +/-:          (Un)grip\n");
  printf(" Num arrows ('S'/'X'):   Handle the arm\n");
  printf(" Space: Reset\n");
}

int main(int argc, char **argv)
{
  
  WbDeviceTag e_communication, r_communication;
  
  int e_channel, r_channel;
  
  wb_robot_init();
  
  base_init();
  arm_init();
  gripper_init();
  //passive_wait(2.0);
  
  //automatic_behavior();
  //display_helper_message();
  
  int pc = 0;
  wb_robot_keyboard_enable(TIME_STEP);
  
  e_communication = wb_robot_get_device("emitter");
  e_channel = wb_emitter_get_channel(e_communication);
  
  r_communication = wb_robot_get_device("receiver");
  r_channel = wb_receiver_get_channel(r_communication);
  wb_receiver_enable(r_communication, TIME_STEP);
  
  printf("The channel are: %d and %d", e_channel, r_channel);
  
  
  /* send null-terminated message */
  const char *message = "Hello !";
  wb_emitter_send(e_communication, message, strlen(message) + 1);

  while (true) {
    step();
    
    if (wb_receiver_get_queue_length(r_communication) > 0) {
      printf("Read!");
      /* read current packet's data */
      const char *buffer = wb_receiver_get_data(r_communication);
      
      /* print null-terminated message */
      printf("Communicating: received \"%s\"\n",buffer);     
      
      /* fetch next packet */
      wb_receiver_next_packet(r_communication);
    }else{
      printf("Broken...\n");
    }
    
    wb_emitter_send(e_communication, message, strlen(message) + 1);
    

    int c = wb_robot_keyboard_get_key();
    if (c && c != pc) {
      switch (c) {
        case WB_ROBOT_KEYBOARD_UP:
          printf("Go forwards\n");
          base_forwards();
          break;
        case WB_ROBOT_KEYBOARD_DOWN:
          printf("Go backwards\n");
          base_backwards();
          break;
        case WB_ROBOT_KEYBOARD_LEFT:
          printf("Strafe left\n");
          base_strafe_left();
          break;
        case WB_ROBOT_KEYBOARD_RIGHT:
          printf("Strafe right\n");
          base_strafe_right();
          break;
        case WB_ROBOT_KEYBOARD_PAGEUP:
          printf("Turn left\n");
          base_turn_left();
          break;
        case WB_ROBOT_KEYBOARD_PAGEDOWN:
          printf("Turn right\n");
          base_turn_right();
          break;
        case WB_ROBOT_KEYBOARD_END:
        case ' ':
          printf("Reset\n");
          base_reset();
          arm_reset();
          break;
        case '+':
        case 388:
        case 65585:
          printf("Grip\n");
          gripper_grip();
          break;
        case '-':
        case 390:
          printf("Ungrip\n");
          gripper_release();
          break;
        case 332:
        case 377:
        case 'A':
          printf("Increase arm height\n");
          arm_increase_height();
          break;
        case 326:
        case 379:
        case 'Z':
          printf("Decrease arm height\n");
          arm_decrease_height();
          break;
        case 330:
        case 378:
        case 'S':
          printf("Increase arm orientation\n");
          arm_increase_orientation();
          break;
        case 328:
        case 376:
        case 'X':
          printf("Decrease arm orientation\n");
          arm_decrease_orientation();
          break;
        default:
          fprintf(stderr, "Wrong keyboard input\n");
          break;
      }
    }
    pc = c;
  }
  
  wb_robot_cleanup();
  
  return 0;
}
