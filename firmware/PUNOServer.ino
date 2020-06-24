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

boolean alreadyConnected = false;

char receiveBuffer[256];
char sendBuffer[256];

void setupPUNO() {
  server.begin();
}

void updatePUNO() {
  WiFiClient client = server.available();

  if (client) {
    if (!alreadyConnected) {
      client.flush();
      Serial.println("client connected");
      alreadyConnected = true;
    }

    int size = client.available();
    if (size > 0) {
      // read
      /*
        Serial.print("Packet [");
        Serial.print(size);
        Serial.println("]");
      */

      for (int i = 0; i < size; i++) {
        receiveBuffer[i] = client.read();
        //Serial.print((int)receiveBuffer[i], HEX);
      }
      //Serial.println();

      // evaluate
      char target = read(0);
      char op = read(1);

      int returnSize = parse(size, target, op);

      // check if no return, then send a 1 back
      if (returnSize == 0) {
        returnSize = 1;
        write(0, 1);
      }

      client.write(sendBuffer, returnSize);
      client.flush();
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

void write(int index, char value) {
  sendBuffer[index] = value;
}

int parse(int size, char target, char op) {

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
#ifdef ENABLE_RGBLED
    WiFiDrv::analogWrite(25, read(2));
    WiFiDrv::analogWrite(26, read(3));
    WiFiDrv::analogWrite(27, read(4));
#endif
    return 0;
  }

  // send servo command
  if (target == tServo && op == opWrite) {
    String cmd = String(receiveBuffer + 2);
    lss_sendRaw(cmd);
    return 0;
  }

  return 0;
}
