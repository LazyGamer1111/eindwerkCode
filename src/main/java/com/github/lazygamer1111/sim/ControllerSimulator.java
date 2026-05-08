package com.github.lazygamer1111.sim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple, thread-safe controller data simulator.
 *
 * Channel mapping (mirrors base code expectations):
 *  - ch0: steering (1000-2000, 1500 center)
 *  - ch2: throttle (1000-2000)
 *  - ch5: beacon trigger (2000 to trigger once)
 *  - ch7: range selector (1000 low, 1500 neutral, 2000 high)
 *  - ch8: kill switch (1000 engaged/stop, 2000 run)
 */
public class ControllerSimulator implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ControllerSimulator.class);

    private final int[] controllerData; // shared with jobs

    private volatile boolean running = true;
    private volatile int beaconTicks = 0; // counts down to auto-reset channel 5

    public ControllerSimulator(int[] controllerData) {
        this.controllerData = controllerData;
    }

    public void stop() { running = false; }

    public synchronized void acceptCommand(String cmd) {
        if (cmd == null || cmd.isEmpty()) return;
        switch (cmd.toLowerCase()) {
            case "w" -> add(2, +25);
            case "s" -> add(2, -25);
            case "a" -> add(0, -25);
            case "d" -> add(0, +25);
            case "c" -> set(0, 1500);
            case "r" -> cycleRange();
            case "k" -> toggleKill();
            case "b" -> pulseBeacon();
            default -> {
                String[] parts = cmd.split("\\s+");
                if (parts.length == 3 && parts[0].equalsIgnoreCase("set")) {
                    try {
                        int ch = Integer.parseInt(parts[1]);
                        int val = Integer.parseInt(parts[2]);
                        if (ch >= 0 && ch < controllerData.length) {
                            set(ch, clamp(val, 1000, 2000));
                            log.info("set ch{}={}.", ch, controllerData[ch]);
                        } else {
                            log.warn("Channel out of range (0-13): {}", ch);
                        }
                    } catch (NumberFormatException e) {
                        log.warn("Invalid set command. Usage: set <ch> <val>");
                    }
                } else {
                    log.info("Unknown command: '{}'. Type 'q' to quit.", cmd);
                }
            }
        }
    }

    private void cycleRange() {
        int cur = controllerData[7];
        if (cur <= 1100) set(7, 1500);
        else if (cur <= 1600) set(7, 2000);
        else set(7, 1000);
        log.info("range ch7 set to {}", controllerData[7]);
    }

    private void toggleKill() {
        set(8, controllerData[8] == 2000 ? 1000 : 2000);
        log.info("kill ch8 set to {}", controllerData[8]);
    }

    private void pulseBeacon() {
        set(5, 2000);
        beaconTicks = 5; // ~50ms with 10ms loop
        log.info("beacon pulse");
    }

    private void add(int ch, int delta) { set(ch, controllerData[ch] + delta); }

    private void set(int ch, int val) { controllerData[ch] = clamp(val, 1000, 2000); }

    private int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }

    @Override
    public void run() {
        while (running) {
            // auto-reset beacon channel after a short pulse
            if (beaconTicks > 0) {
                beaconTicks--;
                if (beaconTicks == 0) {
                    controllerData[5] = 1000;
                }
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
