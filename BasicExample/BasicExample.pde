import static ch.zhdk.PUNODriver.*;

void setup() {
  size(500, 500);
  frameRate(30);

  connectArduino("192.168.1.34");
}

void draw() {
  background(55);

  int pin0 = analogRead(0);
  int pin1 = analogRead(1);
  int pin2 = analogRead(2);
  
  println("PINS: ", pin0, pin1, pin2);
  
  surface.setTitle("PUNO Test " + Math.round(frameRate));
}
