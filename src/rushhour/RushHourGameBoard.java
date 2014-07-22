package rushhour;

import exceptions.IllegalBoardMoveException;
import exceptions.InvalidFirstVehicleException;
import exceptions.InvalidVehicleColorException;
import exceptions.InvalidVehicleException;
import exceptions.OffGameBoardException;
import exceptions.RedCarException;
import exceptions.VehicleDoesNotExistException;
import exceptions.VehicleOverlapException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

/**
 * A game gameBoard for the game Rush Hour.  Vehicles, known as cars and trucks,
 * can be placed on the game gameBoard and shifted until the red car is able to
 * exit through the edge at (3, 6).
 * <p>
 * Vehicles (cars or trucks) can be added to the game gameBoard at specified
 * locations on the gameBoard.  When a vehicle is added to the gameBoard, the
 * (x, y) coordinates given are assumed to be from (1, 1) being in the
 * upper-left corner of the gameBoard, and the coordinate given is assumed to be
 * the top-most, left-most position of the vehicle.
 * <p>
 * It's important to realize that coordinates may change frequently in the way
 * they are displayed depending on whether a location on the gameBoard (where
 * (1, 1) is the upper-left corner) or an index (where (0, 0) is the upper-left
 * corner) is displayed.  For errors specifically, indeces are used for tracking
 * errors within the actual array, but when information regarding the position
 * is output to the user normally, it should be in a location where (1, 1) is
 * the upper-left corner.
 * <p>
 * Once a game baord is constructed, the vehicles on the gameBoard can be moved
 * around in valid directions.  The solve() method can be called to find the
 * minimum number of moves required to solve the given game gameBoard. If the
 * solve() method cannot find a solution, false is returned and the game
 * gameBoard is deemed unsolvable.
 *
 * @author Alex Laird
 * @author Kevin Bender
 * @file RushHourGameBoard.java
 * @version 0.2
 */
public class RushHourGameBoard
{
    /** The unique ID should always have two digits.*/
    private final NumberFormat NUMBER_FORMATTER = new DecimalFormat("00");
    /** The boardWidth of the game gameBoard.*/
    public int boardWidth = 6;
    /** The boardHeight of the game gameBoard.*/
    public int boardHeight = 6;
    /** The two-dimensional array that represents the game gameBoard.*/
    private RushHourVehicle[][] gameBoard;
    /** An array that holds all the vehicles placed on the game gameBoard.*/
    private Vector<RushHourVehicle> vehicles;
    /** The hash state of the gameBoard at the beginning of the game.*/
    private String initialState;
    /** The hash state of the gameBoard when the red car reaches the exit.*/
    private String winningState;

    /** The queue to keep track of gameBoard states that need to be visited as
     * they are unconvered.*/
    private LinkedList<String> stateQueue;
    /** The hash map that will contain all possible valid moves up until a
     * solution is found.*/
    private HashMap<String, String> validMoves;
    /** The hash map that will contain movement directions to correspond to the
     * moves stored in validMoves.*/
    private HashMap<String, String> moveDirections;

    /**
     * Construct the game gameBoard.
     */
    public RushHourGameBoard()
    {
        initGameBoard();
    }

    /**
     * Resets the game board to an empty state.
     */
    public void initGameBoard()
    {
        gameBoard = new RushHourVehicle[boardWidth][boardHeight];
        vehicles = new Vector<RushHourVehicle>();
        stateQueue = new LinkedList<String>();
        validMoves = new HashMap<String, String>();
        moveDirections = new HashMap<String, String>();
        initialState = null;
        winningState = null;
    }

    /**
     * Used when solving the game gameBoard.  Adds the current state to the
     * queue to be explored later and, if not already visited, adds the
     * instructions for the move to the hash maps.
     *
     * @param boardState The current gameBoard state.
     * @param nextState The gameBoard state that has been moved to.
     * @param color The color of the current vehicle.
     * @param uniqueID The unique ID of the current vehicle.
     * @param numSpaces The number of spaces the vehicle was moved.
     * @param dir The direction the vehicle was moved in.
     * @return True if the vehicle was added to the valid moves and move
     * directions hash maps, false otherwise.
     */
    private boolean addIfNotVisited(String boardState, String nextState,
            String color, int uniqueID, int numSpaces, String dir)
    {
        if(!validMoves.containsKey(nextState))
        {
            validMoves.put(nextState, boardState);
            moveDirections.put(nextState,
                    color + " (" + NUMBER_FORMATTER.format(uniqueID) + ") " +
                    dir + " " + numSpaces);
            stateQueue.add(nextState);

            return true;
        }

        return false;
    }

    /**
     * Solve the game gameBoard in the least number of moves possible using a
     * hash map.
     * <p>
     * The algorithm solved the Rush Hour game puzzle in optimal time.
     * It implements a triply nested loop.  The first layer iterates through
     * all possible permutations the game gameBoard can appear in before it is
     * solved.  The second layer of the loops through each car on the game
     * gameBoard at the given gameBoard state from the first loop.  The third
     * loop attempts to move the given car from the second loop as many spaces
     * as possible in each given direction, adding those further moves as new
     * states to evaluate in the first loop.
     * <p>
     * Since this algorithm creates a new game board object at each state of
     * the board, the overhead cost of this object creation may cause the
     * algorithm to run slow. If speed is imperative, use the solveFast()
     * method, which implements the same algorithm but parses the hash map
     * differently and can solve more quickly.
     * <p>
     * This method manipulates the actual game board object. The game board will
     * represent the winning state upon completion of the solve() method. If
     * a solve is desired by the actual game board object should not be
     * manipulated, use the solveFast() method.
     *
     * @return True if the gameBoard was solved, false if it was unsolvable.
     */
    public boolean solve()
    {
        if(!isSolved())
        {
            // set and add the initial state
            initialState = getHash();
            addIfNotVisited(null, initialState, null, 0, 0, null);

            while(!stateQueue.isEmpty())
            {
                // remove the next hash gameBoard state from the queue
                String boardState = stateQueue.remove();

                // construct a game gameBoard that fits the hash gameBoard state
                // given
                RushHourGameBoard gameBoardAtState = getBoardFromHash(
                        boardState);
                Vector<RushHourVehicle> vehiclesAtState = gameBoardAtState.
                        getVehiclesList();

                // grab the details for each car and check positive movements
                for(int i = 0; i < vehiclesAtState.size(); ++i)
                {
                    // grab the current vehicle
                    RushHourVehicle vehicle = vehiclesAtState.elementAt(i);

                    // conditions to continue moving the vehicle as long
                    // as possible
                    boolean canMove = true;
                    // number of spaces the vehicle can be moved in a direction
                    int numSpaces = 1;

                    // move each car a space in each possible positive direction
                    // until it can be moved no further
                    while(canMove)
                    {
                        // the vehicle is horizontal
                        if(vehicle.getOrientation().equals("h"))
                        {
                            // move vehicle right numSpaces, if possible
                            if(gameBoardAtState.canMoveRight(vehicle,
                                    numSpaces))
                            {
                                // move the vehicle right on the game gameBoard
                                // break tempBoard if it moves red car in winning position
                                if(gameBoardAtState.moveRight(vehicle,
                                        numSpaces))
                                {
                                    // add winning state and move to hash maps
                                    winningState = gameBoardAtState.
                                            getHash();
                                    addIfNotVisited(boardState,
                                            winningState, vehicle.getColor(),
                                            vehicle.getUniqueID(),
                                            numSpaces + 1, "right");

                                    return true;
                                }
                                else
                                {
                                    // add to the queue and move hash maps
                                    addIfNotVisited(boardState,
                                            gameBoardAtState.getHash(),
                                            vehicle.getColor(),
                                            vehicle.getUniqueID(), numSpaces,
                                            "right");
                                }

                                // move the vehicle back left on the game
                                // gameBoard
                                gameBoardAtState.moveLeft(vehicle, numSpaces);
                            }
                            else
                            {
                                canMove = false;
                            }
                        }
                        // the vehicle is vertical
                        else
                        {
                            // move vehicle down numSpaces, if possible
                            if(gameBoardAtState.canMoveDown(vehicle, numSpaces))
                            {
                                // move the vehicle down on the game gameBoard
                                gameBoardAtState.moveDown(vehicle, numSpaces);

                                // add to the queue and move hash maps
                                addIfNotVisited(boardState,
                                        gameBoardAtState.getHash(),
                                        vehicle.getColor(),
                                        vehicle.getUniqueID(), numSpaces,
                                        "down");

                                // move the vehicle back up on the game
                                // gameBoard
                                gameBoardAtState.moveUp(vehicle, numSpaces);
                            }
                            else
                            {
                                canMove = false;
                            }
                        }

                        ++numSpaces;
                    }
                }

                // grab the details for each car and check negative movements
                for(int i = 0; i < vehiclesAtState.size(); ++i)
                {
                    // grab the current vehicle
                    RushHourVehicle vehicle = vehiclesAtState.elementAt(i);

                    // conditions to continue moving the vehicle as long as possible
                    boolean canMove = true;
                    // number of spaces the vehicle can be moved in a direction
                    int numSpaces = 1;

                    // move each car a space in each possible negative direction
                    // until it can be moved no further
                    while(canMove)
                    {
                        // the vehicle is horizontal
                        if(vehicle.getOrientation().equals("h"))
                        {
                            // move vehicle left numSpaces, if possible
                            if(gameBoardAtState.canMoveLeft(vehicle, numSpaces))
                            {
                                // move the vehicle left on the game gameBoard
                                gameBoardAtState.moveLeft(vehicle, numSpaces);

                                // add to the queue and move hash maps
                                addIfNotVisited(boardState,
                                        gameBoardAtState.getHash(),
                                        vehicle.getColor(),
                                        vehicle.getUniqueID(), numSpaces,
                                        "left");

                                // move the vehicle back right on the game
                                // gameBoard
                                gameBoardAtState.moveRight(vehicle, numSpaces);
                            }
                            else
                            {
                                canMove = false;
                            }
                        }
                        // the vehicle is vertical
                        else
                        {
                            // move vehicle up numSpaces, if possible
                            if(gameBoardAtState.canMoveUp(vehicle, numSpaces))
                            {
                                // move the vehicle up on the game gameBoard
                                gameBoardAtState.moveUp(vehicle, numSpaces);

                                // add to the queue and move hash maps
                                addIfNotVisited(boardState,
                                        gameBoardAtState.getHash(),
                                        vehicle.getColor(),
                                        vehicle.getUniqueID(),  numSpaces,
                                        "up");

                                // move the vehicle back down on the game
                                // gameBoard
                                gameBoardAtState.moveDown(vehicle, numSpaces);
                            }
                            else
                            {
                                canMove = false;
                            }
                        }

                        ++numSpaces;
                    }
                }
            }

            return false;
        }
        else
        {
            return true;
        }
    }

    /**
     * Solves the game board through string parsing of the hash representation
     * of each board state instead of creating a new game board object at each
     * individual state (since the overhead cost of that is great). Use if the
     * solve time is consistently longer than expected (which, ideally, should
     * never ben more than a few seconds). This method is most necessarily
     * used from the applet.
     * <p>
     * Unlike the solve() method, this method does NOT manipulate the actual
     * game board. If the game board should represent the winning state, a new
     * game board object will have to be created from the final board hash
     * state.
     * <p>
     * The winning state will still be stored in winningState, so the game board
     * can be set to the winning state using setBoardFromHash() and passing
     * the method the return value of getWinningState().
     *
     * @return True if the gameBoard was solved, false if it was unsolvable.
     */
    public boolean solveFast()
    {
        if(!isSolved())
        {
            // set and add the initial state
            initialState = getHash();
            addIfNotVisited(null, initialState, null, 0, 0, null);

            while(!stateQueue.isEmpty())
            {
                // remove the next hash gameBoard state from the queue
                String boardState = stateQueue.remove();

                // grab the details for each car and check positive movements
                for(int i = 0; i < boardState.length(); i += 4)
                {
                    // get the vehicle details
                    int x = Integer.parseInt(boardState.substring(i, i + 1));
                    int y = Integer.parseInt(boardState.substring(i + 1,
                            i + 2));
                    int uniqueID = Integer.parseInt(boardState.substring(i + 2,
                            i + 4));
                    // grab the current vehicle
                    RushHourVehicle vehicle = getVehicleAtIndex(uniqueID - 1);

                    // conditions to continue moving the vehicle as long
                    // as possible
                    boolean canMove = true;
                    // number of spaces the vehicle can be moved in a direction
                    int numSpaces = 1;

                    // move each car a space in each possible positive direction
                    // until it can be moved no further
                    while(canMove)
                    {
                        StringBuilder newBoard = new StringBuilder(boardState);
                        Integer newPosition;

                        // the vehicle is horizontal
                        if(vehicle.getOrientation().equals("h"))
                        {
                            newPosition = x + numSpaces;
                            newBoard.replace(i, i + 1, newPosition.toString());
                            if(canMoveToState(newBoard.toString()))
                            {
                                if(uniqueID == 1 &&
                                        isSolution(newBoard.toString()))
                                {
                                    // add winning state and move to hash maps
                                    winningState = newBoard.toString();
                                    addIfNotVisited(boardState,
                                            winningState, vehicle.getColor(),
                                            uniqueID, numSpaces + 1,
                                            "right");

                                    return true;
                                }
                                else
                                {
                                    addIfNotVisited(boardState,
                                            newBoard.toString(),
                                            vehicle.getColor(), uniqueID,
                                            numSpaces, "right");
                                }
                            }
                            else
                            {
                                canMove = false;
                            }
                        }
                        // the vehicle is vertical
                        else
                        {
                            newPosition = y + numSpaces;
                            newBoard.replace(i + 1, i + 2,
                                    newPosition.toString());
                            if(canMoveToState(newBoard.toString()))
                            {
                                addIfNotVisited(boardState, newBoard.toString(),
                                        vehicle.getColor(), uniqueID, numSpaces,
                                        "down");
                            }
                            else
                            {
                                canMove = false;
                            }
                        }

                        ++numSpaces;
                    }
                }

                // grab the details for each car and check negative movements
                for(int i = 0; i < boardState.length(); i += 4)
                {
                    // get the vehicle details
                    int x = Integer.parseInt(boardState.substring(i, i + 1));
                    int y = Integer.parseInt(boardState.substring(i + 1,
                            i + 2));
                    int uniqueID = Integer.parseInt(boardState.substring(i + 2,
                            i + 4));
                    // grab the current vehicle
                    RushHourVehicle vehicle = getVehicleAtIndex(uniqueID - 1);

                    // conditions to continue moving the vehicle as long
                    // as possible
                    boolean canMove = true;
                    // number of spaces the vehicle can be moved in a direction
                    int numSpaces = 1;

                    // move each car a space in each possible negative direction
                    // until it can be moved no further
                    while(canMove)
                    {
                        StringBuilder newBoard = new StringBuilder(boardState);
                        Integer newPosition;

                        // the vehicle is horizontal
                        if(vehicle.getOrientation().equals("h"))
                        {
                            newPosition = x - numSpaces;
                            newBoard.replace(i, i + 1, newPosition.toString());
                            if(canMoveToState(newBoard.toString()))
                            {
                                addIfNotVisited(boardState, newBoard.toString(),
                                        vehicle.getColor(), uniqueID, numSpaces,
                                        "left");
                            }
                            else
                            {
                                canMove = false;
                            }
                        }
                        // the vehicle is vertical
                        else
                        {
                            newPosition = y - numSpaces;
                            newBoard.replace(i + 1, i + 2,
                                    newPosition.toString());
                            if(canMoveToState(newBoard.toString()))
                            {
                                addIfNotVisited(boardState, newBoard.toString(),
                                        vehicle.getColor(), uniqueID, numSpaces,
                                        "up");
                            }
                            else
                            {
                                canMove = false;
                            }
                        }

                        ++numSpaces;
                    }
                }
            }

            return false;
        }
        else
        {
            return true;
        }
    }

    /**
     * Checks to see if a given hash state is a winning state for the board.
     *
     * @param boardState The current board hash state.
     * @return True if the given board is in a winning state, false otherwise.
     */
    private boolean isSolution(String boardState)
    {
        // get the x location of the first car, which should be at the exit
        int x = Integer.parseInt(boardState.substring(0, 1));
        if(x == boardWidth - 2)
        {
            return true;
        }
        else
        {
            return false;
        }
    }


    /**
     * Ensures that the given board state does not cause any collisions with
     * any other vehicles on the board.
     *
     * @param boardState The current board state to check against all other
     * vehicles for validity.
     * @return True if the given board is valid, false otherwise.
     */
    private boolean canMoveToState(String boardState)
    {
        try
        {
            // start with the blank board
            boolean[][] tempBoard = new boolean[boardHeight][boardWidth];

            // analyze against each other vehicle on the board
            for(int i = 0; i < boardState.length(); i += 4)
            {
                // get the vehicle details
                int x = Integer.parseInt(boardState.substring(i, i + 1));
                int y = Integer.parseInt(boardState.substring(i + 1, i + 2));
                int uniqueID = Integer.parseInt(boardState.substring(i + 2,
                        i + 4));
                RushHourVehicle vehicle = getVehicleAtIndex(uniqueID - 1);

                // add each vehicle to the temporary board
                for(int j = 0; j < vehicle.getVehicleLength(); ++j)
                {
                    if(vehicle.getOrientation().equals("h"))
                    {
                        if(x + j < tempBoard[y].length &&
                                tempBoard[y][x + j] != true)
                        {
                            tempBoard[y][x + j] = true;
                        }
                        else
                        {
                            return false;
                        }
                    }
                    else
                    {
                        if(y + j < tempBoard.length &&
                                tempBoard[y + j][x] != true)
                        {
                            tempBoard[y + j][x] = true;
                        }
                        else
                        {
                            return false;
                        }
                    }
                }
            }
        }
        catch(RuntimeException c)
        {
            // any exception implies a bad board
            return false;
        }

        return true;
    }

    /**
     * Set the gameBoard boardHeight.
     *
     * @param height The new gameBoard boardHeight.
     */
    public void setBoardHeight(int height)
    {
        this.boardHeight = height;
    }

    /**
     * Set the gameBoard boardWidth.
     *
     * @param width The new gameBoard boardWidth.
     */
    public void setBoardWidth(int width)
    {
        this.boardWidth = width;
    }

    /**
     * Retrieve the hash map that contains the solutions each move in the move
     * table.  This should not be called before the solve() method has been
     * called.
     *
     * @return The valid moves hash map.
     */
    public HashMap<String, String> getValidMoves()
    {
        return validMoves;
    }

    /**
     * Retrieve the hash map that contains the directions for reaching each
     * valid move.  This should not be called before the solve() method has
     * been called.
     *
     * @return The directions for valid moves hash map table.
     */
    public HashMap<String, String> getDirections()
    {
        return moveDirections;
    }

    /**
     * Retrieve the hash representation of the initial state of the gameBoard.
     * This should not be called before the solve() method has been called.
     *
     * @return The hash representation of the initial state of the gameBoard.
     */
    public String getInitialState()
    {
        return initialState;
    }

    /**
     * Retrieve the hash representation of the state of the gameBoard when the
     * red car reaches the exit. This should not be called before the solve()
     * method has been called.
     *
     * @return The hash representation of the state of the gameBoard when the
     * red car reaches the exit.
     */
    public String getWinningState()
    {
        return winningState;
    }

    /**
     * Retrieve the hash representation of the current state of the game
     * gameBoard.
     *
     * @return The current state of the gameBoard in its hash representation.
     */
    public String getHash()
    {
        String boardState = "";
        for(int i = 0; i < vehicles.size(); ++i)
        {
            boardState += vehicles.elementAt(i).getHashID();
        }
        return boardState;
    }

    /**
     * Constructs a game gameBoard object tempBoard of a given hash state.
     * 
     * @param hash The given hash state to construct into a gameBoard
     * object.
     * @return An object of the game gameBoard represented in the hash state.
     */
    public RushHourGameBoard getBoardFromHash(String hash)
    {
        RushHourGameBoard boardFromHash = new RushHourGameBoard();
        
        for(int i = 0; i < hash.length(); i += 4)
        {
            // gather details about the vehicle at this location
            int uniqueID = Integer.parseInt(hash.substring(i + 2, i + 4));
            String type = getVehicleAtIndex(uniqueID - 1).getType();
            String color = getVehicleAtIndex(uniqueID - 1).getColor();
            String orientation = getVehicleAtIndex(uniqueID - 1).
                    getOrientation();
            int x = Integer.parseInt(hash.substring(i, i + 1));
            int y = Integer.parseInt(hash.substring(i + 1, i + 2));

            // add the vehicle to our temporary gameBoard state
            boardFromHash.addVehicle(type, color, orientation, x, y);
        }
        
        return boardFromHash;
    }

    /**
     * Sets the current game board (represented as an array of vehicles) to
     * the game board represented from the passed in hash state.  This is used
     * if you want to force the game board to a specific state, but you want to
     * keep all other states of the game board object the same (for instance,
     * the variables for solution directions).
     *
     * @param hash The given hash state to construct into the game board array.
     */
    public void setBoardFromHash(String hash)
    {
        gameBoard = getBoardFromHash(hash).getGameBoard();
    }

    /**
     * Resets the game board and starts it at the state given by the passed in
     * game board.
     *
     * @param newGameBoard The new game board to replace the old one.
     */
    public void setBoardObject(RushHourGameBoard newGameBoard)
    {
        initGameBoard();
        for(int i = 0; i < newGameBoard.getNumVehicles(); ++i)
        {
            RushHourVehicle vehicle = newGameBoard.getVehicleAtIndex(i);
            String type = vehicle.getType();
            String color = vehicle.getColor();
            String orientation = vehicle.getOrientation();
            int x = vehicle.getX();
            int y = vehicle.getY();
            addVehicle(type, color, orientation, x, y);
        }
    }

    /**
     * Check to see if the red car is in a position that implies the game
     * gameBoard is solved.
     *
     * @return True if the gameBoard is in a solved state, false otherwise.
     */
    public boolean isSolved()
    {
        if(vehicles.size() > 0 &&
                vehicles.firstElement().getVehicleX() + 1 == boardWidth - 1)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Checks the current game board against a passed in game board, comparing
     * the states of each car.
     *
     * @param otherBoard The game board to be compared to.
     * @return True if the game boards are not equivalent, false otherwise.
     */
    public boolean boardEqual(RushHourGameBoard otherBoard)
    {
        if(getNumVehicles() != otherBoard.getNumVehicles())
        {
            return false;
        }

        for(int i = 0; i < getNumVehicles(); ++i)
        {
            if(getVehicleAtIndex(i).getVehicleX() !=
                    otherBoard.getVehicleAtIndex(i).getVehicleX() ||
                    getVehicleAtIndex(i).getVehicleY() !=
                    otherBoard.getVehicleAtIndex(i).getVehicleY())
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks to see if the specified vehicle can move num space(s) left.
     *
     * @param vehicle The vehicle to check if it can be moved.
     * @param num The number of spaces to move the vehicle.
     * @return True if the vehicle can be moved, false otherwise.
     */
    public boolean canMoveLeft(RushHourVehicle vehicle, int num)
    {
        // move is illegal on the gameBoard
        if(vehicle.getOrientation().equals("v") ||
                vehicle.getVehicleX() - num < 0)
        {
            return false;
        }
        // move is illegal because another vehicle is in the path
        int max = 0;
        for(int i = 1; i <= num; ++i)
        {
            if(getVehicleAtLocation(vehicle.getVehicleX() - i,
                    vehicle.getVehicleY()) != null)
            {
                max = i - 1;
                break;
            }
            ++max;
        }
        // if the desired number of spaces to move is greater than the number
        // of spaces the vehicle as able to move, the vehicle cannot move
        if(num > max)
        {
            return false;
        }

        return true;
    }

    /**
     * Move the specified vehicle left num space(s).
     *
     * @param vehicle The vehicle to be moved down.
     * @param num The number of spaces to move the vehicle.
     * @throws IllegalBoardMoveException The specified vehicle can is not able
     * to move in this direction due to the end of the gameBoard or another
     * vehicle being in the way.
     */
    public void moveLeft(RushHourVehicle vehicle, int num)
            throws IllegalBoardMoveException
    {
        // ensure that the vehicle can be moved in the direction
        if(!canMoveLeft(vehicle, num))
        {
            throw new IllegalBoardMoveException(vehicle.getColor());
        }

        // move the vehicle on the gameBoard and assign the
        if(vehicle.getType().equals("car"))
        {
            // null the old car location on the game gameBoard
            gameBoard[vehicle.getVehicleY()][vehicle.getVehicleX()] = null;
            gameBoard[vehicle.getVehicleY()][vehicle.getVehicleX() + 1] = null;
            
            // assign the car to its new location
            vehicle.setVehicleX(vehicle.getVehicleX() - num);

            // move the car on the game gameBoard
            gameBoard[vehicle.getVehicleY()][vehicle.getVehicleX()] = vehicle;
            gameBoard[vehicle.getVehicleY()]
                    [vehicle.getVehicleX() + 1] = vehicle;
        }
        else
        {
            // null the old truck location on the game gameBoard
            gameBoard[vehicle.getVehicleY()][vehicle.getVehicleX()] = null;
            gameBoard[vehicle.getVehicleY()][vehicle.getVehicleX() + 1] = null;
            gameBoard[vehicle.getVehicleY()][vehicle.getVehicleX() + 2] = null;

            // assign the truck to its new location
            vehicle.setVehicleX(vehicle.getVehicleX() - num);

            // move the truck on the game gameBoard
            gameBoard[vehicle.getVehicleY()][vehicle.getVehicleX()] = vehicle;
            gameBoard[vehicle.getVehicleY()]
                    [vehicle.getVehicleX() + 1] = vehicle;
            gameBoard[vehicle.getVehicleY()]
                    [vehicle.getVehicleX() + 2] = vehicle;
        }
    }

    /**
     * Checks to see if the specified vehicle can move num space(s) right.
     *
     * @param vehicle The vehicle to check if it can be moved.
     * @param num The number of spaces to move the vehicle.
     * @return True if the vehicle can be moved, false otherwise.
     */
    public boolean canMoveRight(RushHourVehicle vehicle, int num)
    {
        int adjust;
        if(vehicle.getType().equals("car"))
        {
            adjust = 1;
        }
        else
        {
            adjust = 2;
        }
        // move is illegal on the gameBoard
        if(vehicle.getOrientation().equals("v") ||
                vehicle.getVehicleX() + adjust + num >= boardWidth)
        {
            return false;
        }
        // move is illegal because another vehicle is in the path
        int max = 0;
        for(int i = 1; i <= num; ++i)
        {
            if(getVehicleAtLocation(vehicle.getVehicleX() + adjust + i,
                    vehicle.getVehicleY())
                    != null)
            {
                max = i - 1;
                break;
            }
            ++max;
        }
        // if the desired number of spaces to move is greater than the number
        // of spaces the vehicle as able to move, the vehicle cannot move
        if(num > max)
        {
            return false;
        }

        return true;
    }

    /**
     * Move the specified vehicle right num space(s).
     *
     * @param vehicle The vehicle to be moved down.
     * @param num The number of spaces to move the vehicle.
     * @return True if the car moved to the right is the red car and has reached
     * the right side of the gameBoard, false otherwise.
     * @throws IllegalBoardMoveException The specified vehicle can is not able
     * to move in this direction due to the end of the gameBoard or another
     * vehicle being in the way.
     */
    public boolean moveRight(RushHourVehicle vehicle, int num)
            throws IllegalBoardMoveException
    {
        // ensure that the vehicle can be moved in the direction
        if(!canMoveRight(vehicle, num))
        {
            throw new IllegalBoardMoveException(vehicle.getColor());
        }

        // move the vehicle on the gameBoard and assign the
        if(vehicle.getType().equals("car"))
        {
            // null the old car location on the game gameBoard
            gameBoard[vehicle.getVehicleY()][vehicle.getVehicleX()] = null;
            gameBoard[vehicle.getVehicleY()][vehicle.getVehicleX() + 1] = null;

            // assign the car to its new location
            vehicle.setVehicleX(vehicle.getVehicleX() + num);

            // move the car on the game gameBoard
            gameBoard[vehicle.getVehicleY()][vehicle.getVehicleX()] = vehicle;
            gameBoard[vehicle.getVehicleY()]
                    [vehicle.getVehicleX() + 1] = vehicle;
        }
        else
        {
            // null the old truck location on the game gameBoard
            gameBoard[vehicle.getVehicleY()][vehicle.getVehicleX()] = null;
            gameBoard[vehicle.getVehicleY()][vehicle.getVehicleX() + 1] = null;
            gameBoard[vehicle.getVehicleY()][vehicle.getVehicleX() + 2] = null;

            // assign the truck to its new location
            vehicle.setVehicleX(vehicle.getVehicleX() + num);

            // move the truck on the game gameBoard
            gameBoard[vehicle.getVehicleY()][vehicle.getVehicleX()] = vehicle;
            gameBoard[vehicle.getVehicleY()]
                    [vehicle.getVehicleX() + 1] = vehicle;
            gameBoard[vehicle.getVehicleY()]
                    [vehicle.getVehicleX() + 2] = vehicle;
        }

        // check to see if the red vehicle has made it to the finish
        if(vehicle.getColor().equalsIgnoreCase("red") &&
                vehicle.getVehicleX() + 1 == boardWidth - 1)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Checks to see if the specified vehicle can move num space(s) up.
     *
     * @param vehicle The vehicle to check if it can be moved.
     * @param num The number of spaces to move the vehicle.
     * @return True if the vehicle can be moved, false otherwise.
     */
    public boolean canMoveUp(RushHourVehicle vehicle, int num)
    {
        // move is illegal on the gameBoard
        if(vehicle.getOrientation().equals("h") ||
                vehicle.getVehicleY() - num < 0)
        {
            return false;
        }
        // move is illegal because another vehicle is in the path
        int max = 0;
        for(int i = 1; i <= num; ++i)
        {
            if(getVehicleAtLocation(vehicle.getVehicleX(),
                    vehicle.getVehicleY() - i) != null)
            {
                max = i - 1;
                break;
            }
            ++max;
        }
        // if the desired number of spaces to move is greater than the number
        // of spaces the vehicle as able to move, the vehicle cannot move
        if(num > max)
        {
            return false;
        }

        return true;
    }

    /**
     * Move the specified vehicle up num space(s).
     *
     * @param vehicle The vehicle to be moved down.
     * @param num The number of spaces to move the vehicle.
     * @throws IllegalBoardMoveException The specified vehicle can is not able
     * to move in this direction due to the end of the gameBoard or another
     * vehicle being in the way.
     */
    public void moveUp(RushHourVehicle vehicle, int num)
            throws IllegalBoardMoveException
    {
        // ensure that the vehicle can be moved in the direction
        if(!canMoveUp(vehicle, num))
        {
            throw new IllegalBoardMoveException(vehicle.getColor());
        }

        // move the vehicle on the gameBoard and assign the
        if(vehicle.getType().equals("car"))
        {
            // null the old car location on the game gameBoard
            gameBoard[vehicle.getVehicleY()][vehicle.getVehicleX()] = null;
            gameBoard[vehicle.getVehicleY() + 1][vehicle.getVehicleX()] = null;

            // assign the car to its new location
            vehicle.setVehicleY(vehicle.getVehicleY() - num);

            // move the car on the game gameBoard
            gameBoard[vehicle.getVehicleY()][vehicle.getVehicleX()] = vehicle;
            gameBoard[vehicle.getVehicleY() + 1]
                    [vehicle.getVehicleX()] = vehicle;
        }
        else
        {
            // null the old truck location on the game gameBoard
            gameBoard[vehicle.getVehicleY()][vehicle.getVehicleX()] = null;
            gameBoard[vehicle.getVehicleY() + 1][vehicle.getVehicleX()] = null;
            gameBoard[vehicle.getVehicleY() + 2][vehicle.getVehicleX()] = null;

            // assign the truck to its new location
            vehicle.setVehicleY(vehicle.getVehicleY() - num);

            // move the truck on the game gameBoard
            gameBoard[vehicle.getVehicleY()][vehicle.getVehicleX()] = vehicle;
            gameBoard[vehicle.getVehicleY() + 1]
                    [vehicle.getVehicleX()] = vehicle;
            gameBoard[vehicle.getVehicleY() + 2]
                    [vehicle.getVehicleX()] = vehicle;
        }
    }

    /**
     * Checks to see if the specified vehicle can move num space(s) down.
     *
     * @param vehicle The vehicle to check if it can be moved.
     * @param num The number of spaces to move the vehicle.
     * @return True if the vehicle can be moved, false otherwise.
     */
    public boolean canMoveDown(RushHourVehicle vehicle, int num)
    {
        int adjust;
        if(vehicle.getType().equals("car"))
        {
            adjust = 1;
        }
        else
        {
            adjust = 2;
        }
        // move is illegal on the gameBoard
        if(vehicle.getOrientation().equals("h") ||
                vehicle.getVehicleY() + adjust + num >= boardHeight)
        {
            return false;
        }
        // move is illegal because another vehicle is in the path
        int max = 0;
        for(int i = 1; i <= num; ++i)
        {
            if(getVehicleAtLocation(vehicle.getVehicleX(),
                    vehicle.getVehicleY() + adjust + i)
                    != null)
            {
                max = i - 1;
                break;
            }
            ++max;
        }
        // if the desired number of spaces to move is greater than the number
        // of spaces the vehicle as able to move, the vehicle cannot move
        if(num > max)
        {
            return false;
        }

        return true;
    }

    /**
     * Move the specified vehicle down num space(s).
     *
     * @param vehicle The vehicle to be moved down.
     * @param num The number of spaces to move the vehicle.
     * @throws IllegalBoardMoveException The specified vehicle can is not able
     * to move in this direction due to the end of the gameBoard or another
     * vehicle being in the way.
     */
    public void moveDown(RushHourVehicle vehicle, int num)
            throws IllegalBoardMoveException
    {
        // ensure that the vehicle can be moved in the direction
        if(!canMoveDown(vehicle, num))
        {
            throw new IllegalBoardMoveException(vehicle.getColor());
        }

        // move the vehicle on the gameBoard and assign the
        if(vehicle.getType().equals("car"))
        {
            // null the old car location on the game gameBoard
            gameBoard[vehicle.getVehicleY()][vehicle.getVehicleX()] = null;
            gameBoard[vehicle.getVehicleY() + 1][vehicle.getVehicleX()] = null;

            // assign the car to its new location
            vehicle.setVehicleY(vehicle.getVehicleY() + num);

            // move the car on the game gameBoard
            gameBoard[vehicle.getVehicleY()][vehicle.getVehicleX()] = vehicle;
            gameBoard[vehicle.getVehicleY() + 1]
                    [vehicle.getVehicleX()] = vehicle;
        }
        else
        {
            // null the old truck location on the game gameBoard
            gameBoard[vehicle.getVehicleY()][vehicle.getVehicleX()] = null;
            gameBoard[vehicle.getVehicleY() + 1][vehicle.getVehicleX()] = null;
            gameBoard[vehicle.getVehicleY() + 2][vehicle.getVehicleX()] = null;

            // assign the truck to its new location
            vehicle.setVehicleY(vehicle.getVehicleY() + num);

            // move the truck on the game gameBoard
            gameBoard[vehicle.getVehicleY()][vehicle.getVehicleX()] = vehicle;
            gameBoard[vehicle.getVehicleY() + 1]
                    [vehicle.getVehicleX()] = vehicle;
            gameBoard[vehicle.getVehicleY() + 2]
                    [vehicle.getVehicleX()] = vehicle;
        }
    }

    /**
     * Retrieve current game gameBoard.
     *
     * @return The game gameBoard.
     */
    public RushHourVehicle[][] getGameBoard()
    {
        return gameBoard;
    }

    /**
     * Set the game board to the passed in game board.  This can be highgly
     * dangerous.
     *
     * @param gameBoard The new game board.
     */
    public void setGameBoard(RushHourVehicle[][] gameBoard)
    {
        this.gameBoard = gameBoard;
    }

    /**
     * Check for a vehicle at location (x, y) on the game gameBoard.
     *
     * @param x The x-coordinate to check for a vehicle.
     * @param y The y-coordinate to check for a vehicle.
     * @return The vehicle  found (if any) at location (x, y) on the game
     * gameBoard.
     * @throws OffGameBoardException The given coordinates are not on the game
     * gameBoard.
     */
    public RushHourVehicle getVehicleAtLocation(int x, int y)
            throws OffGameBoardException
    {
        try
        {
            return gameBoard[y][x];
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            throw new OffGameBoardException(x, y);
        }
    }

    /**
     * Retrieve the vehicle at index i from the array of vehicles known to be
     * on the gameBoard.
     *
     * @param i The index of the vehicle in the vehicle array.
     * @return The vehicle found at index in in the vehicle array.
     * @throws VehicleDoesNotExistException No vehicle exists at the specified
     * index.
     */
    public RushHourVehicle getVehicleAtIndex(int i)
            throws VehicleDoesNotExistException
    {
        try
        {
            return vehicles.elementAt(i);
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            throw new VehicleDoesNotExistException();
        }
    }

    /**
     * Retrieves the vehicle of the specified color on the gameBoard.
     *
     * @param color The color of the vehicle to be searched for.
     * @return The vehicle object if that color vehicle is found, else null.
     */
    public RushHourVehicle getVehicleByColor(String color)
    {
        for(int i = 0; i < vehicles.size(); ++i)
        {
            if(vehicles.elementAt(i) != null &&
                    vehicles.elementAt(i).getColor().equalsIgnoreCase(color))
            {
                return vehicles.elementAt(i);
            }
        }

        return null;
    }

    /**
     * Retrieve the list of vehicles known to be on the gameBoard.
     *
     * @return The array of vehicles known to be on the gameBoard.
     */
    public Vector<RushHourVehicle> getVehiclesList()
    {
        return vehicles;
    }

    /**
     * Retrieve the number of vehicles on the gameBoard.
     *
     * @return The number of vehicles on the gameBoard.
     */
    public int getNumVehicles()
    {
        return vehicles.size();
    }

    /**
     * Add a vehicle to the game gameBoard at location (x, y).
     *
     * @param type The type of vehicle being added.
     * @param color The color of the vehicle being added.
     * @param orientation The orientation of the vehicle being added.
     * @param x The x-coordinate of the vehicle being added.
     * @param y The y-coordinate of the vehicle being added.
     * @return The added vehicle.
     * @throws InvalidFirstVehicleException The first vehicle must be red.
     * @throws RedCarException The red car must be in the third row so it can
     * exit.
     * @throws InvalidVehicleColorException That given color is already in use
     * by another vehicle.
     * @throws OffGameBoardException The given coordinates are not on the game
     * gameBoard.
     * @throws VehicleOverlapException The vehicle cannot be added at the
     * specified coordinates because it overlaps an already existing vehicle.
     * @throws InvalidVehicleException The vehicle must be of type "car" or
     * "truck."
     */
    public RushHourVehicle addVehicle(String type, String color,
            String orientation, int x, int y)
            throws InvalidFirstVehicleException, RedCarException,
            InvalidVehicleColorException, OffGameBoardException,
            VehicleOverlapException, InvalidVehicleException
    {
        RushHourVehicle vehicle;
        if(type.equals("car"))
        {
            vehicle = addCar(color, orientation, x, y);
        }
        else if(type.equals("truck"))
        {
            vehicle = addTruck(color, orientation, x, y);
        }
        else
        {
            throw new InvalidVehicleException();
        }

        return vehicle;
    }

    /**
     * Add a car to the game gameBoard at location (x, y).
     * 
     * @param color The color of the car being added.
     * @param orientation The orientation of the car being added.
     * @param x The x-coordinate of the car being added.
     * @param y The y-coordinate of the car being added.
     * @return The added car object.
     * @throws InvalidFirstVehicleException The first vehicle must be red.
     * @throws RedCarException The red car must be in the third row so it can
     * exit.
     * @throws InvalidVehicleColorException That given color is already in use
     * by another vehicle.
     * @throws OffGameBoardException The given coordinates are not on the game
     * gameBoard.
     * @throws VehicleOverlapException The vehicle cannot be added at the
     * specified coordinates because it overlaps an already existing vehicle.
     */
    public RushHourVehicle addCar(String color, String orientation, int x,
            int y)
            throws InvalidFirstVehicleException, InvalidVehicleColorException,
            OffGameBoardException, VehicleOverlapException, RedCarException
    {
        // the red car must be the first vehicle added
        if(vehicles.size() == 0 && !color.equalsIgnoreCase("red"))
        {
            throw new InvalidFirstVehicleException();
        }
        // if the vehicle is red, it must be on the third row
        if(color.equalsIgnoreCase("red") && y != 2)
        {
            throw new RedCarException();
        }

        // two vehicles of the same color may not be added to the same gameBoard
        if(getVehicleByColor(color) != null)
        {
            throw new InvalidVehicleColorException();
        }

        try
        {
            // add the car to the game gameBoard
            RushHourVehicle car = new RushHourVehicle(this,
                    vehicles.size() + 1, "car", color, orientation, x, y);
            gameBoard[y][x] = car;
            if(orientation.equals("h"))
            {
                gameBoard[y][x + 1] = car;
            }
            else
            {
                gameBoard[y + 1][x] = car;
            }

            // add the car to the list of vehicles on the game gameBoard
            vehicles.add(car);

            return car;
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            throw new OffGameBoardException(x, y);
        }
    }

    /**
     * Add a truck to the game gameBoard at location (x, y).
     *
     * @param color The color of the car being added.
     * @param orientation The orientation of the car being added.
     * @param x The x-coordinate of the car being added.
     * @param y The y-coordinate of the car being added.
     * @return The added car object.
     * @throws InvalidFirstVehicleException The first vehicle must be red.
     * @throws InvalidVehicleColorException That given color is already in use
     * by another vehicle.
     * @throws OffGameBoardException The given coordinates are not on the game
     * gameBoard.
     * @throws VehicleOverlapException The vehicle cannot be added at the
     * specified coordinates because it overlaps an already existing vehicle.
     */
    public RushHourVehicle addTruck(String color, String orientation, int x,
            int y)
            throws InvalidFirstVehicleException, InvalidVehicleColorException,
            OffGameBoardException, VehicleOverlapException
    {
        // the red car must be the first vehicle added
        if(vehicles.size() == 0)
        {
            throw new InvalidFirstVehicleException();
        }

        // two vehicles of the same color may not be added to the same gameBoard
        if(getVehicleByColor(color) != null)
        {
            throw new InvalidVehicleColorException();
        }

        try
        {
            // add the truck to the game gameBoard
            RushHourVehicle truck = new RushHourVehicle(this,
                    vehicles.size() + 1, "truck", color, orientation, x, y);
            gameBoard[y][x] = truck;
            if(orientation.equals("h"))
            {
                gameBoard[y][x + 1] = truck;
                gameBoard[y][x + 2] = truck;
            }
            else
            {
                gameBoard[y + 1][x] = truck;
                gameBoard[y + 2][x] = truck;
            }

            // add the truck to the list of vehicles on the game gameBoard
            vehicles.add(truck);

            return truck;
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            throw new OffGameBoardException(x, y);
        }
    }

    /**
     * Remove the vehicle of the specified color from the game gameBoard and
     * from the list of vehicles.
     *
     * @param vehicle The vehicle to be removed.
     */
    public void removeVehicle(RushHourVehicle vehicle)
            throws VehicleDoesNotExistException
    {
        if(vehicle == null)
        {
            throw new VehicleDoesNotExistException();
        }
        if(vehicle.getType().equals("car"))
        {
            removeCar(vehicle);
        }
        else
        {
            removeTruck(vehicle);
        }
        // decrement the unique IDs of all vehicles following in the list
        for(int i = vehicle.getUniqueID(); i <= vehicles.size(); ++i)
        {
            // i must be decremented to point at the actual index
            vehicles.get(i - 1).setUniqueID(vehicles.get(i - 1).
                    getUniqueID() - 1);
        }
    }

    /**
     * Remove the specified car from the game gameBoard.
     *
     * @param car The car to be removed.
     */
    public void removeCar(RushHourVehicle car)
    {
        int x = car.getVehicleX();
        int y = car.getVehicleY();

        // remove the car from the game gameBoard
        gameBoard[y][x] = null;
        if(car.getOrientation().equals("h"))
        {
            gameBoard[y][x + 1] = null;
        }
        else
        {
            gameBoard[y + 1][x] = null;
        }

        // remove the car from the list of vehicles
        vehicles.remove(car);
    }

    /**
     * Remove the specified truck from the game gameBoard.
     *
     * @param truck The truck to be removed.
     */
    public void removeTruck(RushHourVehicle truck)
    {
        int x = truck.getVehicleX();
        int y = truck.getVehicleY();

        // remove the car from the game gameBoard
        gameBoard[y][x] = null;
        if(truck.getOrientation().equals("h"))
        {
            gameBoard[y][x + 1] = null;
            gameBoard[y][x + 2] = null;
        }
        else
        {
            gameBoard[y + 1][x] = null;
            gameBoard[y + 2][x] = null;
        }

        // remove the car from the list of vehicles
        vehicles.remove(truck);
    }
}
