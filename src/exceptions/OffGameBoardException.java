package exceptions;

/**
 * Chiefly concerned with vehicles being placed within the bounds of the game
 * board.
 * 
 * @author Alex Laird
 * @file OffGameBoardException.java
 * @version 0.1
 */
public class OffGameBoardException extends RushHourException
{
    /**
     * A vehicles placement has overflowed the boundaries of the game board.
     */
    public OffGameBoardException(int x, int y)
    {
        super(x, y);
    }
}
