package ch.zhdk;

import processing.core.PApplet;

import oscP5.*;
import netP5.*;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class PUNODriver {
    // debug mode
    private static final boolean TRACE = true;

    // query timeout
    public static final int QUERY_TIMEOUT = 1000;

    // TCP Command timeout
    public static final int CMD_TIMEOUT = 2500;

    // mc send delay
    public static int SEND_DELAY = 5;

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

    private static class PUNOProtocol {
        // target
        private static final byte tPinMode = 0x01;
        private static final byte tAnalogPin = 0x02;
        private static final byte tDigitalPin = 0x03;
        private static final byte tServo = 0x04;
        private static final byte tIMU = 0x05;
        private static final byte tLED = 0x06;

        // operation
        private static final byte opRead = 0x01;
        private static final byte opWrite = 0x02;
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

    public static int A0 = 0;
    public static int A1 = 1;
    public static int A2 = 2;
    public static int A3 = 3;
    public static int A4 = 4;
    public static int A5 = 5;

    // channels

    private static PUNOChannel punoArduino;
    private static OscChannel oscWekinator;
    private static PApplet sketch;

    // utils
    public static int map(int value, int startA, int endA, int startB, int endB) {
        return Math.round(PApplet.map(value, startA, endA, startB, endB));
    }

    // setup

    /**
     * Initialises the PUNO context.
     *
     * @param sketch The parent sketch of PUNO
     */
    public static void setupPUNO(PApplet sketch) {
        setupPUNO(sketch, false);
    }

    public static void setupPUNO(PApplet sketch, boolean trace) {
        System.out.print("setting up puno...");
        PUNODriver.sketch = sketch;
        sketch.frameRate(20);

        // make clean shutdown
        /*
        Thread shutdownThread = new Thread(() -> {
            System.out.println("shutting down...");

            if (punoArduino != null && punoArduino.isOpen()) {
                punoArduino.close();
            }

            if (oscWekinator != null)
                oscWekinator.stop();

            System.out.println("finished!");
            sketch.exit();
        });
        shutdownThread.setDaemon(true);
         */
        // todo: application keeps hanging!
        // Runtime.getRuntime().addShutdownHook(shutdownThread);

        System.out.println("done!");
    }

    public static void connectArduino(String address) {
        System.out.print("trying to connect to " + address + "...");
        punoArduino = new PUNOChannel(address, 8000);
        punoArduino.open();
        System.out.println("connected!");
    }

    public static void connectWekinator() {
        connectWekinator("127.0.0.1", 12000, 6448);
    }

    public static void connectWekinator(String address, int inPort, int outPort) {
        System.out.print("trying to connect to wekinator...");
        oscWekinator = new OscChannel(address, inPort, outPort, NS.wekOutput, false);
        oscWekinator.useDelay = false;
        System.out.println("done!");
    }

    // wekinator
    public static class Wekinator {
        public static float output1;
        public static float output2;
        public static float output3;
        public static float output4;
        public static float output5;

        public static void predict(int outputCount, float[] values) {
            predict(outputCount, PUNOUtil.boxFloatArray(values));
        }

        public static void predict(int outputCount, Integer... values) {
            predict(outputCount, PUNOUtil.boxFloatArray(values));
        }

        public static void predict(int outputCount, Float... values) {
            float[] results = evaluate(outputCount, values);

            if (results.length > 0)
                output1 = results[0];

            if (results.length > 1)
                output2 = results[1];

            if (results.length > 2)
                output3 = results[2];

            if (results.length > 3)
                output4 = results[3];

            if (results.length > 4)
                output5 = results[4];
        }

        public static void train(float[] values) {
            train(PUNOUtil.boxFloatArray(values));
        }

        public static void train(Integer... values) {
            train(PUNOUtil.boxFloatArray(values));
        }

        public static void train(Float... values) {
            oscWekinator.sendOsc(NS.wekInput, values);
        }

        public static float[] evaluate(int outputCount, float[] values) {
            return evaluate(outputCount, PUNOUtil.boxFloatArray(values));
        }

        public static float[] evaluate(int outputCount, Integer... values) {
            return evaluate(outputCount, PUNOUtil.boxFloatArray(values));
        }

        public static float[] evaluate(int outputCount, Float... values) {
            // check error here
            return PUNOUtil.unboxFloatArray(oscWekinator.sendOscQueries(NS.wekInput, outputCount, -1000f, values));
        }
    }

    // servo

    // movement
    public static class Servo {
        public static void move(int id, int angle) {
            sendRaw("#" + id + "D" + angle);
        }

        public static void move(int id, int angle, int speed) {
            sendRaw("#" + id + "D" + angle + "SD" + speed);
        }

        public static void moveTimed(int id, int angle, int milliseconds) {
            sendRaw("#" + id + "D" + angle + "T" + milliseconds);
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

        public static void limp(int id) {
            sendRaw("#" + id + "L");
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
            punoArduino.send(PUNOProtocol.tServo, PUNOProtocol.opWrite, command + "\0");
        }
    }

    // imu
    public static class IMU {
        // vars
        public static float roll;
        public static float pitch;
        public static float yaw;

        public static void read() {
            float[] values = imuRead();
            roll = values[0];
            pitch = values[1];
            yaw = values[2];
        }
    }

    // pin
    public static void pinMode(int pin, int mode) {
        punoArduino.send(PUNOProtocol.tPinMode, PUNOProtocol.opWrite, (byte) pin, (byte) mode);
    }

    public static void analogWrite(int pin, int value) {
        punoArduino.send(PUNOProtocol.tAnalogPin, PUNOProtocol.opWrite, (byte) pin, (byte) value);
    }

    public static void digitalWrite(int pin, int value) {
        punoArduino.send(PUNOProtocol.tDigitalPin, PUNOProtocol.opWrite, (byte) pin, (byte) value);
    }

    public static int digitalRead(int pin) {
        punoArduino.send(PUNOProtocol.tDigitalPin, PUNOProtocol.opRead, (byte) pin);
        return punoArduino.read(0);
    }

    public static int analogRead(int pin) {
        punoArduino.send(PUNOProtocol.tAnalogPin, PUNOProtocol.opRead, (byte) pin);
        return punoArduino.readInt(0);
    }

    // returns roll, pitch, yaw in degress (yaw is drifting because no magnet)
    public static float[] imuRead() {
        punoArduino.send(PUNOProtocol.tIMU, PUNOProtocol.opRead);
        ByteBuffer buffer = ByteBuffer.wrap(punoArduino.receiveBuffer).order(ByteOrder.LITTLE_ENDIAN);

        float roll = buffer.getFloat();
        float pitch = buffer.getFloat();
        float yaw = buffer.getFloat();

        return new float[]{roll, pitch, yaw};
    }

    // others
    public static void setLEDColor(int c) {
        punoArduino.send(PUNOProtocol.tLED, PUNOProtocol.opWrite,
                (byte) sketch.red(c), (byte) sketch.green(c), (byte) sketch.blue(c));
    }

    // remote class handling two-way tcp connection
    public static class PUNOChannel {
        private final String address;
        private final int port;

        private Socket client;
        private OutputStream out;
        private InputStream in;

        private byte[] receiveBuffer = new byte[256];
        private byte[] sendBuffer = new byte[256];

        public PUNOChannel(String address, int port) {
            this.address = address;
            this.port = port;
        }

        public void writeInt(int index, int value) {
            sendBuffer[index] = (byte) (value >> 8 & 0xFF);
            sendBuffer[index + 1] = (byte) (value & 0xFF);
        }

        public int readInt(int index) {
            return (read(index) << 8) | read(index + 1);
        }

        public int read(int index) {
            return receiveBuffer[index] & 0xFF;
        }

        public void open() {
            try {
                client = new Socket();
                client.setTcpNoDelay(true);
                client.connect(new InetSocketAddress(address, port), 5000);

                out = new BufferedOutputStream(client.getOutputStream());
                in = new BufferedInputStream((client.getInputStream()));
            } catch (IOException e) {
                System.out.println();
                System.err.println("Connection Error: " + e.getMessage());
                System.exit(0);
            }
        }

        public boolean isOpen() {
            return client.isConnected();
        }

        public int send(byte target, byte operation, String msg) {
            int size = 2 + msg.length();

            // add header
            sendBuffer[0] = target;
            sendBuffer[1] = operation;

            // fill payload
            System.arraycopy(msg.getBytes(), 0, sendBuffer, 2, msg.length());

            return sendRaw(sendBuffer, size);
        }

        public int send(byte target, byte operation, int... params) {
            int size = 2 + (params.length * 2);

            // add header
            sendBuffer[0] = target;
            sendBuffer[1] = operation;

            // fill values
            int ptr = 2;
            for (int i = 0; i < params.length; i++) {
                writeInt(ptr, params[i]);
                ptr += 2;
            }

            return sendRaw(sendBuffer, size);
        }

        public int send(byte target, byte operation, byte... params) {
            int size = 2 + params.length;

            // add header
            sendBuffer[0] = target;
            sendBuffer[1] = operation;

            // add payload
            System.arraycopy(params, 0, sendBuffer, 2, params.length);

            return sendRaw(sendBuffer, size);
        }

        public int sendRaw(byte[] packet) {
            return sendRaw(packet, packet.length);
        }

        public int sendRaw(byte[] packet, int length) {
            try {
                out.write(packet, 0, length);
                out.flush();

                int size;
                int waitCounter = 1;

                Thread.sleep(SEND_DELAY);

                // wait for response
                do {
                    size = in.available();
                    Thread.sleep(1);

                    if (waitCounter++ % CMD_TIMEOUT == 0) {
                        trace("CMD TIMEOUT", String.valueOf(waitCounter));
                        out.write(packet);
                        out.flush();
                    }
                } while (size <= 0);

                // read buffer from stream
                in.read(receiveBuffer, 0, size);
                return size;
            } catch (IOException e) {
                System.err.println("error sending: " + e.getMessage());
            } catch (InterruptedException e) {
                System.err.println("error waiting: " + e.getMessage());
            }
            return -1;
        }

        public void close() {
            try {
                System.out.println("PUNO client is closing...");
                in.close();
                out.close();
                client.close();
            } catch (IOException e) {
                System.err.println("error closing: " + e.getMessage());
            }
        }
    }

    // remote class handling the two-way osc connection
    public static class OscChannel implements Runnable {

        private final NetAddress remoteAddress;
        private final OscP5 osc;
        private DatagramSocket outSocket;

        private final AtomicInteger queryIdCounter = new AtomicInteger(0);
        private final ConcurrentHashMap<Integer, Future> queries = new ConcurrentHashMap<Integer, Future>();
        private final Thread timeoutThread = new Thread(this);
        private volatile boolean running = true;
        private boolean useDelay = true;
        private final boolean exclusiveSocket = false;

        private final boolean useMessageId;
        private final String answerNamespace;
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
                        System.err.println("Error: too few arguments in osc answer");
                        System.err.println(msg);
                    }

                    for (int i = 0; i < future.get().length; i++) {
                        future.set(i, args[i + parameterPtr]);
                    }

                    future.release();
                }
            }
        }

        private <T> T sendOscQuery(String address, T defaultValue, T... args) {
            return sendOscQueries(address, 1, defaultValue, args)[0];
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
            private final Object[] values;
            private final Semaphore mutex = new Semaphore(0);
            private final long timestamp = System.currentTimeMillis();
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
                if (System.currentTimeMillis() - timestamp < QUERY_TIMEOUT) return;

                System.out.println("package timed out!");

                // timed out
                finished = true;
                mutex.release();
            }
        }
    }

    public static class PUNOUtil {
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
                result[i] = array[i];
            }
            return result;
        }

        static Float[] boxFloatArray(float[] array) {
            Float[] result = new Float[array.length];
            for (int i = 0; i < result.length; i++) {
                result[i] = array[i];
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
    }

    // trace methods
    private static void trace(String... msgs) {
        if (!TRACE) return;
        trace(String.join(" ", msgs));
    }

    private static void trace(String msg) {
        if (!TRACE) return;
        System.out.println(msg);
    }
}