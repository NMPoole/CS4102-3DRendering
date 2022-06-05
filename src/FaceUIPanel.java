import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

/**
 * FaceUIPanel: Handles the UI for drawing faces.
 *
 * @author 17004680
 */
public class FaceUIPanel extends JPanel {


    private Face3D face3D; // Face represented in this UI window.

    // Variables used for scaling the 3D face co-ordinates to the window co-ordinates.
    private double face3DMinX = Double.MAX_VALUE, face3DMaxX = Double.MIN_VALUE;
    private double face3DMinY = Double.MAX_VALUE, face3DMaxY = Double.MIN_VALUE;
    private double face3DMinZ = Double.MAX_VALUE, face3DMaxZ = Double.MIN_VALUE;


    /**
     * Constructor:
     */
    public FaceUIPanel() {

        this.face3D = null; // No face to draw on construction.

        this.setLayout(new FlowLayout(FlowLayout.CENTER));
        this.setBackground(Color.white);

    } // FaceUIPanel().

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

        // If given a 3D face, then render it.
        if (this.face3D != null) {
            draw3DFace(graphics2D);
        }

    } // paintComponent().

    /**
     * Draw a 3D face in this window:
     *
     * Orthographic projection of the 3D face parallel to the z-plane, with face polygons being drawn using painter's
     * algorithm. Flat shading is used and it is assumed that faces are perfectly matte with a unity diffuse coefficient
     * and that there is a single directional light source aligned with the viewing direction.
     *
     * @param graphics2D Graphics object to use to draw in this panel.
     */
    private void draw3DFace(Graphics2D graphics2D) {

        ArrayList<Triangle> face3DPolygons = face3D.getFaceData(); // Polygons comprising the face to be rendered.

        // Painter's Algorithm:

        // Sort triangles by depth.
        face3DPolygons.sort(Comparator.comparing(Triangle::getPainterZ));

        // Draw triangles from back of canvas to front.
        // This seems like front to back, but the z-axis of the canvas is inverted (towards viewer, not away from).
        for (int currTriangleIndex = 0; currTriangleIndex < face3DPolygons.size(); currTriangleIndex++) {

            Triangle currTriangle = face3D.getTriangle(currTriangleIndex);
            Triangle currTriangleScaled = scaleTriangle(currTriangle); // Scale triangle to fit in render window.

            Polygon triangleAsPolygon = new Polygon();

            triangleAsPolygon.addPoint(
                    (int) Math.round(currTriangleScaled.getPoint1().getX()),
                    (int) Math.round(currTriangleScaled.getPoint1().getY()));
            triangleAsPolygon.addPoint(
                    (int) Math.round(currTriangleScaled.getPoint2().getX()),
                    (int) Math.round(currTriangleScaled.getPoint2().getY()));
            triangleAsPolygon.addPoint(
                    (int) Math.round(currTriangleScaled.getPoint3().getX()),
                    (int) Math.round(currTriangleScaled.getPoint3().getY()));

            // Render face to canvas depending on specified type.
            if (P2main.renderingType == 0) { // Flat Shading.

                // Flat Shading - Evaluate light model once and apply across triangle (triangle has single colour).
                if (P2main.isLighting) applyIlluminationModel(currTriangleScaled);
                graphics2D.setColor(currTriangleScaled.getPoint1Color()); // Simply use colour of first triangle vertex.
                graphics2D.fillPolygon(triangleAsPolygon);

            } else if (P2main.renderingType == 1) { // Interpolation Shading.

                // Interpolation (Gouraud) Shading - Evaluate light model at each vertex and interpolate over triangle surface.
                // Gives artefacts that require further investigation but not a major issue.
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Antialiasing.
                if (P2main.isLighting) applyIlluminationModelGouraud(currTriangle, currTriangleScaled); // Gouraud Shading.
                graphics2D.setPaint(currTriangleScaled); // Colouring using interpolation - use Paint type of triangle.
                graphics2D.setStroke(new BasicStroke(2f)); // Helps with artefacts associated with the package.
                graphics2D.drawPolygon(triangleAsPolygon); // Interpolation triangle fill doesn't do edges, so add them.
                graphics2D.fillPolygon(triangleAsPolygon);

            } else if (P2main.renderingType == -1) { // Wire-frame (debugging).

                // Shows wire-frame, no fill. Can see all polygons and the density of them in localised areas for debugging.
                graphics2D.setColor(Color.DARK_GRAY);
                graphics2D.setStroke(new BasicStroke(0.1f));
                graphics2D.drawPolygon(triangleAsPolygon);

            }

        }

    } // draw3DFace().

    // Utility Functions:

    /**
     * Perform a rotation of the rendered 3D face in the given direction around the Y-axis.
     *
     * @param isLeftRotate True if rotating leftward (clockwise around Y axis), False for rightward (anti-clockwise around Y axis).
     */
    public void rotationAction(boolean isLeftRotate) {

        // Make sure there is a face to rotate.
        if (face3D != null) {

            // Locate middle of the 3D face model so can rotate around center of the face.
            double faceMiddleX = face3DMinX + ((face3DMaxX - face3DMinX) / 2);
            double faceMiddleZ = face3DMinZ + ((face3DMaxZ - face3DMinZ) / 2);

            // Have to translate all co-ordinate in all triangles of the 3D face.
            for (Triangle currTriangle : face3D.getFaceData()) {
                for (Point3D currPoint : currTriangle.getPoints()) {

                    // Rotation of 45 degrees in the given direction.
                    double rotationAngle;
                    if (isLeftRotate) {
                        rotationAngle = (Math.PI / 10); // 18 degrees clockwise to Y-axis.
                    } else {
                        rotationAngle = -(Math.PI / 10); // 18 degrees anti-clockwise to Y-axis.
                    }

                    // Need to translate to the middle of the face model as the origin.
                    double translatedOriginX = currPoint.getX() - faceMiddleX;
                    double translatedOriginZ = currPoint.getZ() - faceMiddleZ;

                    // x' = x cos θ + z sin θ.
                    double rotatedX = (translatedOriginX * Math.cos(rotationAngle)) + (translatedOriginZ * Math.sin(rotationAngle));
                    // z' = -x sin θ + z cos θ.
                    double rotatedZ = ((-translatedOriginX) * Math.sin(rotationAngle)) + (translatedOriginZ * Math.cos(rotationAngle));

                    // Translate back from converted origin.
                    double finalX = rotatedX + faceMiddleX;
                    double finalZ = rotatedZ + faceMiddleZ;

                    currPoint.setX(finalX);
                    currPoint.setZ(finalZ);

                }
            }

            repaint(); // Draw the rotated face.

        }

    } // rotationAction().

    /**
     * Apply Lambert's lighting model to illuminate a triangle.
     *
     * @param triangle Triangle to apply lighting to.
     */
    private void applyIlluminationModel(Triangle triangle) {

        double intensityIncomingLight = 1.2; // Intensity of incoming light.
        double diffuseCoefficient = 1; // Unity diffuse co-efficient.

        Triplet lightVector = new Triplet(0, 0, -1); // Vector pointing towards the light source.
        Triplet surfaceNormal = getTriangleSurfaceNormal(triangle); // Surface normal vector of triangle.

        double product = dotProduct(surfaceNormal, lightVector);
        product = Math.abs(product * diffuseCoefficient * intensityIncomingLight);

        // Multiply product by each of the three colour channels at each vertex of the triangle.

        double point1Red = getIlluminatedColor(triangle.getPoint1ColorT().getFirst(), product);
        double point1Green =  getIlluminatedColor(triangle.getPoint1ColorT().getSecond(), product);
        double point1Blue =  getIlluminatedColor(triangle.getPoint1ColorT().getThird(), product);
        Triplet illuminatedPoint1Color = new Triplet(point1Red, point1Green, point1Blue); // B.

        double point2Red = getIlluminatedColor(triangle.getPoint2ColorT().getFirst(), product);
        double point2Green =  getIlluminatedColor(triangle.getPoint2ColorT().getSecond(), product);
        double point2Blue =  getIlluminatedColor(triangle.getPoint2ColorT().getThird(), product);
        Triplet illuminatedPoint2Color = new Triplet(point2Red, point2Green, point2Blue); // B.

        double point3Red = getIlluminatedColor(triangle.getPoint3ColorT().getFirst(), product);
        double point3Green =  getIlluminatedColor(triangle.getPoint3ColorT().getSecond(), product);
        double point3Blue =  getIlluminatedColor(triangle.getPoint3ColorT().getThird(), product);
        Triplet illuminatedPoint3Color = new Triplet(point3Red, point3Green, point3Blue); // B.

        // Update colours to account for illumination.
        triangle.setColorTs(illuminatedPoint1Color, illuminatedPoint2Color, illuminatedPoint3Color);

    } // applyIlluminationModel().

    /**
     * Apply Lambert's illumination model to a triangle of the 3D face using Gouraud shading.
     *
     * @param triangle Triangle to apply illumination model to.
     * @param scaledTriangle Scaled version of triangle to apply illumination model to.
     */
    private void applyIlluminationModelGouraud(Triangle triangle, Triangle scaledTriangle) {

        double intensityIncomingLight = 1.2; // Intensity of incoming light.
        double diffuseCoefficient = 1; // Unity diffuse co-efficient.

        Triplet lightVector = new Triplet(0, 0, -1); // Vector pointing towards the light source.

        // Get lists of adjacent triangles for each of the points in this triangle.
        HashMap<Integer, ArrayList<Triangle>> adjacentTriangles = getAdjacentTrianglesToPoints(triangle);

        // Surface lighting applied to the first point:
        Triplet point1AvgSurfaceNormal = getAverageSurfaceNormal(adjacentTriangles.get(1));
        double product = dotProduct(point1AvgSurfaceNormal, lightVector);
        product = Math.abs(product * diffuseCoefficient * intensityIncomingLight);

        double point1Red = getIlluminatedColor(triangle.getPoint1ColorT().getFirst(), product);
        double point1Green =  getIlluminatedColor(triangle.getPoint1ColorT().getSecond(), product);
        double point1Blue =  getIlluminatedColor(triangle.getPoint1ColorT().getThird(), product);
        Triplet illuminatedPoint1Color = new Triplet(point1Red, point1Green, point1Blue);

        // Surface lighting applied to the second point:
        Triplet point2AvgSurfaceNormal = getAverageSurfaceNormal(adjacentTriangles.get(2));
        product = dotProduct(point2AvgSurfaceNormal, lightVector);
        product = Math.abs(product * diffuseCoefficient * intensityIncomingLight);

        double point2Red = getIlluminatedColor(triangle.getPoint2ColorT().getFirst(), product);
        double point2Green =  getIlluminatedColor(triangle.getPoint2ColorT().getSecond(), product);
        double point2Blue =  getIlluminatedColor(triangle.getPoint2ColorT().getThird(), product);
        Triplet illuminatedPoint2Color = new Triplet(point2Red, point2Green, point2Blue);

        // Surface lighting applied to the third point:
        Triplet point3AvgSurfaceNormal = getAverageSurfaceNormal(adjacentTriangles.get(3));
        product = dotProduct(point3AvgSurfaceNormal, lightVector);
        product = Math.abs(product * diffuseCoefficient * intensityIncomingLight);

        double point3Red = getIlluminatedColor(triangle.getPoint3ColorT().getFirst(), product);
        double point3Green =  getIlluminatedColor(triangle.getPoint3ColorT().getSecond(), product);
        double point3Blue =  getIlluminatedColor(triangle.getPoint3ColorT().getThird(), product);
        Triplet illuminatedPoint3Color = new Triplet(point3Red, point3Green, point3Blue);

        // Update colours to account for illumination.
        scaledTriangle.setColorTs(illuminatedPoint1Color, illuminatedPoint2Color, illuminatedPoint3Color);

    } // applyIlluminationModelGouraud().

    /**
     * Given a set of triangles, calculate the average surface normal of the triangles.
     *
     * @param triangles Triangles to get average surface normal of.
     * @return Triplet representing the average surface normal of the triangles.
     */
    private Triplet getAverageSurfaceNormal(ArrayList<Triangle> triangles) {

        double averageX = 0;
        double averageY = 0;
        double averageZ = 0;

        for (Triangle currTriangle : triangles) {

            Triplet currSurfaceNormal = getTriangleSurfaceNormal(currTriangle);

            averageX += currSurfaceNormal.getFirst();
            averageY += currSurfaceNormal.getSecond();
            averageZ += currSurfaceNormal.getThird();

        }

        averageX = averageX / triangles.size();
        averageY = averageY / triangles.size();
        averageZ = averageZ / triangles.size();

        Triplet avgSurfaceNormal = new Triplet(averageX, averageY, averageZ);

        return avgSurfaceNormal;

    } // getAverageSurfaceNormal().

    /**
     * Get the set of triangles adjacent to a point.
     *
     * @param triangle Triangle to get adjacent triangles of its points.
     * @return Mapping of integers (the points) to lists of of triangles adjacent to the point.
     */
    private HashMap<Integer, ArrayList<Triangle>> getAdjacentTrianglesToPoints(Triangle triangle) {

        HashMap<Integer, ArrayList<Triangle>> adjacentTriangles = new HashMap<>();
        adjacentTriangles.put(1, new ArrayList<>());
        adjacentTriangles.put(2, new ArrayList<>());
        adjacentTriangles.put(3, new ArrayList<>());

        for (Triangle currTriangle : face3D.getFaceData()) {
            if (currTriangle.contains(triangle.getPoint1())) {
                adjacentTriangles.get(1).add(currTriangle);
            } else if (currTriangle.contains(triangle.getPoint2())) {
                adjacentTriangles.get(2).add(currTriangle);
            } else if (currTriangle.contains(triangle.getPoint3())) {
                adjacentTriangles.get(3).add(currTriangle);
            }
        }

        return adjacentTriangles;

    } // getTrianglesUsingPoint().

    /**
     * Applies a product used to illuminate a colour whilst maintaining the accepted RGB range.
     *
     * @param colorValue Color value to scale/illuminate. Color value is in R, G, or B channel.
     * @param product Product used to illuminate the color value/channel.
     * @return Illuminated color value based on product, keeping within the RGB range.
     */
    private double getIlluminatedColor(double colorValue, double product) {

        colorValue = colorValue * product;

        // Confine to RGB range.
        if (colorValue > 255) {
            colorValue = 255;
        } else if (colorValue < 0) {
            colorValue = 0;
        }

        return colorValue;

    } // getIlluminatedColor().

    /**
     * Calculate the normalised surface normal of a triangle as a vector.
     *
     * @param triangle Triangle to calculate surface normal for.
     * @return Vector (Triplet) representing the surface normal.
     */
    private Triplet getTriangleSurfaceNormal(Triangle triangle) {

        Triplet U = new Triplet(
                triangle.getPoint2().getX() - triangle.getPoint1().getX(), // x.
                triangle.getPoint2().getY() - triangle.getPoint1().getY(), // y.
                triangle.getPoint2().getZ() - triangle.getPoint1().getZ()); // z.

        Triplet V = new Triplet(
                triangle.getPoint3().getX() - triangle.getPoint1().getX(), // x.
                triangle.getPoint3().getY() - triangle.getPoint1().getY(), // y.
                triangle.getPoint3().getZ() - triangle.getPoint1().getZ()); // z.

        // normalX = (U.y * V.z) - (U.z * V.y).
        double normalX = (U.getSecond() * V.getThird()) - (U.getThird() * V.getSecond());
        // normalY = (U.z * V.x) - (U.x * V.z).
        double normalY = (U.getThird() * V.getFirst()) - (U.getFirst() * V.getThird());
        // normalZ = (U.x * V.y) - (U.y * V.x).
        double normalZ = (U.getFirst() * V.getSecond()) - (U.getSecond() * V.getFirst());

        // Normalise the surface normal vector (make it unit length).
        double vectorMag = Math.sqrt(Math.pow(normalX, 2) + Math.pow(normalY, 2) + Math.pow(normalZ, 2));
        normalX = normalX / vectorMag;
        normalY = normalY / vectorMag;
        normalZ = normalZ / vectorMag;

        Triplet surfaceNormal = new Triplet(normalX, normalY, normalZ);

        return surfaceNormal;

    } // getTriangleSurfaceNormal().

    /**
     * Calculate the dot product of two 1x3 vectors.
     *
     * @param u First vector in dot product.
     * @param v Second vector in dot product.
     * @return Dot product of vectors u and v.
     */
    private double dotProduct(Triplet u, Triplet v) {

        // u . v = (u.x * v.x) + (u.y * v.y) + (u.z * v.z)
        double dotProduct = (u.getFirst() * v.getFirst()) + (u.getSecond() * v.getSecond()) + (u.getThird() * v.getThird());
        return dotProduct;

    } // dotProduct().

    /**
     * Scale a given triangle within the 3D face to a range fitting the current rendering window.
     *
     * @param triangleToScale Triangle to be scaled.
     * @return New triangle object, with scaled co-ordinates and retained colour information.
     */
    private Triangle scaleTriangle(Triangle triangleToScale) {

        // Scale points inside of triangle to the range required by the window.
        Point3D point1Scaled = scalePoint(triangleToScale.getPoint1());
        Point3D point2Scaled = scalePoint(triangleToScale.getPoint2());
        Point3D point3Scaled = scalePoint(triangleToScale.getPoint3());

        // Create new triangle with the scaled points, preserving the colour information.
        Triangle scaledTriangle = new Triangle(point1Scaled, point2Scaled, point3Scaled);
        scaledTriangle.setColorTs(triangleToScale.getPoint1ColorT(), triangleToScale.getPoint2ColorT(),
                triangleToScale.getPoint3ColorT());

        return scaledTriangle;

    } // scaleTriangle().

    /**
     * Scale a given point within a triangle polygon of the 3D face to fit the rendering window.
     *
     * @param pointToScale Point to scale.
     * @return New scaled point.
     */
    private Point3D scalePoint(Point3D pointToScale) {

        // Target range to scale to.
        // No needed to scale Z, but will do so for generality. Use target X range for, say, rotating around the Y axis.
        double windowMinX = 0, windowMinY = 0, windowMinZ = windowMinX;
        double windowMaxX = this.getWidth(), windowMaxY = this.getHeight(), windowMaxZ = windowMaxX;

        // Scaling of the co-ordinate values.
        double scaledX = (((pointToScale.getX() - face3DMinX) / (face3DMaxX - face3DMinX)) * (windowMaxX - windowMinX)) + (windowMinX);
        double scaledY = (((pointToScale.getY() - face3DMinY) / (face3DMaxY - face3DMinY)) * (windowMaxY - windowMinY)) + (windowMinY);
        scaledY = windowMaxY - scaledY; // Down is positive Y direction in window, so make the face right way up.
        double scaledZ = (((pointToScale.getZ() - face3DMinZ) / (face3DMaxZ - face3DMinZ)) * (windowMaxZ - windowMinZ)) + (windowMinZ);

        // Create scaled point and return.
        Point3D scaledPoint = new Point3D(scaledX, scaledY, scaledZ);
        return scaledPoint;

    } // scalePoint().

    /**
     * Retrieve the ranges of the X, Y, and Z co-ordinates in the 3D face so that co-ordinates can be scaled to the
     * current rendering window size.
     *
     * @param face3DPolygons Set of triangles in the 3D face, which comprise the co-ordinates.
     */
    private void getFace3DRanges(ArrayList<Triangle> face3DPolygons) {

        // For every triangle in the face...
        for (Triangle currTriangle : face3DPolygons) {

            // .. and for every point in the triangles...
            for (Point3D currPoint : currTriangle.getPoints()) {

                // ... update the value ranges. This could probably be improved.
                if (currPoint.getX() < face3DMinX) face3DMinX = currPoint.getX();
                if (currPoint.getX() > face3DMaxX) face3DMaxX = currPoint.getX();
                if (currPoint.getY() < face3DMinY) face3DMinY = currPoint.getY();
                if (currPoint.getY() > face3DMaxY) face3DMaxY = currPoint.getY();
                if (currPoint.getZ() < face3DMinZ) face3DMinZ = currPoint.getZ();
                if (currPoint.getZ() > face3DMaxZ) face3DMaxZ = currPoint.getZ();

            }

        }

    } // getFace3DRanges().

    /**
     * @param face3D Set face3D.
     */
    public void render3DFace(Face3D face3D) {

        this.face3D = face3D;
        getFace3DRanges(face3D.getFaceData()); // Update ranges of the co-ordinates for rendering within window dimensions.
        repaint();

    } // render3DFace().


} // FaceUIPanel{}.
