package com.github.lazygamer1111.threads;

import com.fazecast.jSerialComm.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;

/**
 * The type Serial thread.
 */
public class SerialThread extends Thread {
    private volatile int[] controllerData;
    /**
     * Instantiates a new Serial thread.
     *
     * @param data the data
     */
    public SerialThread(int[] data) {
        controllerData = data;
    }
    Logger log = LoggerFactory.getLogger(this.getClass());


    public void run() {
        currentThread().setName("Serial Thread");

        for (SerialPort commPort : SerialPort.getCommPorts()) {
            log.debug("Comm Ports: {}", commPort.getSystemPortName());
        }

        final String portName = "ttyAMA0";
        SerialPort serialPort = SerialPort.getCommPort(portName);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);
        serialPort.setComPortParameters(115200, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);

        serialPort.openPort();
        log.debug("Open serial port");
        try {
            ByteBuffer buffer = ByteBuffer.allocate(64);
            ByteBuffer size = ByteBuffer.allocate(1);
            ByteBuffer temp = ByteBuffer.allocate(31);
            long last = Instant.now().toEpochMilli();
            while (true) {
                buffer.clear();
                serialPort.readBytes(size.array(), 1);
                buffer.put(size);
                size.clear();
                serialPort.readBytes(temp.array(), size.get(0)-1);
                buffer.put(temp);
                temp.clear();
                buffer.order(ByteOrder.LITTLE_ENDIAN);

                controllerData = deserialize(buffer);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        serialPort.closePort();
    }

    private int[] deserialize(ByteBuffer buffer){
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int controllerData[] = new int[14];

        switch (buffer.get(1)){
            case 0x40:
                for (int i = 0; i < 28; i+=2) {
                    controllerData[i/2] = Short.toUnsignedInt(buffer.getShort(i+2));
                }
                buffer.clear();
                return controllerData;
        }

        return controllerData;
    }
}
