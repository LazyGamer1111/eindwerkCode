package com.github.lazygamer1111.components.output;

import com.pi4j.io.pwm.Pwm;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import static java.lang.Math.abs;

/**
 * Servo motor control class for PWM-based servo motors.
 * 
 * This class provides an abstraction for controlling servo motors via PWM signals.
 * It handles the conversion from angle values (degrees) to appropriate PWM duty cycles,
 * taking into account the servo's specific pulse width requirements and frequency.
 * 
 * @author lazygamer1111
 * @version 1.0
 * @since 2025-11-02
 */
public class Servo {
    /**
     * The minimum angle (in degrees) that the servo can rotate to.
     * Default is 0 degrees if not specified in constructor.
     */
    final double minAngle;
    
    /**
     * The maximum angle (in degrees) that the servo can rotate to.
     * Default is 180 degrees if not specified in constructor.
     */
    final double maxAngle;
    
    /**
     * The minimum pulse width (in seconds) corresponding to the minimum angle.
     * Default is 1ms (0.001s) if not specified in constructor.
     */
    final double minPulseWidth;
    
    /**
     * The maximum pulse width (in seconds) corresponding to the maximum angle.
     * Default is 2ms (0.002s) if not specified in constructor.
     */
    final double maxPulseWidth;

    /**
     * The current angle (in degrees) of the servo.
     * Initialized to 0 degrees.
     */
    double angle = 0;
    
    /**
     * The current duty cycle (percentage) of the PWM signal.
     * Calculated based on the angle and pulse width parameters.
     */
    double dutyCycle = 0;

    /**
     * The PWM interface used to control the servo.
     * Provided by Pi4J library.
     */
    Pwm pwm;

    /**
     * Instantiates a new Servo with customizable parameters.
     *
     * This constructor allows full customization of the servo's operating parameters.
     * All parameters except pwm can be null, in which case default values will be used.
     *
     * @param pwm           the Pi4J PWM interface to control the servo
     * @param minAngle      the minimum angle in degrees (default: 0)
     * @param maxAngle      the maximum angle in degrees (default: 180)
     * @param minPulseWidth the minimum pulse width in seconds (default: 0.001s or 1ms)
     * @param maxPulseWidth the maximum pulse width in seconds (default: 0.002s or 2ms)
     * @param frequency     the PWM frequency in Hz (default: 50Hz, standard for most servos)
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
     * Sets the servo to the specified angle.
     *
     * This method calculates the appropriate duty cycle based on the angle
     * and updates the PWM signal to move the servo to that position.
     * The angle must be within the configured min/max angle range.
     *
     * @param angle the target angle in degrees
     * @throws IllegalArgumentException if the angle is outside the valid range
     */
    public void setAngle(double angle) {
        this.angle = angle;
//        LoggerFactory.getLogger(this.getClass()).info("Servo set angle: {}", angle);
        if (angle < minAngle || angle > maxAngle) {
            throw new IllegalArgumentException("Angle must be between " + minAngle + " and " + maxAngle);
        }
        double percent = angle / (abs(maxAngle) + abs(minAngle));

        dutyCycle = calcDutyCycle(maxPulseWidth, minPulseWidth, percent, pwm.frequency());

        pwm.on(this.dutyCycle);
    }

    /**
     * Calculates the PWM duty cycle for a given servo position.
     * 
     * This method converts from a percentage position (0.0 to 1.0) to the appropriate
     * duty cycle percentage based on the pulse width requirements and PWM frequency.
     * 
     * @param maxPulseWidth the maximum pulse width in seconds
     * @param minPulseWidth the minimum pulse width in seconds
     * @param percentage    the position as a percentage (0.0 to 1.0) between min and max angles
     * @param frequency     the PWM frequency in Hz
     * @return              the calculated duty cycle as a percentage (0-100)
     */
    public float calcDutyCycle(double maxPulseWidth, double minPulseWidth, double percentage, int frequency) {
        // Calculate the pulse width for the given percentage position
        double PW = (maxPulseWidth - minPulseWidth) * percentage + minPulseWidth;
        // Convert pulse width to duty cycle percentage
        return (float) ((PW / ((double) 1 /frequency))*100);
    }
}
