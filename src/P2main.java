import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * CS4102 Computer Graphics - P2 3D Rendering:
 *
 * @author 170004680
 */
public class P2main {


    // Message to show is program executed incorrectly.
    private static final String USAGE_MSG = "java P2main <path_to_data_dir> <num_reference_faces> [-fs|-is|-wf] [-l|nl]";
    // Type of 3D face rendering to use.
    public static int renderingType = 0; // 0 = Flat Shading (default), 1 = Interpolation Shading, -1 = Wire-frame.
    public static boolean isLighting = true; // True means add directional lighting, false means no lighting (evenly lit).

    /**
     * Entry point to the CS4102 P2 program.
     *
     * @param args args[0] - Path to data directory.
     *             args[1] - Number of reference faces (between 3 and 199 inclusive).
     *             args[2] - '-fs' = Flat Shading, '-is' = Interpolation Shading, '-wf' = Wire-frame.
     *             args[3] - '-l' = Directional Lighting (Default), '-nl' = No Lighting (Even face lighting).
     */
    public static void main(String[] args) {

        // Check correct number of arguments given.
        if (args.length < 2 || args.length > 4) {
            System.out.println(USAGE_MSG);
            System.exit(-1); // Error status.
        }

        // Check given data directory is valid.
        String dataDirPath = args[0];
        File dataDir = new File(dataDirPath);
        if (!dataDir.exists() || !dataDir.isDirectory()) {
            System.out.println("Error: Provided Data Directory Is Invalid.\n" + USAGE_MSG);
            System.exit(-1); // Error status.
        }

        // Check number of reference faces specified for use is valid (i.e., >= 3 and <= 199).
        int numReferenceFaces = 0;
        try {

            numReferenceFaces = Integer.parseInt(args[1]);
            if (numReferenceFaces < 3 || numReferenceFaces > 199) {
                System.out.println("Error: Provided Number Of Reference Faces Must Be Between 3 And 199 Inclusive.\n" + USAGE_MSG);
                System.exit(-1); // Error status.
            }

        } catch (NumberFormatException e) {
            System.out.println("Error: Provided Number Of Reference Faces Is Invalid.\n" + USAGE_MSG);
            System.exit(-1); // Error status.
        }

        ArrayList<String> argsList = new ArrayList<>(Arrays.asList(args));

        // Determine shading technique to use.
        if (argsList.contains("-wf")) {
            renderingType = -1;
        } else if (argsList.contains("-is")) {
            renderingType = 1;
        }

        // Determines whether to use directional lighting for 3D face rendering.
        if (argsList.contains("-nl")) {
            isLighting = false;
        }

        // Create JFrame for the UI interface.
        JFrame jFrame = new JFrame("CS4102 Computer Graphics P2 - 3D Rendering:");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // On close, exit program.
        jFrame.setLocation(0, 0);
        jFrame.setSize(700, 750);
        jFrame.getContentPane().add(new MainUIPanel(dataDir, numReferenceFaces, 700, 750));
        jFrame.setVisible(true);

    } // main().


} // P2main().
