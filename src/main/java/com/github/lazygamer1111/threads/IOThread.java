package com.github.lazygamer1111.threads;

import com.github.lazygamer1111.components.output.ESC;
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

/**
 * The type Io thread.
 */
public class IOThread extends Thread {


    /**
     * The constant pi4j.
     */
    public static Context pi4j = null;
    private Servo servo = null;
    private ESC esc = null;
    private Logger logger = LoggerFactory.getLogger(IOThread.class);
    private volatile int[] controllerData;
    /**
     * Instantiates a new Io thread.
     *
     * @param controllerData the controller's data
     */
    public IOThread(int[] controllerData) {
        this.controllerData = controllerData;
    }

    public void run() {
        __init__();
        long last = Instant.now().toEpochMilli();

        double lastData = 0d;
         while (true){
             double servoThing = controllerData[4] - 1000;
             logger.debug(servoThing + "");
             long now = Instant.now().toEpochMilli();
             if (now-last > 22) {
                 logger.debug("Time between send = {}", now - last);
             }
             last = now;
             lastData = servoThing;
             servoThing *= 90;
             servoThing /= 1000;
             if (servoThing > 0) {
                 servo.setAngle(servoThing);
             }
             now = Instant.now().toEpochMilli();
//             logger.debug("Time to after servo = {}", now - last);
//             logger.debug("Memory Free: {}", Runtime.getRuntime().freeMemory()/1000000);
//             logger.debug("Memory Total: {}", Runtime.getRuntime().totalMemory()/1000000);
//             logger.debug("Memory Usage: {}", (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1000000);


             try {
                 Thread.sleep(21);
             } catch (InterruptedException e) {
                 throw new RuntimeException(e);
             }
         }
    }

    private void __init__(){
        currentThread().setName("IO Thread");
        logger.info("Starting thread...");

        pi4j = Pi4J.newContextBuilder()
                .add(GpioDDigitalInputProvider.newInstance())
                .add(GpioDDigitalOutputProvider.newInstance())
                .add(LinuxFsPwmProvider.newInstance(0))
                .build();

        DigitalOutputConfigBuilder ledConfig = DigitalOutput.newConfigBuilder(pi4j)
                .id("led")
                .name("led")
                .address(17)
                .shutdown(DigitalState.LOW)
                .initial(DigitalState.LOW)
                .provider("gpiod-digital-output");
        DigitalOutput led = pi4j.create(ledConfig);

        PwmConfigBuilder servoConfig = Pwm.newConfigBuilder(pi4j)
                .id("servo")
                .name("servo")
                .address(2)
                .pwmType(PwmType.HARDWARE)
                .provider("linuxfs-pwm")
                .frequency(50)
                .initial(5);

        PwmConfigBuilder ESCConfig = Pwm.newConfigBuilder(pi4j)
                .id("ESC")
                .name("ESC")
                .address(3)
                .pwmType(PwmType.HARDWARE)
                .provider("linuxfs-pwm")
                .frequency(50)
                .initial(5);

        servo = new Servo(pi4j.create(servoConfig), 0d, 90d, 1d/1000d, 2d/1000d, 50);
        esc = new ESC(pi4j.create(ESCConfig), 1d/1000d, 2d/1000d, 50);

    }
}
