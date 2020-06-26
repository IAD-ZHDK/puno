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

- `digitalWrite(int pin, int value)` - set a pin to either on(1) or off(0) 
- `analogWrite(int, pin, int value)` - write a PWM value to a pin (0-255)
- `int digitalRead(int pin)`- read the digital value from a pin (0 or 1)
- `int anaogRead(int pin)`- read the analog value from a pin (0 to 1023)

### Servo
- `Servo.changeId(int currentID, int newID)` - Seting the ID requires a reset of the motor. ID 254 can be used to broadcast to all attached motors
- `Servo.reset(int Id)` - Reset a motor. Same effect can be achieved with turning on and off the motor. 

- `Servo.move(int id, int angle)` - Move to a specific angle
- `Servo.wheelRPM(int Id, int speed)` - Set the rotation speed (in rotations per minute)
- `Servo.wheelDegree(int id, int speed)` - Set the rotation speed (in degrees per second)
- `Servo.moveRelative(int id, int angle)` - Move to an angle relative to the the current wheel position 

### IMU 
More about rotation axes (https://en.wikipedia.org/wiki/Aircraft_principal_axes)
- `IMU.read()` 
- `IMU.roll`
- `IMU.pitch`
- `IMU.yaw`

### Wekinator
- `Wekinator.train(int... values)`
- `Wekinator.predict(int outputCount, int... values)`
- `Wekinator.output0`
- `Wekinator.output1`
- `Wekinator.output2`
- `Wekinator.output3`
- `Wekinator.output4`

### Onboard Servo LED
- `Servo.setColorLED(int id, int colorCode)` - 0=Off (black); 1=Red 2=Green; 3=Blue; 4=Yellow; 5=Cyan; 6=Magenta; 7=White;
- `Servo.setBlinkingLED(int id, int rate)` -

### Arduino LED
- `setLEDColor(color c)` -

## About
Developed by Florian Bruggisser & Luke Franzke ZHdK 2020
