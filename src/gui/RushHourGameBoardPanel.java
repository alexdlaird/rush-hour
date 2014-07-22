package gui;

import java.awt.Graphics;
import java.awt.Point;
import javax.swing.JPanel;
import rushhour.RushHourGameBoard;
import rushhour.RushHourVehicle;

/**
 * The class that contains the graphical elements of the game board object.
 *
 * @author Alex Laird
 * @file RushHourGameBoardPanel.java
 * @version 0.1
 */
class RushHourGameBoardPanel extends JPanel implements Runnable
{
    /** The number of milliseconds to sleep between animating frames.*/
    private final int ANIMATION_SLEEP = 100;
    /** The number of pixels to jump for each frame in an animation.*/
    private final int PIXEL_JUMP = 1;
    /** The x-offset of a horizontal vehicle from the top left portion of the
     * square its placed on.*/
    protected final int xPlaceCarOffsetH = 4;
    /** The y-offset of a horizontal vehicle from the top left portion of the
     * square its placed on.*/
    protected final int yPlaceCarOffsetH = 0;
    /** The x-offset of a vertical vehicle from the top left portion of the
     * square its placed on.*/
    protected final int xPlaceCarOffsetV = 3;
    /** The y-offset of a vertical vehicle from the top left portion of the
     * square its placed on.*/
    protected final int yPlaceCarOffsetV = 5;
    /** The x-offset of a horizontal vehicle from the top left portion of the
     * square its placed on.*/
    protected final int xPlaceTruckOffsetH = 10;
    /** The y-offset of a horizontal vehicle from the top left portion of the
     * square its placed on.*/
    protected final int yPlaceTruckOffsetH = 0;
    /** The x-offset of a vertical vehicle from the top left portion of the
     * square its placed on.*/
    protected final int xPlaceTruckOffsetV = 4;
    /** The y-offset of a vertical vehicle from the top left portion of the
     * square its placed on.*/
    protected final int yPlaceTruckOffsetV = 5;
    /** A reference to the frame panel interface.*/
    private RushHourPanel framePanel;
    /** The object that represents the game board.*/
    private RushHourGameBoard gameBoard;
    /** The thread for animation.*/
    private Thread moveThread;
    /** The vehicle that is being moved in an animation.*/
    private RushHourVehicle moveVehicle;
    /** The coordinates to move a vehicle to in animation.*/
    private Point moveCoordinates;

    /**
     * Constructs the Rush Hour interface panel and establishes a reference
     * to the game board object.
     *
     * @param framePanel A reference to the frame panel interface.
     * @param gameBoard A reference to the game board object.
     */
    public RushHourGameBoardPanel(RushHourPanel framePanel,
            RushHourGameBoard gameBoard)
    {
        this.framePanel = framePanel;
        this.gameBoard = gameBoard;
        moveVehicle = null;
        moveThread = null;
        moveCoordinates = null;
    }

    /**
     * Ensures that, on the GUI, a vehicle is able to be dragged to a particular
     * location without causing a collision or walking off the game board.
     *
     * @param vehicle The vehicle trying to be moved.
     */
    public boolean vehicleLocationIsValid(RushHourVehicle vehicle)
    {
        // the vehicle's front is off the game board
        if(vehicle.getX() < 0 || vehicle.getY() < 0)
        {
            return false;
        }
        Point vehicleFrontLoc = RushHourDomain.simplifyPoint(
                vehicle.getLocation(), getWidth(), gameBoard.boardWidth,
                getHeight(), gameBoard.boardHeight);
        // the vehicle is horizontal
        if(vehicle.getOrientation().equals("h"))
        {
            Point vehicleRearLoc = RushHourDomain.simplifyPoint(
                    new Point(vehicle.getX() + vehicle.getVehicleIconLength(),
                    vehicle.getY()), getWidth(), gameBoard.boardWidth,
                    getHeight(), gameBoard.boardHeight);
            int moveLeft = vehicleFrontLoc.x - vehicle.getVehicleX();
            int moveRight = vehicleRearLoc.x - vehicle.getVehicleX() -
                    vehicle.getVehicleLength() + 1;
            if(moveLeft < 0 && !gameBoard.canMoveLeft(vehicle,
                    Math.abs(moveLeft)))
            {
                return false;
            }
            if(moveRight > 0 && !gameBoard.canMoveRight(vehicle, moveRight))
            {
                return false;
            }
        }
        // the vehicle is vertical
        else
        {
            Point vehicleRearLoc = RushHourDomain.simplifyPoint(
                    new Point(vehicle.getX(), vehicle.getY() +
                    vehicle.getVehicleIconLength()), getWidth(),
                    gameBoard.boardWidth, getHeight(), gameBoard.boardHeight);
            int moveUp = vehicleFrontLoc.y - vehicle.getVehicleY();
            int moveDown = vehicleRearLoc.y - vehicle.getVehicleY() -
                    vehicle.getVehicleLength() + 1;
            if(moveUp < 0 && !gameBoard.canMoveUp(vehicle, Math.abs(moveUp)))
            {
                return false;
            }
            if(moveDown > 0 && !gameBoard.canMoveDown(vehicle, moveDown))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Sets the moveVehicle to the vehicle that needs to be moved and
     * moveCoordinates to the location it needs to be moved to.  Then launches
     * the animation thread to run until the vehicle has been fully moved.
     *
     * @param vehicle The vehicle to be moved.
     * @param coordinates The coordinates to move the vehicle to.
     */
    public void goMove(RushHourVehicle vehicle, Point coordinates)
    {
        // finish old move first
        if(moveVehicle != null)
        {
            moveVehicle.setLocation(moveCoordinates);
            repaint();
        }

        // assign vehicle and move coordinates
        moveVehicle = vehicle;
        if(vehicle.getOrientation().equals("h"))
        {
            if(vehicle.getType().equals("car"))
            {
                moveCoordinates = new Point(coordinates.x + xPlaceCarOffsetH,
                        coordinates.y + yPlaceCarOffsetH);
            }
            else
            {
                moveCoordinates = new Point(coordinates.x + xPlaceTruckOffsetH,
                        coordinates.y + yPlaceTruckOffsetH);
            }
        }
        else
        {
            if(vehicle.getType().equals("car"))
            {
                moveCoordinates = new Point(coordinates.x + xPlaceCarOffsetV,
                        coordinates.y + yPlaceCarOffsetV);
            }
            else
            {
                moveCoordinates = new Point(coordinates.x + xPlaceTruckOffsetV,
                        coordinates.y + yPlaceTruckOffsetV);
            }
        }

        // launch the animation thread
        if(moveThread == null)
        {
            framePanel.resetButton.setEnabled(false);
            framePanel.solveButton.setEnabled(false);
            moveThread = new Thread(this);
            moveThread.start();
        }
    }

    /**
     * Moves the vehicle a preset number of pixels each iteration in the
     * direction of the final moveCoordinates point.
     */
    public void moveVehicle()
    {
        int x = (int) moveVehicle.getLocation().getX();
        int y = (int) moveVehicle.getLocation().getY();
        // set the new x-coordinate
        if(x < moveCoordinates.x)
        {
            x += PIXEL_JUMP;
            // overshot the course
            if(x > moveCoordinates.x)
            {
                x = moveCoordinates.x;
            }
        }
        else
        {
            x -= PIXEL_JUMP;
            // overshot the course
            if(x < moveCoordinates.x)
            {
                x = moveCoordinates.x;
            }
        }
        // set the new y-coordinate
        if(y < moveCoordinates.y)
        {
            y += PIXEL_JUMP;
            // overshot the course
            if(y > moveCoordinates.y)
            {
                y = moveCoordinates.y;
            }
        }
        else
        {
            y -= PIXEL_JUMP;
            // overshot the course
            if(y < moveCoordinates.y)
            {
                y = moveCoordinates.y;
            }
        }

        moveVehicle.setLocation(x, y);

        // once the final location is reached, halt the thread
        if(moveVehicle != null &&
                moveVehicle.getLocation().x == moveCoordinates.x &&
                moveVehicle.getLocation().y == moveCoordinates.y)
        {
            framePanel.resetButton.setEnabled(true);
            if(gameBoard.getNumVehicles() > 1)
            {
                framePanel.solveButton.setEnabled(true);
            }
            moveThread = null;
        }
    }

    /**
     * The method which paints the panel after a move has been performed in
     * animation.
     * 
     * @param g The graphics to paint the panel with.
     */
    @Override
    public void paint(Graphics g)
    {
        if(moveVehicle != null)
        {
            moveVehicle();
        }
        super.paintComponents(g);
    }

    /**
     * The thread run method launched when an animation needs to be performed
     * to move a car from its current location to the moveCoordinates.
     */
    public void run()
    {
        Thread currentThread = Thread.currentThread();

        while(currentThread == moveThread)
        {
            try
            {
                Thread.sleep(ANIMATION_SLEEP);
            }
            catch(InterruptedException e) { }

            repaint();
        }

        moveVehicle = null;
        moveCoordinates = null;
    }
}
