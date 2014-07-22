package exceptions;

/**
 * Chiefly concerned with board moves being legal (not landing on another car,
 * not jumping over cars, etc.).
 * 
 * @author Alex Laird
 * @file IllegalBoardMoveException.java
 * @version 0.1
 */
public class IllegalBoardMoveException extends RushHourException
{
    /**
     * The vehicle cannot move in the specified direction or the specified
     * number of spaces on the board.
     */
    public IllegalBoardMoveException(String color)
    {
        super(color);
    }
}
