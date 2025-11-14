package com.github.lazygamer1111.components.output;

import com.github.lazygamer1111.dataTypes.PIOMSG;
import kotlin.text.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

public class ESC {
    private static final Logger log = LoggerFactory.getLogger(ESC.class);
    private final FileInputStream in;
    private final FileOutputStream out;
    public int id;

    public ESC(int pin, int speedkbs, File inPipe, File outPipe) throws IOException {
        in = new FileInputStream(inPipe);
        out = new FileOutputStream(outPipe);
        id = init_SM(pin, speedkbs);
    }

    public void sendFrame(int throttle, boolean telemetry) {
        try {
            write(PIOMSG.fromThrottle(id, throttle, telemetry));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        short frame = (short) throttle;
//        if (telemetry) {
//            frame |= 2048;
//        }
//
//        frame = addChecksum(frame);
//
////        log.debug("Sending frame: {}", frame);
//
//        put(id, frame);
    }

    private short addChecksum(short frame){
        short crc = (short) ((~(frame ^ (frame >> 4) ^ (frame >> 8))) & 0x0F);
//        log.debug("CRC: {}", crc);
        crc = (short) (frame | (crc << 12));
        return crc;
    }

    public int init_SM(int pin, int speed) throws IOException {
        assert out != null;
        String command = String.format("ADD %d %d", pin, speed);
        out.write(command.getBytes(Charsets.UTF_8));
        log.debug("Sent command: {}", command);
        while (true) {
            if (in.available() > 0) {
                byte[] buffer = new byte[in.available()];
                in.read(buffer);
                String response = new String(buffer, Charsets.UTF_8);
                if (response.startsWith("OK")) {
                    response = response.split(" ")[1].replace("\n", "");
                    return Integer.parseInt(response);
                } else {
                    log.error("Failed to add ESC: {}", response);
                }
            }
        }
    }

    public PIOMSG write(PIOMSG msg) throws IOException {
       put(msg);
       return pop(true);
    }

    private void put(PIOMSG msg) throws IOException {
        assert out != null;
        out.write(msg.toString().getBytes(Charsets.UTF_8));
    }

    private PIOMSG pop(boolean blocking) throws IOException {
        if (blocking){
            while (true) {
                if (in.available() > 0) {
                    byte[] buffer = new byte[in.available()];
                    in.read(buffer);
                    String response = new String(buffer, Charsets.UTF_8);
                    String[] responseArr = response.split(" ");
                    return new PIOMSG(responseArr[0], Integer.parseInt(responseArr[1]), responseArr[2]);
                }
            }
        } else {
            if (in.available() > 0) {
                byte[] buffer = new byte[in.available()];
                in.read(buffer);
                String response = new String(buffer, Charsets.UTF_8);
                String[] responseArr = response.split(" ");
                return new PIOMSG(responseArr[0], Integer.parseInt(responseArr[1]), responseArr[2]);
            }

            return null;
        }
    }
}
