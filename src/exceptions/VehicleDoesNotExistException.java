package exceptions;

/**
 * Chiefly concerned with vehicles that are searched for but do not actually
 * exist.
 * 
 * @author Alex Laird
 * @file VehicleDoesNotExistException.java
 * @version 0.1
 */
public class VehicleDoesNotExistException extends RushHourException
{
    /**
     * The first vehicle must be the red car.
     */
    public VehicleDoesNotExistException()
    {
        
    }
}
