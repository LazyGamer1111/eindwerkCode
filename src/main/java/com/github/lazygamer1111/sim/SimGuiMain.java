package com.github.lazygamer1111.sim;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import javax.swing.SwingUtilities;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

/**
 * Visual GUI simulator entry point. This sets up the same simulated jobs (no hardware)
 * and opens a Swing UI to control and visualize the controller state, servo angle,
 * and ESC frame values. Does not modify production code.
 */
public class SimGuiMain {
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

        SwingUtilities.invokeLater(() -> {
            SimGuiFrame frame = new SimGuiFrame(controllerData, simulator);
            frame.setVisible(true);
        });
    }

    private static void initControllerDefaults() {
        for (int i = 0; i < controllerData.length; i++) controllerData[i] = 1000;
        controllerData[0] = 1500; // steering center
        controllerData[2] = 1000; // throttle min
        controllerData[5] = 1000; // beacon off
        controllerData[7] = 1500; // range selector neutral
        controllerData[8] = 2000; // kill switch not engaged
    }
}
