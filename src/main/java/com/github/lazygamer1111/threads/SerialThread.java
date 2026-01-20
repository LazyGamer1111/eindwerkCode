package com.github.lazygamer1111.threads;

import com.fazecast.jSerialComm.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;

/**
 * Serial Communication Thread for reading controller data.
 * 
 * This thread is responsible for establishing and maintaining a serial connection
 * to a controller device (likely an Arduino or similar microcontroller) and
 * continuously reading controller input data. The data is read from the serial port,
 * deserialized from a binary format, and stored in a shared array that can be
 * accessed by other threads.
 * 
 * The thread runs in an infinite loop, reading data packets from the serial port
 * and updating the shared controller data array.
 * 
 * @author lazygamer1111
 * @version 1.0
 * @since 2025-11-06
 */
public class SerialThread extends Thread {
    /**
     * Shared controller data array accessed by multiple threads.
     * This array is updated with the latest controller input values
     * read from the serial port.
     */
    private volatile int[] controllerData;
    
    /**
     * Temporary array used during deserialization.
     * This array holds the deserialized data before it is copied
     * to the shared controllerData array.
     */
    private int[] temp = new int[14];
    
    /**
     * Logger for this class.
     */
    Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * Constructs a new Serial Thread with the specified controller data array.
     * 
     * This thread will read controller data from a serial port and store it
     * in the provided array, which is shared with other threads.
     *
     * @param data the shared controller data array to be updated with values from the serial port
     */
    public SerialThread(int[] data) {
        controllerData = data;
    }


    /**
     * Main execution method for the thread.
     * 
     * This method establishes a serial connection to the controller device,
     * configures the serial port parameters, and enters an infinite loop to
     * continuously read and process data packets from the serial port.
     * 
     * The method performs the following steps:
     * 1. Sets the thread name for easier identification
     * 2. Lists available serial ports for debugging
     * 3. Opens and configures the serial port (ttyAMA0)
     * 4. Continuously reads data packets, deserializes them, and updates the shared array
     */
    public void run() {
        // Set thread name for easier identification in logs
        currentThread().setName("Serial Thread");

        // List available serial ports for debugging
        for (SerialPort commPort : SerialPort.getCommPorts()) {
            log.debug("Comm Ports: {}", commPort.getSystemPortName());
        }

        // Configure serial port (Raspberry Pi UART port)
        final String portName = "ttyAMA0";
        SerialPort serialPort = SerialPort.getCommPort(portName);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);
        serialPort.setComPortParameters(115200, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);

        // Open the serial port
        serialPort.openPort();
        log.debug("Open serial port");
        
        try {
            // Allocate buffers for reading data
            ByteBuffer buffer = ByteBuffer.allocate(64);  // Main data buffer
            ByteBuffer size = ByteBuffer.allocate(1);     // Size byte buffer

            ByteBuffer bufferKiss = ByteBuffer.allocate(10);  // Main data buffer
            long last = Instant.now().toEpochMilli();
            
            // Main data reading loop
            while (true) {
                // Clear buffer for new data
                buffer.clear();
                
                // Read packet size byte first
                serialPort.readBytes(size.array(), 1);
                
                // Read the rest of the packet based on size
                serialPort.readBytes(buffer.array(), size.get(0)-1);
                size.clear();
                
                // Set buffer to little-endian byte order (matches controller data format)
                buffer.order(ByteOrder.LITTLE_ENDIAN);

                // Deserialize the data and update the shared array
                temp = deserialize(buffer);
                System.arraycopy(temp, 0, controllerData, 0, temp.length);


            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        
        // Close the serial port if the loop exits
        serialPort.closePort();
    }

    /**
     * Deserializes controller data from a binary buffer.
     * 
     * This method reads a binary data packet from the provided buffer and
     * converts it into an array of integer values representing controller inputs.
     * The method expects a specific packet format starting with a header byte (0x40)
     * followed by a series of 16-bit values.
     *
     * @param buffer the ByteBuffer containing the binary data packet
     * @return an array of integer values representing controller inputs
     */
    private int[] deserialize(ByteBuffer buffer){
        // Ensure buffer is in little-endian byte order
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Create array to hold deserialized data
        int[] controllerData = new int[14];

        // Read the header byte
        byte start = buffer.get();

        // Process the packet based on the header byte
        switch (start){
            case 0x40:  // Standard controller data packet
                // Read 14 short values (28 bytes) and convert to unsigned int
                for (int i = 0; i < 28; i+=2) {
                    controllerData[i/2] = Short.toUnsignedInt(buffer.getShort());
                }
                buffer.clear();
                return controllerData;
        }

        // Return empty array if header byte is not recognized
        return controllerData;
    }


}
