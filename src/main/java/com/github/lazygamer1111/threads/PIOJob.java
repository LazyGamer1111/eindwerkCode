package com.github.lazygamer1111.threads;

import com.github.lazygamer1111.components.output.ESC;
import com.github.lazygamer1111.dataTypes.ESCCommands;
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
        int throttle = (controllerData[2] - 1000) + 48;

        try {
            if (controllerData[5] == 2000) {
                log.info("Sending DSHOT_CMD_BEACON1");
                esc.sendFrame(ESCCommands.DSHOT_CMD_BEACON1.ordinal(), true);
                return;
            }


            if (controllerData[8] == 1000) {
                esc.sendFrame(0, true);
            } else if (controllerData[7] == 2000) {
                esc.sendFrame(Math.clamp(throttle+1000, 1048, 2047), false);
            } else if (controllerData[7] == 1000) {
                esc.sendFrame(Math.clamp(throttle, 48, 1047), false);
            } else if (controllerData[7] == 1500) {
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
