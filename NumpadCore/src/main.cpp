#include <Arduino.h>
#include "view.h"
#include "networking.h"
#include "sideled.h"
#include <Wire.h>
#include <SPI.h>
#include <PIR.h>
#include <vector>
#include "soundwav.h"

void event_handler_num(struct _lv_obj_t * obj, lv_event_t event);
void event_handler_ok(struct _lv_obj_t * obj, lv_event_t event);
void event_handler_box(struct _lv_obj_t * obj, lv_event_t event);
void init_gui_elements();
void mqtt_callback(char* topic, byte* payload, unsigned int length);

unsigned long next_lv_task = 0;
unsigned long next_sensor_read = 0;

PIR sensor;
lv_obj_t * led;

// Audio
Speaker speaker;
unsigned long next_sound_play = 0;
size_t sound_pos = 0;

// Sensor state
uint8_t last_state;
uint8_t new_state;

boolean alarm_state = false;
boolean alarm_triggered = false;

void init_sensor() {
  Wire.begin();
  sensor.add(36);
}

void init_gui_elements() {
  int c = 1;
  for(int y = 0; y < 3; y++) {
    for(int x = 0; x < 3; x++) {
      add_button(String(c).c_str(), event_handler_num, 5 + x*80, 5 + y*80, 70, 70);
      c++;
    }
  }
  add_button("OK", event_handler_ok, 245, 5, 70, 70);
  add_button("0", event_handler_num, 245, 165, 70, 70);
  
  led = add_led(260, 100, 30, 30);
}

void activate_LED() {
  lv_led_on(led);                            // Turn the LED ON
  lv_led_set_bright(led, LV_LED_BRIGHT_MAX); // Set the LED brightness to the maximum value
}

void deactivate_LED() {
  lv_led_off(led);
}

// ----------------------------------------------------------------------------
// MQTT callback
// ----------------------------------------------------------------------------

void mqtt_callback(char* topic, byte* payload, unsigned int length) {
    char * buf = (char *)malloc((sizeof(char)*(length+1)));
    memcpy(buf, payload, length);
    buf[length] = '\0';
    String payloadS = String(buf);
    payloadS.trim();

  if(String(topic) == "oliver-sascha-alarm/activity") {
    if(payloadS == "true") {
      alarm_state = true;
      activate_LED();
      set_sideled_state(SIDELED_STATE_ACTIVE);
    } else if(payloadS == "false") {
      alarm_state = false;
      deactivate_LED();
      set_sideled_state(SIDELED_STATE_OFF);
    }
  }

  if(String(topic) == "oliver-sascha-alarm/triggered") {
    if(payloadS == "true") {
      alarm_triggered = true;
      deactivate_LED();
      set_sideled_state(SIDELED_STATE_ALARM);
    }
    if(payloadS == "false") {
      alarm_triggered = false;
      set_sideled_state(alarm_state ? SIDELED_STATE_ACTIVE : SIDELED_STATE_OFF);
    }
  }
}

// ----------------------------------------------------------------------------
// UI event handlers
// ----------------------------------------------------------------------------

String buffer = "";

// PIN
String PIN = "0";

void event_handler_num(struct _lv_obj_t * obj, lv_event_t event) {
  if(event == LV_EVENT_CLICKED) {
    lv_obj_t * child = lv_obj_get_child(obj, NULL);
    String num = String(lv_label_get_text(child));
    num.trim();
    buffer += num;
  }
}

lv_obj_t * mbox;

void event_handler_box(struct _lv_obj_t * obj, lv_event_t event) {
  String textBtn = String(lv_msgbox_get_active_btn_text(obj));
  if(event == LV_EVENT_VALUE_CHANGED) {
    if(textBtn == "Send") {
      if(buffer == PIN) {
        mqtt_publish("oliver-sascha-alarm/triggered", "false");
        mqtt_publish("oliver-sascha-alarm/activity", "false");
        set_sideled_state(SIDELED_STATE_SUCCESS);
      }
    }
    buffer = "";
    close_message_box(mbox);
  }
}

void event_handler_ok(struct _lv_obj_t * obj, lv_event_t event) {
  if(event == LV_EVENT_CLICKED) {
    mbox = show_message_box(buffer.c_str(), "Send", "Cancel", event_handler_box);
  }
}

void read_sensor() {
  last_state = sensor.lastValue();
  new_state = sensor.read();

  if (new_state != last_state && new_state == 1) {
    mqtt_publish("oliver-sascha-alarm/triggered", "true");
  }
}

// ----------------------------------------------------------------------------
// MAIN LOOP
// ----------------------------------------------------------------------------

void loop() {
  if(next_lv_task < millis()) {
    lv_task_handler();
    next_lv_task = millis() + 5;
  }

  if(next_sensor_read < millis()) {
    read_sensor();
    next_sensor_read = millis() + 1000;
  }

  mqtt_loop();

  if (alarm_state && alarm_triggered && next_sound_play < millis()) {
    size_t byteswritten = speaker.PlaySound(sounddata + sound_pos, NUM_ELEMENTS);
    sound_pos = sound_pos + byteswritten;
    if (sound_pos >= NUM_ELEMENTS) {
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
  init_sensor();
  set_sideled_state(SIDELED_STATE_OFF);
}