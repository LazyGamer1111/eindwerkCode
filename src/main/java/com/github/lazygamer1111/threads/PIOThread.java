package com.github.lazygamer1111.threads;

import com.github.lazygamer1111.components.output.ESC;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class PIOThread extends Thread {
    private volatile int[] controllerData;
    private ESC esc;

    public PIOThread(int[] controllerData, ESC esc){
        this.controllerData = controllerData;
        this.esc = esc;
    }

    @Override
    public void run() {

        while(true){
            LoggerFactory.getLogger(this.getClass()).debug("Data = {}", controllerData[4]);
            if (controllerData[4] > 1500) {
                esc.put(esc.sm, (short) 1);
            } else {
                esc.put(esc.sm, (short) 0);
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
