package rushhour;

import exceptions.InvalidVehicleColorException;
import exceptions.InvalidVehicleException;
import exceptions.OffGameBoardException;
import exceptions.RedCarException;
import exceptions.VehicleOverlapException;
import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * A vehicle must be either a car or a truck.  It can be placed on the game
 * board and its (vehicleX, vehicleY) coordinates can be shifted depending on
 * its horizontal or vertical orientation.  Vehicles contain a unique ID, which
 * represents the order in which they were added to the game board, and a unique
 * hash ID, which combines their unique ID with their (vehicleX, vehicleY)
 * coordinate index on the game board.
 * <p>
 * Vehicles can also be distinguished by their color.  Two vehicles should not
 * have the same color, but the vehicle object does no assurance checking to
 * make sure a vehicle of the specified color does not already exist.
 * <p>
 * Cars have a vehicleLength of two (2) and trucks have a vehicleLength of three
 * (3).  All vehicles have a vehicleWidth of one (1).
 *
 * @author Alex Laird
 * @author Kevin Bender
 * @file RushHourVehicle.java
 * @version 0.2
 */
public class RushHourVehicle extends JLabel
{
    /** The unique ID should always have two digits.*/
    private final NumberFormat NUMBER_FORMATTER = new DecimalFormat("00");
    /** The ID used to represent the vehicle and its unique spot on the board.*/
    private String hashID;
    /** A pointer to the game board that the vehicle is a part of.*/
    private RushHourGameBoard gameBoard;
    /** The unique number that represents this car on the board (represents the
     * sequence in which the vehicle was added to the board.)*/
    private int uniqueID;
    /** The type of the vehicle.*/
    private String type;
    /** The color of the vehicle.*/
    private String color;
    /** The orientation of the vehicle.*/
    private String orientation;
    /** The y-coordinate of the vehicle.*/
    private int vehicleX;
    /** The y-coordinate of the vehicle.*/
    private int vehicleY;
    /** The length of the vehicle.*/
    private int vehicleLength;
    /** the length of the icon of the vehicle.*/
    private int vehicleIconLength;
    /** The boardWidth of the vehicle.*/
    private int vehicleWidth;
    /** The boardWidth of the icon of the vehicle.*/
    private int vehicleIconWidth;
    /** The rule for the alpha (composite) of the image.*/
    private int rule;
    /** The alpha of the image.*/
    private float composite;

    /**
     * Construct a vehicle that can be placed on the game board.
     *
     * @param gameBoard The game board object.  If a null game board object is
     * passed, the object will not be aligned on the GUI, nor will the icon
     * image be assigned.
     * @param uniqueID The unique number that represents this car on the board
     * (represents the sequence in which the vehicle was added to the board.)
     * @param type The type of the new vehicle.
     * @param color The color of the new vehicle.
     * @param orientation The orientation of the new vehicle.
     * @param x The x-coordinate of the new vehicle.
     * @param y The y-coordinate of the new vehicle.
     * @throws InvalidVehicleColorException The specified vehicle color is
     * either null or already exists on another vehicle on the game board.
     * @throws InvalidVehicleException The vehicle was placed improperly.
     * @throws OffGameBoardException The specified coordinates are off the game
     * board.
     * @throws RedCarException The red car must be in the third row so it can
     * exit.
     * @throws VehicleOverlapException The vehicle is placed at coordinates
     * that cause it to overlap an already existing vehicle.
     */
    public RushHourVehicle(RushHourGameBoard gameBoard, int uniqueID,
            String type, String color, String orientation, int x, int y)
            throws InvalidVehicleColorException, InvalidVehicleException,
            RedCarException, OffGameBoardException, VehicleOverlapException
    {
        rule = AlphaComposite.SRC_OVER;
        composite = 1.0f;
        int horiz = 0;
        int vert = 0;
        
        vehicleWidth = 1;
        vehicleIconWidth = 85;
        
        this.gameBoard = gameBoard;
        this.uniqueID = uniqueID;
        
        if(color != null)
        {
            this.color = color;
        }
        else
        {
            throw new InvalidVehicleColorException();
        }
        
        if(orientation != null || !orientation.equals("h") ||
                !orientation.equals("v"))
        {
            this.orientation = orientation;
        }
        else
        {
            throw new InvalidVehicleException();
        }
        
        if(gameBoard != null && (x >= 0 && x < gameBoard.boardWidth && y >= 0 &&
                y < gameBoard.boardHeight))
        {
            if(color.equals("red") && y != 2)
            {
                throw new RedCarException();
            }
            else
            {
                this.vehicleX = x;
                this.vehicleY = y;
            }
        }
        else if(gameBoard != null)
        {
            throw new OffGameBoardException(x, y);
        }
        
        this.type = type;
        if(type.equals("car"))
        {
            vehicleLength = 2;
            vehicleIconLength = 170;

            // verify that placing the car in this position does not overlap
            // a car already on the board
            if(orientation.equals("h"))
            {
                if(gameBoard != null &&
                        (gameBoard.getVehicleAtLocation(x, y) != null ||
                        gameBoard.getVehicleAtLocation(x + 1, y) != null))
                {
                    throw new VehicleOverlapException(x, y, color);
                }
                horiz = vehicleIconLength;
                vert = vehicleIconWidth;
            }
            else
            {
                if(gameBoard != null &&
                        (gameBoard.getVehicleAtLocation(x, y) != null ||
                        gameBoard.getVehicleAtLocation(x, y + 1) != null))
                {
                    throw new VehicleOverlapException(x, y, color);
                }
                horiz = vehicleIconWidth;
                vert = vehicleIconLength;
            }
        }
        else if(type.equals("truck"))
        {
            vehicleLength = 3;
            vehicleIconLength = 255;

            // verify that placing the car in this position does not overlap
            // a car already on the board
            if(orientation.equals("h"))
            {
                if(gameBoard != null &&
                        (gameBoard.getVehicleAtLocation(x, y) != null ||
                        gameBoard.getVehicleAtLocation(x + 1, y) != null ||
                        gameBoard.getVehicleAtLocation(x + 2, y) != null))
                {
                    throw new VehicleOverlapException(x, y, color);
                }
                horiz = vehicleIconLength;
                vert = vehicleIconWidth;
            }
            else
            {
                if(gameBoard != null &&
                        (gameBoard.getVehicleAtLocation(x, y) != null ||
                        gameBoard.getVehicleAtLocation(x, y + 1) != null ||
                        gameBoard.getVehicleAtLocation(x, y + 2) != null))
                {
                    throw new VehicleOverlapException(x, y, color);
                }
                horiz = vehicleIconWidth;
                vert = vehicleIconLength;
            }
        }
        else
        {
            throw new InvalidVehicleException();
        }
        // assuming we're a valid car on the game board, set the GUI components
        if(gameBoard != null)
        {
            try
            {
                // set the vehicle image
                super.setIcon(new ImageIcon(getClass().getResource("/images/" +
                            type + "_" + color.toLowerCase().replace(" ", "_") +
                            "_" + orientation + ".png")));
                super.setSize(horiz, vert);
            }
            catch(NullPointerException e)
            {
                super.setIcon(null);
                super.setSize(horiz, vert);
            }
        }

        hashID = x + "" + y + NUMBER_FORMATTER.format(uniqueID);
    }

    /**
     * Set the alpha (composite) of the vehicle. After setting the composite,
     * call repaint() on the vehicle to make the change appear on the interface.
     * Give the composite a floating number between 0.0 and 1.0.
     *
     * @param composite The new composite.
     */
    public void setComposite(float composite)
    {
        this.composite = composite;
    }

    /**
     * The paintComponent() method is overriden to encroprate alpha (composite)
     * manipulation to the 2D graphics. To set the alpha of this vehicle, call
     * setComposite(), assing the composite to a float between 0.0 and 1.0,
     * then call repaint() on the vehicle.
     *
     * Do not call this method directly. Instead, use repaint().
     * 
     * @param g The graphics of the vehicle.
     */
    @Override
    public void paintComponent(Graphics g)
    {
        Graphics2D g2 = (Graphics2D) g;
        AlphaComposite ac = AlphaComposite.getInstance(rule, composite);
        g2.setComposite(ac);
        super.paintComponent(g2);
    }

    /**
     * Reset the icon image to the default image for this vehicle. Also resets
     * the opacity of the image.
     */
    public void resetIconImage()
    {
        try
        {
            super.setIcon(new ImageIcon(getClass().getResource("/images/" +
                    type + "_" + color.toLowerCase().replace(" ", "_") +
                    "_" + orientation + ".png")));
        }
        catch(NullPointerException c)
        {
            super.setIcon(null);
        }
        rule = AlphaComposite.SRC_OVER;
        composite = 1.0f;
    }

    /**
     * Reconstruct the hash ID.
     */
    private void remakeHashID()
    {
        hashID = vehicleX + "" + vehicleY + NUMBER_FORMATTER.format(uniqueID);
    }

    /**
     * Retrieve the hash ID for the vehicle.
     *
     * @return The hash ID that represents the vehicle and its location
     * on the board.
     */
    public String getHashID()
    {
        return hashID;
    }

    /**
     * Retrieve the unique ID of the vehicle.
     *
     * @return The unique ID of the vehicle.
     */
    public int getUniqueID()
    {
        return uniqueID;
    }

    /**
     * Set the unique ID of the vehicle to a new unique ID.
     *
     * @param uniqueID The new unique ID.
     */
    public void setUniqueID(int uniqueID)
    {
        this.uniqueID = uniqueID;
        
        remakeHashID();
    }

    /**
     * Retrieve the type of the vehicle.  Returns "car" or "truck".
     *
     * @return The type of the vehicle.
     */
    public String getType()
    {
        return type;
    }

    /**
     * Retrieve the color of the vehicle.
     *
     * @return The color of the vehicle.
     */
    public String getColor()
    {
        return color;
    }

    /**
     * Retrieve the orientation of the vehicle.  Returns "h" for horizontal and
     * "v" for vertical.
     *
     * @return The orientation of the vehicle.
     */
    public String getOrientation()
    {
        return orientation;
    }
    
    /**
     * Retrieve the x-coordinate of the vehicle.
     *
     * @return The x-coordinate of the vehicle.
     */
    public int getVehicleX()
    {
        return vehicleX;
    }

    /**
     * Set the new vehicleX-coordinate of the vehicle.  Vehicles have no
     * knowledge of other vehicles on the game board, so the game board handles
     * checks for vehicle overlap.
     *
     * @param x The new vehicleX-coordinate of the vehicle.
     * @throws OffGameBoardException The specified coordinates are off the game
     * board.
     */
    public void setVehicleX(int x)
            throws OffGameBoardException
    {
        if(x >= 0 && x < gameBoard.boardWidth)
        {
            try
            {
                int oldX = this.vehicleX;
                this.vehicleX = x;
                RushHourVehicle[][] tempGameBoard = gameBoard.getGameBoard();
                if(type.equals("car"))
                {
                    if(orientation.equals("h"))
                    {
                        tempGameBoard[this.vehicleY][oldX] = null;
                        tempGameBoard[this.vehicleY][oldX + 1] = null;
                        tempGameBoard[this.vehicleY][x] = this;
                        tempGameBoard[this.vehicleY][x + 1] = this;
                    }
                    else
                    {
                        tempGameBoard[this.vehicleY][oldX] = null;
                        tempGameBoard[this.vehicleY + 1][oldX] = null;
                        tempGameBoard[this.vehicleY][x] = this;
                        tempGameBoard[this.vehicleY + 1][x] = this;
                    }
                }
                else
                {
                    if(orientation.equals("h"))
                    {
                        tempGameBoard[this.vehicleY][oldX] = null;
                        tempGameBoard[this.vehicleY][oldX + 1] = null;
                        tempGameBoard[this.vehicleY][oldX + 2] = null;
                        tempGameBoard[this.vehicleY][x] = this;
                        tempGameBoard[this.vehicleY][x + 1] = this;
                        tempGameBoard[this.vehicleY][x + 2] = this;
                    }
                    else
                    {
                        tempGameBoard[this.vehicleY][oldX] = null;
                        tempGameBoard[this.vehicleY + 1][oldX] = null;
                        tempGameBoard[this.vehicleY + 2][oldX] = null;
                        tempGameBoard[this.vehicleY][x] = this;
                        tempGameBoard[this.vehicleY + 1][x] = this;
                        tempGameBoard[this.vehicleY + 2][x] = this;
                    }
                }
                gameBoard.setGameBoard(tempGameBoard);
            }
            catch(ArrayIndexOutOfBoundsException c)
            {
                throw new OffGameBoardException(x, vehicleY);
            }
        }
        else
        {
            throw new OffGameBoardException(x, vehicleY);
        }

        remakeHashID();
    }

    /**
     * Retrieve the y-coordinate of the vehicle.
     *
     * @return The y-coordinate of the vehicle.
     */
    public int getVehicleY()
    {
        return vehicleY;
    }

    /**
     * Set the new y-coordinate of the vehicle. Vehicles have no knowledge of
     * other vehicles on the game board, so the game board handles checks for
     * vehicle overlap.
     *
     * @param y The new y-coordinate of the vehicle.
     * @throws OffGameBoardException The specified coordinates are off the game
     * board.
     */
    public void setVehicleY(int y)
            throws OffGameBoardException
    {
        if(y >= 0 && y < gameBoard.boardHeight)
        {
            try
            {
                int oldY = this.vehicleY;
                this.vehicleY = y;
                RushHourVehicle[][] tempGameBoard = gameBoard.getGameBoard();
                if(type.equals("car"))
                {
                    if(orientation.equals("h"))
                    {
                        tempGameBoard[oldY][this.vehicleX] = null;
                        tempGameBoard[oldY][this.vehicleX + 1] = null;
                        tempGameBoard[y][this.vehicleX] = this;
                        tempGameBoard[y][this.vehicleX + 1] = this;
                    }
                    else
                    {
                        tempGameBoard[oldY][this.vehicleX] = null;
                        tempGameBoard[oldY + 1][this.vehicleX] = null;
                        tempGameBoard[y][this.vehicleX] = this;
                        tempGameBoard[y + 1][this.vehicleX] = this;
                    }
                }
                else
                {
                    if(orientation.equals("h"))
                    {
                        tempGameBoard[oldY][this.vehicleX] = null;
                        tempGameBoard[oldY][this.vehicleX + 1] = null;
                        tempGameBoard[oldY][this.vehicleX + 2] = null;
                        tempGameBoard[y][this.vehicleX] = this;
                        tempGameBoard[y][this.vehicleX + 1] = this;
                        tempGameBoard[y][this.vehicleX + 2] = this;
                    }
                    else
                    {
                        tempGameBoard[oldY][this.vehicleX] = null;
                        tempGameBoard[oldY + 1][this.vehicleX] = null;
                        tempGameBoard[oldY + 2][this.vehicleX] = null;
                        tempGameBoard[y][this.vehicleX] = this;
                        tempGameBoard[y + 1][this.vehicleX] = this;
                        tempGameBoard[y + 2][this.vehicleX] = this;
                    }
                }
                gameBoard.setGameBoard(tempGameBoard);
            }
            catch(ArrayIndexOutOfBoundsException c)
            {
                throw new OffGameBoardException(vehicleX, y);
            }
        }
        else
        {
            throw new OffGameBoardException(vehicleX, y);
        }

        remakeHashID();
    }

    /**
     * Retrieve the length of the vehicle.
     *
     * @return The length of the vehicle.
     */
    public int getVehicleLength()
    {
        return vehicleLength;
    }

    /**
     * Retrieve the vehicle icon length.
     *
     * @return The length of the icon of the vehicle.
     */
    public int getVehicleIconLength()
    {
        return vehicleIconLength;
    }

    /**
     * Retrieve the boardWidth of the vehicle.
     *
     * @return The boardWidth of the vehicle.
     */
    public int getVehicleWidth()
    {
        return vehicleWidth;
    }

    /**
     * Retrieve the vehicle icon boardWidth.
     *
     * @return The boardWidth of the icon of the vehicle.
     */
    public int getVehicleIconWidth()
    {
        return vehicleIconWidth;
    }

    /**
     * A string representation of the vehicle, which is the uniqe
     * identification number.
     *
     * @return The string representation of the vehicle.
     */
    @Override
    public String toString()
    {
        return NUMBER_FORMATTER.format(uniqueID);
    }
}
