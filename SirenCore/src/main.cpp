#include <Arduino.h>
#include "view.h"
#include "networking.h"
#include "sideled.h"
#include <vector>
#include "soundwav.h"
#include <Wire.h>
#include <PIR.h>
#include <string.h>

void event_handler_checkbox(struct _lv_obj_t *obj, lv_event_t event);
void event_handler_button(struct _lv_obj_t *obj, lv_event_t event);
void init_gui_elements();
void mqtt_callback(char *topic, byte *payload, unsigned int length);

unsigned long next_lv_task = 0;
unsigned long next_sensor_read = 0;

CRGB color;
uint8_t state = SIDELED_STATE_OFF;
uint8_t led_start = 0;
uint8_t led_end = 0;
uint8_t last_state;
uint8_t new_state;

Speaker speaker;
PIR sensor;

unsigned long next_sound_play = 0;
size_t sound_pos = 0;
bool sound_enabled = false;
bool alarm_triggered = false;
const char *activity_topic = "oliver-sascha-alarm/activity";
const char *triggered_topic = "oliver-sascha-alarm/triggered";

lv_obj_t *led;

void event_handler_button(struct _lv_obj_t *obj, lv_event_t event)
{
}

void init_sensor()
{
  Wire.begin();
  sensor.add(36);
}

void init_gui_elements()
{
}

// ----------------------------------------------------------------------------
// MQTT callback
// ----------------------------------------------------------------------------

void mqtt_callback(char *topic, byte *payload, unsigned int length)
{
  // Parse Payload into Colour string
  char *buf = (char *)malloc((sizeof(char) * (length + 1)));
  memcpy(buf, payload, length);
  buf[length] = '\0';
  String payloadS = String(buf);
  payloadS.trim();
  if (strcmp(topic, triggered_topic) == 0)
  {
    alarm_triggered = strcmp(payloadS.c_str(), "true") == 0;
    set_sideled_state(alarm_triggered ? SIDELED_STATE_ALARM : sound_enabled ? SIDELED_STATE_ACTIVE
                                                                            : SIDELED_STATE_OFF);
  }
  if (strcmp(topic, activity_topic) == 0)
  {
    sound_enabled = strcmp(payloadS.c_str(), "true") == 0;
    set_sideled_state(sound_enabled ? SIDELED_STATE_ACTIVE : SIDELED_STATE_OFF);
  }
}

// ----------------------------------------------------------------------------
// MAIN LOOP
// ----------------------------------------------------------------------------

void read_sensor()
{
  last_state = sensor.lastValue();
  new_state = sensor.read();

  if (new_state != last_state && new_state == 1 && sound_enabled) //  change detected?
  {
    mqtt_publish(triggered_topic, "true");
  }
}

void loop()
{
  if (next_lv_task < millis())
  {
    lv_task_handler();
    next_lv_task = millis() + 5;
  }

  if (next_sensor_read < millis())
  {
    read_sensor();
    next_sensor_read = millis() + 1000;
  }

  mqtt_loop();

  if (false && sound_enabled && alarm_triggered && next_sound_play < millis())
  {
    size_t byteswritten = speaker.PlaySound(sounddata + sound_pos, NUM_ELEMENTS);

    sound_pos = sound_pos + byteswritten;
    if (sound_pos >= NUM_ELEMENTS)
    {
      sound_pos = 0;
    }
    next_sound_play = millis() + 100;
  }
}

// ----------------------------------------------------------------------------
// MAIN SETUP
// ----------------------------------------------------------------------------

void setup()
{
  init_m5();
  init_display();
  Serial.begin(115200);
  lv_obj_t *wifiConnectingBox = show_message_box_no_buttons("Connecting to WiFi...");
  lv_task_handler();
  delay(5);
  setup_wifi();
  mqtt_init(mqtt_callback);
  close_message_box(wifiConnectingBox);
  init_gui_elements();
  init_sideled();
  init_sensor();
  set_sideled_state(SIDELED_STATE_OFF);
}