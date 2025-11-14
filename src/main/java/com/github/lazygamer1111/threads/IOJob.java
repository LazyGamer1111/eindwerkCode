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
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * The type Io thread.
 */
public class IOJob implements Job {
    private Servo servo = null;
    private final Logger logger = LoggerFactory.getLogger(IOJob.class);
    private volatile int[] controllerData;

//    public IOJob(int[] controllerData) {
//        this.controllerData = controllerData;
//        __init__();
//    }

//    public void run() {
//        long last = Instant.now().toEpochMilli();
//
//        double lastData = 0d;
//         while (true){
//             double servoThing = controllerData[2] - 1000;
////             logger.debug(Arrays.toString(controllerData));
//             long now = Instant.now().toEpochMilli();
////             logger.debug("Time between send = {}", now - last);
//             last = now;
//             lastData = servoThing;
//             servoThing *= 90;
//             servoThing /= 1000;
//             if (servoThing > 0) {
//                 servo.setAngle(servoThing);
//             }
//             now = Instant.now().toEpochMilli();
//// logger.debug("Time to after servo = {}", now - last);
//             try {
//                 Thread.sleep(21);
//             } catch (InterruptedException e) {
//                 throw new RuntimeException(e);
//             }
//         }
//    }

//    private void __init__(){
//        logger.info("Starting thread...");
//
//        pi4j = Pi4J.newContextBuilder()
//                .add(GpioDDigitalInputProvider.newInstance())
//                .add(GpioDDigitalOutputProvider.newInstance())
//                .add(LinuxFsPwmProvider.newInstance(0))
//                .build();
//
//        DigitalOutputConfigBuilder ledConfig = DigitalOutput.newConfigBuilder(pi4j)
//                .id("led")
//                .name("led")
//                .address(17)
//                .shutdown(DigitalState.LOW)
//                .initial(DigitalState.LOW)
//                .provider("gpiod-digital-output");
//        DigitalOutput led = pi4j.create(ledConfig);
//
//        PwmConfigBuilder servoConfig = Pwm.newConfigBuilder(pi4j)
//                .id("servo")
//                .name("servo")
//                .address(2)
//                .pwmType(PwmType.HARDWARE)
//                .provider("linuxfs-pwm")
//                .frequency(50)
//                .initial(5);
//        servo = new Servo(pi4j.create(servoConfig), 0d, 90d, 1d/1000d, 2d/1000d, 50);
//    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        double servoThing = controllerData[2] - 1000;
//             logger.debug(Arrays.toString(controllerData));
        long now = Instant.now().toEpochMilli();
//             logger.debug("Time between send = {}", now - last);
        servoThing *= 90;
        servoThing /= 1000;
        if (servoThing > 0) {
            servo.setAngle(servoThing);
        }
        now = Instant.now().toEpochMilli();
//             logger.debug("Time to after servo = {}", now - last);
        try {
            Thread.sleep(21);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void setControllerData(int[] controllerData) {
        this.controllerData = controllerData;
    }

    public void setServo(Servo servo) {
        this.servo = servo;
    }
}
