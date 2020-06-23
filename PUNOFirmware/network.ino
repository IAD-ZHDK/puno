int keyIndex = 0;
int status = WL_IDLE_STATUS;

void setupWiFi() {
  // check for the WiFi module:
  if (WiFi.status() == WL_NO_MODULE) {
    Serial.println("Communication with WiFi module failed!");
    // don't continue
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
  if (status != WL_CONNECTED) {
    Serial.println("lost wifi connection, trying to reconnect...");

    WiFi.end();

    setupWiFi();

#ifdef ENABLE_MDNS
    setupMDNS();
#endif
  }
}
