#define PORT 8000

// protocol
#define tPinMode 0x01
#define tAnalogPin 0x02
#define tDigitalPin 0x03
#define tServo 0x04
#define tIMU 0x05
#define tLED 0x06

// operation
#define opRead 0x01
#define opWrite 0x02

WiFiServer server(PORT);
WiFiClient client;

boolean alreadyConnected = false;

char receiveBuffer[256];
char sendBuffer[256];

void setupPUNO() {
  server.begin();
}

void updatePUNO() {
  // try to connect client
  if (!alreadyConnected) {
    client = server.available();
  }

  // handle connection
  if (client) {

    // check first time connect
    if (!alreadyConnected) {
      client.flush();
      alreadyConnected = true;
      setLED(0, INFO_LED_BRIGHTNESS, 0);
      Serial.print("client ");
      Serial.print(client.remoteIP());
      Serial.println(" connected");
    }

    // check disconnect
    if (!client.connected() && alreadyConnected) {
      alreadyConnected = false;
      Serial.println("client disconnected");

      // send servo halt
      lss_halt_all();
      setLED(0, 0, INFO_LED_BRIGHTNESS);
      return;
    }

    int inputSize = client.available();
    if (inputSize > 0) {
      for (int i = 0; i < inputSize; i++) {
        receiveBuffer[i] = client.read();
      }

      // evaluate
      char target = read(0);
      char op = read(1);

      int returninputSize = parse(inputSize, target, op);

      // check if no return, then send a 1 back
      if (returninputSize == 0) {
        returninputSize = 1;
        write(0, 1);
      }

      client.write(sendBuffer, returninputSize);
    }
  }

  delay(5);
}

int read(int index) {
  return receiveBuffer[index] & 0xFF;
}

int readInt(int index) {
  return (read(index) << 8) | read(index + 1);
}

void writeInt(int index, int value) {
  write(index, value >> 8 & 0xFF);
  write(index + 1, value & 0xFF);
}

void writeFloat(int index, float value) {
  byte *b = (byte *)&value;
  sendBuffer[index] = b[0];
  sendBuffer[index + 1] = b[1];
  sendBuffer[index + 2] = b[2];
  sendBuffer[index + 3] = b[3];
}

void write(int index, char value) {
  sendBuffer[index] = value;
}

int parse(int inputSize, char target, char op) {

  // set pin mode
  if (target == tPinMode && op == opWrite) {
    pinMode(read(2), read(3));
    return 0;
  }

  // analog write
  if (target == tAnalogPin && op == opWrite) {
    analogWrite(read(2), read(3));
    return 0;
  }

  // digital write
  if (target == tDigitalPin && op == opWrite) {
    digitalWrite(read(2), read(3));
    return 0;
  }

  // analog read
  if (target == tAnalogPin && op == opRead) {
    int value = analogRead(read(2));
    writeInt(0, value);
    return 2;
  }

  // digital read
  if (target == tDigitalPin && op == opRead) {
    char value = digitalRead(read(2));
    write(0, value);
    return 1;
  }

  // set led color
  if (target == tLED && op == opWrite) {
    setLED(read(2), read(3), read(4));
    return 0;
  }

  // send servo command
  if (target == tServo && op == opWrite) {
    String cmd = String(receiveBuffer + 2);
    lss_sendRaw(cmd);
    return 0;
  }

  // imu
  if (target == tIMU && op == opRead) {
    writeFloat(0, complementaryRoll);
    writeFloat(4, complementaryPitch);
    writeFloat(8, complementaryYaw);
    return 12;
  }

  // debug info
  Serial.print("NOP: ");
  Serial.println(inputSize);

  return 0;
}
