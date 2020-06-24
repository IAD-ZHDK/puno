// settings
//#define ENABLE_MDNS
#define ENABLE_RGBLED

// network
#include <SPI.h>
#include <WiFiNINA.h>
#include <WiFiUdp.h>

#ifdef ENABLE_RGBLED
#include <utility/wifi_drv.h>
#endif

#include "secrets.h"

// mdns
#ifdef ENABLE_MDNS
#include <ArduinoMDNS.h>
#endif

// lss
#include <SoftwareSerial.h>

// imu
#include <Arduino_LSM6DS3.h>
