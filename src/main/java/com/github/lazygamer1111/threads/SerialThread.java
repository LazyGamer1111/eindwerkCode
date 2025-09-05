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

    public void run() {
        currentThread().setName("Serial Thread");
        Logger log = LoggerFactory.getLogger(this.getClass());

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
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            long last = Instant.now().toEpochMilli();
            while (true) {
                buffer.clear();
                serialPort.readBytes(buffer.array(), 32);
                buffer.clear();
                serialPort.readBytes(buffer.array(), 32);
                buffer.clear();
                serialPort.readBytes(buffer.array(), 1);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                if (buffer.get(0) == 0x20) {
                    buffer.clear();
                    serialPort.readBytes(buffer.array(), 1);
                    if (buffer.get(0) == 0x40){
                        buffer.clear();
                        serialPort.readBytes(buffer.array(), 28);
                        for (int i = 0; i < 28; i+=2) {
                            controllerData[i/2] = Short.toUnsignedInt(buffer.getShort(i));
                        }
                        buffer.clear();
                        serialPort.readBytes(buffer.array(), 2);
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        serialPort.closePort();
    }
}
