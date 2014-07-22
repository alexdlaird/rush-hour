package exceptions;

/**
 * Chiefly concerned with vehicle color (two vehicles cannot have the same
 * color).
 * 
 * @author Alex Laird
 * @file InvalidVehicleColorException.java
 * @version 0.1
 */
public class InvalidVehicleColorException extends RushHourException
{
    /**
     * Only one vehicle of each color may be added to the game board.
     */
    public InvalidVehicleColorException()
    {
        
    }
}
