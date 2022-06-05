import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.Timer;

/**
 * MainUIPanel: Handles the UI for the 2D implementation (basic specification).
 *
 * @author 17004680
 */
class MainUIPanel extends JPanel {


    private final Face3DReader face3DReader; // Reader for 3D faces from data files.
    private Face3D currentRenderedFace; // Current face being rendered.
    private final int numReferenceFaces; // Number of reference faces to use.
    // Set of points defining the polygon with reference faces as vertices. Let index i be the i list in referenceFaces.
    private final ArrayList<Point2D> referenceFacesPolygonPoints;

    private final int POINT_SIZE = 8; // Size of drawn points.
    private Point2D clickedPoint; // User clicked point which determines the interpolation between reference faces.
    private boolean clickedPointIsReference; // Whether the clicked point is on eof the polygon vertices.

    private final FaceUIPanel faceUIPanel; // Used for rendering 3D faces.

    private Timer autoRotateTimer = null; // Timer used for auto-rotation of 3D face model.


    /**
     * Constructor: Create reference face polygon and add a mouse listener to get clicked points for interpolation.
     *
     * @param dataDir           Directory containing the data files.
     * @param numReferenceFaces Number of reference faces, which are simply lists of triangles.
     * @param width             Width of window being constructed.
     * @param height            Height of window being constructed.
     */
    public MainUIPanel(File dataDir, int numReferenceFaces, int width, int height) {

        this.face3DReader = new Face3DReader(dataDir); // Reader for 3D faces.
        this.currentRenderedFace = null;
        this.numReferenceFaces = numReferenceFaces; // Number of reference faces.
        referenceFacesPolygonPoints = calculateReferenceFacePolygonPoints(width, height); // Vertices of reference face polygon.

        this.setLayout(new FlowLayout(FlowLayout.CENTER));
        this.setBackground(Color.white);
        this.addMouseListener(new PointHandler(this)); // Add mouse listener for clicks.

        // Add UI labels to advise user of how to use the GUI.
        String mainLabel = "<html>A " + numReferenceFaces + "-sided regular polygon has been drawn.\n" +
                " The vertices of the polygon represent reference 3D face models.\n</html>";
        this.add(new JLabel(mainLabel));
        String noteLabel = "<html>NOTE: Interpolation of n reference faces has noticeable delay for larger n (~20s for 199 reference faces).\n</html>";
        this.add(new JLabel(noteLabel));
        String note2Label = "<html>NOTE: Interpolation (Gouraud) shading has significant delay (order of minutes).\n</html>";
        this.add(new JLabel(note2Label));
        String pointCLickControlLabel = "<html><font color='blue'>Click</font> within the polygon to interpolate a face from the reference faces.\n</html>";
        this.add(new JLabel(pointCLickControlLabel));
        String pointCLickControlLabel2 = "<html><font color='blue'>Clicking</font> a vertex of the polygon will draw the corresponding reference face.\n</html>";
        this.add(new JLabel(pointCLickControlLabel2));

        // Add rotation buttons:

        String rotationControlLabel = "<html><font color='blue'>Click</font> to rotate the 3D rendered face about the Y-axis.\n</html>";
        this.add(new JLabel(rotationControlLabel));

        // User clicks button to rotate 3D face clockwise around Y-axis.
        Button leftRotateButton = new Button("Left"); // Left rotation button (around Y-axis).
        ActionListener leftRotateListener = this::leftRotateAction;
        leftRotateButton.addActionListener(leftRotateListener);
        this.add(leftRotateButton);

        // Automatic rotation of the model in the rightward direction.
        Button autoRotateButton = new Button("Auto");
        ActionListener autoRotateListener = this::autoRotateAction;
        autoRotateButton.addActionListener(autoRotateListener);
        this.add(autoRotateButton);

        // User clicks button to rotate 3D face counter-clockwise around Y-axis.
        Button rightRotateButton = new Button("Right"); // Right rotation button (around Y-axis).
        ActionListener rightRotateListener = this::rightRotateAction;
        rightRotateButton.addActionListener(rightRotateListener);
        this.add(rightRotateButton);

        // Create panel for drawing 3D faces in.
        JFrame jFrame = new JFrame("Face Rendering Window:");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // On close, exit program.
        jFrame.setLocation(width + 5, 0);
        jFrame.setSize(width, height);
        this.faceUIPanel = new FaceUIPanel();
        jFrame.getContentPane().add(this.faceUIPanel);
        jFrame.setVisible(true);

    } // MainUIPanel().


    // UI Painting Methods:

    /**
     * Determines how the current panel is to be painted.
     *
     * @param graphics Graphics object from java.awt.
     */
    @Override
    protected void paintComponent(Graphics graphics) {

        super.paintComponent(graphics);

        Graphics2D graphics2D = (Graphics2D) graphics;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Add antialiasing.

        // Drawing the regular polygon of reference faces.
        drawReferenceFacePolygon(graphics2D, referenceFacesPolygonPoints);

        // Draw clicked point if valid.
        drawClickedPoint(graphics2D);

    } // paintComponent().

    /**
     * Draw the reference face polygon in the window.
     *
     * @param graphics2D Graphics object used to draw in the window.
     */
    private void drawReferenceFacePolygon(Graphics2D graphics2D, ArrayList<Point2D> referenceFacePolygonPoints) {

        // Draw the vertices, representing the reference face points.
        for (Point2D currPoint : referenceFacePolygonPoints) {

            double x = currPoint.getX() - ((double) POINT_SIZE / 2);
            double y = currPoint.getY() - ((double) POINT_SIZE / 2);

            // Set colour appropriately - want to chow clicked points as blue and a reference may be clicked.
            if (clickedPoint == currPoint) {
                graphics2D.setColor(Color.BLUE);
            } else {
                graphics2D.setColor(Color.BLACK);
            }

            graphics2D.fill(new Ellipse2D.Double(x, y, POINT_SIZE, POINT_SIZE)); // Add point to canvas.

        }

        graphics2D.setColor(Color.BLACK); // Colour to use for drawing the polygon edges.

        // Draw lines between every pair of vertices.
        Point2D prevPoint = null;
        for (Point2D currPoint : referenceFacePolygonPoints) {

            if (prevPoint != null) {
                graphics2D.drawLine((int) prevPoint.getX(), (int) prevPoint.getY(), (int) currPoint.getX(), (int) currPoint.getY());
            }

            prevPoint = currPoint;

        }

        // Draw line from last point to the first.
        Point2D firstPoint = referenceFacePolygonPoints.get(0);
        graphics2D.drawLine((int) prevPoint.getX(), (int) prevPoint.getY(), (int) firstPoint.getX(), (int) firstPoint.getY());

    } // drawReferenceFacePolygon().

    /**
     * Draw the clicked point if valid in the window.
     *
     * @param graphics2D Graphics object used to draw in the window.
     */
    private void drawClickedPoint(Graphics2D graphics2D) {

        if (this.clickedPoint != null && !clickedPointIsReference) {
            double x = this.clickedPoint.getX() - ((double) POINT_SIZE / 2);
            double y = this.clickedPoint.getY() - ((double) POINT_SIZE / 2);
            graphics2D.setColor(Color.BLUE);
            graphics2D.fill(new Ellipse2D.Double(x, y, POINT_SIZE, POINT_SIZE)); // Add point to canvas.
        }

    } // drawClickedPoint().


    // Utility Functions:

    /**
     * Rotate rendered 3D face leftward around the Y-axis.
     *
     * @param event Event triggering the action.
     */
    private void leftRotateAction(ActionEvent event) {
        faceUIPanel.rotationAction(true);
    } // leftRotateAction().

    /**
     * Rotate rendered 3D face rightward around the Y-axis.
     *
     * @param event Event triggering the action.
     */
    private void rightRotateAction(ActionEvent event) {
        faceUIPanel.rotationAction(false);
    } // rightRotateAction().

    /**
     * Automatic rotation of the face model in the rightward direction every second (i.e., clockwise to y-axis).
     *
     * @param event Event triggering the action.
     */
    private void autoRotateAction(ActionEvent event) {

        // Interpolation shading to slow for auto-rotation.
        if (P2main.renderingType == 1) { // Interpolation Shading.
            String warnMsg = "Interpolation (Gouraud) shading is too slow for automatic rotation.";
            JOptionPane.showMessageDialog(null, warnMsg, "Info: ", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // If not already auto-rotating, then auto-rotate.
        if (autoRotateTimer == null) {

            autoRotateTimer = new Timer();

            autoRotateTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    faceUIPanel.rotationAction(false);
                }
            }, 0, 1000);

        } else { // If auto-rotating, turn it off.

            autoRotateTimer.cancel();
            autoRotateTimer = null;

        }

    } // autoRotateAction().

    /**
     * Given a clicked point in the window, calculate the relative weights
     *
     * @param clickedPoint User clicked point.
     */
    public void setClickedPoint(Point clickedPoint) {

        // If clickedPoint is reference point, then do not interpolate, draw reference point.
        Point2D clickedPointIsReference = isClickedPointAReference(clickedPoint, referenceFacesPolygonPoints);
        if (clickedPointIsReference != null) {

            this.clickedPoint = clickedPointIsReference;
            this.clickedPointIsReference = true;
            this.repaint(); // Update UI.

            int referenceFaceIndex = referenceFacesPolygonPoints.indexOf(clickedPointIsReference);
            currentRenderedFace = face3DReader.getReferenceFace(referenceFaceIndex + 1); // Get references face.
            faceUIPanel.render3DFace(currentRenderedFace); // Draw the reference face.

        } else {

            // Ensure that the clicked point is inside of the polygon.
            Polygon referenceFacePolygon = new Polygon();
            for (Point2D currPoint : referenceFacesPolygonPoints) {
                referenceFacePolygon.addPoint(
                        (int) currPoint.getX() - (POINT_SIZE / 2),
                        (int) currPoint.getY() - (POINT_SIZE / 2));
            }
            boolean valid = referenceFacePolygon.contains(clickedPoint);

            this.clickedPoint = (valid) ? clickedPoint : null;
            this.clickedPointIsReference = false;
            this.repaint(); // Update UI.

            if (valid) {

                // Get interpolation weights to use based on the clicked point in the canvas.
                ArrayList<Double> interpolationWeights = calculateInterpolationWeights(clickedPoint, referenceFacesPolygonPoints);
                // Get interpolated face using weights.
                currentRenderedFace = face3DReader.getInterpolatedFace(interpolationWeights);
                faceUIPanel.render3DFace(currentRenderedFace); // Draw the interpolated face.

            }

        }

    } // setClickedPoint().

    /**
     * Check if the clicked point is one of the reference points.
     *
     * @param clickedPoint                User clicked point.
     * @param referenceFacesPolygonPoints Reference face polygon points.
     * @return Reference point if found, false otherwise.
     */
    private Point2D isClickedPointAReference(Point clickedPoint, ArrayList<Point2D> referenceFacesPolygonPoints) {

        // Check all reference points and check if point contains the clicked point.
        for (Point2D currReferencePoint : referenceFacesPolygonPoints) {

            // Cast for better contains method which takes drawn point size into account.
            Point3D currRefPoint3D = new Point3D(currReferencePoint.getX(), currReferencePoint.getY(), POINT_SIZE);

            if (currRefPoint3D.contains(clickedPoint, POINT_SIZE)) {
                return currReferencePoint;
            }

        }

        return null;

    } // clickedPointIsReference().

    /**
     * Calculate the set of points needed to draw a regular n-sided polygon in the window. where n is the number of
     * reference faces to use as specified to the program.
     *
     * @param width  Width of the window the polygon is being drawn in.
     * @param height Height of the window the polygon is being drawn in.
     * @return Array list of point comprising the vertices of the polygon.
     */
    private ArrayList<Point2D> calculateReferenceFacePolygonPoints(double width, double height) {

        int radius = 270;

        // Set of points defining the polygon of reference faces.
        ArrayList<Point2D> referenceFacePolygonPoints = new ArrayList<>();

        // Get set of points at the vertices of the reference face polygon.
        for (int i = 0; i < numReferenceFaces; i++) {

            double ratio = (double) i / numReferenceFaces;
            // Rotate polygon for aesthetic on the window.
            double rotation = -(Math.PI / 2);
            // Center points around the center of the window instead of the origin.
            double x = radius * Math.cos((2 * Math.PI * ratio) + rotation) + (0.5 * width);
            double y = radius * Math.sin((2 * Math.PI * ratio) + rotation) + (0.57 * height);
            Point2D referenceFacePolygonPoint = new Point2D.Double(x, y);
            // Add point to the polygon.
            referenceFacePolygonPoints.add(referenceFacePolygonPoint);

        }

        return referenceFacePolygonPoints;

    } // calculateReferenceFacePolygonPoints().

    /**
     * Calculate interpolation weights to use, which are the amounts of each reference face at the vertices of the polygon.
     *
     * @param clickedPoint          User clicked point defining the interpolation.
     * @param referenceFacesPolygon Reference face polygon to get interpolation weights of w.r.t. the clicked point.
     * @return List of weights, where index i is the weight for the ith vertex (i.e., reference face).
     */
    private ArrayList<Double> calculateInterpolationWeights(Point clickedPoint, ArrayList<Point2D> referenceFacesPolygon) {

        ArrayList<Double> weights = new ArrayList<>();

        // Simple distance based interpolation weights. Can be improved with generalized barycentric co-ordinates.
        for (Point2D currReferenceFaceVertex : referenceFacesPolygon) {

            double distance = Point2D.distance(currReferenceFaceVertex.getX(), currReferenceFaceVertex.getY(),
                    clickedPoint.getX(), clickedPoint.getY());
            double weight = 1 / distance;

            weights.add(referenceFacesPolygon.indexOf(currReferenceFaceVertex), weight);

        }

        // Get weights as percentile of weights sum.
        double sum = weights.stream().mapToDouble(Double::doubleValue).sum();

        for (int i = 0; i < weights.size(); i++) {

            double newWeight = weights.get(i) / sum;
            weights.set(i, newWeight);

        }

        return weights;

    } // calculateInterpolationWeights().


} // MainUIPanel{}.
