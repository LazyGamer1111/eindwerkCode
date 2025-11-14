package com.github.lazygamer1111.dataTypes.Archive;

import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * Thread-safe container for remote controller data.
 * 
 * This class extends the abstract Data class to provide specific implementation
 * for handling remote controller input data. It stores 14 channel values in a
 * thread-safe AtomicIntegerArray, allowing concurrent access from multiple threads.
 * 
 * The controller data typically represents various inputs from a remote controller,
 * such as joystick positions, button states, and other control values.
 * 
 * @author lazygamer1111
 * @version 1.0
 * @since 2025-11-06
 */
public class ControllerData extends Data {
    /**
     * Thread-safe array storing the 14 controller channel values.
     * Using AtomicIntegerArray ensures that updates to individual channels
     * are atomic operations, preventing race conditions when accessed from
     * multiple threads.
     */
    AtomicIntegerArray channels = new AtomicIntegerArray(14);

    /**
     * Retrieves all controller channel values as an array.
     * <p>
     * This method creates a copy of the current channel values in a regular
     * Integer array, ensuring that the returned data is a snapshot that won't
     * be affected by subsequent updates to the channels.
     * 
     * @return an Integer array containing all 14 channel values
     */
    @Override
    public Object getData() {
        // Create a new array to hold the channel values
        Integer[] get = new Integer[14];
        
        // Copy each channel value to the new array
        for (int i = 0; i < 14; i++) {
            get[i] = channels.get(i);
        }
        
        return get;
    }

    /**
     * Updates all controller channel values from an array.
     * <p>
     * This method accepts an Integer array and updates all channel values
     * in the AtomicIntegerArray. It performs type checking to ensure that
     * the provided data is an Integer array.
     * 
     * @param data the new channel values as an Integer array
     */
    @Override
    public void setData(Object data) {
        // Check if the data is an Integer array (using pattern matching for instanceof)
        if (data instanceof Integer[] data1) {
            // Update each channel with the corresponding value from the array
            for (int i = 0; i < 14; i++) {
                channels.set(i, data1[i]);
            }
        }
        // Silently ignore if the data is not an Integer array
    }
}
