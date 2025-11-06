package com.github.lazygamer1111.dataTypes;

/**
 * Abstract base class for all data types in the application.
 * 
 * This class defines the common interface for all data types used in the application.
 * It provides methods for getting and setting data in a type-agnostic way,
 * allowing different data types to be handled uniformly throughout the application.
 * 
 * Subclasses must implement the getData() and setData() methods to provide
 * type-specific behavior for data access and modification.
 * 
 * @author lazygamer1111
 * @version 1.0
 * @since 2025-11-06
 */
public abstract class Data {

    /**
     * Retrieves the current data value.
     * 
     * This method returns the current value of the data in a type-agnostic way.
     * The actual return type depends on the specific implementation in subclasses,
     * but it is returned as an Object for uniformity across all data types.
     *
     * @return the current data value as an Object
     */
    public abstract Object getData();

    /**
     * Updates the current data value.
     * 
     * This method sets the data to a new value. Implementations should perform
     * appropriate type checking and conversion as needed. If the provided data
     * is not of a compatible type, implementations may throw an exception.
     *
     * @param data the new data value to set
     * @throws RuntimeException if the provided data is of an incompatible type
     */
    public abstract void setData(Object data);
}
