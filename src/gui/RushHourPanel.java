package gui;

import exceptions.OffGameBoardException;
import exceptions.RedCarException;
import exceptions.VehicleOverlapException;
import java.awt.Cursor;
import rushhour.RushHourGameBoard;
import rushhour.RushHourVehicle;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

/**
 * This class implements the GUI handler for constructing and manipulating a
 * visual Rush Hour game board. It is built in a manner that it ties directly
 * into a Rush Hour game board object that allows vehicle manipulations through
 * the specified methods. It is expected that errors will be caught and handled
 * within the Rush Hour game board object and not within this GUI. If they are
 * to be thrown to the GUI, they should be thrown as a known exception for that
 * particular event that the GUI knows to catch and handle.
 * <p>
 * This interface allows the user to add, remove, and move vehicles by dragging.
 * Once the red vehicle is over the exit arrow, the game board is flagged as
 * solved.
 * <p>
 * There are three main modes: Edit mode, Play! mode, and Cheat mode.
 * <p>
 * Edit mode is when most buttons are unlocked and the user is allowed to build
 * the board freely. Though the user can technically move the red vehicle into
 * a winning position to win the game, this is not what this mode is intended
 * for. When the user is done building the board, he or she should click Play!
 * <p>
 * Play! mode is when all board editing buttons are locked, the number of moves
 * made is reset to zero, and the user is expected to attempt to solve the
 * game board from the given state of the board.  The user can click the Edit
 * at any point to return to Edit mode, but this will cause the number of moves
 * made to be inaccurate from that point on. The user can also click the Solve
 * button to run the solveFast() algorithm.
 * <p>
 * Once the user has pressed teh Solve button, a solution is either found or
 * the game board is flagged as unsolvable. If a solution is found, it is not
 * shown to the user immedietly. Rather, the Solve button turns into the Cheat
 * button and the user must click this before being walked through the solution.
 * <p>
 * In Cheat mode, the user can either click the Next button to be automatically
 * walked through the solution, or he or she may click and drag the vehicle
 * they are instructed to move the specified number of spaces in the given
 * direction.  In Cheat mode, all vehicles except the vehicle currently being
 * instructed to move are locked. To exit Cheat mode, click the Stop button to
 * return to normal board solving. However, once you have entered cheat mode,
 * the cheated flag is set and, even upon solving the game board, the user
 * will be known as a cheater.
 * <p>
 * When the Solve button is pressed, two methods can be used to solve it, though
 * solveFast() is significantly more efficient. The Solve button can also call
 * solve() to acheive the same result.
 * 
 * @author Alex Laird
 * @file RushHourPanel.java
 * @version 0.2
 */
public class RushHourPanel extends JPanel implements MouseListener,
        MouseMotionListener
{
    /** The default status message.*/
    protected final String DEFAULT_STATUS = "Add, remove, or drag to setup. " +
            "Then click \"Play!\".";
    /** The default status during a game solve.*/
    protected final String DEFAULT_USER_SOLVING_STATUS = "Drag the vehicles " +
            "to get the red car to the arrow.";
    /** The array of colors a vehicle can be.*/
    private final Object[] VEHICLE_COLORS = new Object[] {"Color:", "Red",
        "Aqua", "Black", "Blue", "Camo", "Green", "Grey", "Light Blue", "Lime",
        "Orange", "Pink", "Purple", "Violet", "White", "Yellow"};
    /** The default cursor icon.*/
    private final Cursor RUSH_HOUR_DEFAULT_CURSOR =
            new Cursor(Cursor.DEFAULT_CURSOR);
    /** The cursor icon to be used when adding or removing a vehicle.*/
    private final Cursor RUSH_HOUR_CROSSHAIR_CURSOR =
            new Cursor(Cursor.CROSSHAIR_CURSOR);
    /** The x offset of the vehicle from the cursor when placing a vehicle.*/
    private final int X_PLACE_OFFSET = 15;
    /** The y offset of the vehicle from the cursor when placing a vehicle.*/
    private final int Y_PLACE_OFFSET = 15;
    /** The horizontal car placer image.*/
    private final RushHourVehicle CAR_H_PLACER = new RushHourVehicle(null, 0,
            "car", "", "h", 0, 0);
    /** The vertical car placer image.*/
    private final RushHourVehicle CAR_V_PLACER = new RushHourVehicle(null, 0,
            "car", "", "v", 0, 0);
    /** The horizontal truck placer image.*/
    private final RushHourVehicle TRUCK_H_PLACER = new RushHourVehicle(null, 0,
            "truck", "", "h", 0, 0);
    /** The vertical truck placer image.*/
    private final RushHourVehicle TRUCK_V_PLACER = new RushHourVehicle(null, 0,
            "truck", "", "v", 0, 0);

    /** The object that can be reverted to that represents the unsolved board
     state at the start of the game.*/
    protected RushHourGameBoard startUnsolved;
    /** The object that can be reverted to that represents the unsolved board
     state at the last press of the Solve button.*/
    protected RushHourGameBoard lastUnsolved;
    /** The main form of the GUI, if it is stand-alone and not applet-based.*/
    private RushHourFrame mainFrame;
    /** The main form of the GUI, if it is the applet-based application.*/
    private RushHourApplet mainApplet;
    /** The object that represents the game board.*/
    protected RushHourGameBoard gameBoard;
    /** The graphic object that represents the game board.*/
    protected RushHourGameBoardPanel boardPanel;
    /** The vehicle (if any) that is selected for dragging.*/
    private RushHourVehicle selectedVehicle;
    /** The list of directions to solve the game board optimally.*/
    private Vector<String> directions;
    /** The current instruction on in the directions*/
    private int instrNum;
    /** Keeps track of the number of moves the user has made since reset.*/
    private int numMovesMade;
    /** The move count from the last unsolved game board.*/
    private int lastUnsolvedMoveCount;
    /** On game solve, the number of spaces a specific vehicle is being moved
     in a given direction.*/
    private Vector<Integer> spacesToMoveTo;
    /** Set true if a vehicle is selected and moved before relased.*/
    private boolean selectedVehicleMoved;
    /** Set true when the red car is moved to the right into solved position.*/
    private boolean solveUnreported;
    /** The x offset of the vehicle from the cursor when it is being dragged.*/
    private int xDragOffset;
    /** The y offset of the vehicle from the cursor when it is being dragged.*/
    private int yDragOffset;
    /** The last good location of a dragged vehicle.*/
    private Point lastGood;
    /** When removing a vehicle, on rollover vehicle images are temporarily
     swapped with the remove icon.  This is the last vehicle rolled over.*/
    private RushHourVehicle lastRolloverVehicle;
    /** The object pointing to the last moved vehicle.*/
    private RushHourVehicle lastMovedVehicle;
    
    /** True if the Add Car button has been pressed and the waiting for the
     * red car to be placed.*/
    private boolean waitForAddRedCar;
    /** True if the Add Car button has been pressed and is waiting for the car
     to be placed.*/
    private boolean waitForAddCar;
    /** True if the Add Truck button has been pressed and is waiting for the
     * truck to be placed.*/
    private boolean waitForAddTruck;
    /** True if the Remove Vehicle button has been pressed and is waiting for
     * the vehicle to be selected.*/
    private boolean waitForRemove;
    /** True if the game board is currently walking through a solution.*/
    private boolean waitForSolve;
    /** True if the program is waiting for the first move after the Solve
     button has been pressed, false otherwise.*/
    private boolean waitForFirstMove;
    /** True if the user cheated, false otherwise.*/
    private boolean cheated;
    /** True if the user is currently userSolving the board on their own.*/
    protected boolean userSolving;
    /** True if the application is waiting for a game to be loaded. This same
     variable is used for waiting for a solve to complete since those two
     actions will never be performed simultaneously.*/
    protected boolean waitForLoad;
    
    /**
     * Constructs the interface for the Rush Hour game, knowing the application
     * is stand-alone.
     */
    public RushHourPanel(RushHourFrame frame)
    {
        mainFrame = frame;
        initComponents();
        initMyComponents();
    }

    /**
     * Constructs the interface for the Rush Hour game, knowing the application
     * is applet-based.
     */
    public RushHourPanel(RushHourApplet applet)
    {
        mainApplet = applet;
        initComponents();
        initMyComponents();
    }

    /**
     * Manual initialization of components is done here.
     */
    private void initMyComponents()
    {
        // initialize variables to their default states
        gameBoard = new RushHourGameBoard();
        boardPanel = new RushHourGameBoardPanel(this, gameBoard);
        lastUnsolved = null;
        lastUnsolvedMoveCount = 0;
        selectedVehicle = null;
        lastMovedVehicle = null;
        if(mainFrame != null)
        {
            mainFrame.isReset = true;
        }
        else
        {
            mainApplet.isReset = true;
        }
        solveUnreported = false;
        waitForAddCar = false;
        waitForAddTruck = false;
        waitForRemove = false;
        waitForSolve = false;
        waitForFirstMove = false;
        waitForLoad = false;
        userSolving = false;
        selectedVehicleMoved = false;
        cheated = false;
        xDragOffset = 0;
        yDragOffset = 0;
        numMovesMade = 0;

        // initialize GUI components
        setLocation(50, 50);
        boardPanel.setOpaque(false);
        boardPanel.setLayout(null);
        boardPanel.setBounds(0, 0, 531, 531);
        boardPanel.addMouseListener(this);
        boardPanel.addMouseMotionListener(this);
        controlPanel.addMouseMotionListener(this);

        // add vehicle placer images
        CAR_H_PLACER.setVisible(false);
        CAR_V_PLACER.setVisible(false);
        TRUCK_H_PLACER.setVisible(false);
        TRUCK_V_PLACER.setVisible(false);
        boardPanel.add(CAR_H_PLACER);
        boardPanel.add(CAR_V_PLACER);
        boardPanel.add(TRUCK_H_PLACER);
        boardPanel.add(TRUCK_V_PLACER);
        boardPanel.repaint();
        mainPanel.add(boardPanel, javax.swing.JLayeredPane.PALETTE_LAYER);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JLayeredPane();
        oneOne = new javax.swing.JLayeredPane();
        oneTwo = new javax.swing.JLayeredPane();
        oneThree = new javax.swing.JLayeredPane();
        oneFour = new javax.swing.JLayeredPane();
        oneFive = new javax.swing.JLayeredPane();
        oneSix = new javax.swing.JLayeredPane();
        twoOne = new javax.swing.JLayeredPane();
        twoTwo = new javax.swing.JLayeredPane();
        twoThree = new javax.swing.JLayeredPane();
        twoFour = new javax.swing.JLayeredPane();
        twoFive = new javax.swing.JLayeredPane();
        twoSix = new javax.swing.JLayeredPane();
        threeOne = new javax.swing.JLayeredPane();
        threeTwo = new javax.swing.JLayeredPane();
        threeThree = new javax.swing.JLayeredPane();
        threeFour = new javax.swing.JLayeredPane();
        threeFive = new javax.swing.JLayeredPane();
        threeSix = new javax.swing.JLayeredPane();
        exitLabel = new javax.swing.JLabel();
        fourOne = new javax.swing.JLayeredPane();
        fourTwo = new javax.swing.JLayeredPane();
        fourThree = new javax.swing.JLayeredPane();
        fourFour = new javax.swing.JLayeredPane();
        fourFive = new javax.swing.JLayeredPane();
        fourSix = new javax.swing.JLayeredPane();
        fiveOne = new javax.swing.JLayeredPane();
        fiveTwo = new javax.swing.JLayeredPane();
        fiveThree = new javax.swing.JLayeredPane();
        fiveFour = new javax.swing.JLayeredPane();
        fiveFive = new javax.swing.JLayeredPane();
        fiveSix = new javax.swing.JLayeredPane();
        sixOne = new javax.swing.JLayeredPane();
        sixThree = new javax.swing.JLayeredPane();
        sixFour = new javax.swing.JLayeredPane();
        sixFive = new javax.swing.JLayeredPane();
        sixSix = new javax.swing.JLayeredPane();
        sixTwo = new javax.swing.JLayeredPane();
        controlPanel = new javax.swing.JPanel();
        addCarButton = new javax.swing.JButton();
        removeVehicleButton = new javax.swing.JButton();
        colorComboBox = new javax.swing.JComboBox();
        addTruckButton = new javax.swing.JButton();
        solveButton = new javax.swing.JButton();
        statusTextField = new javax.swing.JTextField();
        resetButton = new javax.swing.JButton();
        controlSeparator = new javax.swing.JSeparator();
        orientationComboBox = new javax.swing.JComboBox();

        setName("framePanel"); // NOI18N

        oneOne.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        oneOne.setBounds(0, 0, 80, 80);
        mainPanel.add(oneOne, javax.swing.JLayeredPane.DEFAULT_LAYER);

        oneTwo.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        oneTwo.setBounds(90, 0, 80, 80);
        mainPanel.add(oneTwo, javax.swing.JLayeredPane.DEFAULT_LAYER);

        oneThree.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        oneThree.setBounds(180, 0, 80, 80);
        mainPanel.add(oneThree, javax.swing.JLayeredPane.DEFAULT_LAYER);

        oneFour.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        oneFour.setBounds(270, 0, 80, 80);
        mainPanel.add(oneFour, javax.swing.JLayeredPane.DEFAULT_LAYER);

        oneFive.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        oneFive.setBounds(360, 0, 80, 80);
        mainPanel.add(oneFive, javax.swing.JLayeredPane.DEFAULT_LAYER);

        oneSix.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        oneSix.setBounds(450, 0, 80, 80);
        mainPanel.add(oneSix, javax.swing.JLayeredPane.DEFAULT_LAYER);

        twoOne.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        twoOne.setBounds(0, 90, 80, 80);
        mainPanel.add(twoOne, javax.swing.JLayeredPane.DEFAULT_LAYER);

        twoTwo.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        twoTwo.setBounds(90, 90, 80, 80);
        mainPanel.add(twoTwo, javax.swing.JLayeredPane.DEFAULT_LAYER);

        twoThree.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        twoThree.setBounds(180, 90, 80, 80);
        mainPanel.add(twoThree, javax.swing.JLayeredPane.DEFAULT_LAYER);

        twoFour.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        twoFour.setBounds(270, 90, 80, 80);
        mainPanel.add(twoFour, javax.swing.JLayeredPane.DEFAULT_LAYER);

        twoFive.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        twoFive.setBounds(360, 90, 80, 80);
        mainPanel.add(twoFive, javax.swing.JLayeredPane.DEFAULT_LAYER);

        twoSix.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        twoSix.setBounds(450, 90, 80, 80);
        mainPanel.add(twoSix, javax.swing.JLayeredPane.DEFAULT_LAYER);

        threeOne.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        threeOne.setBounds(0, 180, 80, 80);
        mainPanel.add(threeOne, javax.swing.JLayeredPane.DEFAULT_LAYER);

        threeTwo.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        threeTwo.setBounds(90, 180, 80, 80);
        mainPanel.add(threeTwo, javax.swing.JLayeredPane.DEFAULT_LAYER);

        threeThree.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        threeThree.setBounds(180, 180, 80, 80);
        mainPanel.add(threeThree, javax.swing.JLayeredPane.DEFAULT_LAYER);

        threeFour.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        threeFour.setBounds(270, 180, 80, 80);
        mainPanel.add(threeFour, javax.swing.JLayeredPane.DEFAULT_LAYER);

        threeFive.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        threeFive.setBounds(360, 180, 80, 80);
        mainPanel.add(threeFive, javax.swing.JLayeredPane.DEFAULT_LAYER);

        threeSix.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        exitLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/exit_arrow.png"))); // NOI18N
        exitLabel.setBounds(30, 0, 40, 80);
        threeSix.add(exitLabel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        threeSix.setBounds(450, 180, 80, 80);
        mainPanel.add(threeSix, javax.swing.JLayeredPane.DEFAULT_LAYER);

        fourOne.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        fourOne.setBounds(0, 270, 80, 80);
        mainPanel.add(fourOne, javax.swing.JLayeredPane.DEFAULT_LAYER);

        fourTwo.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        fourTwo.setBounds(90, 270, 80, 80);
        mainPanel.add(fourTwo, javax.swing.JLayeredPane.DEFAULT_LAYER);

        fourThree.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        fourThree.setBounds(180, 270, 80, 80);
        mainPanel.add(fourThree, javax.swing.JLayeredPane.DEFAULT_LAYER);

        fourFour.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        fourFour.setBounds(270, 270, 80, 80);
        mainPanel.add(fourFour, javax.swing.JLayeredPane.DEFAULT_LAYER);

        fourFive.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        fourFive.setBounds(360, 270, 80, 80);
        mainPanel.add(fourFive, javax.swing.JLayeredPane.DEFAULT_LAYER);

        fourSix.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        fourSix.setBounds(450, 270, 80, 80);
        mainPanel.add(fourSix, javax.swing.JLayeredPane.DEFAULT_LAYER);

        fiveOne.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        fiveOne.setBounds(0, 360, 80, 80);
        mainPanel.add(fiveOne, javax.swing.JLayeredPane.DEFAULT_LAYER);

        fiveTwo.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        fiveTwo.setBounds(90, 360, 80, 80);
        mainPanel.add(fiveTwo, javax.swing.JLayeredPane.DEFAULT_LAYER);

        fiveThree.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        fiveThree.setBounds(180, 360, 80, 80);
        mainPanel.add(fiveThree, javax.swing.JLayeredPane.DEFAULT_LAYER);

        fiveFour.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        fiveFour.setBounds(270, 360, 80, 80);
        mainPanel.add(fiveFour, javax.swing.JLayeredPane.DEFAULT_LAYER);

        fiveFive.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        fiveFive.setBounds(360, 360, 80, 80);
        mainPanel.add(fiveFive, javax.swing.JLayeredPane.DEFAULT_LAYER);

        fiveSix.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        fiveSix.setBounds(450, 360, 80, 80);
        mainPanel.add(fiveSix, javax.swing.JLayeredPane.DEFAULT_LAYER);

        sixOne.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        sixOne.setBounds(0, 450, 80, 80);
        mainPanel.add(sixOne, javax.swing.JLayeredPane.DEFAULT_LAYER);

        sixThree.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        sixThree.setBounds(180, 450, 80, 80);
        mainPanel.add(sixThree, javax.swing.JLayeredPane.DEFAULT_LAYER);

        sixFour.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        sixFour.setBounds(270, 450, 80, 80);
        mainPanel.add(sixFour, javax.swing.JLayeredPane.DEFAULT_LAYER);

        sixFive.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        sixFive.setBounds(360, 450, 80, 80);
        mainPanel.add(sixFive, javax.swing.JLayeredPane.DEFAULT_LAYER);

        sixSix.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        sixSix.setBounds(450, 450, 80, 80);
        mainPanel.add(sixSix, javax.swing.JLayeredPane.DEFAULT_LAYER);

        sixTwo.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        sixTwo.setBounds(90, 450, 80, 80);
        mainPanel.add(sixTwo, javax.swing.JLayeredPane.DEFAULT_LAYER);

        addCarButton.setText("Add Car");
        addCarButton.setToolTipText("Add a car to the game board");
        addCarButton.setNextFocusableComponent(addTruckButton);
        addCarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addCarButtonActionPerformed(evt);
            }
        });

        removeVehicleButton.setText("Remove Vehicle");
        removeVehicleButton.setToolTipText("Remove the selected vehicle from the game board");
        removeVehicleButton.setEnabled(false);
        removeVehicleButton.setNextFocusableComponent(addCarButton);
        removeVehicleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeVehicleButtonActionPerformed(evt);
            }
        });

        colorComboBox.setModel(new DefaultComboBoxModel(VEHICLE_COLORS));
        colorComboBox.setToolTipText("Select the color of the next vehicle");
        colorComboBox.setEnabled(false);
        colorComboBox.setNextFocusableComponent(orientationComboBox);
        colorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorComboBoxActionPerformed(evt);
            }
        });

        addTruckButton.setText("Add Truck");
        addTruckButton.setToolTipText("Add a truck to the game board");
        addTruckButton.setEnabled(false);
        addTruckButton.setNextFocusableComponent(addCarButton);
        addTruckButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTruckButtonActionPerformed(evt);
            }
        });

        solveButton.setText("Play!");
        solveButton.setToolTipText("Ready to play? Click me!");
        solveButton.setEnabled(false);
        solveButton.setNextFocusableComponent(solveButton);
        solveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                solveButtonActionPerformed(evt);
            }
        });

        statusTextField.setEditable(false);
        statusTextField.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        statusTextField.setText("Load a game, or click \"Add Car\" to build your own.");
        statusTextField.setToolTipText("Status information regarding the current game board");

        resetButton.setText("Reset");
        resetButton.setToolTipText("Reset the game board");
        resetButton.setEnabled(false);
        resetButton.setNextFocusableComponent(addCarButton);
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        controlSeparator.setOrientation(javax.swing.SwingConstants.VERTICAL);

        orientationComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Orientation:", "Horizontal", "Vertical" }));
        orientationComboBox.setToolTipText("Select the orientation of the next vehicle");
        orientationComboBox.setEnabled(false);
        orientationComboBox.setNextFocusableComponent(addCarButton);
        orientationComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                orientationComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout controlPanelLayout = new javax.swing.GroupLayout(controlPanel);
        controlPanel.setLayout(controlPanelLayout);
        controlPanelLayout.setHorizontalGroup(
            controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(controlPanelLayout.createSequentialGroup()
                        .addComponent(addCarButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addTruckButton, javax.swing.GroupLayout.PREFERRED_SIZE, 98, Short.MAX_VALUE)
                        .addGap(8, 8, 8)
                        .addComponent(removeVehicleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 127, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(controlSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 12, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(resetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(solveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(controlPanelLayout.createSequentialGroup()
                        .addComponent(colorComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(orientationComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(statusTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)))
                .addContainerGap())
        );
        controlPanelLayout.setVerticalGroup(
            controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(addTruckButton)
                        .addComponent(removeVehicleButton)
                        .addComponent(addCarButton)
                        .addComponent(solveButton)
                        .addComponent(resetButton))
                    .addComponent(controlSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(colorComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(statusTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(orientationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(8, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(controlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 531, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 531, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(controlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(7, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * The event that occurs when the Add Car button is pressed.  Adds a car
     * to the game board, allowing the user to drop the car at a desired
     * location on the game board. If this is the first (red) car, the car is
     * only allowed to be added to the third row, so the hovering car will only
     * slide along that row.
     * 
     * @param evt The addCar event.
     */
    private void addCarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addCarButtonActionPerformed
        defaultStates();

        // the car is the red car
        if(gameBoard.getNumVehicles() == 0)
        {
            waitForAddRedCar = true;
            RushHourVehicle car = gameBoard.addCar("Red", "h", 0, 2);
            Point point = RushHourDomain.unsimplifyPoint(new Point(
                    car.getVehicleX(), car.getVehicleY()),
                    boardPanel.getWidth(), gameBoard.boardWidth,
                    boardPanel.getHeight(), gameBoard.boardHeight);
            car.setLocation((int) point.getX() + X_PLACE_OFFSET,
                    (int) point.getY() + Y_PLACE_OFFSET);
            boardPanel.add(car);
            boardPanel.goMove(car, point);
            boardPanel.repaint();
            // update GUI
            try
            {
                colorComboBox.setSelectedIndex(1);
            }
            catch(IllegalArgumentException i)
            {
                colorComboBox.setSelectedIndex(0);
            }
            colorComboBox.setSelectedItem("Red");
            orientationComboBox.setSelectedItem("Horizontal");
            statusTextField.setText("Click a square to place the red car.");
            addTruckButton.setEnabled(false);
        }
        // the car is not the red car
        else
        {
            statusTextField.setText("Click a square on the board to place " +
                "the car.");
        }

        // wait for the user to place the car on the game board
        waitForAddCar = true;
        if(orientationComboBox.getSelectedIndex() == 1)
        {
            CAR_H_PLACER.setIcon(new ImageIcon(getClass().getResource(
                            "/images/car_" + colorComboBox.getSelectedItem().
                            toString().toLowerCase().replace(" ", "_") +
                            "_h.png")));
        }
        else
        {
            CAR_V_PLACER.setIcon(new ImageIcon(getClass().getResource(
                            "/images/car_" + colorComboBox.getSelectedItem().
                            toString().toLowerCase().replace(" ", "_") +
                            "_v.png")));
        }
        setCursor(RUSH_HOUR_CROSSHAIR_CURSOR);
        resetButton.setEnabled(true);
        resetButton.setText("Drop");
        resetButton.setToolTipText("Stop placement of this car");
    }//GEN-LAST:event_addCarButtonActionPerformed

    /**
     * The event that occurs when the Remove Vehicle button is pressed.  Allows
     * the user to select a vehicle from the game board for removal.
     *
     * @param evt The removeVehicle event.
     */
    private void removeVehicleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeVehicleButtonActionPerformed
        defaultStates();
        
        statusTextField.setText("Click the vehicle you wish to remove.");
        
        // wait for the user to select a vehicle for removal
        waitForRemove = true;
        setCursor(RUSH_HOUR_CROSSHAIR_CURSOR);
        resetButton.setEnabled(true);
        resetButton.setText("Stop");
        resetButton.setToolTipText("Stop vehicle removal");
    }//GEN-LAST:event_removeVehicleButtonActionPerformed

    /**
     * The event that occurs when the Add Truck button is pressed.  Adds a truck
     * to the game board, allowing the user to drop the truck at a desired
     * location on the game board.
     *
     * @param evt The addTruck event.
     */
    private void addTruckButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTruckButtonActionPerformed
        defaultStates();
        
        statusTextField.setText("Click a square on the board to place " +
                "the truck.");
        
        // wait for the user to place the car on the game board
        waitForAddTruck = true;
        if(orientationComboBox.getSelectedIndex() == 1)
        {
            TRUCK_H_PLACER.setIcon(new ImageIcon(getClass().getResource(
                            "/images/truck_" + colorComboBox.getSelectedItem().
                            toString().toLowerCase().replace(" ", "_") +
                            "_h.png")));
        }
        else
        {
            TRUCK_V_PLACER.setIcon(new ImageIcon(getClass().getResource(
                            "/images/truck_" + colorComboBox.getSelectedItem().
                            toString().toLowerCase().replace(" ", "_") +
                            "_v.png")));
        }
        setCursor(RUSH_HOUR_CROSSHAIR_CURSOR);
        resetButton.setEnabled(true);
        resetButton.setText("Drop");
        resetButton.setToolTipText("Stop placement of this car");
    }//GEN-LAST:event_addTruckButtonActionPerformed

    /**
     * The event that occurs when the color of a vehicle is changed. This allows
     * the user to specify the color of the next vehicle to be added to the
     * game board. This may be switched mid-add.
     * <p>
     * If the first item is selected, which says "Color: " and is obviously not
     * a valid selection, the Add Car and Add Truck buttons are disabled until a
     * valid selection is made.
     *
     * @param evt The colorComboBox event.
     */
    private void colorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorComboBoxActionPerformed
        if(waitForLoad)
        {
            return;
        }
        // the add buttons are only visible if both a color and an orientation
        // have been selected
        if(colorComboBox.getSelectedIndex() != 0 &&
                orientationComboBox.getSelectedIndex() != 0 &&
                gameBoard.getNumVehicles() > 0)
        {
            addCarButton.setEnabled(true);
            addTruckButton.setEnabled(true);
        }
        else
        {
            addCarButton.setEnabled(false);
            addTruckButton.setEnabled(false);
        }

        if(gameBoard.getNumVehicles() > 0)
        {
            if(!userSolving && !waitForFirstMove && !gameBoard.isSolved())
            {
                statusTextField.setText(DEFAULT_STATUS);
            }
            else
            {
                statusTextField.setText(DEFAULT_USER_SOLVING_STATUS);
            }
        }
        else
        {
            statusTextField.setText("Load a game, or click \"Add Car\" to " +
                    "build your own.");
            addCarButton.requestFocus();
        }

        if(waitForAddCar && colorComboBox.getSelectedIndex() != 0)
        {
            if(orientationComboBox.getSelectedIndex() == 1)
            {
                CAR_H_PLACER.setIcon(new ImageIcon(getClass().getResource(
                        "/images/car_" + colorComboBox.getSelectedItem().
                        toString().toLowerCase().replace(" ", "_") +
                        "_h.png")));
            }
            else if(orientationComboBox.getSelectedIndex() == 2)
            {
                CAR_V_PLACER.setIcon(new ImageIcon(getClass().getResource(
                        "/images/car_" + colorComboBox.getSelectedItem().
                        toString().toLowerCase().replace(" ", "_") +
                        "_v.png")));
            }
        }
        if(waitForAddTruck && colorComboBox.getSelectedIndex() != 0)
        {
            if(orientationComboBox.getSelectedIndex() == 1)
            {
                TRUCK_H_PLACER.setIcon(new ImageIcon(getClass().getResource(
                        "/images/truck_" + colorComboBox.getSelectedItem().
                        toString().toLowerCase().replace(" ", "_") +
                        "_h.png")));
            }
            else if(orientationComboBox.getSelectedIndex() == 2)
            {
                TRUCK_V_PLACER.setIcon(new ImageIcon(getClass().getResource(
                        "/images/truck_" + colorComboBox.getSelectedItem().
                        toString().toLowerCase().replace(" ", "_") +
                        "_v.png")));
            }
        }

        if(addCarButton.isEnabled())
        {
            addCarButton.requestFocus();
        }
        else if(orientationComboBox.isEnabled())
        {
            orientationComboBox.requestFocus();
        }
    }//GEN-LAST:event_colorComboBoxActionPerformed

    /**
     * The event that occurs when the Reset button is pressed. If changes have
     * been made to the game board, the user will be prompted before the game
     * board is completely reset.
     * <p>
     * This button also says "Drop," "Stop," and "Edit." It says "Drop" when an
     * add is being performed. If it is pressed at that point, the add is
     * stopped and the vehicle that was being added is not added to the game
     * board.  If it says "Edit," clicking it will return the user from Play!
     * mode back to Edit mode.  If it says "Stop," the user is either in the
     * middle of removing a vehicle, at which point it will stop vehicle
     * removal, or the user is in Cheat mode, at which point pressing it will
     * return them to Play! mode (but marked as a cheater).
     * 
     * @param evt The reset event.
     */
    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        defaultStates();

        // reset the game board
        if(resetButton.getText().equals("Reset"))
        {
            statusTextField.setText("Resetting the game board ...");
            int response = JOptionPane.showOptionDialog(this,
                    "Are you sure you want to reset the game board?",
                    "Reset Game Board", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE, null,
                    new Object[] {"Risk it", "Nope"}, null);
            if(response == JOptionPane.YES_OPTION)
            {
                resetGameBoard();
                if(mainApplet != null)
                {
                    mainApplet.lastIndex = 0;
                    mainApplet.loadGameCombo.setSelectedIndex(0);
                }
            }
            else
            {
                statusTextField.setText(DEFAULT_STATUS);
            }
        }
        // stop walking through a solution
        else if(resetButton.getText().equals("Edit"))
        {
            revertButtonTexts();
            try
            {
                colorComboBox.setSelectedIndex(1);
            }
            catch(IllegalArgumentException i)
            {
                colorComboBox.setSelectedIndex(0);
            }
            orientationComboBox.setSelectedIndex(1);
            colorComboBox.setEnabled(true);
            orientationComboBox.setEnabled(true);
            addCarButton.setEnabled(true);
            addTruckButton.setEnabled(true);
            removeVehicleButton.setEnabled(true);
            removeVehicleButton.setEnabled(true);
            solveButton.setEnabled(true);
            userSolving = false;
            waitForFirstMove = false;
            waitForSolve = false;
            statusTextField.setText(DEFAULT_STATUS);
        }
        else if(resetButton.getText().equals("Stop") &&
                solveButton.getText().equals("Next"))
        {
            resetButton.setText("Edit");
            resetButton.setToolTipText("Stop solving and return to game " +
                    "board setup");
            solveButton.setText("Solve");
            solveButton.setToolTipText("Generate (but do not yet show) the " +
                    "optimal solution from the current board");
            statusTextField.setText(DEFAULT_USER_SOLVING_STATUS);
            userSolving = true;
            waitForSolve = false;
        }
        // drop the current vehicle or stop removal
        else
        {
            if(waitForAddRedCar)
            {
                RushHourVehicle redCar = gameBoard.getVehicleAtIndex(0);
                gameBoard.removeCar(redCar);
                waitForAddRedCar = false;
                boardPanel.remove(redCar);
                boardPanel.repaint();
                solveButton.setEnabled(false);
            }
            if(gameBoard.getNumVehicles() == 0)
            {
                resetButton.setEnabled(false);
            }
            resetButton.setText("Reset");
            resetButton.setToolTipText("Reset the game board");
            statusTextField.setText(DEFAULT_STATUS);
            if(gameBoard.getNumVehicles() == 0)
            {
                colorComboBox.setSelectedIndex(0);
                orientationComboBox.setSelectedIndex(0);
                addCarButton.setEnabled(true);
            }
        }

        addCarButton.requestFocus();
    }//GEN-LAST:event_resetButtonActionPerformed

    /**
     * The event that occurs when the orientation of the vehicle is changed.
     * This allows the user to specify the orientation (horizontal or veritcal)
     * or the next vehicle to be added to the game board.
     * <p>
     * If the first item is selected, which says "Orientation: " and is
     * obviously not a valid selection, the Add Car and Add Truck buttons are
     * disabled until a valid selection is made.
     *
     * @param evt The orientationComboBox event.
     */
    private void orientationComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_orientationComboBoxActionPerformed
        // the add buttons are only visible if both a color and an orientation
        // have been selected
        if(colorComboBox.getSelectedIndex() != 0 &&
                orientationComboBox.getSelectedIndex() != 0 &&
                gameBoard.getNumVehicles() > 0)
        {
            addCarButton.setEnabled(true);
            addTruckButton.setEnabled(true);
        }
        else
        {
            addCarButton.setEnabled(false);
            addTruckButton.setEnabled(false);
        }

        if(gameBoard.getNumVehicles() > 0)
        {
            if(!userSolving && !waitForFirstMove && !gameBoard.isSolved())
            {
                statusTextField.setText(DEFAULT_STATUS);
            }
            else
            {
                statusTextField.setText(DEFAULT_USER_SOLVING_STATUS);
            }
        }
        else
        {
            statusTextField.setText("Load a game, or click \"Add Car\" to " +
                    "build your own.");
        }

        addCarButton.requestFocus();

        if(waitForAddCar && colorComboBox.getSelectedIndex() != 0)
        {
            if(orientationComboBox.getSelectedIndex() == 1)
            {
                CAR_H_PLACER.setIcon(new ImageIcon(getClass().getResource(
                        "/images/car_" + colorComboBox.getSelectedItem().
                        toString().toLowerCase().replace(" ", "_") +
                        "_h.png")));
            }
            else if(orientationComboBox.getSelectedIndex() == 2)
            {
                CAR_V_PLACER.setIcon(new ImageIcon(getClass().getResource(
                        "/images/car_" + colorComboBox.getSelectedItem().
                        toString().toLowerCase().replace(" ", "_") +
                        "_v.png")));
            }
        }
        if(waitForAddTruck && colorComboBox.getSelectedIndex() != 0)
        {
            if(orientationComboBox.getSelectedIndex() == 1)
            {
                TRUCK_H_PLACER.setIcon(new ImageIcon(getClass().getResource(
                        "/images/truck_" + colorComboBox.getSelectedItem().
                        toString().toLowerCase().replace(" ", "_") +
                        "_h.png")));
            }
            else if(orientationComboBox.getSelectedIndex() == 2)
            {
                TRUCK_V_PLACER.setIcon(new ImageIcon(getClass().getResource(
                        "/images/truck_" + colorComboBox.getSelectedItem().
                        toString().toLowerCase().replace(" ", "_") +
                        "_v.png")));
            }
        }

        if(addCarButton.isEnabled())
        {
            addCarButton.requestFocus();
        }
        else if(colorComboBox.isEnabled())
        {
            colorComboBox.requestFocus();
        }
    }//GEN-LAST:event_orientationComboBoxActionPerformed

    /**
     * The event that occurs when the Solve button is pressed.  Calls
     * the solveFast() method on the current game board.  If the game board can
     * be solved, step-by-step instructions will be given to the user in the
     * statusTextField for how to solve the game board in the minimum number of
     * moves possible.
     * <p>
     * The Solve button may also say "Play!," "Next," or "Cheat." If the solve
     * button says "Play!," the user is in Edit mode and pressing it will
     * move the user into Play! mode, disabling all board-construction
     * related buttons. If the solve button says "Cheat," a solution to the
     * game board exists and pressing the button will force to user into
     * Cheat mode, which will walk them through the optimal solution. If the
     * button says "Next," the user is already in Cheat mode and being walked
     * through the solution, so pressing the button will move them to the next
     * step of the solution.
     * <p>
     * This function can also call the solve() method to acheive the same
     * result, but the solveFast() is considerably more efficient.
     *
     * @param evt The solve event.
     */
    private void solveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_solveButtonActionPerformed
        defaultStates();
        
        // solve the game board optimally, if possible
        if(solveButton.getText().equals("Play!"))
        {
            if(gameBoard.isSolved())
            {
                statusTextField.setText("Well, that's boring ...");
            }
            else
            {
                lastUnsolved = null;
                startUnsolved = gameBoard.getBoardFromHash(
                        gameBoard.getHash());
                numMovesMade = 0;
                resetButton.setText("Edit");
                resetButton.setToolTipText("Stop solving and return " +
                        "to game board setup");
                solveButton.setText("Solve");
                solveButton.setToolTipText("Generate (but do not yet " +
                        "show) the optimal solution from the current board");
                statusTextField.setText(DEFAULT_USER_SOLVING_STATUS);
                addCarButton.setEnabled(false);
                addTruckButton.setEnabled(false);
                removeVehicleButton.setEnabled(false);
                colorComboBox.setEnabled(false);
                orientationComboBox.setEnabled(false);
            }
        }
        else if(solveButton.getText().equals("Solve"))
        {
            if(gameBoard.isSolved())
            {
                statusTextField.setText("Well, that's boring ...");
            }
            else
            {
                statusTextField.setText("Finding the optimal solution ...");
                resetButton.setEnabled(false);
                solveButton.setEnabled(false);
                waitForLoad = true;
                Runnable solve = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // create new game board object so we don't solve our
                        // current representation
                        if(startUnsolved == null ||
                                (mainFrame != null && mainFrame.isReset) ||
                                (mainApplet != null && mainApplet.isReset))
                        {
                            startUnsolved = gameBoard.getBoardFromHash(
                                    gameBoard.getHash());
                        }
                        else
                        {
                            lastUnsolved = gameBoard.getBoardFromHash(
                                    gameBoard.getHash());
                            lastUnsolvedMoveCount = numMovesMade;
                        }
                        RushHourGameBoard solveGameBoard = gameBoard.
                                getBoardFromHash(gameBoard.getHash());
                        if(solveGameBoard.solveFast())
                        {
                            // assigned toe the new, solved game board
                            try
                            {
                                solveGameBoard.setBoardFromHash(
                                        solveGameBoard.getWinningState());
                            }
                            catch(NullPointerException c)
                            {
                                statusTextField.setText("Well, that was " +
                                        "pointless ...");
                            }

                            // clear the number of directions needs to be known
                            // before output, so store directions for later
                            // output
                            directions = new Vector<String>();
                            // clear the list of spaces to move to for each
                            // direction
                            spacesToMoveTo = new Vector<Integer>();

                            // walk through the directions and store them in
                            // reverse, since they are backwards
                            RushHourDomain.recurseThroughSolution(directions,
                                    solveGameBoard.getValidMoves(),
                                    solveGameBoard.getDirections(),
                                    solveGameBoard.getWinningState());
                            instrNum = 0;

                            // modify button texts
                            solveButton.setText("Cheat");
                            solveButton.setToolTipText("Walk through the " +
                                    "optimal solution from the current board");
                            resetButton.setText("Edit");
                            resetButton.setToolTipText("Stop solving and " +
                                    "return to game board setup");

                            // disable game board construction components
                            waitForFirstMove = true;
                            resetButton.setEnabled(true);
                            solveButton.setEnabled(true);
                            if(mainFrame != null)
                            {
                                mainFrame.saveMenuItem.setEnabled(false);
                            }
                            if(directions.size() == 1)
                            {
                                statusTextField.setText("Boring. Drag the " +
                                        "red car to the exit.");
                            }
                            else
                            {
                                statusTextField.setText("Solution found. " +
                                        "Click \"Cheat\" to surrender.");
                            }

                            solveButton.requestFocus();
                        }
                        else
                        {
                            resetButton.setText("Reset");
                            resetButton.setToolTipText("Reset the game board");
                            solveButton.setText("Play!");
                            solveButton.setToolTipText("Ready to play? " +
                                    "Click me!");
                            addCarButton.setEnabled(true);
                            addTruckButton.setEnabled(true);
                            removeVehicleButton.setEnabled(true);
                            colorComboBox.setEnabled(true);
                            orientationComboBox.setEnabled(true);
                            orientationComboBox.setSelectedIndex(1);
                            try
                            {
                                colorComboBox.setSelectedIndex(1);
                            }
                            catch(IllegalArgumentException c)
                            {
                                colorComboBox.setSelectedIndex(0);
                            }
                            resetButton.setEnabled(true);
                            solveButton.setEnabled(true);
                            addCarButton.requestFocus();
                            statusTextField.setText("The current game board " +
                                    "is unsolvable.");
                        }
                        waitForLoad = false;
                    }
                };
                new Thread(solve).start();
            }
        }
        // the revert button has been pressed
        else if(solveButton.getText().equals("Revert"))
        {
            String oldStatus = statusTextField.getText();
            statusTextField.setText("Reverting the game board ...");
            if(startUnsolved != null && lastUnsolved != null &&
                    !startUnsolved.boardEqual(lastUnsolved))
            {
                int response = JOptionPane.showOptionDialog(this,
                        "Would you like to revert the game back to the " +
                        "first unsolved\nstate of the game, or to the " +
                        "unsolved state at the last press\nof the " +
                        "Solve button?", "Revert Game Board",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null, new Object[] {"Start", "Last Solve",
                        "Don't Revert"}, null);
                if(response == 0)
                {
                    revertGameBoard(startUnsolved, 0);
                    solveButton.setEnabled(true);
                    lastUnsolved = null;
                    lastUnsolvedMoveCount = 0;
                    if(mainFrame != null)
                    {
                        mainFrame.isReset = true;
                    }
                    else if(mainApplet != null)
                    {
                        mainApplet.isReset = true;
                    }
                }
                else if(response == 1)
                {
                    revertGameBoard(lastUnsolved, lastUnsolvedMoveCount);
                    solveButton.setEnabled(true);
                    if(mainFrame != null)
                    {
                        mainFrame.isReset = true;
                    }
                    else if(mainApplet != null)
                    {
                        mainApplet.isReset = true;
                    }
                }
                else
                {
                    statusTextField.setText(oldStatus);
                }
            }
            else
            {
                int response = JOptionPane.showOptionDialog(this,
                        "Are you sure you want to revert back to the start " +
                        "of the game?\nIt'll be unsolved, of course.",
                        "Revert Game Board", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null,
                        new Object[] {"Revert", "Don't Revert"},
                        null);
                if(response == 0)
                {
                    if(startUnsolved != null)
                    {
                        revertGameBoard(startUnsolved, 0);
                        solveButton.setEnabled(true);
                        lastUnsolved = null;
                        lastUnsolvedMoveCount = 0;
                        if(mainFrame != null)
                        {
                            mainFrame.isReset = true;
                        }
                        else if(mainApplet != null)
                        {
                            mainApplet.isReset = true;
                        }
                    }
                    else
                    {
                        revertGameBoard(lastUnsolved, lastUnsolvedMoveCount);
                        solveButton.setEnabled(true);
                        if(mainFrame != null)
                        {
                            mainFrame.isReset = true;
                        }
                        else if(mainApplet != null)
                        {
                            mainApplet.isReset = true;
                        }
                    }
                }
                else
                {
                    statusTextField.setText(oldStatus);
                }
            }
        }
        // move to the next step of the solved directions
        else
        {
            if(waitForFirstMove)
            {
                int response = JOptionPane.showOptionDialog(this,
                        "WARNING: in most countries, this is considered " +
                        "cheating.\nIf you continue, you partially lose " +
                        "control of solving the game for\nyourself, and you " +
                        "will be walked through the optimal solution.\nAre " +
                        "you sure you wish to continue?", "Cheater!",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null, new Object[] {"I'm A Cheater",
                        "No, I'm Honorable"}, null);
                if(response == JOptionPane.YES_OPTION)
                {
                    resetButton.setText("Stop");
                    resetButton.setToolTipText("Regain some of your honor, " +
                            "and stop cheating");
                    solveButton.setText("Next");
                    solveButton.setToolTipText("Move to the next step of the " +
                                "solution");
                    cheated = true;
                    userSolving = false;
                    waitForSolve = true;
                    waitForFirstMove = false;
                    moveToNextStep();
                }
            }
            else
            {
                try
                {
                    moveToNextStep();
                }
                catch(ArrayIndexOutOfBoundsException c)
                {
                    statusTextField.setText("Well, that was boring ...");
                }
            }
        }
}//GEN-LAST:event_solveButtonActionPerformed

    /**
     * Revert the game board to the last unsolved state prior to hitting the
     * Solve button. The revert button may contain up to two states: the Start
     * state, which is always present, and the Last Solve state.  The Last Solve
     * state reverts the user back to the state of the game board at the point
     * of the Solve button press (if that point differs from the Start state).
     * The Start state is the state at either the game load or when the user
     * pressed the Play! button.
     * <p>
     * If changes have been made to the game board, the user is prompted.
     *
     * @param unsolved The unsolved state to revert to.
     * @param revertMoves The number of moves stored in the revert.
     */
    private void revertGameBoard(final RushHourGameBoard unsolved,
            final int revertMoves)
    {
        revertButtonTexts();
        RushHourGameBoard tempStartUnsolved = startUnsolved;
        RushHourGameBoard tempLastUnsolved = null;
        if(unsolved == lastUnsolved)
        {
            tempLastUnsolved = lastUnsolved;
        }
        resetGameBoard();
        startUnsolved = tempStartUnsolved;
        if(tempLastUnsolved != null)
        {
            lastUnsolved = tempLastUnsolved;
        }
        resetButton.setText("Edit");
        resetButton.setToolTipText("Stop solving and return to game board " +
                "setup");
        solveButton.setText("Solve");
        solveButton.setToolTipText("Generate (but do not yet " +
                        "show) the optimal solution from the current board");
        addCarButton.setEnabled(false);
        resetButton.setEnabled(false);
        solveButton.setEnabled(false);
        waitForLoad = true;
        Runnable revert = new Runnable()
        {
            @Override
            public void run()
            {
                // initliaize interface for game board
                for(int i = 0; i < unsolved.getNumVehicles(); ++i)
                {
                    RushHourVehicle vehicle = unsolved.
                            getVehicleAtIndex(i);
                    // position and add the car to the GUI
                    Point point = RushHourDomain.unsimplifyPoint(
                            new Point(vehicle.getVehicleX(),
                            vehicle.getVehicleY()),
                            boardPanel.getWidth(),
                            unsolved.boardWidth,
                            boardPanel.getHeight(),
                            unsolved.boardHeight);
                    gameBoard.addVehicle(vehicle.getType(), vehicle.getColor(),
                            vehicle.getOrientation(), vehicle.getVehicleX(),
                            vehicle.getVehicleY());
                    vehicle = gameBoard.getVehicleAtIndex(i);
                    boardPanel.add(vehicle);
                    vehicle.setLocation(point);
                    boardPanel.repaint();
                    boardPanel.goMove(vehicle, point);
                    // remove color from combo box
                    colorComboBox.removeItem(vehicle.getColor());
                }
                // update GUI
                resetButton.setEnabled(true);
                solveButton.setEnabled(true);
                statusTextField.setText("The game board has been reverted!");
                numMovesMade = revertMoves;
                waitForFirstMove = true;
                if(mainFrame != null)
                {
                    mainFrame.newMenuItem.setEnabled(true);
                    mainFrame.saveMenuItem.setEnabled(true);
                }
                waitForLoad = false;
            }
        };
        new Thread(revert).start();
    }

    /**
     * Displays the next step in the userSolving of the game board.
     */
    private void moveToNextStep()
    {
        if(instrNum == directions.size())
        {
            resetButton.setText("Edit");
            resetButton.setToolTipText("Stop solving and return to game " +
                    "board setup");
            solveButton.setText("Revert");
            solveButton.setToolTipText("Revert the game board back " +
                    "to its unsolved state");
            RushHourVehicle vehicle = gameBoard.getVehicleAtIndex(0);
            vehicle.setVehicleX(4);
            boardPanel.goMove(vehicle, RushHourDomain.unsimplifyPoint(new Point(
                    vehicle.getVehicleX(), vehicle.getVehicleY()),
                    boardPanel.getWidth(), gameBoard.boardWidth,
                    boardPanel.getHeight(), gameBoard.boardHeight));
            ++numMovesMade;
            if(numMovesMade != 1)
            {
                statusTextField.setText("Congratulations, Cheater! You won " +
                        "in " + numMovesMade + " moves!");
            }
            else
            {
                statusTextField.setText("Congratulations, Cheater! You won " +
                        "in 1 move!");
            }
            waitForSolve = false;
        }
        else
        {
            waitForSolve = true;
            // get the next move from the directions list
            String currentMove = directions.get(instrNum);
            String lastMove = null;
            if(instrNum > 0)
            {
                lastMove = directions.get(instrNum - 1);
            }
            // grab the vehicle number from the move string
            int id = Integer.parseInt(currentMove.substring(
                    currentMove.indexOf("(") + 1, currentMove.indexOf(")")));
            // get the vehicle object and its move
            RushHourVehicle vehicle = gameBoard.getVehicleAtIndex(id - 1);
            String moveDir = currentMove.substring(currentMove.indexOf(")") + 2,
                    currentMove.lastIndexOf(" "));
            // the vehicle is moving in the positive direction
            if(moveDir.equals("right") || moveDir.equals("down"))
            {
                if(vehicle.getOrientation().equals("h"))
                {
                    spacesToMoveTo.add(Integer.parseInt(currentMove.substring(
                            currentMove.lastIndexOf(" ") + 1)) +
                            vehicle.getVehicleX());
                }
                else
                {
                    spacesToMoveTo.add(Integer.parseInt(currentMove.substring(
                            currentMove.lastIndexOf(" ") + 1)) +
                            vehicle.getVehicleY());
                }
            }
            // the vehicle is moving in the negative direction
            else
            {
                if(vehicle.getOrientation().equals("h"))
                {
                    spacesToMoveTo.add(vehicle.getVehicleX() - Integer.parseInt(
                            currentMove.substring(currentMove.lastIndexOf(" ")
                            + 1)));
                }
                else
                {
                    spacesToMoveTo.add(vehicle.getVehicleY() - Integer.parseInt(
                            currentMove.substring(currentMove.lastIndexOf(" ")
                            + 1)));
                }
            }
            // format the move instruction to fit the status text field
            currentMove = "(" + (instrNum + 1) + " of " + directions.size() +
                    ") Move " + currentMove.substring(0,
                    currentMove.indexOf("(")).toLowerCase() +
                    vehicle.getType() + " " + currentMove.substring(
                    currentMove.indexOf(")") + 2);
            // parse vehicle movement instructions for GUI
            if(lastMove != null)
            {
                // grab the vehicle number from the move string
                int lastID = Integer.parseInt(lastMove.substring(
                        lastMove.indexOf("(") + 1, lastMove.indexOf(")")));
                // get the vehicle object
                RushHourVehicle lastVehicle = gameBoard.getVehicleAtIndex(
                        lastID - 1);
                if(lastVehicle.getOrientation().equals("h"))
                {
                    int numSpaces = spacesToMoveTo.get(instrNum - 1) -
                            lastVehicle.getVehicleX();
                    // move left
                    if(numSpaces < 0)
                    {
                        gameBoard.moveLeft(lastVehicle, Math.abs(numSpaces));
                        boardPanel.goMove(lastVehicle,
                                RushHourDomain.unsimplifyPoint(new Point(
                                lastVehicle.getVehicleX(),
                                lastVehicle.getVehicleY()),
                                boardPanel.getWidth(), gameBoard.boardWidth,
                                boardPanel.getHeight(), gameBoard.boardHeight));
                    }
                    // move right
                    else if(numSpaces > 0)
                    {
                        gameBoard.moveRight(lastVehicle, numSpaces);
                        boardPanel.goMove(lastVehicle,
                                RushHourDomain.unsimplifyPoint(new Point(
                                lastVehicle.getVehicleX(),
                                lastVehicle.getVehicleY()),
                                boardPanel.getWidth(), gameBoard.boardWidth,
                                boardPanel.getHeight(), gameBoard.boardHeight));
                    }
                }
                else
                {
                    int numSpaces = spacesToMoveTo.get(instrNum - 1) -
                            lastVehicle.getVehicleY();
                    // move up
                    if(numSpaces < 0)
                    {
                        gameBoard.moveUp(lastVehicle, Math.abs(numSpaces));
                        boardPanel.goMove(lastVehicle,
                                RushHourDomain.unsimplifyPoint(new Point(
                                lastVehicle.getVehicleX(),
                                lastVehicle.getVehicleY()),
                                boardPanel.getWidth(), gameBoard.boardWidth,
                                boardPanel.getHeight(), gameBoard.boardHeight));
                    }
                    // move down
                    else if(numSpaces > 0)
                    {
                        gameBoard.moveDown(lastVehicle, numSpaces);
                        boardPanel.goMove(lastVehicle,
                                RushHourDomain.unsimplifyPoint(new Point(
                                lastVehicle.getVehicleX(),
                                lastVehicle.getVehicleY()),
                                boardPanel.getWidth(), gameBoard.boardWidth,
                                boardPanel.getHeight(), gameBoard.boardHeight));
                    }
                }
                ++numMovesMade;
            }
            // increment for the next instruction
            ++instrNum;

            // check if the last instruction has been reached
            if(instrNum == directions.size())
            {
                statusTextField.setText(currentMove + " for the win!");
            }
            else
            {
                statusTextField.setText(currentMove + ".");
            }
            solveButton.requestFocus();
            boardPanel.repaint();
        }
    }

    /**
     * Sets the default states when nothing is being placed, nothing is being
     * removed, and no forms of solving are going on. Also restores the default
     * cursor.
     */
    protected void defaultStates()
    {
        waitForAddCar = false;
        waitForAddTruck = false;
        waitForRemove = false;
        waitForSolve = false;
        userSolving = false;
        setCursor(RUSH_HOUR_DEFAULT_CURSOR);
    }

    /**
     * Reset the game board object as well as the interface to their default
     * states.
     */
    protected void resetGameBoard()
    {
        // reset variables
        startUnsolved = null;
        lastUnsolved = null;
        selectedVehicle = null;
        directions = null;
        instrNum = 0;
        numMovesMade = 0;
        lastUnsolvedMoveCount = 0;
        spacesToMoveTo = null;
        selectedVehicleMoved = false;
        solveUnreported = false;
        xDragOffset = 0;
        yDragOffset = 0;
        lastGood = null;
        lastRolloverVehicle = null;
        lastMovedVehicle = null;
        waitForAddRedCar = false;
        waitForAddCar = false;
        waitForAddTruck = false;
        waitForRemove = false;
        waitForSolve = false;
        waitForFirstMove = false;
        cheated = false;
        userSolving = false;
        waitForLoad =false;
        startUnsolved = null;
        lastUnsolved = null;
        lastUnsolvedMoveCount = 0;
        // remove all GUI vehicles
        for(int i = 0; i < gameBoard.getNumVehicles(); ++i)
        {
            boardPanel.remove(gameBoard.getVehicleAtIndex(i));
        }
        // remove all vehicle objects
        gameBoard.initGameBoard();
        boardPanel.repaint();

        // reinitialize variables
        if(mainFrame != null)
        {
            mainFrame.isReset = true;
        }
        else
        {
            mainApplet.isReset = true;
        }

        // reset component enabled/disabled states
        addTruckButton.setEnabled(false);
        removeVehicleButton.setEnabled(false);
        colorComboBox.setSelectedIndex(0);
        colorComboBox.setEnabled(false);
        colorComboBox.setModel(new DefaultComboBoxModel(VEHICLE_COLORS));
        orientationComboBox.setSelectedIndex(0);
        orientationComboBox.setEnabled(false);
        resetButton.setEnabled(false);
        resetButton.setText("Reset");
        resetButton.setToolTipText("Reset the game board");
        solveButton.setText("Play!");
        solveButton.setToolTipText("Ready to play? Click me!");
        solveButton.setEnabled(false);
        if(mainFrame != null)
        {
            mainFrame.saveMenuItem.setEnabled(false);
            mainFrame.newMenuItem.setEnabled(false);
        }
        addCarButton.setEnabled(true);

        statusTextField.setText("The game board has been reset!");
    }

    /**
     * Revert button for the Reset and Solve buttons back to their default
     * states, "Reset" and "Play!"
     */
    protected void revertButtonTexts()
    {
        // revert button texts
        solveButton.setText("Play!");
        solveButton.setToolTipText("Ready to play? Click me!");
        resetButton.setText("Reset");
        resetButton.setToolTipText("Reset the game board");

        // enable game board construction components
        addCarButton.setEnabled(true);
        addTruckButton.setEnabled(true);
        colorComboBox.setEnabled(true);
        orientationComboBox.setEnabled(true);
        solveButton.setEnabled(false);
        if(mainFrame != null)
        {
            mainFrame.saveMenuItem.setEnabled(true);
        }
    }

    /**
     * After Add Car, Add Truck, or Remove Vehicle is pressed, the interface
     * waits for a mouse click within the game board to place (or remove) the
     * vehicle.
     * <p>
     * If waitForLoad is set, a mouse click is immedietly ignored.
     *
     * @param e The mouse click event.
     */
    @Override
    public void mouseClicked(MouseEvent e)
    {
        if(waitForLoad)
        {
            return;
        }
        // the interface is waiting for a car to be dropped
        if(waitForAddCar)
        {
            try
            {
                // get the coordinates to place the new vehicle
                Point clickedSimple = RushHourDomain.simplifyPoint(e.getPoint(),
                        boardPanel.getWidth(), gameBoard.boardWidth,
                        boardPanel.getHeight(), gameBoard.boardHeight);
                RushHourVehicle car;
                String color;
                String orientation;
                if(!waitForAddRedCar)
                {
                    // get the color and orientation of the new vehicle
                    color = (String) colorComboBox.getSelectedItem();
                    int index = orientationComboBox.getSelectedIndex();
                    if(index == 1)
                    {
                        orientation = "h";
                    }
                    else
                    {
                        orientation = "v";
                    }
                    // add the vehicle to the game board
                    car = gameBoard.addCar(color, orientation,
                            clickedSimple.x, clickedSimple.y);
                }
                else
                {
                    if(clickedSimple.x == 4)
                    {
                        statusTextField.setText("It's no fun to place the " +
                                "red car there.");
                        return;
                    }
                    car = gameBoard.getVehicleAtIndex(0);
                    car.setVehicleX(clickedSimple.x);
                    color = car.getColor();
                    orientation = car.getOrientation();
                }

            
                // position and add the car to the GUI
                Point point = RushHourDomain.unsimplifyPoint(new Point(
                        car.getVehicleX(), car.getVehicleY()),
                        boardPanel.getWidth(), gameBoard.boardWidth,
                        boardPanel.getHeight(), gameBoard.boardHeight);
                if(!waitForAddRedCar)
                {
                    boardPanel.add(car);
                    car.setLocation(e.getX() + X_PLACE_OFFSET,
                            e.getY() + Y_PLACE_OFFSET);
                }
                else
                {
                    car.setLocation(e.getX() + X_PLACE_OFFSET, car.getY());
                }
                boardPanel.goMove(car, point);
                boardPanel.repaint();
                // update GUI
                colorComboBox.removeItem(color);
                try
                {
                    colorComboBox.setSelectedIndex(1);
                }
                catch(IllegalArgumentException i)
                {
                    colorComboBox.setSelectedIndex(0);
                }
                if(gameBoard.getNumVehicles() > 1)
                {
                    statusTextField.setText("Added the " + color.toLowerCase() +
                            " car.");
                }
                else
                {
                    statusTextField.setText(DEFAULT_STATUS);
                }

                // once the first (red) car has been added, enabled features
                colorComboBox.setEnabled(true);
                orientationComboBox.setEnabled(true);
                addTruckButton.setEnabled(true);
            }
            catch(RedCarException c)
            {
                statusTextField.setText("The red car must be placed in the " +
                        "third row.");
                return;
            }
            catch(VehicleOverlapException c)
            {
                statusTextField.setText("Two vehicles cannot overlap.");
                return;
            }
            catch(OffGameBoardException c)
            {
                statusTextField.setText("Vehicles cannot be placed partially " +
                        "off the board.");
                return;
            }

            // set back to defaults
            if(mainFrame != null)
            {
                mainFrame.isReset = false;
            }
            else
            {
                mainApplet.isReset = false;
            }
            waitForAddCar = false;
            waitForAddRedCar = false;
            setCursor(RUSH_HOUR_DEFAULT_CURSOR);
            resetButton.setEnabled(false);
            addTruckButton.requestFocus();
            resetButton.setText("Reset");
            resetButton.setToolTipText("Reset the game board");
        }

        // the interface is waiting for a truck to be dropped
        if(waitForAddTruck)
        {
            // get the coordinates to place the new vehicle
            Point clickedSimple = RushHourDomain.simplifyPoint(e.getPoint(),
                    boardPanel.getWidth(), gameBoard.boardWidth,
                    boardPanel.getHeight(), gameBoard.boardHeight);
            // get the color and orientation of the new vehicle
            String color = (String) colorComboBox.getSelectedItem();
            int index = orientationComboBox.getSelectedIndex();
            String orientation;
            if(index == 1)
            {
                orientation = "h";
            }
            else
            {
                orientation = "v";
            }

            try
            {
                // add the vehicle to the game board
                RushHourVehicle truck = gameBoard.addTruck(color, orientation,
                        clickedSimple.x, clickedSimple.y);
                // position and add the truck to the GUI
                Point point = RushHourDomain.unsimplifyPoint(new Point(
                        truck.getVehicleX(), truck.getVehicleY()),
                        boardPanel.getWidth(), gameBoard.boardWidth,
                        boardPanel.getHeight(), gameBoard.boardHeight);
                boardPanel.add(truck);
                truck.setLocation(e.getX() + X_PLACE_OFFSET,
                        e.getY() + Y_PLACE_OFFSET);
                boardPanel.goMove(truck, point);
                boardPanel.repaint();
                // update GUI
                colorComboBox.removeItem(color);
                try
                {
                    colorComboBox.setSelectedIndex(1);
                }
                catch(IllegalArgumentException i)
                {
                    colorComboBox.setSelectedIndex(0);
                }
                statusTextField.setText("Added the " + color.toLowerCase() +
                        " truck.");
            }
            catch(VehicleOverlapException c)
            {
                statusTextField.setText("Two vehicles cannot overlap.");
                return;
            }
            catch(OffGameBoardException c)
            {
                statusTextField.setText("Vehicles cannot be placed partially " +
                        "off the board.");
                return;
            }

            // set back to defaults
            if(mainFrame != null)
            {
                mainFrame.isReset = false;
            }
            else
            {
                mainApplet.isReset = false;
            }
            waitForAddTruck = false;
            setCursor(RUSH_HOUR_DEFAULT_CURSOR);
            resetButton.setEnabled(false);
            addCarButton.requestFocus();
            resetButton.setText("Reset");
            resetButton.setToolTipText("Reset the game board");
        }
        
        // the interface is waiting for a vehicle to be selected for removal
        if(waitForRemove)
        {
            // get the coordinates to place the new vehicle
            Point clickedSimple = RushHourDomain.simplifyPoint(e.getPoint(),
                    boardPanel.getWidth(), gameBoard.boardWidth,
                    boardPanel.getHeight(), gameBoard.boardHeight);
            // get the vehicle at the selected location
            RushHourVehicle vehicle = gameBoard.getVehicleAtLocation(
                    clickedSimple.x, clickedSimple.y);
            // a vehicle was not found at the location clickedSimple
            if(vehicle == null)
            {
                statusTextField.setText("There is not a vehicle there.");
                return;
            }
            // remove the selected vehicle
            else
            {
                // do not allow the red car to be removed
                if(vehicle.getColor().equalsIgnoreCase("red"))
                {
                    statusTextField.setText("The red car cannot be removed.");
                    return;
                }
                else
                {
                    // get color of the vehicle
                    String color = vehicle.getColor();
                    // remove vehicle from game board
                    gameBoard.removeVehicle(vehicle);
                    // remove vehicle from GUI
                    boardPanel.remove(vehicle);
                    boardPanel.repaint();
                    // add the color back to the color combo box so a vehicle of
                    // that color may be added again to the GUI
                    colorComboBox.addItem(color);
                    colorComboBox.setSelectedItem(color);
                    statusTextField.setText("Removed the " +
                            color.toLowerCase() + " " + vehicle.getType() +
                            ".");
                }
            }

            // set back to defaults
            if(mainFrame != null)
            {
                mainFrame.isReset = false;
            }
            else
            {
                mainApplet.isReset = false;
            }
            waitForRemove = false;
            setCursor(RUSH_HOUR_DEFAULT_CURSOR);
            resetButton.setEnabled(false);
            addCarButton.requestFocus();
            resetButton.setText("Reset");
            resetButton.setToolTipText("Reset the game board");
        }
        
        if(gameBoard.getNumVehicles() <= 1)
        {
            // reset component enabled/disabled states
            if(gameBoard.getNumVehicles() == 1)
            {
                resetButton.setEnabled(true);
                if(mainFrame != null)
                {
                    mainFrame.newMenuItem.setEnabled(true);
                }
            }
            removeVehicleButton.setEnabled(false);
            solveButton.setEnabled(false);
            if(mainFrame != null)
            {
                mainFrame.saveMenuItem.setEnabled(false);
            }
        }
        else
        {
            // set components enabled/disabled states
            if(solveButton.getText().equals("Play!"))
            {
                removeVehicleButton.setEnabled(true);
            }
            resetButton.setEnabled(true);
            if(!gameBoard.isSolved())
            {
                solveButton.setEnabled(true);
            }
            if(mainFrame != null)
            {
                mainFrame.saveMenuItem.setEnabled(true);
                mainFrame.newMenuItem.setEnabled(true);
            }
        }

        if(colorComboBox.getSelectedIndex() == 0 &&
                gameBoard.getNumVehicles() > 0)
        {
            addCarButton.setEnabled(false);
            addTruckButton.setEnabled(false);
        }

        if(userSolving || waitForFirstMove)
        {
            removeVehicleButton.setEnabled(false);
        }

        // hide placer images
        CAR_H_PLACER.setVisible(false);
        CAR_V_PLACER.setVisible(false);
        TRUCK_H_PLACER.setVisible(false);
        TRUCK_V_PLACER.setVisible(false);
    }

    /**
     * When the mouse is clicked, the interface grabs the vehicle (if any) that
     * is located at the cursor coordinates and sets it as the selectedVehicle.
     * However, if waitForSolve is set, that selectedVehicle may only be the
     * vehicle specified in the next solution instruction, therefore if it is
     * NOT the vehicle specifiec in the next solution instruction,
     * selectedVehicle remains (or is set to) null.
     * <p>
     * If waitForLoad is set, a mouse press is immedietly ignored.
     *
     * @param e The mouse press event.
     */
    @Override
    public void mousePressed(MouseEvent e)
    {
        if(waitForLoad)
        {
            return;
        }
        Point clickedSimple = RushHourDomain.simplifyPoint(e.getPoint(),
                boardPanel.getWidth(), gameBoard.boardWidth,
                boardPanel.getHeight(), gameBoard.boardHeight);
        // get the vehicle at the clicked location
        try
        {
            selectedVehicle = gameBoard.getVehicleAtLocation(clickedSimple.x,
                    clickedSimple.y);
        }
        catch(OffGameBoardException c)
        {
            selectedVehicle = null;
        }
        if(selectedVehicle != null)
        {
            xDragOffset = e.getX() - selectedVehicle.getX();
            yDragOffset = e.getY() - selectedVehicle.getY();
            lastGood = selectedVehicle.getLocation();
        }
        if(waitForSolve)
        {
           if(instrNum > 0)
           {
               String currentMove = directions.get(instrNum - 1);
               // grab the vehicle number from the move string
               int id = Integer.parseInt(currentMove.substring(
                       currentMove.indexOf("(") + 1,
                       currentMove.indexOf(")")));
               // get the vehicle object
               RushHourVehicle vehicle = gameBoard.getVehicleAtIndex(id - 1);
               if(selectedVehicle != vehicle)
               {
                   selectedVehicle = null;
               }
           }
           else
           {
               selectedVehicle = null;
           }
        }
    }

    /**
     * When the mouse click is released, the selected vhiecle (if any) is
     * snapped to its new closest (and valid) location on the game board.
     * <p>
     * If the new location snapped to differs from the old location of the
     * vehicle prior to dragging, the number of moves made is incremented.
     * However, if the same vehicle is moved twice in a row, it only counts as
     * one move, so the number of moves made will not be incremented the second
     * time.
     * <p>
     * If the vehicle moved was the red car and the location it was moved to
     * is the winning position, the winning message is displayed the the game
     * board is set as solved.  Additionally, the Solve button text then changes
     * to "Revert."
     * <p>
     * If waitForLoad is set, a mouse release is immedietly ignored.
     *
     * @param e The mouse release event.
     */
    @Override
    public void mouseReleased(MouseEvent e)
    {
        if(waitForLoad)
        {
            return;
        }
        if(!(waitForAddCar || waitForAddTruck || waitForRemove))
        {
            // if the board was solved prior to the move (if move happened)
            boolean wasSolved = gameBoard.isSolved();
            // snap the selected vehicle to its new location and unselect
            if(selectedVehicle != null)
            {
                Point vehicleFrontLoc = RushHourDomain.simplifyPoint(
                        selectedVehicle.getLocation(), boardPanel.getWidth(),
                        gameBoard.boardWidth, boardPanel.getHeight(),
                        gameBoard.boardHeight);

                // the vehicle is horizontal
                if(selectedVehicle.getOrientation().equals("h"))
                {
                    Point vehicleRearLoc = RushHourDomain.simplifyPoint(
                            new Point(selectedVehicle.getX() +
                            selectedVehicle.getVehicleIconLength(),
                            selectedVehicle.getY()), boardPanel.getWidth(),
                            gameBoard.boardWidth, boardPanel.getHeight(),
                            gameBoard.boardHeight);
                    int moveLeft = vehicleFrontLoc.x -
                            selectedVehicle.getVehicleX();
                    int moveRight = vehicleRearLoc.x -
                            selectedVehicle.getVehicleX() -
                            selectedVehicle.getVehicleLength() + 1;
                    if(moveLeft < 0 &&
                            gameBoard.canMoveLeft(selectedVehicle,
                            Math.abs(moveLeft)))
                    {
                        gameBoard.moveLeft(selectedVehicle, Math.abs(moveLeft));
                        selectedVehicleMoved = true;
                    }
                    else if(moveRight > 0 &&
                            gameBoard.canMoveRight(selectedVehicle, moveRight))
                    {
                        solveUnreported =
                                gameBoard.moveRight(selectedVehicle, moveRight);
                        selectedVehicleMoved = true;
                    }
                }
                // the vehicle is vertical
                else
                {
                    Point vehicleRearLoc = RushHourDomain.simplifyPoint(
                            new Point(selectedVehicle.getX(),
                            selectedVehicle.getY() +
                            selectedVehicle.getVehicleIconLength()),
                            boardPanel.getWidth(), gameBoard.boardWidth,
                            boardPanel.getHeight(), gameBoard.boardHeight);
                    int moveUp = vehicleFrontLoc.y -
                            selectedVehicle.getVehicleY();
                    int moveDown = vehicleRearLoc.y -
                            selectedVehicle.getVehicleY() -
                            selectedVehicle.getVehicleLength() + 1;
                    if(moveUp < 0 &&
                            gameBoard.canMoveUp(selectedVehicle,
                            Math.abs(moveUp)))
                    {
                        gameBoard.moveUp(selectedVehicle, Math.abs(moveUp));
                        selectedVehicleMoved = true;
                    }
                    else if(moveDown > 0 &&
                            gameBoard.canMoveDown(selectedVehicle, moveDown))
                    {
                        gameBoard.moveDown(selectedVehicle, moveDown);
                        selectedVehicleMoved = true;
                    }
                }

                boardPanel.goMove(selectedVehicle,
                        RushHourDomain.unsimplifyPoint(
                        new Point(selectedVehicle.getVehicleX(),
                        selectedVehicle.getVehicleY()), boardPanel.getWidth(),
                        gameBoard.boardWidth, boardPanel.getHeight(),
                        gameBoard.boardHeight));
            }
            // the dragged vehicle moved to another square
            if(selectedVehicleMoved)
            {
                if(waitForFirstMove && !waitForSolve &&
                        !solveButton.getText().equals("Play!"))
                {
                    solveButton.setText("Solve");
                    solveButton.setToolTipText("Generate (but do not yet " +
                        "show) the optimal solution from the current board");
                    waitForFirstMove = false;
                    userSolving = true;
                }
                
                // if we're waitForSolve, numMovesMade is already incremented
                if(!waitForSolve && (lastMovedVehicle != selectedVehicle ||
                        numMovesMade == 0))
                {
                    ++numMovesMade;
                }
                lastMovedVehicle = selectedVehicle;
                if(mainFrame != null)
                {
                    mainFrame.isReset = false;
                }
                else
                {
                    mainApplet.isReset = false;
                }
                
                if(!gameBoard.isSolved() && gameBoard.getNumVehicles() > 1)
                {
                    solveButton.setEnabled(true);
                }
                // if the red vehicle is dragged away from winning
                if(!gameBoard.isSolved() && wasSolved)
                {
                    if(!solveButton.getText().equals("Play!"))
                    {
                        solveButton.setText("Solve");
                        solveButton.setToolTipText("Generate (but do not " +
                                "yet show) the optimal solution from the " +
                                "current board");
                    }
                    numMovesMade = 0;
                }
            }

            if(selectedVehicle != null &&
                    selectedVehicle != gameBoard.getVehicleAtIndex(0) &&
                    directions != null && instrNum == directions.size() &&
                    !gameBoard.isSolved() &&
                    !solveButton.getText().equals("Play!"))
            {
                solveButton.setText("Solve");
                solveButton.setToolTipText("Generate (but do not yet " +
                        "show) the optimal solution from the current board");
            }

            selectedVehicle = null;

            if(gameBoard.isSolved() && solveUnreported)
            {
                if(lastUnsolved != null || startUnsolved != null)
                {
                    solveButton.setEnabled(true);
                    resetButton.setText("Edit");
                    resetButton.setToolTipText("Stop solving and return to " +
                            "game board setup");
                    solveButton.setText("Revert");
                    solveButton.setToolTipText("Revert the game board back " +
                            "to its unsolved state");
                }
                else
                {
                    solveButton.setEnabled(false);
                }
                
                if(numMovesMade != 1)
                {
                    if(!cheated)
                    {
                        statusTextField.setText("Congratulations! You won in " +
                                numMovesMade + " moves!");
                    }
                    else
                    {
                        statusTextField.setText("Congratulations, Cheater! " +
                                "You won in " + numMovesMade + " moves!");
                    }
                }
                else
                {
                    if(!cheated)
                    {
                        statusTextField.setText("Congratulations! You won in 1 " +
                                "move!");
                    }
                    else
                    {
                        statusTextField.setText("Congratulations, Cheater! " +
                                "You won in 1 move!");
                    }
                }
                solveUnreported = false;
            }
            else if(gameBoard.getNumVehicles() > 0 && !waitForSolve)
            {
                if(!userSolving && !waitForFirstMove && !gameBoard.isSolved()
                        || solveButton.getText().equals("Play!"))
                {
                    if(solveButton.getText().equals("Solve"))
                    {
                        statusTextField.setText(DEFAULT_USER_SOLVING_STATUS);
                    }
                    else
                    {
                        statusTextField.setText(DEFAULT_STATUS);
                    }
                }
                else if(!gameBoard.isSolved())
                {
                    statusTextField.setText(DEFAULT_USER_SOLVING_STATUS);
                }
            }
        }
        if(waitForSolve && selectedVehicleMoved)
        {
            moveToNextStep();
        }
        selectedVehicleMoved = false;
    }

    /**
     * Unused, but overriden since it's abstract.
     *
     * @param e The mouse entering area of backPanel event.
     */
    @Override
    public void mouseEntered(MouseEvent e) {}

    /**
     * Unused, but overriden since it's abstract.
     * 
     * @param e The mouse exiting area of backPanel event.
     */
    @Override
    public void mouseExited(MouseEvent e) {}

    /**
     * If the mouse is moved within the boardPanel and a car is being added or
     * removed, the mouse coordinates are retrieved and appropriate visual
     * measures are taken.  For instance, if a vehicle is being added, the
     * vehicle image placeholder is attached to the mouse coordinates at each
     * move. If a vehicle is being removed, the vehicle at the specified mouse
     * coordinates (if any) is partially faded in order to emulate selection.
     * <p>
     * If waitForLoad is set, a mouse movement is immedietly ignored.
     *
     * @param e The mouse moving event.
     */
    @Override
    public void mouseMoved(MouseEvent e)
    {
        if(waitForLoad)
        {
            return;
        }
        if(e.getComponent() == boardPanel)
        {
            if(waitForAddRedCar)
            {
                RushHourVehicle redCar = gameBoard.getVehicleAtIndex(0);
                redCar.setLocation(e.getX() - xDragOffset, redCar.getY());
            }
            // display the car placeholder when car is being placed
            else if(waitForAddCar)
            {
                // position the placholder image to follow cursor until clicked
                if(orientationComboBox.getSelectedIndex() == 1)
                {
                    CAR_H_PLACER.setBounds(e.getX() - X_PLACE_OFFSET,
                            e.getY() - Y_PLACE_OFFSET,
                            CAR_H_PLACER.getVehicleIconLength(),
                            CAR_H_PLACER.getVehicleIconWidth());
                    CAR_H_PLACER.setVisible(true);
                }
                else
                {
                    CAR_V_PLACER.setBounds(e.getX() - X_PLACE_OFFSET,
                            e.getY() - Y_PLACE_OFFSET,
                            CAR_V_PLACER.getVehicleIconWidth(),
                            CAR_V_PLACER.getVehicleIconLength());
                    CAR_V_PLACER.setVisible(true);
                }
            }
            // display the truck placeholder when truck is being placed
            else if(waitForAddTruck)
            {
                // position the placholder image to follow cursor until clicked
                if(orientationComboBox.getSelectedIndex() == 1)
                {
                    TRUCK_H_PLACER.setBounds(e.getX() - X_PLACE_OFFSET,
                            e.getY() - Y_PLACE_OFFSET,
                            TRUCK_H_PLACER.getVehicleIconLength(),
                            TRUCK_H_PLACER.getVehicleIconWidth());
                    TRUCK_H_PLACER.setVisible(true);
                }
                else
                {
                    TRUCK_V_PLACER.setBounds(e.getX() - X_PLACE_OFFSET,
                            e.getY() - Y_PLACE_OFFSET,
                            TRUCK_V_PLACER.getVehicleIconWidth(),
                            TRUCK_V_PLACER.getVehicleIconLength());
                    TRUCK_V_PLACER.setVisible(true);
                }
            }
            else if(waitForRemove)
            {
                // temporarily swap the vehicle image with the remove image
                Point simplePoint = RushHourDomain.simplifyPoint(e.getPoint(),
                        boardPanel.getWidth(), gameBoard.boardWidth,
                        boardPanel.getHeight(), gameBoard.boardHeight);
                RushHourVehicle vehicleAtPoint;
                try
                {
                    vehicleAtPoint = gameBoard.getVehicleAtLocation(
                            simplePoint.x, simplePoint.y);
                }
                catch(OffGameBoardException c)
                {
                    vehicleAtPoint = null;
                }

                // if this is the first vehicle rollover, set  lastRollover
                if(lastRolloverVehicle == null)
                {
                    lastRolloverVehicle = vehicleAtPoint;
                }

                // if continually rolling over same vehicle, set remove image
                if(vehicleAtPoint != null &&
                        vehicleAtPoint == lastRolloverVehicle)
                {
                    if(!vehicleAtPoint.getColor().equalsIgnoreCase("red"))
                    {
                        vehicleAtPoint.setComposite(0.50f);
                        vehicleAtPoint.repaint();
                        lastRolloverVehicle = vehicleAtPoint;
                    }
                }
                // once roll off vehicle, reset image
                else if(lastRolloverVehicle != null)
                {
                    lastRolloverVehicle.resetIconImage();
                    lastRolloverVehicle.repaint();
                    lastRolloverVehicle = null;
                }
            }
            else
            {
                // hide the placholder once it's set
                CAR_H_PLACER.setVisible(false);
                CAR_V_PLACER.setVisible(false);
                TRUCK_H_PLACER.setVisible(false);
                TRUCK_V_PLACER.setVisible(false);
            }
        }
        else
        {
            // hide the placeholder image when cursor goes to controlPanel
            CAR_H_PLACER.setVisible(false);
            CAR_V_PLACER.setVisible(false);
            TRUCK_H_PLACER.setVisible(false);
            TRUCK_V_PLACER.setVisible(false);
            if(lastRolloverVehicle != null)
            {
                lastRolloverVehicle.resetIconImage();
                lastRolloverVehicle = null;
            }
        }
    }

    /**
     * So long as a vehicle is not being added or removed and selectedVehicle
     * (set on mousePressed) is not null, the selected vehicle will be dragged
     * in the horizontal or vertical (depending on the orientation of the
     * vehicle) direction of the vehicle as long as that drag is valid. If that
     * drag is not valid, the vehicle will be left at the last valid drag
     * location until the mouse is released.
     * <p>
     * If waitForLoad is set, a mouse drag is immedietly ignored.
     *
     * @param e The mouse drag event.
     */
    @Override
    public void mouseDragged(MouseEvent e)
    {
        if(waitForLoad)
        {
            return;
        }
        // do not allow if we're in the middle of another event
        if(!(waitForAddCar || waitForAddTruck || waitForRemove))
        {
            // a vehicle is currently being dragged
            if(selectedVehicle != null)
            {
                Point coordinates = e.getPoint();
                // the vehicle is horizontal
                if(selectedVehicle.getOrientation().equals("h"))
                {
                    selectedVehicle.setLocation(coordinates.x -
                                xDragOffset, selectedVehicle.getY());
                    if(boardPanel.vehicleLocationIsValid(selectedVehicle))
                    {
                        lastGood = selectedVehicle.getLocation();
                    }
                    else
                    {
                        selectedVehicle.setLocation(lastGood);
                    }
                }
                // the vehicle is vertical
                else
                {
                    selectedVehicle.setLocation(selectedVehicle.getX(),
                                coordinates.y - yDragOffset);
                    if(boardPanel.vehicleLocationIsValid(selectedVehicle))
                    {
                        lastGood = selectedVehicle.getLocation();
                    }
                    else
                    {
                        selectedVehicle.setLocation(lastGood);
                    }
                }
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JButton addCarButton;
    protected javax.swing.JButton addTruckButton;
    protected javax.swing.JComboBox colorComboBox;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JSeparator controlSeparator;
    private javax.swing.JLabel exitLabel;
    private javax.swing.JLayeredPane fiveFive;
    private javax.swing.JLayeredPane fiveFour;
    private javax.swing.JLayeredPane fiveOne;
    private javax.swing.JLayeredPane fiveSix;
    private javax.swing.JLayeredPane fiveThree;
    private javax.swing.JLayeredPane fiveTwo;
    private javax.swing.JLayeredPane fourFive;
    private javax.swing.JLayeredPane fourFour;
    private javax.swing.JLayeredPane fourOne;
    private javax.swing.JLayeredPane fourSix;
    private javax.swing.JLayeredPane fourThree;
    private javax.swing.JLayeredPane fourTwo;
    private javax.swing.JLayeredPane mainPanel;
    private javax.swing.JLayeredPane oneFive;
    private javax.swing.JLayeredPane oneFour;
    private javax.swing.JLayeredPane oneOne;
    private javax.swing.JLayeredPane oneSix;
    private javax.swing.JLayeredPane oneThree;
    private javax.swing.JLayeredPane oneTwo;
    protected javax.swing.JComboBox orientationComboBox;
    protected javax.swing.JButton removeVehicleButton;
    protected javax.swing.JButton resetButton;
    private javax.swing.JLayeredPane sixFive;
    private javax.swing.JLayeredPane sixFour;
    private javax.swing.JLayeredPane sixOne;
    private javax.swing.JLayeredPane sixSix;
    private javax.swing.JLayeredPane sixThree;
    private javax.swing.JLayeredPane sixTwo;
    protected javax.swing.JButton solveButton;
    protected javax.swing.JTextField statusTextField;
    private javax.swing.JLayeredPane threeFive;
    private javax.swing.JLayeredPane threeFour;
    private javax.swing.JLayeredPane threeOne;
    private javax.swing.JLayeredPane threeSix;
    private javax.swing.JLayeredPane threeThree;
    private javax.swing.JLayeredPane threeTwo;
    private javax.swing.JLayeredPane twoFive;
    private javax.swing.JLayeredPane twoFour;
    private javax.swing.JLayeredPane twoOne;
    private javax.swing.JLayeredPane twoSix;
    private javax.swing.JLayeredPane twoThree;
    private javax.swing.JLayeredPane twoTwo;
    // End of variables declaration//GEN-END:variables
}
/**
 * The extensions filter for Rush Hour game board files.  Files should be of
 * the extension .ldf, .rsh, or .dat to be valid.
 *
 * @author Alex Laird
 * @version 0.1
 */
class ExtensionFileFilter extends FileFilter
{
    /** Description of the extension filter.*/
    String description;
    /** Extensions accepted in the filter.*/
    String extensions[];

    /**
     * Construct the extension filter with only one accepted extension.
     *
     * @param description Description of the extension filter.
     * @param extensions Accepted extensions.
     */
    public ExtensionFileFilter(String description, String extension)
    {
        this(description, new String[] { extension });
    }

    /**
     * Construct the extension filter with an array of accepted extensions.
     *
     * @param description Description of the extension filter.
     * @param extensions Accepted extensions.
     */
    public ExtensionFileFilter(String description, String extensions[])
    {
        if (description == null)
        {
            this.description = extensions[0];
        }
        else
        {
            this.description = description;
        }
        
        this.extensions = (String[]) extensions.clone();
        
        for (int i = 0, n = this.extensions.length; i < n; i++)
        {
            this.extensions[i] = this.extensions[i].toLowerCase();
        }
    }

    /**
     * Check to see if the specified file is of the accepted extension.
     *
     * @param file The file to be checked.
     * @return True if the file is acceptable, false otherwise.
     */
    @Override
    public boolean accept(File file)
    {
        // always allow directories
        if (file.isDirectory())
        {
            return true;
        }
        // allow only those extensions found in the accepted extensions array
        else
        {
            String path = file.getAbsolutePath().toLowerCase();
            for (int i = 0, n = extensions.length; i < n; i++)
            {
                String extension = extensions[i];
                if((path.endsWith(extension) && (path.charAt(path.length() -
                        extension.length() - 1)) == '.'))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Retrieve the extension filter description.
     *
     * @return The description of the extension filter.
     */
    @Override
    public String getDescription()
    {
        return description;
    }
}
