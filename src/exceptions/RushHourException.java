package exceptions;

/**
 * Generic exception for the Rush Hour game which all other exceptions are
 * extended from.
 * 
 * @author Alex Laird
 * @file RushHourException.java
 * @version 0.1
 */
public class RushHourException extends RuntimeException
{
    /**
     * Generic Rush Hour exception.
     */
    public RushHourException()
    {

    }

    /**
     * @param x The x-coordinate of a vehicle.
     * @param y The y-coordinate of a vehicle.
     * @param color The color of a vehicle.
     */
    public RushHourException(int x, int y, String color)
    {
        
    }

    /**
     * @param x The x-coordinate of a vehicle.
     * @param y The y-coordinate of a vehicle.
     */
    public RushHourException(int x, int y)
    {
        
    }

    /**
     * @param color The color of a vehicle.
     */
    public RushHourException(String color)
    {

    }
}
