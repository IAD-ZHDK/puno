import processing.serial.*;

final String unixName = "usbmodem";
final int baudRate = 9600;

Serial port;

void setup() {
  size(150, 150);

  // try to find device in list
  String[] devices = Serial.list();
  String deviceName = null;

  // find device os specific
  String os = System.getProperty("os.name");
  if (os.contains("Windows")) {
    deviceName = findOnWindows(devices);
  } else {
    deviceName = findOnUnix(devices);
  }

  if (deviceName == null) {
    println("Could not find Arduino!");
    println("Available Devices: ");
    printArray(devices);
    System.exit(0);
  }

  println("found ", deviceName);
  port = new Serial(this, deviceName, baudRate);
}

void draw() {
  background(55);
  String msg = port.readStringUntil('\n');
  if (msg != null)
    print(msg);
}

String findOnUnix(String[] devices) {
  for (int i = 0; i < devices.length; i++) {
    if (devices[i].contains(unixName)) {
      return devices[i];
    }
  }
  return null;
}

String findOnWindows(String[] devices) {
  String found = null;
  for (String device : devices) {
    print("checking ", device, "...");
    try { 
      Serial serial = new Serial(this, device, baudRate);
      if (serial.active()) {
        delay(1000);
        String input = serial.readString();
        if (input != null && input.contains("PUNO")) {
          found = device;
        }
      }
      serial.stop();
      println("no!");
    } 
    catch (Exception ex) {
      println(" could not open!");
    }
    if (found != null) {
      println(" found!");
      return found;
    }
  }
  return null;
}
