package com.github.lazygamer1111.dataTypes.Archive;

/**
 * The type Bluetooth data.
 */
public class BluetoothData extends Data {
    private String data = "";

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        if (data instanceof String) {
            this.data = (String) data;
        } else {
            throw new RuntimeException("Invalid data type: " + data.getClass());
        }
    }
}
