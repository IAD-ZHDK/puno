import static ch.zhdk.PUNODriver.*;

void setup() {
  size(500, 500);

  connectArduino("192.168.1.34");
  connectWekinator();

  pinMode(2, INPUT);

  Servo.setColorLED(0, 1);
  Servo.setColorLED(1, 2);
}

void draw() {
  background(55);

  int lsRight = analogRead(2);
  println("Right: " + lsRight + " L:");

  
  Wekinator.send(lsRight);
  
  
  //float[] values = Wekinator.evaluate(2, lsRight, lsLeft);
  
  //Servo.wheelRPM(0, (int)(values[0] * 60.0));
  //Servo.wheelRPM(1, (int)(values[1] * 60.0));

  /*
  if(lsRight > 300) {
   Servo.wheelRPM(0, 60);
   } else {
   Servo.wheelRPM(0, 20);
   }
   
   if(lsLeft > 300) {
   Servo.wheelRPM(1, 60);
   } else {
   Servo.wheelRPM(1, 20);
   }
   */
}
