package com.github.lazygamer1111.sim;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

/**
 * Simulation entry point. This sets up a scheduler with simulated jobs and a simple
 * interactive controller simulator that adjusts the controllerData array based on
 * keyboard inputs. This does not touch any hardware or the base runtime.
 *
 * Run this class via a dedicated Run Configuration to test movement logic.
 */
public class SimMain {
    private static final Logger log = LoggerFactory.getLogger(SimMain.class);

    private static final int[] controllerData = new int[14];

    public static void main(String[] args) throws Exception {
        initControllerDefaults();

        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();

        JobDataMap ioMap = new JobDataMap();
        ioMap.put("ControllerData", controllerData);
        JobDetail ioJob = JobBuilder.newJob(SimIOJob.class)
                .withIdentity("SimIOJob")
                .usingJobData(ioMap)
                .build();
        Trigger ioTrig = TriggerBuilder.newTrigger()
                .withIdentity("SimIOJobTrigger")
                .startNow()
                .withSchedule(simpleSchedule().withIntervalInMilliseconds(21).repeatForever())
                .build();

        JobDataMap pioMap = new JobDataMap();
        pioMap.put("ControllerData", controllerData);
        JobDetail pioJob = JobBuilder.newJob(SimPIOJob.class)
                .withIdentity("SimPIOJob")
                .usingJobData(pioMap)
                .build();
        Trigger pioTrig = TriggerBuilder.newTrigger()
                .withIdentity("SimPIOJobTrigger")
                .startNow()
                .withSchedule(simpleSchedule().withIntervalInMilliseconds(10).repeatForever())
                .build();

        scheduler.scheduleJob(ioJob, ioTrig);
        scheduler.scheduleJob(pioJob, pioTrig);

        ControllerSimulator simulator = new ControllerSimulator(controllerData);
        Thread simThread = new Thread(simulator, "ControllerSimulator");
        simThread.setDaemon(true);
        simThread.start();

        printHelp();

        // Keep main alive until user quits
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String line = br.readLine();
            if (line == null) continue;
            line = line.trim();
            if ("q".equalsIgnoreCase(line) || "quit".equalsIgnoreCase(line) || "exit".equalsIgnoreCase(line)) {
                log.info("Exiting simulation...");
                scheduler.shutdown();
                break;
            }
            // Forward all other commands to simulator (it also reads stdin itself, but this allows single-line control here)
            simulator.acceptCommand(line);
        }
    }

    private static void initControllerDefaults() {
        for (int i = 0; i < controllerData.length; i++) controllerData[i] = 1000;
        controllerData[0] = 1500; // steering center (use 1500-2000 range mapped in SimIOJob)
        controllerData[2] = 1000; // throttle min
        controllerData[5] = 1000; // beacon off
        controllerData[7] = 1500; // range selector neutral
        controllerData[8] = 2000; // kill switch not engaged
    }

    private static void printHelp() {
        log.info("Simulation started. Controls (type and press Enter):\n" +
                "  w: throttle up (+25)\n" +
                "  s: throttle down (-25)\n" +
                "  a: steer left (-25)\n" +
                "  d: steer right (+25)\n" +
                "  c: center steering (1500)\n" +
                "  r: cycle range (ch7: 1000 -> 1500 -> 2000 -> 1000)\n" +
                "  k: toggle kill switch (ch8: 2000/1000)\n" +
                "  b: beacon pulse (ch5: 2000 for one tick)\n" +
                "  set <ch> <val>: set raw channel (0-13) to value (1000-2000)\n" +
                "  q: quit\n");
    }
}
