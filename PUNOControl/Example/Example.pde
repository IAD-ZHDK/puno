void setup() {
  size(500, 500);
  connectArduino("10.128.142.31");
  // connectWekinator();
  pinMode(1, INPUT);
  pinMode(2, INPUT);
  //Servo.changeId(254, 1);
  //Servo.reset(254);
  Servo.setColorLED(1, 5);
 // Servo.setColorLED(1, 2);
}

void draw() {
  background(55);
  int sensor0 = analogRead(0);
  int sensor1 = analogRead(1);
  println("Senor 0: " + sensor0);
  println("Senor 1: " + sensor1);
  Servo.move(1, (int)map(sensor0, 0, 1024, 0, 3600));
  //Wekinator.send(lsRight);
  
  
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
