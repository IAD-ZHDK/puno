int keyIndex = 0;
int status = WL_IDLE_STATUS;

void setupWiFi() {

#ifdef UBLOX
  // check for the WiFi module
  if (WiFi.status() == WL_NO_MODULE) {
    Serial.println("Communication with WiFi module failed!");
    setLED(INFO_LED_BRIGHTNESS, 0, 0);
    while (true);
  }
#endif

  // set hostname
#ifdef UBLOX
  WiFi.setHostname(ARDUINO_NAME);
#endif

#ifdef UBLOX
  String fv = WiFi.firmwareVersion();
  Serial.print("NINA Version: ");
  Serial.println(fv);
  if (fv < WIFI_FIRMWARE_LATEST_VERSION) {
    Serial.println("Please upgrade the firmware");
  }
#endif

#ifdef ARDUINO_ESP8266_NODEMCU
  Serial.println("setting STA mode...");
  WiFi.mode(WIFI_STA);
#endif

  // attempt to connect to Wifi network:
  while (status != WL_CONNECTED) {
    Serial.print("Connecting to SSID: ");
    Serial.println(ssid);
    status = WiFi.begin(ssid, pass);

#ifdef ARDUINO_ESP8266_NODEMCU
    int count = 0;
    while (WiFi.status() != WL_CONNECTED) {
      delay(1000);
      Serial.print(".");
      status = WiFi.status();
      count++;

      if (count > 20) {
        Serial.println();
        Serial.println("could not connect!");
        delay(5000);
        ESP.restart();
      }
      Serial.println();
    }
#endif
  }

  // once you are connected :
  Serial.println("Connected!");
  setLED(0, 0, INFO_LED_BRIGHTNESS);
  printWifiData();
}

void printWifiData() {
  // print your board's IP address:
  IPAddress ip = WiFi.localIP();
  Serial.print("IP Address: ");
  Serial.println(ip);
  Serial.print("Name: ");
  Serial.println(ARDUINO_NAME);
}

void wifiUpdate() {
  // Checks if Wifi signal lost
  if (WiFi.RSSI() == 0)
  {
    // show to the user
    setLED(20, 0, 0);
    delay(1000);

    // reset arduino
    resetFunc();
  }
}

void setLED(int red, int green, int blue) {
  // turn rgb-led red
#ifdef ENABLE_RGBLED
  WiFiDrv::analogWrite(25, red);
  WiFiDrv::analogWrite(26, green);
  WiFiDrv::analogWrite(27, blue);
#endif
}
