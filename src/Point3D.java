import java.awt.*;

/**
 * Custom data object to define a point in 3D.
 *
 * @author 170004680
 */
public class Point3D {


    private double x = 0; // X Co-ordinate for this point.
    private double y = 0; // Y Co-ordinate for this point.
    private double z = 0; // Z Co-ordinate for this point.


    /**
     * 3D point constructor: Used for the advanced specification.
     *
     * @param x X Co-ordinate for this point.
     * @param y Y Co-ordinate for this point.
     * @param z Z Co-ordinate for this point.
     */
    public Point3D(double x, double y, double z) {

        this.x = x;
        this.y = y;
        this.z = z;

    } // Point3D().

    // Utility Functions:

    /**
     * @param x Add x to existing x value.
     */
    public void updateX(double x) {
        this.x = this.x + x;
    } // updateX().

    /**
     * @param y Add y to existing y value.
     */
    public void updateY(double y) {
        this.y = this.y + y;
    } // updateY().

    /**
     * @param z Add z to existing z value.
     */
    public void updateZ(double z) {
        this.z = this.z + z;
    } // updateZ().

    /**
     * @param x Add x to existing x value.
     * @param y Add y to existing y value.
     * @param z Add z to existing z value.
     */
    public void updateXYZ(double x, double y, double z) {

        updateX(x);
        updateY(y);
        updateZ(z);

    } // updateXYZ().

    /**
     * Method checks if this point contains the 2D point object.
     *
     * @param point Point to check this point contains.
     * @return True if point is within this point, false otherwise.
     */
    public boolean contains(Point point, int pointSize) {

        // The point must be within the dimensions of this point.
        return (point.getX() >= this.x && point.getX() <= this.x + pointSize) &&
                (point.getY() >= this.y && point.getY() <= this.y + pointSize);

    } // contains().

    // Getters and Setters:

    /**
     * @return this.x
     */
    public double getX() {
        return x;
    } // getX().

    /**
     * @param x Set the x co-ordinate of this point.
     */
    public void setX(double x) {
        this.x = x;
    } // setX().

    /**
     * @return this.y
     */
    public double getY() {
        return y;
    } // getY().

    /**
     * @param y Set the y co-ordinate of this point.
     */
    public void setY(double y) {
        this.y = y;
    } // setY().

    /**
     * @return this.z
     */
    public double getZ() {
        return z;
    } // getZ().

    /**
     * @param z Set the z co-ordinate of this point.
     */
    public void setZ(double z) {
        this.z = z;
    } //setZ().

    // Equals and Hash-Code Override.

    /**
     * Check equality of this point with another object.
     *
     * @param obj Object to compare this point with.
     * @return True if obj is this point, or has the same co-ordinates.
     */
    @Override
    public boolean equals(Object obj) {

        // Self check.
        if (this == obj) return true;
        // Null check.
        if (obj == null) return false;
        // Type check and cast.
        if (getClass() != obj.getClass()) return false;

        Point3D other = (Point3D) obj;

        // Field comparison.
        return (other.getX() == this.x) && (other.getY() == this.y) && (other.getZ() == this.z);

    } // equals().

    /**
     * Override hash cod method based on co-ordinate attributes
     *
     * @return Hash code for this Point3D object.
     */
    @Override
    public int hashCode() {

        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(z);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;

    } // hashCode().


} // Point3D{}.
