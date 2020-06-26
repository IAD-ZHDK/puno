// settings
//#define ENABLE_MDNS
#define ENABLE_RGBLED
//#define ENABLE_WATCHDOG

// network
#include <SPI.h>
#include <WiFiNINA.h>
#include <WiFiUdp.h>

#ifdef ENABLE_RGBLED
#include <utility/wifi_drv.h>
#endif

#ifdef ENABLE_WATCHDOG
#include <avr/wdt.h>
#define WDTO_8S   9
#endif

#define INFO_LED_BRIGHTNESS 20

#include "secrets.h"

// mdns
#ifdef ENABLE_MDNS
#include <ArduinoMDNS.h>
#endif

// lss
#include <SoftwareSerial.h>

// imu
#include <Arduino_LSM6DS3.h>

// global vars
float complementaryRoll, complementaryPitch, complementaryYaw;  // units degrees (excellent roll, pitch, yaw minor drift)

// global methods
void setLED(int red, int green, int blue);
void(* resetFunc) (void) = 0;
