import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Custom data object to represent a triangle, with associated colours at the vertices.
 *
 * @author 170004680
 */
public class Triangle implements Paint {


    private Point3D point1; // First point of the triangle.
    private Triplet point1Color; // Color of the first point in the triangle. Triplet as RGB.
    private Point3D point2; // Second point of the triangle.
    private Triplet point2Color; // Color of the second point in the triangle. Triplet as RGB.
    private Point3D point3; // Third point of the triangle.
    private Triplet point3Color; // Color of the third point in the triangle. Triplet as RGB.

    private final Point3D[] sorted; // Keep sorted list of points defining the triangle for comparison to other triangles.


    /**
     * Constructor: Create a Triangle object.
     *
     * @param point1 First point of the triangle.
     * @param point2 Second point of the triangle.
     * @param point3 Third point of the triangle.
     */
    public Triangle(Point3D point1, Point3D point2, Point3D point3) {

        this.point1 = point1;
        this.point2 = point2;
        this.point3 = point3;

        //this.edge12 = new Edge(point1, point2);
        //this.edge13 = new Edge(point1, point3);
        //this.edge23 = new Edge(point2, point3);

        this.sorted = new Point3D[]{point1, point2, point3};
        Arrays.sort(sorted, Comparator.comparing(Point3D::getZ)); // Keep sorted list of point for comparisons.

    } // Triangle().

    // Utility Functions:

    /**
     * Check if this triangle contains the given point.
     *
     * @param point Point to check if contained within the triangle.
     * @return True of point in this triangle, false otherwise.
     */
    public boolean contains(Point3D point) {

        return Arrays.asList(this.sorted).contains(point);

    } // contains().

    /**
     * @param point1Update Add point1Update co-ordinates to existing point1 co-ordinates.
     * @param point2Update Add point2Update co-ordinates to existing point2 co-ordinates.
     * @param point3Update Add point3Update co-ordinates to existing point3 co-ordinates.
     */
    public void updatePoints(Point3D point1Update, Point3D point2Update, Point3D point3Update) {

        this.point1.updateXYZ(point1Update.getX(), point1Update.getY(), point1Update.getZ());
        this.point2.updateXYZ(point2Update.getX(), point2Update.getY(), point2Update.getZ());
        this.point3.updateXYZ(point3Update.getX(), point3Update.getY(), point3Update.getZ());

    } // updatePoints().

    /**
     * @param point1ColorUpdate Add point1ColorUpdate RGB values to existing point1Color RGB values.
     * @param point2ColorUpdate Add point2ColorUpdate RGB values to existing point2Color RGB values.
     * @param point3ColorUpdate Add point3ColorUpdate RGB values to existing point3Color RGB values.
     */
    public void updateColors(Triplet point1ColorUpdate, Triplet point2ColorUpdate, Triplet point3ColorUpdate) {

        // Updating color of point 1.
        this.point1Color = new Triplet(
                this.point1Color.getFirst() + point1ColorUpdate.getFirst(), // R.
                this.point1Color.getSecond() + point1ColorUpdate.getSecond(), // G.
                this.point1Color.getThird() + point1ColorUpdate.getThird()); // B.

        // Updating color of point 2.
        this.point2Color = new Triplet(
                this.point2Color.getFirst() + point2ColorUpdate.getFirst(), // R.
                this.point2Color.getSecond() + point2ColorUpdate.getSecond(), // G.
                this.point2Color.getThird() + point2ColorUpdate.getThird()); // B.

        // Updating color of point 3.
        this.point3Color = new Triplet(
                this.point3Color.getFirst() + point3ColorUpdate.getFirst(), // R.
                this.point3Color.getSecond() + point3ColorUpdate.getSecond(), // G.
                this.point3Color.getThird() + point3ColorUpdate.getThird()); // B.

    } // updateColors().

    // Getters and Setters:

    /**
     * @return Z co-ordinate of point with largest Z co-ordinate.
     */
    public double getPainterZ() {
        return sorted[2].getZ();
    } // getLargestZPoint().

    /**
     * @return Set of points in this triangle as an array list.
     */
    public ArrayList<Point3D> getPoints() {

        ArrayList<Point3D> points = new ArrayList<>();

        points.add(point1);
        points.add(point2);
        points.add(point3);

        return points;

    } // getPoints().

    /**
     * @return point1.
     */
    public Point3D getPoint1() {
        return point1;
    } //getPoint1().

    /**
     * @param point1 Set point1.
     */
    public void setPoint1(Point3D point1) {
        this.point1 = point1;
    } // setPoint1().

    /**
     * @return point2.
     */
    public Point3D getPoint2() {
        return point2;
    } // getPoint2().

    /**
     * @param point2 Set point2.
     */
    public void setPoint2(Point3D point2) {
        this.point2 = point2;
    } // setPoint2().

    /**
     * @return point3.
     */
    public Point3D getPoint3() {
        return point3;
    } // getPoint3().

    /**
     * @param point3 Set point3.
     */
    public void setPoint3(Point3D point3) {
        this.point3 = point3;
    } // setPoint3().


    /**
     * @param point1Color Set point1Color.
     * @param point2Color Set point2Color.
     * @param point3Color Set point3Color.
     */
    public void setColorTs(Triplet point1Color, Triplet point2Color, Triplet point3Color) {

        setPoint1Color(point1Color);
        setPoint2Color(point2Color);
        setPoint3Color(point3Color);

    } // setColors().

    /**
     * @return point1Color.
     */
    public Triplet getPoint1ColorT() {
        return point1Color;
    } // getPoint1Color().

    /**
     * @return point1Color as Color object.
     */
    public Color getPoint1Color() {

        return new Color(
                (int) this.point1Color.getFirst(), // R.
                (int) this.point1Color.getSecond(), // G.
                (int) this.point1Color.getThird()); // B.

    } // getPoint1Color().

    /**
     * @param point1Color Set point1Color.
     */
    public void setPoint1Color(Triplet point1Color) {
        this.point1Color = point1Color;
    } // setPoint1Color().

    /**
     * @return point2Color.
     */
    public Triplet getPoint2ColorT() {
        return point2Color;
    } // getPoint2Color().

    /**
     * @return point2Color as Color object.
     */
    public Color getPoint2Color() {

        return new Color(
                (int) this.point2Color.getFirst(), // R.
                (int) this.point2Color.getSecond(), // G.
                (int) this.point2Color.getThird()); // B.

    } // getPoint2Color().

    /**
     * @param point2Color Set point2Color.
     */
    public void setPoint2Color(Triplet point2Color) {
        this.point2Color = point2Color;
    } // setPoint2Color().

    /**
     * @return point3Color.
     */
    public Triplet getPoint3ColorT() {
        return point3Color;
    }

    /**
     * @return point3Color as Color object.
     */
    public Color getPoint3Color() {

        return new Color(
                (int) this.point3Color.getFirst(), // R.
                (int) this.point3Color.getSecond(), // G.
                (int) this.point3Color.getThird()); // B.

    } // getPoint3Color().

    /**
     * @param point3Color Set point3Color.
     */
    public void setPoint3Color(Triplet point3Color) {
        this.point3Color = point3Color;
    } // setPoint3Color().

    // Equals and Hash-Code Override:

    /**
     * Two triangles are equal if they are the same object or are defined by the same points.
     *
     * @param o Object to compare equality to this triangle.
     * @return True if the above conditions met, false otherwise.
     */
    @Override
    public boolean equals(Object o) {

        // Self Check.
        if (this == o) return true;
        // Type Check.
        if (!(o instanceof Triangle)) return false;

        Triangle face = (Triangle) o;

        // Check if attributes are equivalent.
        return Arrays.equals(face.sorted, this.sorted);

    } // equals().

    /**
     * @return Hash code of this triangle, based on the points defining the triangle.
     */
    @Override
    public int hashCode() {

        int result = point1.hashCode();
        result = 31 * result + point2.hashCode();
        result = 31 * result + point3.hashCode();
        return result;

    } // hashCode().

    /**
     * Override: Create a paint context for gradient filling a triangle.
     *
     * @param cm Color model.
     * @param deviceBounds Device bounds.
     * @param userBounds User bounds.
     * @param xform Affine transform.
     * @param hints Rendering hints.
     * @return PaintContext for painting a triangle.
     */
    @Override
    public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds,
                                      AffineTransform xform, RenderingHints hints) {

        TriangleInterpPaintContext bgpc = new TriangleInterpPaintContext(
                new Point2D.Float((float) getPoint1().getX(), (float) getPoint1().getY()),
                new Point2D.Float((float) getPoint2().getX(), (float) getPoint2().getY()),
                new Point2D.Float((float) getPoint3().getX(), (float) getPoint3().getY()),
                getPoint1Color(), getPoint2Color(), getPoint3Color(), xform,
                hints.get(RenderingHints.KEY_ANTIALIASING) == RenderingHints.VALUE_ANTIALIAS_ON);

        return bgpc;

    } // createContext().

    /**
     * Override:
     *
     * @return Transparency Value.
     */
    @Override
    public int getTransparency() {

        int a1 = getPoint1Color().getAlpha();
        int a2 = getPoint2Color().getAlpha();
        int a3 = getPoint3Color().getAlpha();

        int transparency = ((a1 & a2 & a3) == 0xff) ? this.OPAQUE : this.TRANSLUCENT;

        return transparency;

    } // getTransparency().

} // Triangle{}.
