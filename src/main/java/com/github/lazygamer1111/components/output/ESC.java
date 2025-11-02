package com.github.lazygamer1111.components.output;

import java.util.HashMap;
import java.util.Map;

public class ESC {
    private static final Map<Integer, Integer> speed = new HashMap<>();
    static {
        speed.put(600, 625);
        speed.put(1200, 313);
        speed.put(300, 1250);
        speed.put(150, 2500);
    }

    public ESC(int pin, int speedkbs) {
        init_SM(pin, speed.get(speedkbs));
    }

    public void sendFrame(int throttle, boolean telemetry) {
        short frame = (short) throttle;
        if (telemetry) {
            frame |= 2048;
        }

        frame = addChecksum(frame);

        put(frame);
    }

    private short addChecksum(short frame){
        short crc = (short) ((~(frame ^ (frame >> 4) ^ (frame >> 8))) & 0x0F);
        crc = (short) (frame | crc);
        return crc;
    }

    private native void init_SM(int pin, int sm);

    public native void put(short data);
    private native short pop();
}
