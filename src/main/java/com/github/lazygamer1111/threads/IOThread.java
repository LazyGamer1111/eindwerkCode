package com.github.lazygamer1111.threads;

import com.github.lazygamer1111.components.output.Servo;
import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfigBuilder;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.pwm.Pwm;
import com.pi4j.io.pwm.PwmConfigBuilder;
import com.pi4j.io.pwm.PwmType;
import com.pi4j.plugin.gpiod.provider.gpio.digital.GpioDDigitalInputProvider;
import com.pi4j.plugin.gpiod.provider.gpio.digital.GpioDDigitalOutputProvider;
import com.pi4j.plugin.linuxfs.provider.pwm.LinuxFsPwmProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Arrays;

/**
 * Input/Output Thread for controlling servo motors and other GPIO devices.
 * 
 * This thread is responsible for handling GPIO and PWM operations, particularly
 * for controlling servo motors based on controller input data. It initializes the
 * Pi4J context, configures GPIO pins and PWM outputs, and continuously updates
 * the servo position based on controller data.
 * 
 * The thread runs in an infinite loop, reading controller data and adjusting
 * the servo angle accordingly at regular intervals.
 * 
 * @author lazygamer1111
 * @version 1.0
 * @since 2025-11-06
 */
public class IOThread extends Thread {


    /**
     * The Pi4J context used for GPIO and PWM operations.
     * This is shared across all instances of IOThread.
     */
    public static Context pi4j = null;
    
    /**
     * The servo motor instance controlled by this thread.
     * Initialized in the __init__() method.
     */
    private Servo servo = null;
    
    /**
     * Logger for this class.
     */
    private Logger logger = LoggerFactory.getLogger(IOThread.class);
    
    /**
     * Shared controller data array accessed by multiple threads.
     * This array contains input values from the controller that are used
     * to determine the servo position.
     */
    private volatile int[] controllerData;
    /**
     * Constructs a new IO Thread with the specified controller data.
     * 
     * This thread will use the provided controller data array to control
     * servo motors and other GPIO devices.
     *
     * @param controllerData shared array containing controller input values
     */
    public IOThread(int[] controllerData) {
        this.controllerData = controllerData;
    }

    /**
     * Main execution method for the thread.
     * 
     * This method initializes the thread and Pi4J context, then enters an infinite loop
     * that continuously reads controller data and updates the servo position accordingly.
     * The thread sleeps for 21ms between iterations to maintain a consistent update rate
     * and prevent excessive CPU usage.
     */
    public void run() {
        // Initialize the thread and Pi4J context
        __init__();
        
        // Track timing for performance logging
        long last = Instant.now().toEpochMilli();
        double lastData = 0d;
        
        // Main control loop
        while (true){
            // Get servo position from controller data (index 2)
            // Subtract 1000 to center the value around 0
            double servoThing = controllerData[2] - 1000;
            
            // Log controller data for debugging
            logger.debug(Arrays.toString(controllerData));
            
            // Calculate and log time between iterations
            long now = Instant.now().toEpochMilli();
            logger.debug("Time between send = {}", now - last);
            last = now;
            lastData = servoThing;
            
            // Scale the servo position to the appropriate angle range (0-90 degrees)
            servoThing *= 90;
            servoThing /= 1000;
            
            // Only set the servo angle if the value is positive
            if (servoThing > 0) {
                servo.setAngle(servoThing);
            }
            
            // Log time taken to update servo
            now = Instant.now().toEpochMilli();
            logger.debug("Time to after servo = {}", now - last);
            
            // Sleep to maintain consistent update rate
            try {
                Thread.sleep(21);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Initializes the thread and Pi4J context.
     * 
     * This method sets the thread name, initializes the Pi4J context with appropriate
     * providers, configures a LED on GPIO pin 17, and sets up a servo motor on PWM
     * channel 2 with a frequency of 50Hz.
     */
    private void __init__(){
        // Set thread name for easier identification in logs
        currentThread().setName("IO Thread");
        logger.info("Starting thread...");

        // Initialize Pi4J context with required providers
        pi4j = Pi4J.newContextBuilder()
                .add(GpioDDigitalInputProvider.newInstance())
                .add(GpioDDigitalOutputProvider.newInstance())
                .add(LinuxFsPwmProvider.newInstance(0))
                .build();

        // Configure LED on GPIO pin 17
        DigitalOutputConfigBuilder ledConfig = DigitalOutput.newConfigBuilder(pi4j)
                .id("led")
                .name("led")
                .address(17)
                .shutdown(DigitalState.LOW)  // Turn off LED when program exits
                .initial(DigitalState.LOW)   // Start with LED off
                .provider("gpiod-digital-output");
        DigitalOutput led = pi4j.create(ledConfig);

        // Configure servo on PWM channel 2
        PwmConfigBuilder servoConfig = Pwm.newConfigBuilder(pi4j)
                .id("servo")
                .name("servo")
                .address(2)
                .pwmType(PwmType.HARDWARE)
                .provider("linuxfs-pwm")
                .frequency(50)               // 50Hz is standard for most servos
                .initial(5);                 // Initial duty cycle
        
        // Create servo with range 0-90 degrees and pulse width 1-2ms
        servo = new Servo(pi4j.create(servoConfig), 0d, 90d, 1d/1000d, 2d/1000d, 50);


    }
}
