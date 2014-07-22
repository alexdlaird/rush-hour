package exceptions;

/**
 * Chiefly concerned with the red car being the first vehicle added to the
 * board.
 * 
 * @author Alex Laird
 * @file InvalidFirstVehicleException.java
 * @version 0.1
 */
public class InvalidFirstVehicleException extends RushHourException
{
    /**
     * The first vehicle must be the red car.
     */
    public InvalidFirstVehicleException()
    {
        
    }
}
