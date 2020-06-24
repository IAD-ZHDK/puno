#include "sketch.h"

#define ARDUINO_ID "0"
#define ARDUINO_NAME "puno-"ARDUINO_ID

void setup() {
  Serial.begin(9600);
  delay(500);

  Serial.println("-----------------");
  Serial.println("PUNO Server");

  setupIMU();
  setupServo();
  setupWiFi();
  setupOSCBridge();
#ifdef ENABLE_MDNS
  setupMDNS();
#endif

  Serial.println("setup has been done!");
}

void loop() {
  updateIMU();
  updateOSCBridge();
  wifiUpdate();

#ifdef ENABLE_MDNS
  updateMDNS();
#endif
}
