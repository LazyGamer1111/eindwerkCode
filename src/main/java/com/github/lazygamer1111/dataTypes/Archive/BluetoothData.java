package com.github.lazygamer1111.dataTypes.Archive;

/**
 * Data container for Bluetooth communication data.
 * 
 * This class extends the abstract Data class to provide specific implementation
 * for handling Bluetooth communication data, which is stored as String values.
 * It enforces type safety by ensuring that only String data can be stored.
 * 
 * @author lazygamer1111
 * @version 1.0
 * @since 2025-11-06
 */
public class BluetoothData extends Data {
    /**
     * The Bluetooth data stored as a String.
     * Initialized to an empty string.
     */
    private String data = "";

    /**
     * Retrieves the current Bluetooth data.
     * 
     * @return the current Bluetooth data as an Object (actually a String)
     */
    @Override
    public Object getData() {
        return data;
    }

    /**
     * Updates the Bluetooth data with a new value.
     * 
     * This method enforces type safety by checking that the provided data
     * is a String. If not, it throws a RuntimeException.
     * 
     * @param data the new Bluetooth data to set
     * @throws RuntimeException if the provided data is not a String
     */
    @Override
    public void setData(Object data) {
        // Ensure the data is a String
        if (data instanceof String) {
            this.data = (String) data;
        } else {
            // Throw an exception if the data is not a String
            throw new RuntimeException("Invalid data type: " + data.getClass());
        }
    }
}
