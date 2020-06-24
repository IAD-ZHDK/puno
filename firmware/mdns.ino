#ifdef ENABLE_MDNS
WiFiUDP udp;
MDNS mdns(udp);

void setupMDNS() {
  Serial.println("publishing mdns...");
  mdns.begin(WiFi.localIP(), ARDUINO_NAME);
}

void updateMDNS() {
  mdns.run();
}
#endif
