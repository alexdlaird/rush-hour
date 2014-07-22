package exceptions;

/**
 * Cheifly concerned with invalid vehicles (must be of type "car" or "truck").
 * 
 * @author Alex Laird
 * @file InvalidVehicleException.java
 * @version 0.1
 */
public class InvalidVehicleException extends RushHourException
{
    /**
     * The vehicle must be of type "car" or "truck."
     */
    public InvalidVehicleException()
    {
        
    }
}
