# PUNO
PUNO (Processing-Arduino) is a framework to program microcontrollers from within Processing.

## Setup

### Install the Firmware
To setup the firmware on an Arduino UNO WiFi Rev2, download and install the [WiFiNINA](https://github.com/arduino-libraries/WiFiNINA) library (or from [library manager](https://www.arduino.cc/en/guide/libraries#toc2)).

### Find the Address
To connect with an Ardunio, you first have to get its IP address. Follow the steps to obtain the address:

1. Open Arduino IDE
2. Plugin Arduino with a USB cable
3. Open Serial Monitor (Baudrate: `9600`)
4. Arduino will print out it's IP address (f.e. `192.168.1.3`)

## API
The API is very similar to the default Arduino API. It is possible to read and write digital and analog pins, set pin modes and even some higher level methods to control [LSS](http://www.lynxmotion.com/c-189-smart-servos.aspx) servos.

### Setup & Connection

- `setupPUNO(PApplet sketch)` - Initialises the PUNO driver. This method has to be called in `setup()`, before any other method.
- `connectArduino(String address)` - Connects to a network Arduino device. The address is the [IP address](#find-the-address) of the device.
- `connectWekinator()` - Connects PUNO to directly to a local instance of Wekinator. This instance should use the default ports.

### Digital & Analog Pins

### Servo
- `Servo.changeId(int currentID, int newID)` -
- `Servo.reset(int Id)` -

- `Servo.move(int id, int angle)` - Move to a specific angle
- `Servo.wheelRPM(int Id, int speed)` - Set the rotation speed (in rotations per minute)
- `Servo.wheelDegree(int id, int speed)` - Set the rotation speed (in degrees per second)
- `Servo.moveRelative(int id, int angle)` - Move to an angle relativew to the the current wheel position 

### IMU
- `Servo.imuRead()` -

### Onboard Servo LED
- `Servo.setColorLED(int id, int colorCode)` -
- `Servo.setBlinkingLED(int id, int rate)` -

### Arduino LED
- `Servo.setLEDColor(int c)` -

## Examples
tbd

## About
Developed by Florian Bruggisser & Luke Franzke ZHdK 2020
