import java.util.ArrayList;

/**
 * Custom data object representing a reference face.
 *
 * @author 170004680
 */
public class Face3D {


    private ArrayList<Triangle> faceData; // Array list of triangles for shape information.


    /**
     * Blank constructor:
     */
    public Face3D() {
        faceData = new ArrayList<>();
    } // Face3D().

    /**
     * Constructor: A reference face is comprised of shape and color information.
     *
     * @param faceData Array list of triangles for shape information.
     */
    public Face3D(ArrayList<Triangle> faceData) {
        this.faceData = faceData;
    } // Face3D().

    // Utility Functions:

    /**
     * @param index Index of triangle to retrieve.
     * @return Triangle at index.
     */
    public Triangle getTriangle(int index) {
        return faceData.get(index);
    } // getTriangle().

    /**
     * @param triangle Add triangle to the set of face triangles.
     */
    public void addTriangle(Triangle triangle) {
        faceData.add(triangle);
    } // addTriangle().


    // Getters and Setters:

    /**
     * @return shape.
     */
    public ArrayList<Triangle> getFaceData() {
        return faceData;
    } // getShape().

    /**
     * @param faceData Set shape.
     */
    public void setFaceData(ArrayList<Triangle> faceData) {
        this.faceData = faceData;
    } // setShape().


} // Face3D{}.
