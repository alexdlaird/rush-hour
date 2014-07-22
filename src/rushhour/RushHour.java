package rushhour;

import exceptions.RushHourException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Vector;

/**
 * This class reads in a Rush Hour game from input files specified in the
 * dataFiles array.  Input files should be formatted in the following manner:
 * <br>-The first line contains a positive integer which represents the number
 * of vehicles that will be added to the game board.
 * <br>-That number of vehicles should then follow in the input file.
 * <br>-Each vehicle must contain a 1) type (car or truck), 2) color,
 *  3) orientation (h or v), 4) y-coordinate (1-boardHeight),
 *  5) x-coordinate (1-boardWidth). Each of these characteristics must be on
 *  a separate line.
 * <p>
 * The game board is created and each vehicle specified in the input file is
 * added to the board.  No two vehicles can have the same color.  Once the board
 * is successfully constructed, the solve() method on the game board is called
 * to optimally find a solution to the given game board in the minimum number of
 * moves.
 * <p>
 * If no solution is found, false is returned from solve() and this class will
 * respond accordingly, outputting failure to the console.  If a solution is
 * found, the initial game board, a list of moves required to solve minimally,
 * and the winning game board will be displayed.  If specified, this class will
 * output a visual representation of the game board after each move.  To specify
 * this visual representation, pass true instead of false into the
 * outputSolution() method.
 * 
 * @author Alex Laird
 * @author Kevin Bender
 * @file RushHour.java
 * @version 0.1
 */
public class RushHour
{
    /** The unique ID should always have two digits.*/
    private final NumberFormat NUMBER_FORMATTER = new DecimalFormat("00");
    /**The array of data files to be read. Add all input files to the array.*/
    private final String[] DATA_FILES = new String[] {"Levels/Level1.rsh",
                                                      "Levels/Level2.rsh",
                                                      "Levels/Level35.rsh",
                                                      "Levels/Level36.rsh"};
    /** The input reader for the file given by the inputName variable.*/
    private BufferedReader input;
    /** The array that represents the game board.*/
    private RushHourGameBoard gameBoard;

    /**
     * Parses the input file and place the cars and trucks into their
     * respective locations on a game board object, which is returned.
     *
     * @param input The reader which reads from the input file.
     * @param start The first line read from the set of data.
     * @return The parsed representation of the game board from the input file
     * as an object of a game board.
     * @throws RushHourException A characteristic of the vehicle given in
     * the data file is invalid.
     * @throws ArrayIndexOutOfBoundsException The coordinate given in the data
     * file for placing this vehicle is invalid.
     * @throws IOException An error occured when reading the next line from the
     * data file.
     * @throws NumberFormatException A number given in the data file was not an
     * integer greater than zero.
     */
    private RushHourGameBoard parseInputFile(BufferedReader input, String start)
            throws RushHourException, IOException, NumberFormatException
    {
        String type;
        String color;
        String orientation;
        int x;
        int y;

        // the number of vehicles to be added to the board
        int n = Integer.parseInt(start);
        if(n < 0)
        {
            throw new NumberFormatException("--Error: Invalid Input File--\n" +
                    "The number given for the number of cars to be placed on " +
                    "the board must be a positive integer.");
        }

        // initialize the game board
        RushHourGameBoard gameBoardBuild = new RushHourGameBoard();

        // read and add the rest of the vehicles to the game board
        for(int i = 0; i < n; ++i)
        {
            // read the first car, since it must be the red car
            type = input.readLine();
            color = input.readLine();
            orientation = input.readLine();
            y = Integer.parseInt(input.readLine()) - 1;
            x = Integer.parseInt(input.readLine()) - 1;
            gameBoardBuild.addVehicle(type, color, orientation, x, y);
        }

        return gameBoardBuild;
    }

    /**
     * Prints a rough output of the game board to the console. Displays the
     * unique identification number for each car at each location on the board.
     *
     * @param gameBoard The game board to output to the console.
     */
    private void outputGameBoard(RushHourGameBoard gameBoard)
    {
        System.out.println(" -- -- -- -- -- --");
        
        for(int i = 0; i < gameBoard.boardWidth; ++i)
        {
            System.out.print("|");
            
            for(int j = 0; j < gameBoard.boardHeight; ++j)
            {
                RushHourVehicle vehicle = gameBoard.getVehicleAtLocation(j, i);
                if(j != gameBoard.boardWidth - 1)
                {
                    if(vehicle != null)
                    {
                        System.out.print(vehicle + "|");
                    }
                    else
                    {
                        System.out.print("  |");
                    }
                }
                else
                {
                    if(i == 2 && j == gameBoard.boardWidth - 1)
                    {
                        if(vehicle != null)
                        {
                            System.out.print(vehicle);
                        }
                        else
                        {
                            System.out.print("  ");
                        }
                    }
                    else
                    {
                        if(vehicle != null)
                        {
                            System.out.print(vehicle + "|");
                        }
                        else
                        {
                            System.out.print("  |");
                        }
                    }
                }
            }

            System.out.println("\n -- -- -- -- -- --");
        }
    }

    /**
     * Since the instructions will be in reverse order, recurse backward through
     * all instructions and then add them to the vector of directions.
     *
     * @param directions The vector that the directions will be added to for
     * output to the console.
     * @param hash The hash representation at each direction (for outputting a
     * game board to the console).
     * @param validMoves The hash map that contains board states for attaining
     * valid moves.
     * @param directionsForValidMoves The hash map that contains directions
     * for valid moves.
     * @param nextState The next state to follow on the hash map.
     */
    private void getOrderedDirections(Vector<String> directions,
            Vector<String> hash, HashMap<String, String> validMoves,
            HashMap<String, String> directionsForValidMoves, String nextState)
    {
        String prevState = validMoves.get(nextState);
        
        if(prevState != null)
        {
                getOrderedDirections(directions, hash, validMoves,
                        directionsForValidMoves, prevState);

                directions.add(directionsForValidMoves.get(nextState));
                hash.add(nextState);
        }
    }

    /**
     * Output the optimal solution for solving the game for the given data set.
     * A visual representation of the game board will be displayed at the
     * beginning and end of all moves.  To output a visual representation of the
     * board after each move, the final parameter should be set to true.
     *
     * @param validMoves The hash map that contains board states for attaining
     * valid moves.
     * @param directionsForValidMoves The hash map that contains directions
     * for valid moves.
     * @param initialState The initial hash state of the board.
     * @param winningState The winning hash state of the board.
     * @param outputBoard True if a visual representation of the game board
     * should be output after each move, false otherwise.
     */
    private void outputSolution(HashMap<String, String> validMoves,
            HashMap<String, String> directionsForValidMoves,
            String initialState, String winningState, boolean outputBoard)
    {
        System.out.println("::Initial Board::");
        outputGameBoard(gameBoard.getBoardFromHash(initialState));
        System.out.println("Key: ");
        for(int i = 0; i < gameBoard.getVehiclesList().size(); ++i)
        {
            if(i != gameBoard.getVehiclesList().size() - 1)
            {
                System.out.print(NUMBER_FORMATTER.format(i + 1) + "=" +
                        gameBoard.getVehicleAtIndex(i).getColor() + ", ");
            }
            else
            {
                System.out.println(NUMBER_FORMATTER.format(i + 1) + "=" +
                        gameBoard.getVehicleAtIndex(i).getColor());
            }
        }

        // the number of directions needs to be known before output, so store
        // directions for later output
        Vector<String> directions = new Vector<String>();
        // when the board is output eat time, a hash representation must exist
        // for that direction to create the board
        Vector<String> hash = new Vector<String>();
        // walk through the directions and store them in reverse, since they
        // are backwards
        getOrderedDirections(directions, hash, validMoves,
                directionsForValidMoves, winningState);

        System.out.println("\n::Solution::");
        System.out.println("Number of Moves: " + directions.size());
        for(int i = 0; i < directions.size(); ++i)
        {
            System.out.print("\nMove " + directions.get(i));
            if(outputBoard)
            {
                System.out.println(":");
                if(i == directions.size() - 1)
                {
                    System.out.println("::Winning Board::");
                }
                outputGameBoard(gameBoard.getBoardFromHash(hash.
                        get(i)));
            }
        }
        if(!outputBoard)
        {
            System.out.println("\n\n::Winning Board::");
            outputGameBoard(gameBoard.getBoardFromHash(winningState));
        }

        System.out.println(" -----------------\n\n");
    }

    /**
     * Output if the given board is unsolvable.
     *
     * @param initialState The initial state of the board.
     */
    private void outputUnsolvable(String initialState)
    {
        System.out.println("::Initial Board::");
        outputGameBoard(gameBoard.getBoardFromHash(initialState));
        System.out.println("Key: ");
        for(int i = 0; i < gameBoard.getVehiclesList().size(); ++i)
        {
            if(i != gameBoard.getVehiclesList().size() - 1)
            {
                System.out.print(NUMBER_FORMATTER.format(i + 1) + "=" +
                        gameBoard.getVehicleAtIndex(i).getColor() + ", ");
            }
            else
            {
                System.out.println(NUMBER_FORMATTER.format(i + 1) + "=" +
                        gameBoard.getVehicleAtIndex(i).getColor());
            }
        }
        System.out.println("--THE GIVEN BOARD IS UNSOLVABLE--");
        System.out.println(" -----------------\n\n");
    }

    /**
     * Calls the execution methods.
     */
    private void run()
    {
        try
        {
            // continue reading data files as long as they are in the list
            for(int i = 0; i < DATA_FILES.length; ++i)
            {
                // instantiate the input reader and the first line
                input = new BufferedReader(new FileReader(DATA_FILES[i]));
                String start;
                
                // continue grabbing problem sets until the end of the file
                while((start = input.readLine()) != null)
                {
                    System.out.println("--" + DATA_FILES[i] + "--");
                    
                    gameBoard = parseInputFile(input, start);

                    if(!gameBoard.solveFast())
                    {
                        outputUnsolvable(gameBoard.getInitialState());
                    }
                    else
                    {
                        // assigned toe the new, solved game board
                        gameBoard.setBoardFromHash(
                                gameBoard.getWinningState());

                        outputSolution(gameBoard.getValidMoves(),
                                gameBoard.getDirections(),
                                gameBoard.getInitialState(),
                                gameBoard.getWinningState(), false);
                    }
                }

                input.close();
            }
        }
        catch(FileNotFoundException e)
        {
            System.out.println("--Error: File Not Found--");
            System.out.println("The data file could not be found. Check the" +
                    "file name and try again.");
        }
        catch(IOException e)
        {
            System.out.println("--Error: Unknown Input/Ouput Fault--");
            System.out.println("An unknown error has occured when trying to "
                    + "read from the data file.");
        }
        catch(RushHourException e) {}
    }
    
    /**
     * Responsible for executing the application, then immedietly calls the
     * non-static run().
     *
     * @param args The command-line arguments.
     */
    public static void main(String[] args)
    {
        // get out of static land
        RushHour rushHour = new RushHour();
        rushHour.run();
    }
}
