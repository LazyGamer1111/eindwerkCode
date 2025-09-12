package com.github.lazygamer1111.components.output;

import com.pi4j.io.pwm.Pwm;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import static java.lang.Math.abs;

/**
 * The type Servo.
 */
public class Servo {
    /**
     * The Min angle.
     */
    final double minAngle;
    /**
     * The Max angle.
     */
    final double maxAngle;
    /**
     * The Min pulse width.
     */
    final double minPulseWidth;
    /**
     * The Max pulse width.
     */
    final double maxPulseWidth;

    /**
     * The Angle.
     */
    double angle = 0;
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
     * @param minAngle      the min angle
     * @param maxAngle      the max angle
     * @param minPulseWidth the min pulse width
     * @param maxPulseWidth the max pulse width
     * @param frequency     the frequency
     */
    public Servo(Pwm pwm, @Nullable Double minAngle, @Nullable Double maxAngle, @Nullable Double minPulseWidth, @Nullable Double maxPulseWidth, @Nullable Integer frequency) {
        this.pwm = pwm;
        this.minAngle = minAngle == null ? 0 : minAngle;
        this.maxAngle = maxAngle == null ? 180 : maxAngle;
        this.minPulseWidth = minPulseWidth == null ? (double) 1 / 1000 : minPulseWidth;
        this.maxPulseWidth = maxPulseWidth == null ? (double) 2 / 1000 : maxPulseWidth;
        this.pwm.frequency(frequency == null ? 50 : frequency);
    }

    /**
     * Sets angle.
     *
     * @param angle the angle
     */
    public void setAngle(double angle) {
        this.angle = angle;
//        LoggerFactory.getLogger(this.getClass()).info("Servo set angle: {}", angle);
        if (angle < minAngle || angle > maxAngle) {
            throw new IllegalArgumentException("Angle must be between " + minAngle + " and " + maxAngle);
        }
        double percent = angle / (abs(maxAngle) + abs(minAngle));

        calcDutyCycle(percent);

        pwm.on(this.dutyCycle);
    }

    /**
     * Calc duty cycle.
     *
     * @param percentage the percentage
     */
    public void calcDutyCycle(double percentage) {
        double PW = (maxPulseWidth - minPulseWidth) * percentage + minPulseWidth;
        dutyCycle = (float) ((PW / ((double) 1 /pwm.frequency()))*100);
    }
}
