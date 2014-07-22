package gui;

import exceptions.RushHourException;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Vector;
import rushhour.RushHourGameBoard;
import rushhour.RushHourVehicle;

/**
 * The domain of the Rush Hour GUI handles intermediate communication not
 * directly with the GUI but imperative to GUI functionality.  These methods
 * can only be called by classes within the gui package and are helper methods
 * to the GUIs functionality.
 *
 * @author Alex Laird
 * @file RushHourDomain.java
 * @version 0.2
 */
public class RushHourDomain
{
    /**
     * Loads a saved game from the given input file.
     *
     * @param file The file to be read from.
     * @param gameBoard The empty game board to load into.
     * @throws RushHourException A characteristic of the vehicle given in
     * the data file is invalid.
     * @throws ArrayIndexOutOfBoundsException The coordinate given in the data
     * file for placing this vehicle is invalid.
     * @throws IOException An error occured when reading the next line from the
     * data file.
     * @throws NumberFormatException A number given in the data file was not an
     * integer greater than zero.
     */
    public static void loadGame(Object file, RushHourGameBoard gameBoard)
            throws RushHourException, IOException, NumberFormatException
    {
        BufferedReader input;
        if(file instanceof File)
        {
            input = new BufferedReader(new FileReader((File) file));
        }
        else
        {
            input = new BufferedReader(new InputStreamReader((
                    (URL) file).openStream()));
        }
        
        String type;
        String color;
        String orientation;
        int x;
        int y;

        // the number of vehicles to be added to the board
        int n = Integer.parseInt(input.readLine());
        if(n < 0)
        {
            throw new NumberFormatException("--Error: Invalid Input File--\n" +
                    "The number given for the number of cars to be placed on " +
                    "the board must be a positive integer.");
        }

        // read and add the rest of the vehicles to the game board
        for(int i = 0; i < n; ++i)
        {
            // read the first car, since it must be the red car
            type = input.readLine();
            color = input.readLine();
            // correct color capitalization
            color = color.substring(0, 1).toUpperCase() + color.substring(1);
            // if the color is lightblue, add space
            if(color.equalsIgnoreCase("lightblue"))
            {
                color = "Light Blue";
            }
            orientation = input.readLine();
            y = Integer.parseInt(input.readLine()) - 1;
            x = Integer.parseInt(input.readLine()) - 1;
            gameBoard.addVehicle(type, color, orientation, x, y);
        }

        input.close();
    }

    /**
     * Saves the current game board to the specified file.
     *
     * @param file The file to be written to.
     * @param gameBoard The game board to be written to the file.
     * @throws IOException An error occured when reading the next line from the
     * data file.
     */
    public static void saveGame(File file, RushHourGameBoard gameBoard)
            throws IOException
    {
        BufferedWriter output = new BufferedWriter(new FileWriter(file));
        output.write(gameBoard.getNumVehicles() + "\n");
        for(int i = 0; i < gameBoard.getNumVehicles(); ++i)
        {
            RushHourVehicle vehicle = gameBoard.getVehicleAtIndex(i);
            output.write(vehicle.getType() + "\n");
            output.write(vehicle.getColor() + "\n");
            output.write(vehicle.getOrientation() + "\n");
            output.write(vehicle.getVehicleY() + 1 + "\n");
            output.write(vehicle.getVehicleX() + 1 + "\n");
        }
        
        output.flush();
        output.close();
    }

    /**
     * Since the instructions will be in reverse order, recurse backward through
     * all instructions and then add them to the vector of directions.
     *
     * @param buildDirections The vector that the directions will be added to for
     * output to the console.
     * @param validMoves The hash map that contains board states for attaining
     * valid moves.
     * @param directionsForValidMoves The hash map that contains directions
     * for valid moves.
     * @param nextState The next state to follow on the hash map.
     */
    public static void recurseThroughSolution(Vector<String> buildDirections,
            HashMap<String, String> validMoves,
            HashMap<String, String> directionsForValidMoves, String nextState)
    {
        String prevState = validMoves.get(nextState);

        if(prevState != null)
        {
                recurseThroughSolution(buildDirections, validMoves,
                        directionsForValidMoves, prevState);

                buildDirections.add(directionsForValidMoves.get(nextState));
        }
    }

    /**
     * Unsimplifies the single-digit (x, y) coordinate retrieved from a
     * coordinate within the game board object to a three-digit (x, y) location
     * that corresponds to the GUI.
     *
     * @param coordinates The single-digit point from the GUI.
     * @param guiWidth The width of the GUI board.
     * @param width The width of the board.
     * param guiHeight the height of the GUI board.
     * @param height The height of the board.
     * @return The unsimplified point.
     */
    public static Point unsimplifyPoint(Point coordinates, int guiWidth, int width,
            int guiHeight, int height)
    {
        int x = (coordinates.x * (guiWidth / width));
        int y = (coordinates.y * (guiHeight / height));

        Point unsimplified = new Point(x, y);
        
        return unsimplified;
    }

    /**
     * Simplifies the three-digit (x, y) coordinate retrieved from a location
     * on the GUI to a single-digit (x, y) coordinate that corresponds to
     * coordinates within the game board object.
     *
     * @param clicked The three-digit point from the GUI.
     * @param guiWidth The width of the GUI board.
     * @param width The width of the board.
     * param guiHeight the height of the GUI board.
     * @param height The height of the board.
     * @return The simplified point.
     */
    public static Point simplifyPoint(Point clicked, int guiWidth, int width,
            int guiHeight, int height)
    {
        int x = (clicked.x / (guiWidth / width));
        int y = (clicked.y / (guiHeight / height));

        Point simplified = new Point(x, y);

        return simplified;
    }
}
