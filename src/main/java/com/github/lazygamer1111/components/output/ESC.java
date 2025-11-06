package com.github.lazygamer1111.components.output;

import java.util.HashMap;
import java.util.Map;

/**
 * Electronic Speed Controller (ESC) interface class.
 * 
 * This class provides an interface to control ESCs via a state machine (SM)
 * on the Raspberry Pi. It handles initialization of the state machine with
 * appropriate timing parameters based on the desired communication speed,
 * and provides methods to send control frames to the ESC.
 * 
 * The class uses native methods to interact with the hardware.
 * 
 * @author lazygamer1111
 * @version 1.0
 * @since 2025-11-06
 */
public class ESC {
    /** 
     * Mapping of communication speeds (in kbps) to timing values.
     * These values are used to configure the state machine for different communication speeds.
     */
    private static final Map<Integer, Integer> speed = new HashMap<>();
    
    /**
     * State machine handle returned by the native initialization method.
     * This handle is used in subsequent calls to the native methods.
     */
    public int sm;

    /**
     * Static initializer block that populates the speed mapping.
     * Each entry maps a communication speed in kbps to a timing value
     * used by the state machine.
     */
    static {
        // Map communication speeds (kbps) to timing values
        speed.put(600, 625);   // 600 kbps
        speed.put(1200, 313);  // 1200 kbps
        speed.put(300, 1250);  // 300 kbps
        speed.put(150, 2500);  // 150 kbps
    }

    /**
     * Constructs a new ESC controller for the specified pin and communication speed.
     * 
     * This constructor initializes the state machine on the specified pin with
     * timing parameters appropriate for the requested communication speed.
     *
     * @param pin the GPIO pin number to use for ESC communication
     * @param speedkbs the communication speed in kilobits per second (valid values: 150, 300, 600, 1200)
     * @throws IllegalArgumentException if an unsupported speed is specified
     */
    public ESC(int pin, int speedkbs) {
        // Look up the timing value for the requested speed
        Integer timeZero = speed.get(speedkbs);
        if (timeZero == null) {
            throw new IllegalArgumentException("Invalid speedkbs: " + speedkbs + " (150,300,600,1200)");
        }
        // Initialize the state machine with the appropriate pin and timing
        sm = init_SM(pin, speed.get(speedkbs));
    }

    /**
     * Sends a control frame to the ESC.
     * 
     * This method constructs a frame with the specified throttle value,
     * optionally sets the telemetry bit, adds a checksum, and sends
     * the frame to the ESC via the state machine.
     *
     * @param throttle the throttle value to send (0-2047)
     * @param telemetry whether to request telemetry data from the ESC
     */
    public void sendFrame(int throttle, boolean telemetry) {
        // Create the basic frame with the throttle value
        short frame = (short) throttle;
        
        // Set the telemetry bit if requested
        if (telemetry) {
            frame |= 2048;  // Set bit 11 (telemetry request bit)
        }

        // Add checksum to the frame
        frame = addChecksum(frame);

        // Send the frame to the ESC
        put(sm, frame);
    }

    /**
     * Calculates and adds a 4-bit checksum to the frame.
     * 
     * The checksum is calculated as the inverted XOR of the frame,
     * the frame shifted right by 4 bits, and the frame shifted right by 8 bits,
     * masked to 4 bits. The checksum is then OR'd with the original frame.
     *
     * @param frame the frame to add a checksum to
     * @return the frame with checksum added
     */
    private short addChecksum(short frame){
        // Calculate CRC: invert(frame XOR (frame>>4) XOR (frame>>8)) & 0x0F
        short crc = (short) ((~(frame ^ (frame >> 4) ^ (frame >> 8))) & 0x0F);
        // Add CRC to frame by OR'ing it with the original frame
        crc = (short) (frame | crc);
        return crc;
    }

    /**
     * Initializes a state machine for ESC communication.
     * 
     * This native method initializes a PIO state machine on the specified pin
     * with the specified timing parameters.
     *
     * @param pin the GPIO pin number to use
     * @param sm the timing parameter for the state machine
     * @return a handle to the initialized state machine
     */
    private native int init_SM(int pin, int sm);

    /**
     * Sends data to the ESC via the state machine.
     * 
     * This native method sends the specified data to the ESC
     * using the specified state machine.
     *
     * @param sm the state machine handle
     * @param data the data to send
     */
    public native void put(int sm, short data);
    
    /**
     * Receives data from the ESC via the state machine.
     * 
     * This native method reads data from the ESC using the specified state machine.
     *
     * @param sm the state machine handle
     * @return the data received from the ESC
     */
    private native short pop(int sm);
}
