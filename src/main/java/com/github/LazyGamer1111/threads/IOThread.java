package com.github.LazyGamer1111.threads;

import com.github.LazyGamer1111.dataTypes.AsyncData;
import com.github.LazyGamer1111.dataTypes.BluetoothData;
import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfigBuilder;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.plugin.gpiod.provider.gpio.digital.GpioDDigitalInputProvider;
import com.pi4j.plugin.gpiod.provider.gpio.digital.GpioDDigitalOutputProvider;
import com.pi4j.plugin.linuxfs.provider.pwm.LinuxFsPwmProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type My thread.
 */
public class IOThread extends Thread {
    public static Context pi4j = null;
    private final AsyncData<BluetoothData> bluetoothData;

    /**
     * Instantiates a new My thread.
     */
    public IOThread(AsyncData<BluetoothData> bluetoothData) {
        this.bluetoothData = bluetoothData;
    }

    public void run() {
        currentThread().setName("IO Thread");
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.info("Starting thread...");

        pi4j = Pi4J.newContextBuilder()
                .add(GpioDDigitalInputProvider.newInstance())
                .add(GpioDDigitalOutputProvider.newInstance())
                .add(LinuxFsPwmProvider.newInstance(2))
                .add(LinuxFsPwmProvider.newInstance(3))
                .build();

        DigitalOutputConfigBuilder ledConfig = DigitalOutput.newConfigBuilder(pi4j)
                 .id("led")
                 .name("led")
                 .address(17)
                 .shutdown(DigitalState.LOW)
                 .initial(DigitalState.LOW)
                .provider("gpiod-digital-output");
         DigitalOutput led = pi4j.create(ledConfig);

         while (true){
             bluetoothData.getDataBlocking();
             led.toggle();
         }
    }
}
