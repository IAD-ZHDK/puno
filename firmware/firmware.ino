#include "sketch.h"

#define ARDUINO_ID "0"
#define ARDUINO_NAME "puno-"ARDUINO_ID

void setup() {
  Serial.begin(9600);
  delay(500);

  Serial.println("---------------");
  Serial.println("PUNO Server TCP");

  setupIMU();
  setupServo();
  setupWiFi();
  setupPUNO();
#ifdef ENABLE_MDNS
  setupMDNS();
#endif

  Serial.println("Ready for connection!");
}

void loop() {
  updateIMU();
  updatePUNO();
  wifiUpdate();

#ifdef ENABLE_MDNS
  updateMDNS();
#endif
}
