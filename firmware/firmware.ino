#include "sketch.h"

#define ARDUINO_ID "0"
#define ARDUINO_NAME "puno-"ARDUINO_ID

void setup() {
  Serial.begin(9600);
  delay(500);

  setLED(INFO_LED_BRIGHTNESS / 2, INFO_LED_BRIGHTNESS / 2, 0);

  Serial.println("-------------------");
  Serial.println("PUNO Server TCP 1.2");

#ifdef ENABLE_IMU
  setupIMU();
#endif

  setupServo();
  setupWiFi();
  setupPUNO();
  
#ifdef ENABLE_MDNS
  setupMDNS();
#endif

#ifdef ENABLE_WATCHDOG
  wdt_enable(WDTO_8S);
#endif

  Serial.println("Ready for connection!");
}

void loop() {
#ifdef ENABLE_IMU
  updateIMU();
#endif

  updatePUNO();
  wifiUpdate();

#ifdef ENABLE_MDNS
  updateMDNS();
#endif

#ifdef ENABLE_WATCHDOG
  wdt_reset();
#endif
}
