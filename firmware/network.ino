int keyIndex = 0;
int status = WL_IDLE_STATUS;

void setupWiFi() {
  // check for the WiFi module:
  if (WiFi.status() == WL_NO_MODULE) {
    Serial.println("Communication with WiFi module failed!");
    setLED(INFO_LED_BRIGHTNESS, 0, 0);
    while (true);
  }

  // set hostname
  WiFi.setHostname(ARDUINO_NAME);

  String fv = WiFi.firmwareVersion();
  Serial.print("NINA Version: ");
  Serial.println(fv);
  if (fv < WIFI_FIRMWARE_LATEST_VERSION) {
    Serial.println("Please upgrade the firmware");
  }

  // attempt to connect to Wifi network:
  while (status != WL_CONNECTED) {
    Serial.print("Connecting to SSID: ");
    Serial.println(ssid);
    status = WiFi.begin(ssid, keyIndex, pass);
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
