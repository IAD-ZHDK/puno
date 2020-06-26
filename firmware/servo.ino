SoftwareSerial servoSerial(8, 9);

void setupServo() {
  Serial.println("setting up lss...");
  servoSerial.begin(115200);
  delay(100);
  // this is used to clear the serial buffer
  servoSerial.print("#0D1500\r");
  delay(1000);
}

void lss_changeId(int currentId, int newId) {
  lss_sendRaw(String("#") + currentId + String("CID") + newId);
}

void lss_reset(int id) {
  lss_sendRaw(String("#") + id + String("RESET"));
}

void lss_halt_all() {
  lss_sendRaw("#254H");
}

void lss_sendRaw(String command) {
  servoSerial.print(command + "\r");
}
