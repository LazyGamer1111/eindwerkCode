package com.github.lazygamer1111.threads;

import com.github.lazygamer1111.components.output.ESC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * PIO (Programmable I/O) Thread for controlling ESC hardware.
 * 
 * This thread monitors controller data and controls an Electronic Speed Controller (ESC)
 * based on the values received. It runs continuously, checking the controller data
 * at regular intervals and updating the ESC state accordingly.
 * 
 * @author lazygamer1111
 * @version 1.0
 * @since 2025-11-06
 */
public class PIOThread extends Thread {
    /** Shared controller data array accessed by multiple threads */
    private volatile int[] controllerData;
    
    /** Logger for this class */
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    /** The Electronic Speed Controller instance to be controlled */
    private ESC esc;

    /**
     * Constructs a new PIO Thread with the specified controller data and ESC.
     *
     * @param controllerData shared array containing controller input values
     * @param esc the Electronic Speed Controller to be controlled by this thread
     */
    public PIOThread(int[] controllerData, ESC esc){
        this.controllerData = controllerData;
        this.esc = esc;
    }

    /**
     * Main execution method for the thread.
     * 
     * This method runs in an infinite loop, continuously monitoring the controller data
     * and updating the ESC state based on the value at index 4 of the controller data array.
     * If the value is greater than 1500, it sets the ESC to state 1, otherwise to state 0.
     * The thread sleeps for 10ms between iterations to prevent excessive CPU usage.
     */
    @Override
    public void run() {
        // Initialize the thread
        this.__init__();

        // Main control loop
        while(true){
            // Log the current controller data value being monitored
            log.debug("Data = {}", controllerData[4]);
            
            // Check if the controller value exceeds the threshold
            if (controllerData[4] > 1500) {
                // Activate the ESC (state 1)
                esc.put(esc.sm, (short) 1);
            } else {
                // Deactivate the ESC (state 0)
                esc.put(esc.sm, (short) 0);
            }

            try {
                // Sleep to prevent excessive CPU usage
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Initializes the thread by setting its name.
     * This method is called at the beginning of the run method.
     */
    private void __init__() {
        this.setName("PIO Thread");
    }
}
