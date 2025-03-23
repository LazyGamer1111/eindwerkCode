package com.github.LazyGamer1111.Threads;

import com.fazecast.jSerialComm.SerialPort;
import com.github.LazyGamer1111.dataTypes.AsyncData;
import com.github.LazyGamer1111.dataTypes.BluetoothData;
import com.lmax.disruptor.dsl.Disruptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Objects;

public class SerialThread extends Thread {
    private final AsyncData<BluetoothData> bluetoothData;
    private final BluetoothData localData = new BluetoothData();

    public SerialThread(AsyncData<BluetoothData> data) {
        bluetoothData = data;
    }

    public void run() {
        currentThread().setName("Serial Thread");
        Logger log = LoggerFactory.getLogger(this.getClass());

        for (SerialPort commPort : SerialPort.getCommPorts()) {
            log.debug("Comm Ports: {}", commPort.getSystemPortName());
        }

        final String portName = "ttyAMA0";
        SerialPort serialPort = SerialPort.getCommPort(portName);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
        serialPort.setComPortParameters(9600, 8, 1, 0);
        serialPort.openPort();
        log.debug("Open serial port");
        try {
            while (true) {
                ByteBuffer buffer = ByteBuffer.allocate(1);
                serialPort.readBytes(buffer.array(), buffer.array().length);
                if (!String.valueOf(buffer.get(0)).equals(localData.getData()) && !String.valueOf(buffer.get(0)).equals("83")) {
                    bluetoothData.setData(String.valueOf(buffer.get(0)));
                }
                localData.setData(String.valueOf(buffer.get(0)));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        serialPort.closePort();
    }
}
