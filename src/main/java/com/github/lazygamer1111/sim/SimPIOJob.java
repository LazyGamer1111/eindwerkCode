package com.github.lazygamer1111.sim;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulated version of the PIOJob. It mirrors the throttle/range/kill-switch
 * logic but instead of sending frames to an ESC, it logs the computed values.
 */
public class SimPIOJob implements Job {
    private static final Logger log = LoggerFactory.getLogger(SimPIOJob.class);

    // Smoothing state (time-based, mirrors PIOJob)
    private int lastFrame = 0;
    private long lastUpdateNanos = 0L;
    private static final double SMOOTH_DURATION_SECONDS = 1.0; // from start to end ≈ 1s

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        int[] controllerData = (int[]) context.getJobDetail().getJobDataMap().get("ControllerData");
        if (controllerData == null) return;

        int throttle = (controllerData[2] - 1000) + 48;

        try {
            if (controllerData[5] == 2000) {
                log.info("[SIM ESC] Beacon command requested (DSHOT_CMD_BEACON1)");
                return;
            }

            int desiredFrame = -1;
            boolean telemetry = false;

            // Kill switch or safety stop
            if (controllerData[8] == 1000) {
                desiredFrame = 0;
                telemetry = true; // keep telemetry when hard stop requested
            } else if (controllerData[7] == 2000) {
                // Forward/high range
                desiredFrame = clamp(throttle + 1000, 1048, 2047);
            } else if (controllerData[7] == 1000) {
                // Low/normal range
                desiredFrame = clamp(throttle, 48, 1047);
            } else if (controllerData[7] == 1500) {
                // Neutral
                desiredFrame = 0;
            }

            if (desiredFrame >= 0) {
                // Time-based smoothing with asymmetric slew rates. Instant snap to 0 on kill/neutral.
                int frameToSend;
                long now = System.nanoTime();
                if (lastUpdateNanos == 0L) {
                    lastUpdateNanos = now;
                }
                double dt = (now - lastUpdateNanos) / 1_000_000_000.0; // seconds
                lastUpdateNanos = now;

                if (desiredFrame == 0) {
                    frameToSend = 0;
                } else {
                    int delta = desiredFrame - lastFrame;
                    if (delta != 0) {
                        // 1-second smoothing to reach desired regardless of distance
                        double stepExact = Math.abs(delta) * (dt / SMOOTH_DURATION_SECONDS);
                        int step = (int) Math.max(1, Math.round(stepExact));
                        if (delta > 0) {
                            frameToSend = Math.min(lastFrame + step, desiredFrame);
                        } else {
                            frameToSend = Math.max(lastFrame - step, desiredFrame);
                        }
                    } else {
                        frameToSend = desiredFrame;
                    }
                }

                log.info("[SIM ESC] frame={} telemetry={} (desired={} last={})", frameToSend, telemetry, desiredFrame, lastFrame);
                lastFrame = frameToSend;
            }
        } catch (Exception e) {
            log.error("[SIM ESC] Failed to compute frame", e);
        }

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
