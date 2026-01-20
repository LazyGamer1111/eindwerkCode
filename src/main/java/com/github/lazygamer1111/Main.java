package com.github.lazygamer1111;


import com.github.lazygamer1111.components.output.ESC;
import com.github.lazygamer1111.components.output.Servo;
import com.github.lazygamer1111.threads.IOJob;
import com.github.lazygamer1111.threads.PIOJob;
import com.github.lazygamer1111.threads.SerialKiss;
import com.github.lazygamer1111.threads.SerialThread;
import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfigBuilder;
import com.pi4j.io.pwm.Pwm;
import com.pi4j.io.pwm.PwmConfigBuilder;
import com.pi4j.io.pwm.PwmType;
import com.pi4j.plugin.gpiod.provider.gpio.digital.GpioDDigitalInputProvider;
import com.pi4j.plugin.gpiod.provider.gpio.digital.GpioDDigitalOutputProvider;
import com.pi4j.plugin.linuxfs.provider.pwm.LinuxFsPwmProvider;
import io.avaje.config.Config;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.quartz.SimpleScheduleBuilder.*;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * The type Main.
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static final ArrayList<Thread> threads = new ArrayList<>();
    static volatile int[] controllerData = new int[14];
    static boolean DEBUG = false;
    public static ESC esc;
    public static Servo servo;
    public static Scheduler scheduler;
    public static Context pi4j;

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                log.info("Shutting down...");
                scheduler.shutdown();
                pi4j.shutdown();
            } catch (SchedulerException e) {
                throw new RuntimeException(e);
            }
            for (Thread thread : threads) {
                thread.interrupt();
            }
        }));


        log.debug("Starting I/O...");
        createIO();

        log.debug("Starting scheduler...");

        createScheduler();

        DEBUG = Config.getBool("debug");

        try {
            createThreads();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        if (DEBUG) {
            int port = 8080;
            if (args.length > 0) {
                port = Integer.parseInt(args[0]);
            }

            new DebugServer(port).run();
        }

        while (true);
    }

    /**
     * Create threads.
     */
    private static void createThreads() {
        Thread serial = new SerialThread(controllerData);
        Thread serialKiss = new SerialKiss();
        threads.add(serial);
        threads.add(serialKiss);

        for (Thread thread : threads) {
            thread.start();
        }
    }

    private static void createIO() {
        pi4j = Pi4J.newContextBuilder()
                .add(LinuxFsPwmProvider.newInstance(0))
                .build();

        PwmConfigBuilder servoConfig = Pwm.newConfigBuilder(pi4j)
                .id("servo")
                .name("servo")
                .address(2)
                .pwmType(PwmType.HARDWARE)
                .provider("linuxfs-pwm")
                .frequency(50)
                .initial(5);
        servo = new Servo(pi4j.create(servoConfig), 0d, 90d, 1d/1000d, 2d/1000d, 50);
        try {
            esc = new ESC(4, 300, new File("/home/pi/NamedPipes/PIOTelemetry"), new File("/home/pi/NamedPipes/PIOPipe"));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    private static void createScheduler() throws SchedulerException {
        log.debug("ESC = {}", esc);

        scheduler = StdSchedulerFactory.getDefaultScheduler();

        scheduler.start();

        JobDataMap JobMapIO = new JobDataMap();
        JobMapIO.put("servo", servo);
        JobMapIO.put("ControllerData", controllerData);

        JobDetail IOJob = JobBuilder.newJob(IOJob.class)
                .withIdentity("IOJob")
                .usingJobData(JobMapIO)
                .build();
        Trigger IOJobTrigger = TriggerBuilder.newTrigger().withIdentity("IOJobTrigger")
                .startNow()
                .withSchedule(simpleSchedule()
                        .withIntervalInMilliseconds(21)
                        .repeatForever())
                .build();

        JobDataMap jobMapPIO = new JobDataMap();
        jobMapPIO.put("ESC", esc);
        jobMapPIO.put("ControllerData", controllerData);

        JobDetail PIOJob = JobBuilder.newJob(PIOJob.class)
                .withIdentity("PIOJob")
                .usingJobData(jobMapPIO)
                .build();
        Trigger PIOJobTrigger = TriggerBuilder.newTrigger().withIdentity("PIOJobTrigger")
                .startNow()
                .withSchedule(simpleSchedule()
                        .withIntervalInMilliseconds(10)
                        .repeatForever())
                .build();

        scheduler.scheduleJob(IOJob, IOJobTrigger);
        scheduler.scheduleJob(PIOJob, PIOJobTrigger);

        log.debug("Scheduler started");
    }
}
