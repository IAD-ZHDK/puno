// quick fix to support Arduino Rev 2
#define OscWiFi ArduinoOSC::Manager<WiFiUDP>::getInstance()

#define IN_PORT 8000
#define OUT_PORT 9000

#define NS_ANSWER "/iad/answer"

void setupOSCBridge() {
  Serial.println("setting up OSC bridge...");
  setupOSCCommands();
  setupQueries();
}

void updateOSCBridge() {
  OscWiFi.update();
}

void setupOSCCommands() {
  // subscribe to servo commands
  OscWiFi.subscribe(IN_PORT, "/iad/servo/cmd", [&](String & command) {
    lss_sendRaw(command);
  });

  // pin commands
  OscWiFi.subscribe(IN_PORT, "/iad/pin/mode", [&](int& pin, int& mode) {
    pinMode(pin, mode);
  });

  OscWiFi.subscribe(IN_PORT, "/iad/pin/digitalWrite", [&](int& pin, int& value) {
    digitalWrite(pin, value);
  });

  OscWiFi.subscribe(IN_PORT, "/iad/pin/analogWrite", [&](int& pin, int& value) {
    analogWrite(pin, value);
  });

  // led commands
  OscWiFi.subscribe(IN_PORT, "/iad/led/color", [&](int& r, int& g, int& b) {
#ifdef ENABLE_RGBLED
    WiFiDrv::analogWrite(25, r);
    delay(2);
    WiFiDrv::analogWrite(26, g);
    delay(2);
    WiFiDrv::analogWrite(27, b);
    delay(2);
#endif
  });
}

void setupQueries() {
  // PIN
  OscWiFi.subscribe(IN_PORT, "/iad/test", [](const OscMessage & m) {
    OscWiFi.send(m.remoteIP(), OUT_PORT, NS_ANSWER, m.arg<int>(0), 42);
  });

  OscWiFi.subscribe(IN_PORT, "/iad/pin/digitalRead", [](const OscMessage & m) {
    int data = digitalRead(m.arg<int>(1));
    OscWiFi.send(m.remoteIP(), OUT_PORT, NS_ANSWER, m.arg<int>(0), data);
  });

  OscWiFi.subscribe(IN_PORT, "/iad/pin/digitalReadMulti", [](const OscMessage & m) {
    OscMessage msg;
    msg.init(NS_ANSWER);
    msg.push(m.arg<int>(0));

    for (int i = 1; i < m.size(); i++) {
      int data = digitalRead(m.arg<int>(i));
      msg.push(data);
    }

    OscWiFi.getClient().send(m.remoteIP(), OUT_PORT, msg);
  });

  OscWiFi.subscribe(IN_PORT, "/iad/pin/analogRead", [](const OscMessage & m) {
    int data = analogRead(m.arg<int>(1));
    OscWiFi.send(m.remoteIP(), OUT_PORT, NS_ANSWER, m.arg<int>(0), data);
  });

  OscWiFi.subscribe(IN_PORT, "/iad/pin/analogReadMulti", [](const OscMessage & m) {
    OscMessage msg;
    msg.init(NS_ANSWER);
    msg.push(m.arg<int>(0));

    for (int i = 1; i < m.size(); i++) {
      int data = analogRead(m.arg<int>(i));
      msg.push(data);
    }

    OscWiFi.getClient().send(m.remoteIP(), OUT_PORT, msg);
  });

  // IMU
  OscWiFi.subscribe(IN_PORT, "/iad/imu/raw", [](const OscMessage & m) {
    //readIMU(false);
    OscWiFi.send(m.remoteIP(), OUT_PORT, NS_ANSWER, m.arg<int>(0),
                 accelX, accelY, accelZ,
                 gyroX, gyroY, gyroZ);
  });

  OscWiFi.subscribe(IN_PORT, "/iad/imu/rpy", [](const OscMessage & m) {
    //readIMU();
    //postProcessIMU();
    OscWiFi.send(m.remoteIP(), OUT_PORT, NS_ANSWER, m.arg<int>(0),
                 complementaryRoll, complementaryPitch, complementaryYaw);
  });
}
