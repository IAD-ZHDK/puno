# PUNO
PUNO (Processing-Arduino) is a framework to program microcontrollers from within Processing.

## Setup

To setup the firmware on an Arduino UNO WiFi Rev2, download and install the [WiFiNINA](https://github.com/arduino-libraries/WiFiNINA) library (or from [library manager](https://www.arduino.cc/en/guide/libraries#toc2)).

To connect with an Ardunio, you first have to get its IP address. Follow the steps to obtain the address:

1. Open Arduino IDE
2. Plugin Arduino with a USB cable
3. Open Serial Monitor (Baudrate: `9600`)
4. Arduino will print out it's IP address (f.e. `192.168.1.3`)

## API
The API is very similar to the default Arduino API. It is possible to read and write digital and analog pins, set pin modes and even some higher level methods to control [LSS](http://www.lynxmotion.com/c-189-smart-servos.aspx) servos.

### Connection

- `setupPUNO(PApplet sketch)`

### PIN

### Servo

### IMU

## Examples
tbd
