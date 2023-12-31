#ifndef SIDELED_H_   /* Include guard */
#define SIDELED_H_

#define SIDELED_NUM_LEDS 10
#define SIDELED_DATA_PIN 25

#define SIDELED_STATE_OFF 0
#define SIDELED_STATE_ACTIVE 1
#define SIDELED_STATE_ALARM 3

#include <Arduino.h>
#include <FastLED.h>

void init_sideled();

void set_sideled_state(int state);

#endif /* SIDELED_H_ */