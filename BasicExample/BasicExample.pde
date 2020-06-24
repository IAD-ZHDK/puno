import static ch.zhdk.PUNODriver.*;

void setup() {
  size(500, 500);

  setupPUNO(this);
  connectArduino("192.168.1.34");
}

void draw() {
  background(55);

  int pin0 = analogRead(0);
  int pin1 = analogRead(1);
  int pin2 = analogRead(2);

  println("PINS: ", pin0, pin1, pin2);

  float[] data = imuRead();
  println("Roll: ", data[0], " Pitch: ", data[1], " Yaw: ", data[2]);

  surface.setTitle("PUNO Test " + Math.round(frameRate));
}
