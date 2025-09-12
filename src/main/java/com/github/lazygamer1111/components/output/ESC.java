package com.github.lazygamer1111.components.output;

import com.pi4j.io.pwm.Pwm;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import static java.lang.Math.abs;


public class ESC {
    /**
     * The Min pulse width.
     */
    final double minPulseWidth;
    /**
     * The Max pulse width.
     */
    final double maxPulseWidth;
    /**
     * The Duty cycle.
     */
    double dutyCycle = 0;

    /**
     * The Pwm.
     */
    Pwm pwm;

    /**
     * Instantiates a new Servo.
     *
     * @param pwm           the pwm
     * @param minPulseWidth the min pulse width
     * @param maxPulseWidth the max pulse width
     * @param frequency     the frequency
     */
    public ESC(Pwm pwm, @Nullable Double minPulseWidth, @Nullable Double maxPulseWidth, @Nullable Integer frequency) {
        this.pwm = pwm;
        this.minPulseWidth = minPulseWidth == null ? (double) 1 / 1000 : minPulseWidth;
        this.maxPulseWidth = maxPulseWidth == null ? (double) 2 / 1000 : maxPulseWidth;
        this.pwm.frequency(frequency == null ? 50 : frequency);
    }

    public void set(double channel){
        calcDutyCycle(channel);
        pwm.on(this.dutyCycle);
        LoggerFactory.getLogger(this.getClass()).info("ESC set channel: {}", channel);
    }

    /**
     * Calc duty cycle.
     *
     * @param channel the channel with value 1000-2000
     */
    public void calcDutyCycle(double channel) {
        dutyCycle = (float) ((channel / ((double) 1 /pwm.frequency()))*100);
    }
}
