#include <Arduino.h>
#include "view.h"
#include "networking.h"
#include "sideled.h"
#include <vector>
#include "soundwav.h"


void event_handler_checkbox(struct _lv_obj_t * obj, lv_event_t event);
void event_handler_button(struct _lv_obj_t * obj, lv_event_t event);
void init_gui_elements();
void mqtt_callback(char* topic, byte* payload, unsigned int length);

unsigned long next_lv_task = 0;

CRGB color;
uint8_t state = SIDELED_STATE_OFF;
uint8_t led_start = 0;
uint8_t led_end = 0;

Speaker speaker;

unsigned long next_sound_play = 0;
size_t sound_pos = 0;
bool sound_enabled = false;

lv_obj_t * led;

void event_handler_button(struct _lv_obj_t * obj, lv_event_t event) {
}

void init_gui_elements() {
}

// ----------------------------------------------------------------------------
// MQTT callback
// ----------------------------------------------------------------------------

void mqtt_callback(char* topic, byte* payload, unsigned int length) {
  // Parse Payload into Colour string
  char * buf = (char *)malloc((sizeof(char)*(length+1)));
  memcpy(buf, payload, length);
  buf[length] = '\0';
  String payloadS = String(buf);
  payloadS.trim();

  set_sideled_color(0, 10, CRGB::Red);
  set_sideled_state(0, 10, SIDELED_STATE_BLINK);
}

// ----------------------------------------------------------------------------
// MAIN LOOP
// ----------------------------------------------------------------------------

void loop() {
  if(next_lv_task < millis()) {
    lv_task_handler();
    next_lv_task = millis() + 5;
  }

  // Uncomment the following lines to enable MQTT
  //mqtt_loop();

  if(sound_enabled && next_sound_play < millis()) {
    size_t byteswritten = speaker.PlaySound(sounddata + sound_pos, NUM_ELEMENTS);
    
    sound_pos = sound_pos + byteswritten;
    if(sound_pos >= NUM_ELEMENTS) {
      sound_pos = 0;
    }
    next_sound_play = millis() + 100;
  }
}

// ----------------------------------------------------------------------------
// MAIN SETUP
// ----------------------------------------------------------------------------

void setup() {
  init_m5();
  init_display();
  Serial.begin(115200);
  lv_obj_t * wifiConnectingBox = show_message_box_no_buttons("Connecting to WiFi...");
  lv_task_handler();
  delay(5);
  setup_wifi();
  mqtt_init(mqtt_callback);
  close_message_box(wifiConnectingBox);
  init_gui_elements();
  init_sideled();
  set_sideled_color(0, 10, CRGB::Red);
  set_sideled_state(0, 10, SIDELED_STATE_BLINK);
}