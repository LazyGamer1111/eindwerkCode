package com.github.lazygamer1111.threads;

import com.fazecast.jSerialComm.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SerialKiss extends Thread {

    Logger log = LoggerFactory.getLogger(this.getClass());


    public void run() {
        final String portName2 = "ttyAMA2";
        SerialPort serialPort2 = SerialPort.getCommPort(portName2);
        serialPort2.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
        serialPort2.setComPortParameters(115200, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);

        try {
            serialPort2.openPort();
            log.debug("Open serial port");

            ByteBuffer buffer = ByteBuffer.allocate(10);

            while (true) {
                int bytes = serialPort2.bytesAvailable();
                log.info(String.valueOf(serialPort2.bytesAvailable()));
                if (serialPort2.bytesAvailable() >= 10) {
                    serialPort2.readBytes(buffer.array(), 10);
                    deserializeKiss(buffer);
                    buffer.clear();
                }
            }
        } finally {
            serialPort2.closePort();
            log.info("Close serial port");
        }
    }


    private void deserializeKiss(ByteBuffer buffer){
        buffer.order(ByteOrder.BIG_ENDIAN);

        for (int i = 0; i < 10; i++) {
            log.info(String.valueOf(buffer.get()));
        }
    }
}
