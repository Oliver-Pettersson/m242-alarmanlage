#include "view.h"

uint32_t startTime, frame = 0; // For frames-per-second estimate
TFT_eSPI tft = TFT_eSPI();
static lv_disp_buf_t disp_buf;
static lv_color_t buf[LV_HOR_RES_MAX * 10];

static lv_style_t button_disabled_style;

void init_m5() {
  M5.begin(true, true, true, true);
  tft.begin();
  tft.setRotation(1);
  M5.Axp.SetLcdVoltage(2800);
  M5.Axp.SetLcdVoltage(3300);
  M5.Axp.SetBusPowerMode(0);
  M5.Axp.SetCHGCurrent(AXP192::kCHG_190mA);
  M5.Axp.SetLDOEnable(3, true);
  delay(150);
  M5.Axp.SetLDOEnable(3, false);
  M5.Axp.SetLed(1);
  delay(100);
  M5.Axp.SetLed(0);
  M5.Axp.SetLDOVoltage(3, 3300);
  M5.Axp.SetLed(1);
  startTime = millis();

  //Speaker setup
  M5.begin();
}

void my_disp_flush(lv_disp_drv_t *disp, 
     const lv_area_t *area, lv_color_t *color_p){
  uint32_t w = (area->x2 - area->x1 + 1);
  uint32_t h = (area->y2 - area->y1 + 1);
  tft.startWrite();
  tft.setAddrWindow(area->x1, area->y1, w, h);
  tft.pushColors(&color_p->full, w * h, true);
  tft.endWrite();
  lv_disp_flush_ready(disp);
}

void init_display() {
  lv_disp_buf_init(&disp_buf, buf, NULL, LV_HOR_RES_MAX * 10);
  lv_init();
  
  
  //-------------------------------------------------------------------
  /*Initialize the display*/
  lv_disp_drv_t disp_drv;
  lv_disp_drv_init(&disp_drv);
  disp_drv.hor_res  = 320;
  disp_drv.ver_res  = 240;
  disp_drv.flush_cb = my_disp_flush;
  disp_drv.buffer   = &disp_buf;
  lv_disp_drv_register(&disp_drv);
  
  //-------------------------------------------------------------------
  /*Initialize the (dummy) input device driver*/
  lv_indev_drv_t indev_drv;
  lv_indev_drv_init(&indev_drv);
  indev_drv.type = LV_INDEV_TYPE_POINTER;
  lv_indev_drv_register(&indev_drv);

  lv_style_init(&button_disabled_style);
  lv_style_set_border_color(&button_disabled_style, LV_STATE_DISABLED, LV_COLOR_GRAY);
  lv_style_set_text_color(&button_disabled_style, LV_STATE_DISABLED, LV_COLOR_GRAY);
}


lv_obj_t * show_message_box(const char * text, const char * ok_button_text, const char * no_button_text, lv_event_cb_t event)
{
    static const char * btns[] ={ok_button_text, no_button_text, ""};

    lv_obj_t * mbox1 = lv_msgbox_create(lv_scr_act(), NULL);
    lv_msgbox_set_text(mbox1, text);
    lv_msgbox_add_btns(mbox1, btns);
    lv_obj_set_width(mbox1, 200);
    lv_obj_set_event_cb(mbox1, event);
    lv_obj_align(mbox1, NULL, LV_ALIGN_CENTER, 0, 0); 

    return mbox1;
}

void close_message_box(lv_obj_t * msgbox) {
  lv_msgbox_start_auto_close(msgbox, 10);
}


lv_obj_t * show_message_box_no_buttons(const char * text)
{
    lv_obj_t * mbox1 = lv_msgbox_create(lv_scr_act(), NULL);
    lv_msgbox_set_text(mbox1, text);
    lv_obj_set_width(mbox1, 200);
    lv_obj_align(mbox1, NULL, LV_ALIGN_CENTER, 0, 0); 
    return mbox1;
}

// https://docs.lvgl.io/7.11/widgets/led.html
lv_obj_t * add_led(lv_coord_t x_ofs, lv_coord_t y_ofs, lv_coord_t w, lv_coord_t h)
{
  lv_obj_t * led1  = lv_led_create(lv_scr_act(), NULL);
  lv_obj_align(led1, NULL, LV_ALIGN_IN_TOP_LEFT, x_ofs, y_ofs);
  lv_obj_set_width(led1, w);
  lv_obj_set_height(led1, h);
  lv_led_off(led1);
  return led1;
}