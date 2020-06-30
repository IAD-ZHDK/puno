// settings
//#define ENABLE_MDNS
//#define ENABLE_WATCHDOG

// network (for all platforms)
#ifdef ESP_PLATFORM
#include <WiFi.h>
#include <WiFiUdp.h>
#elif defined (ESP8266)
#include <ESP8266WiFi.h>
#include <WiFiUdp.h>
#elif defined (ARDUINO_AVR_UNO_WIFI_REV2)\
        || defined(ARDUINO_SAMD_MKRWIFI1010)\
        || defined(ARDUINO_SAMD_MKRVIDOR4000)\
        || defined(ARDUINO_SAMD_NANO_33_IOT)
#include <SPI.h>
#include <WiFiNINA.h>
#include <WiFiUdp.h>
#define ENABLE_RGBLED
#define UBLOX
#define ENABLE_IMU
#elif defined (ARDUINO_SAMD_MKR1000)
#include <SPI.h>
#include <WiFi101.h>
#include <WiFiUdp.h>
#endif

#ifdef ENABLE_RGBLED
#include <utility/wifi_drv.h>
#endif

#ifdef ENABLE_WATCHDOG
#include <avr/wdt.h>
#define WDTO_8S   9
#endif

#define INFO_LED_BRIGHTNESS 250

#include "secrets.h"

// mdns
#ifdef ENABLE_MDNS
#include <ArduinoMDNS.h>
#endif

// lss
#include <SoftwareSerial.h>

// imu
#ifdef ENABLE_IMU
#include <Arduino_LSM6DS3.h>
#endif

// global vars
float complementaryRoll, complementaryPitch, complementaryYaw;  // units degrees (excellent roll, pitch, yaw minor drift)

// global methods
void setLED(int red, int green, int blue);
void(* resetFunc) (void) = 0;
