package telran.monitoring.pulse;
import java.net.*;
import java.time.Duration;
import java.util.Random;
import java.util.stream.IntStream;

import telran.monitoring.pulse.dto.SensorData;

import java.util.*;


public class PulseSenderAppl {
    private static final int N_PACKETS = 100;
    private static final long TIMEOUT = 500;
    private static final int N_PATIENTS = 5;
    private static final int MIN_PULSE_VALUE = 50;
    private static final int MAX_PULSE_VALUE = 200;
    private static final double JUMP_PROBABILITY = 15;
    private static final double JUMP_POSITIVE_PROBABILITY = 70;
    private static final int MIN_JUMP_PERCENT = 10;
    private static final int MAX_JUMP_PERCENT = 100;
    private static final int PATIENT_ID_PRINTED_VALUES = 3;
    private static final String HOST = "localhost";
    private static final int PORT = 5000;
    private static Random random = new Random();
    private static DatagramSocket socket;
    private static Map<Long, Integer> pulseStorage = new HashMap<>();

    public static void main(String[] args) throws Exception {
        socket = new DatagramSocket();
        IntStream.rangeClosed(1, N_PACKETS)
                .forEach(PulseSenderAppl::sendPulse);
    }

    static void sendPulse(int seqNumber) {
        SensorData data = getRandomSensorData(seqNumber);
        if (data.patientId() == PATIENT_ID_PRINTED_VALUES) {
            System.out.println("Patient " + data.patientId() + ": Pulse = " + data.value()); //prints data of only one patient to check logic
        }
        String jsonData = data.toString();
        sendDatagramPacket(jsonData);
        try {
            Thread.sleep(TIMEOUT);
        } catch (InterruptedException e) {
          
        }
    }

    private static void sendDatagramPacket(String jsonData) {
        byte[] buffer = jsonData.getBytes();
        try {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(HOST), PORT);
            socket.send(packet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static SensorData getRandomSensorData(int seqNumber) {
        long patientId = random.nextInt(1, N_PATIENTS + 1);
        int value = getRandomPulseValue(patientId);
        return new SensorData(seqNumber, patientId, value, System.currentTimeMillis());
    }

    private static int getRandomPulseValue(long patientId) {
    	
    	int returnValue = -1;
    	
    	int prev = pulseStorage.getOrDefault(patientId, -1);

        if (prev == -1) {
            int firstValue = random.nextInt(MIN_PULSE_VALUE, MAX_PULSE_VALUE + 1);
            pulseStorage.put(patientId, firstValue);
            returnValue = firstValue;
        } else {
           
            boolean isJump = random.nextInt(100) < JUMP_PROBABILITY;
            if (!isJump) {
                returnValue = prev;
            }

           
            boolean isPositiveJump = random.nextInt(100) < JUMP_POSITIVE_PROBABILITY;
            int jumpPercent = random.nextInt(MIN_JUMP_PERCENT, MAX_JUMP_PERCENT + 1);

          
            int newValue = prev + (isPositiveJump ? 1 : -1) * (prev * jumpPercent / 100);

          
            if (newValue > MAX_PULSE_VALUE) {
                newValue = MAX_PULSE_VALUE;
            } else if (newValue < MIN_PULSE_VALUE) {
                newValue = MIN_PULSE_VALUE;
            }

           
            pulseStorage.put(patientId, newValue);
            returnValue = newValue;
        }
        return returnValue;
    }
}

