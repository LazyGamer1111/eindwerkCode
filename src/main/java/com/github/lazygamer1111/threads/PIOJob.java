package com.github.lazygamer1111.threads;

import com.github.lazygamer1111.components.output.ESC;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PIOJob implements Job {
    private volatile int[] controllerData;
    private ESC esc;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(JobExecutionContext c) throws JobExecutionException {
        int throttle = (controllerData[2] - 1000);

        try {
            if (controllerData[9] == 1000) {
                esc.sendFrame(0, true);
            } else if (controllerData[8] == 2000) {
                esc.sendFrame(throttle, false);
            } else if (controllerData[8] == 1000) {
                esc.sendFrame(throttle + 1024, false);
            } else if (controllerData[8] == 1500) {
                esc.sendFrame(0, false);
            }
        } catch (Exception e) {
            log.error("Failed to send frame!", e);
        }

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void setControllerData(int[] controllerData) {
        this.controllerData = controllerData;
    }

    public void setESC(ESC esc) {
        this.esc = esc;
    }
}
