package com.github.lazygamer1111.sim;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulated version of the IOJob. Computes a servo angle based on controllerData[0]
 * and logs it instead of driving real hardware.
 */
public class SimIOJob implements Job {
    private static final Logger log = LoggerFactory.getLogger(SimIOJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        int[] controllerData = (int[]) context.getJobDetail().getJobDataMap().get("ControllerData");
        if (controllerData == null) return;

        double servoThing = controllerData[0] - 1000; // 0..1000
        double angle = (servoThing * 90) / 1000.0; // 0..90
        if (angle > 0) {
            double simAngle = 90 - angle; // follow base job mapping
            log.info("[SIM SERVO] angle={}", String.format("%.1f", simAngle));
        }

        try {
            Thread.sleep(21);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
