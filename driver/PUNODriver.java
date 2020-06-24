package ch.zhdk;

import processing.core.PApplet;

import oscP5.*;
import netP5.*;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class PUNODriver {
    // query timeout
    private static final int TIME_OUT = 500;

    // mc send delay
    private static final int SEND_DELAY = 66;

    private static class NS {
        // arduino
        private static final String servo = "/iad/servo/";
        private static final String pin = "/iad/pin/";
        private static final String answer = "/iad/answer";

        private static final String test = "/iad/test";

        private static final String servoCommand = servo + "cmd";

        private static final String pinMode = pin + "mode";
        private static final String digitalWrite = pin + "digitalWrite";
        private static final String analogWrite = pin + "analogWrite";

        private static final String digitalRead = pin + "digitalRead";
        private static final String analogRead = pin + "analogRead";

        private static final String digitalReadMulti = pin + "digitalReadMulti";
        private static final String analogReadMulti = pin + "analogReadMulti";

        private static final String ledColor = "/iad/led/color";
        private static final String imuRaw = "/iad/imu/raw";
        private static final String imuRPY = "/iad/imu/rpy";

        // wekinator
        private static final String wekInput = "/wek/inputs";
        private static final String wekOutput = "/wek/outputs";
    }

    // defines from arduino.h
    public static int HIGH = 0x1;
    public static int LOW = 0x0;

    public static int PWMRANGE = 1023;

    //GPIO FUNCTIONS;
    public static int INPUT = 0x00;
    public static int INPUT_PULLUP = 0x02;
    public static int INPUT_PULLDOWN_16 = 0x04;
    public static int OUTPUT = 0x01;
    public static int OUTPUT_OPEN_DRAIN = 0x03;
    public static int WAKEUP_PULLUP = 0x05;
    public static int WAKEUP_PULLDOWN = 0x07;
    public static int SPECIAL = 0xF8;
    public static int FUNCTION_0 = 0x08;
    public static int FUNCTION_1 = 0x18;
    public static int FUNCTION_2 = 0x28;
    public static int FUNCTION_3 = 0x38;
    public static int FUNCTION_4 = 0x48;

    public static int LED_BUILTIN = 25;

    // channels

    private static OscChannel oscArduino;
    private static OscChannel oscWekinator;
    private static PApplet sketch;

    // setup
    public static void setupPUNO(PApplet sketch) {
        PUNODriver.sketch = sketch;
        sketch.frameRate(30);

        ShutdownHandler handler = new ShutdownHandler();
    }

    public static void connectArduino(int arduinoId) {
        connectArduino("puno-" + arduinoId + ".local");
    }

    public static void connectArduino(String address) {
        oscArduino = new OscChannel(address, 9000, 8000, NS.answer, true);
    }

    public static void connectWekinator() {
        connectWekinator("127.0.0.1", 12000, 6448);
    }

    public static void connectWekinator(String address, int inPort, int outPort) {
        oscWekinator = new OscChannel(address, inPort, outPort, NS.wekOutput, false);
        oscWekinator.useDelay = false;
    }

    // wekinator
    public static class Wekinator {
        public static void send(float[] values) {
            send(boxFloatArray(values));
        }

        public static void send(Integer... values) {
            send(boxFloatArray(values));
        }

        public static void send(Float... values) {
            oscWekinator.sendOsc(NS.wekInput, (Object[]) values);
        }

        public static float[] evaluate(int outputCount, float[] values) {
            return evaluate(outputCount, boxFloatArray(values));
        }

        public static float[] evaluate(int outputCount, Integer... values) {
            return evaluate(outputCount, boxFloatArray(values));
        }

        public static float[] evaluate(int outputCount, Float... values) {
            // check error here
            return unboxFloatArray(oscWekinator.sendOscQueries(NS.wekInput, outputCount, -1000f, values));
        }
    }

    // servo

    // movement
    public static class Servo {
        public static void move(int id, int angle) {
            sendRaw("#" + id + "D" + angle);
        }

        public static void moveRelative(int id, int angle) {
            sendRaw("#" + id + "MD" + angle);
        }

        public static void wheelDegree(int id, int speed) {
            sendRaw("#" + id + "WD" + speed);
        }

        public static void wheelRPM(int id, int speed) {
            sendRaw("#" + id + "WR" + speed);
        }

        public static void halt(int id) {
            sendRaw("#" + id + "H");
        }

        // settings
        public static void setGyre(int id, boolean cc) {
            sendRaw("#" + id + "CG" + (cc ? "1" : "-1"));
        }

        public static void setGyreClockwise(int id) {
            sendRaw("#" + id + "CG1");
        }

        public static void setGyreCounterClockwise(int id) {
            sendRaw("#" + id + "CG-1");
        }

        public static void setColorLED(int id, int colorCode) {
            sendRaw("#" + id + "CLED" + colorCode);
        }

        public static void setBlinkingLED(int id, int rate) {
            sendRaw("#" + id + "CLB" + rate);
        }

        public static void changeId(int id, int newId) {
            sendRaw("#" + id + "CID" + newId);
        }

        public static void reset(int id) {
            sendRaw("#" + id + "RESET");
        }

        public static void sendRaw(String command) {
            oscArduino.sendOsc(NS.servoCommand, command);
        }
    }

    // pin
    public static void pinMode(int pin, int mode) {
        oscArduino.sendOsc(NS.pinMode, pin, mode);
    }

    public static void analogWrite(int pin, int value) {
        oscArduino.sendOsc(NS.analogWrite, pin, value);
    }

    public static void digitalWrite(int pin, int value) {
        oscArduino.sendOsc(NS.digitalWrite, pin, value);
    }

    public static int digitalRead(int pin) {
        return oscArduino.sendOscQuery(NS.digitalRead, -1, pin);
    }

    public static int[] digitalRead(Integer... pins) {
        return unboxIntArray(oscArduino.sendOscQueries(NS.digitalReadMulti, pins.length, -1, pins));
    }

    public static int analogRead(int pin) {
        return oscArduino.sendOscQuery(NS.analogRead, -1, pin);
    }

    public static int[] analogRead(Integer... pins) {
        return unboxIntArray(oscArduino.sendOscQueries(NS.analogReadMulti, pins.length, -1, pins));
    }

    // imu
    // returns Acceleration x, y, z and Gyroscope x, y, z
    public static float[] imuReadRaw() {
        return unboxFloatArray(oscArduino.sendOscQueries(NS.imuRaw, 6, -1000f));
    }

    // returns roll, pitch, yaw in degress (yaw is drifting because no magnet)
    public static float[] imuRead() {
        return unboxFloatArray(oscArduino.sendOscQueries(NS.imuRPY, 3, -1000f));
    }

    // others
    public static void setLEDColor(int c) {
        oscArduino.sendOsc(NS.ledColor, (int) sketch.red(c), (int) sketch.green(c), (int) sketch.blue(c));
    }

    // test
    public static int sendTest() {
        return oscArduino.sendOscQuery(NS.test, -1);
    }

    public static void sendTestRGB() {
        oscArduino.sendOsc("/test/rgb", 1, 2, 3);
    }

    public static void sendTestW() {
        oscArduino.sendOsc("/test/w", 7);
    }

    // remote class handling the two-way osc connection
    public static class OscChannel implements Runnable {

        private NetAddress remoteAddress;
        private OscP5 osc;
        private DatagramSocket outSocket;

        private final AtomicInteger queryIdCounter = new AtomicInteger(0);
        private ConcurrentHashMap<Integer, Future> queries = new ConcurrentHashMap<Integer, Future>();
        private Thread timeoutThread = new Thread(this);
        private volatile boolean running = true;
        private boolean useDelay = true;
        private boolean exclusiveSocket = false;

        private boolean useMessageId;
        private String answerNamespace;
        private int msgCount = 0;

        OscChannel(String address, int inPort, int outPort, String answerNamespace, boolean useMessageId) {
            remoteAddress = new NetAddress(address, outPort);
            osc = new OscP5(this, inPort);
            this.answerNamespace = answerNamespace;
            this.useMessageId = useMessageId;

            try {
                outSocket = new DatagramSocket();
                outSocket.setBroadcast(true);
            } catch (SocketException ex) {
                System.out.println("Socket: " + ex.getMessage());
            }

            timeoutThread.setDaemon(true);
            timeoutThread.start();
        }

        void stop() {
            try {
                running = false;
                osc.stop();
                timeoutThread.join(1000);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }

        public void run() {
            List<Integer> keysToRemove = new ArrayList<>();
            while (running) {
                try {

                    keysToRemove.clear();

                    // check
                    for (Integer key : queries.keySet()) {
                        Future future = queries.get(key);
                        future.checkTimeout();

                        if (future.finished)
                            keysToRemove.add(key);
                    }

                    // cleanup
                    for (Integer key : keysToRemove) {
                        queries.remove(key);
                    }

                    Thread.sleep(250);
                } catch (Exception ex) {
                    System.out.println("Error while cleanup: " + ex.getMessage());
                }
            }
        }

        private void oscEvent(OscMessage msg) {
            if (exclusiveSocket && !msg.netAddress().address().equals(remoteAddress.address()))
                return;

            if (msg.checkAddrPattern(answerNamespace)) {
                // get future and update it with the return value
                int parameterPtr = 0;

                int id = queryIdCounter.get();
                if (useMessageId) {
                    id = msg.get(parameterPtr).intValue();
                    parameterPtr++;
                }

                if (queries.containsKey(id)) {
                    Future future = queries.get(id);
                    Object[] args = msg.arguments();

                    if (args.length - parameterPtr < future.values.length) {
                        System.out.println("Error: too few arguments in osc answer");
                        System.out.println(msg);
                    }

                    for (int i = 0; i < future.get().length; i++) {
                        future.set(i, args[i + parameterPtr]);
                    }

                    future.release();
                }
            }
        }

        private <T> T sendOscQuery(String address, T defaultValue, T... args) {
            return (T) sendOscQueries(address, 1, defaultValue, args)[0];
        }

        private <T> T[] sendOscQueries(String address, int resultLength, T defaultValue, T... args) {
            Future future = new Future(resultLength, defaultValue);
            int id = queryIdCounter.incrementAndGet();
            queries.put(id, future);

            OscMessage msg = new OscMessage(address);

            if (useMessageId)
                msg.add(id);

            addObjectArgs(msg, args);
            sendOsc(msg, remoteAddress);

            // block
            future.await();

            return (T[]) future.get();
        }

        // osc helper methods
        private void sendOsc(OscMessage msg) {
            sendOsc(msg, remoteAddress);
        }

        private synchronized void sendOsc(OscMessage msg, final NetAddress address) {
            try {
                byte[] data = msg.getBytes();
                DatagramPacket myPacket = new DatagramPacket(data, data.length, address.inetaddress(), address.port());
                outSocket.send(myPacket);

                // update msg count
                msgCount++;

                // wait a bit to not stress the micro-controller
                // bug filed
                // https://github.com/arduino-libraries/WiFiNINA/issues/108
                if (useDelay && msgCount % 2 == 0) {
                    Thread.sleep(SEND_DELAY);
                    msgCount = 0;
                }
            } catch (Exception ex) {
                System.out.println("Socket: " + ex.getMessage());
            }
        }

        private void sendOsc(String namespace, Object... args) {
            OscMessage msg = new OscMessage(namespace);
            addObjectArgs(msg, args);
            sendOsc(msg, remoteAddress);
        }

        // helper methods
        void addObjectArgs(OscMessage msg, Object... args) {
            for (Object obj : args) {
                if (obj instanceof Integer)
                    msg.add((int) obj);
                else if (obj instanceof Float)
                    msg.add((float) obj);
                else if (obj instanceof String)
                    msg.add((String) obj);
            }
        }

        class Future {
            private volatile Object[] values;
            private Semaphore mutex = new Semaphore(0);
            private long timestamp = System.currentTimeMillis();
            private volatile boolean finished = false;

            public Future(int resultCount, Object defaultValue) {
                values = new Object[resultCount];
                for (int i = 0; i < values.length; i++) {
                    values[i] = defaultValue;
                }
            }

            void await() {
                try {
                    mutex.acquire();
                } catch (Exception ex) {
                    System.out.println("Future Error: " + ex.getMessage());
                }
            }

            synchronized void release() {
                this.finished = true;
                mutex.release();
            }

            Object[] get() {
                return values;
            }

            void set(int index, Object value) {
                values[index] = value;
            }

            synchronized void checkTimeout() {
                if (System.currentTimeMillis() - timestamp < TIME_OUT) return;

                System.out.println("package timed out!");

                // timed out
                finished = true;
                mutex.release();
            }
        }
    }

    static int[] unboxIntArray(Object[] array) {
        int[] result = new int[array.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (int) array[i];
        }
        return result;
    }

    static float[] unboxFloatArray(Object[] array) {
        float[] result = new float[array.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (float) array[i];
        }
        return result;
    }

    static Integer[] boxIntArray(int[] array) {
        Integer[] result = new Integer[array.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (Integer) array[i];
        }
        return result;
    }

    static Float[] boxFloatArray(float[] array) {
        Float[] result = new Float[array.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (Float) array[i];
        }
        return result;
    }

    static Float[] boxFloatArray(Integer[] array) {
        Float[] result = new Float[array.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (float) array[i];
        }
        return result;
    }

    // clean shutdown
    static class ShutdownHandler implements Runnable {

        public ShutdownHandler() {
            Runtime.getRuntime().addShutdownHook(new Thread(this));
        }

        public void run() {
            System.out.println("shutting down...");

            if (oscArduino != null) {
                Servo.halt(254);
                oscArduino.stop();
            }

            if (oscWekinator != null)
                oscWekinator.stop();

            System.out.println("done!");
        }
    }
}