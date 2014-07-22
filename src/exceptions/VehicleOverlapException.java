package exceptions;

/**
 * Chiefly concerned with vehicles that are placed one over another.
 * 
 * @author Alex Laird
 * @file VehicleOverlapException.java
 * @version 0.1
 */
public class VehicleOverlapException extends RushHourException
{
    /**
     * The vehicle location specified overlaps a currently existing vehicle.
     */
    public VehicleOverlapException(int x, int y, String color)
    {
        super(x, y, color);
    }
}
