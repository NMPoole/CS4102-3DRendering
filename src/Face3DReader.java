import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Methods for reading reference faces from data files.
 *
 * @author 170004680
 */
public class Face3DReader {


    File dataDir; // Directory containing the data files.
    ArrayList<Triplet> mesh; // Get the mesh to be used by the reference faces.
    ArrayList<Triplet> sh000; // Average shape information.
    ArrayList<Triplet> tx000; // Average color information.
    ArrayList<Double> shEV; // Shape information weights.
    ArrayList<Double> txEV;// Color information weights.


    /**
     * Constructor:
     *
     * @param dataDir Directory containing the data files.
     */
    public Face3DReader(File dataDir) {

        this.dataDir = dataDir;
        this.mesh = getMesh(dataDir); // Get the mesh to be used by the reference faces.
        this.sh000 = getFaceDataFile(dataDir, 0, true); // Average shape information.
        this.tx000 = getFaceDataFile(dataDir, 0, false); // Average color information.
        this.shEV = getWeightFile(dataDir, true); // Shape information weights.
        this.txEV = getWeightFile(dataDir, false); // Color information weights.

    } // Face3DReader().

    // Utility Functions:

    /**
     * Given a set of interpolation weights, get the interpolated face given by interpolating over all faces from
     * sh_001 to sh_00n, using the given weights.
     *
     * @param interpolationWeights Weights to use for the interpolation. Order such that i-th weight is for (i + 1)th face file.
     * @return Interpolated face.
     */
    public Face3D getInterpolatedFace(ArrayList<Double> interpolationWeights) {

        Face3D interpolatedFace = new Face3D(); // Interpolated face object to return.

        int numReferenceFaces = interpolationWeights.size(); // Number of reference faces to interpolate over.

        // For every reference face...
        for (int currRefFaceIndex = 0; currRefFaceIndex < numReferenceFaces; currRefFaceIndex++) {

            Double shEVWeight = shEV.get(currRefFaceIndex); // Get weight at faceNum-th line, w.
            Double txEVWeight = txEV.get(currRefFaceIndex); // Get weight at faceNum-th line, w.
            // Get reference face for current face (currRefFaceIndex + 1).
            Face3D face3D = getReferenceFace(dataDir, currRefFaceIndex + 1, mesh, sh000, tx000, shEVWeight, txEVWeight);

            // Interpolation weight for this reference face.
            Double currInterpolationWeight = interpolationWeights.get(currRefFaceIndex);

            // For every triangle, get the required percentages of co-ordinates and colors for current interpolation weight.
            for (int currTriangleIndex = 0; currTriangleIndex < mesh.size(); currTriangleIndex++) {

                Triangle currTriangle = face3D.getFaceData().get(currTriangleIndex);

                // Interpolating point 1.
                Point3D interpolatedPoint1 = new Point3D(
                        currInterpolationWeight * currTriangle.getPoint1().getX(),
                        currInterpolationWeight * currTriangle.getPoint1().getY(),
                        currInterpolationWeight * currTriangle.getPoint1().getZ());

                // Interpolating point1Color.
                Triplet interpolatedPoint1Color = new Triplet(
                        currInterpolationWeight * currTriangle.getPoint1ColorT().getFirst(), // R.
                        currInterpolationWeight * currTriangle.getPoint1ColorT().getSecond(), // G.
                        currInterpolationWeight * currTriangle.getPoint1ColorT().getThird()); // B.

                // Interpolating point 2.
                Point3D interpolatedPoint2 = new Point3D(
                        currInterpolationWeight * currTriangle.getPoint2().getX(),
                        currInterpolationWeight * currTriangle.getPoint2().getY(),
                        currInterpolationWeight * currTriangle.getPoint2().getZ());

                // Interpolating point2Color.
                Triplet interpolatedPoint2Color = new Triplet(
                        currInterpolationWeight * currTriangle.getPoint2ColorT().getFirst(), // R.
                        currInterpolationWeight * currTriangle.getPoint2ColorT().getSecond(), // G.
                        currInterpolationWeight * currTriangle.getPoint2ColorT().getThird()); // B.

                // Interpolating point 3.
                Point3D interpolatedPoint3 = new Point3D(
                        currInterpolationWeight * currTriangle.getPoint3().getX(),
                        currInterpolationWeight * currTriangle.getPoint3().getY(),
                        currInterpolationWeight * currTriangle.getPoint3().getZ());

                // Interpolating point3Color.
                Triplet interpolatedPoint3Color = new Triplet(
                        currInterpolationWeight * currTriangle.getPoint3ColorT().getFirst(), // R.
                        currInterpolationWeight * currTriangle.getPoint3ColorT().getSecond(), // G.
                        currInterpolationWeight * currTriangle.getPoint3ColorT().getThird()); // B.

                // If current face is 0th, then add triangle to interpolatedFace, otherwise update existing triangles.
                if (currRefFaceIndex == 0) { // If first face, add triangle to the set of triangles comprising face.

                    Triangle newTriangle = new Triangle(interpolatedPoint1, interpolatedPoint2, interpolatedPoint3);
                    newTriangle.setColorTs(interpolatedPoint1Color, interpolatedPoint2Color, interpolatedPoint3Color);
                    interpolatedFace.addTriangle(newTriangle);

                } else { // For every other face, we want to add to the existing co-ordinates and colors.

                    // Update existing triangle.
                    Triangle currInterpolatedTriangle = interpolatedFace.getTriangle(currTriangleIndex);
                    currInterpolatedTriangle.updatePoints(interpolatedPoint1, interpolatedPoint2, interpolatedPoint3);
                    currInterpolatedTriangle.updateColors(interpolatedPoint1Color, interpolatedPoint2Color, interpolatedPoint3Color);

                }

            } // for (every triangle).

        } // for (every reference face).

        return interpolatedFace;

    } // getInterpolatedFace().

    /**
     * Get a given reference face using a specified face number and mesh.
     *
     * @param faceNum Number of the face to get shape/color data for (i.e., 1 through to 199 inclusive).
     * @return Face3D object, which contains the shape and color information of the reference face.
     */
    public Face3D getReferenceFace(int faceNum) {

        Double shEVWeight = shEV.get(faceNum - 1); // Get weight at faceNum-th line, w.
        Double txEVWeight = txEV.get(faceNum - 1); // Get weight at faceNum-th line, w.

        // Get shape and color information for the reference face.
        Face3D face3D = getReferenceFace(dataDir, faceNum, mesh, sh000, tx000, shEVWeight, txEVWeight);
        return face3D;

    } // getReferenceFace().

    /**
     * Get a given reference face using a specified face number and mesh.
     *
     * @param dataDir    Directory containing the data files.
     * @param faceNum    Number of the face to get shape/color data for (i.e., 1 through to 199 inclusive).
     * @param mesh       Mesh used to define the shape and color information.
     * @param sh000      Average face shape information.
     * @param tx000      Average face colour information.
     * @param shEVWeight Shape weight to use for current faceNum.
     * @param txEVWeight Colour weight to use for current faceNum.
     * @return Face3D object, which contains the shape and color information of the reference face.
     */
    private Face3D getReferenceFace(File dataDir, int faceNum, ArrayList<Triplet> mesh,
                                    ArrayList<Triplet> sh000, ArrayList<Triplet> tx000,
                                    Double shEVWeight, Double txEVWeight) {

        // Get shape and color information for the reference face.
        ArrayList<Triangle> faceWShapeData = getReferenceFaceShape(dataDir, faceNum, mesh, sh000, shEVWeight);
        getReferenceFaceColor(dataDir, faceNum, mesh, tx000, txEVWeight, faceWShapeData);

        // Add to the set of reference faces.
        Face3D face3D = new Face3D(faceWShapeData);
        return face3D;

    } // getReferenceFace().

    /**
     * Get the color information for a given face from the corresponding CSV files:
     * <p>
     * "The corresponding colours are similarly computed by adding the colours in tx_000.csv (the average face colour)
     * summed with the colour offsets in tx_00n.csv multiplied by the n-th weight in tx EV.csv."
     *
     * @param dataDir        Directory containing the data files.
     * @param faceNum        Number of the face to get shape data for (i.e., 1 through to 199 inclusive).
     * @param mesh           Mesh used to define the shape and color information.
     * @param tx000          Average color information.
     * @param weight         Color weight associated with the current face.
     * @param faceWShapeData Face to update with color information (should already have shape info but not relevant).
     */
    private void getReferenceFaceColor(File dataDir, int faceNum, ArrayList<Triplet> mesh,
                                       ArrayList<Triplet> tx000, Double weight,
                                       ArrayList<Triangle> faceWShapeData) {

        ArrayList<Triplet> tx00n = getFaceDataFile(dataDir, faceNum, false); // Color information offsets.

        int currTriangleTripletIndex = 0;
        // For every triangle (as indices) specified in the mesh.
        for (Triplet currTriangleTriplet : mesh) {

            int firstPointIndex = (int) currTriangleTriplet.getFirst() - 1; // i1
            Triplet currAverageFacePoint1 = tx000.get(firstPointIndex); // av1.
            Triplet currOffsets1 = tx00n.get(firstPointIndex); // off1.
            Triplet triangleColor1 = new Triplet(
                    currAverageFacePoint1.getFirst() + currOffsets1.getFirst() * weight, // R.
                    currAverageFacePoint1.getSecond() + currOffsets1.getSecond() * weight, // G.
                    currAverageFacePoint1.getThird() + currOffsets1.getThird() * weight); // B.

            int secondPointIndex = (int) currTriangleTriplet.getSecond() - 1; // i2.
            Triplet currAverageFacePoint2 = tx000.get(secondPointIndex); // av2.
            Triplet currOffsets2 = tx00n.get(secondPointIndex); // off2.
            Triplet triangleColor2 = new Triplet(
                    currAverageFacePoint2.getFirst() + currOffsets2.getFirst() * weight, // R.
                    currAverageFacePoint2.getSecond() + currOffsets2.getSecond() * weight, // G.
                    currAverageFacePoint2.getThird() + currOffsets2.getThird() * weight); // B.

            int thirdPointIndex = (int) currTriangleTriplet.getThird() - 1; // i3.
            Triplet currAverageFacePoint3 = tx000.get(thirdPointIndex); // av3.
            Triplet currOffsets3 = tx00n.get(thirdPointIndex); // off3.
            Triplet triangleColor3 = new Triplet(
                    currAverageFacePoint3.getFirst() + currOffsets3.getFirst() * weight, // R.
                    currAverageFacePoint3.getSecond() + currOffsets3.getSecond() * weight, // G.
                    currAverageFacePoint3.getThird() + currOffsets3.getThird() * weight); // B.

            faceWShapeData.get(currTriangleTripletIndex).setPoint1Color(triangleColor1);
            faceWShapeData.get(currTriangleTripletIndex).setPoint2Color(triangleColor2);
            faceWShapeData.get(currTriangleTripletIndex).setPoint3Color(triangleColor3);

            currTriangleTripletIndex += 1;

        }

        // Note that the ordering of triangles in the array list equals the ordering indexed by the mesh.
        //return faceWShapeData; // Already changes by reference.

    } // getReferenceFaceColor().

    /**
     * Get the shape information for a given face from the corresponding CSV files:
     * <p>
     * "The n-th face will have the 3D coordinates of its vertices computed by adding the coordinates in sh_000.csv
     * (the average face shape) summed with the coordinate offsets in sh_00n.csv multiplied by the n-th weight in
     * sh_EV.csv".
     *
     * @param dataDir Directory containing the data files.
     * @param faceNum Number of the face to get shape data for (i.e., 1 through to 199 inclusive).
     * @param mesh    Mesh used to define the shape and color information.
     * @param sh000   Average shape information.
     * @param weight  Shape weight associated with the current face.
     * @return Array list of triangles representing the shape information of the given face number.
     */
    private ArrayList<Triangle> getReferenceFaceShape(File dataDir, int faceNum, ArrayList<Triplet> mesh,
                                                      ArrayList<Triplet> sh000, Double weight) {

        ArrayList<Triangle> shapeData = new ArrayList<>();

        ArrayList<Triplet> sh00n = getFaceDataFile(dataDir, faceNum, true); // Shape information offsets.

        // For every triangle (as indices) specified in the mesh.
        for (Triplet currTriangleTriplet : mesh) {

            int firstPointIndex = (int) currTriangleTriplet.getFirst() - 1; // i1
            Triplet currAverageFacePoint1 = sh000.get(firstPointIndex); // av1.
            Triplet currOffsets1 = sh00n.get(firstPointIndex); // off1.
            // p1 = ((av1.x + off1.x) * w, (av1.y + off1.y) * w, (av1.z + off1.z) * w).
            Point3D trianglePoint1 = new Point3D(
                    currAverageFacePoint1.getFirst() + currOffsets1.getFirst() * weight, // x.
                    currAverageFacePoint1.getSecond() + currOffsets1.getSecond() * weight, // y.
                    currAverageFacePoint1.getThird() + currOffsets1.getThird() * weight); // z.

            int secondPointIndex = (int) currTriangleTriplet.getSecond() - 1; // i2.
            Triplet currAverageFacePoint2 = sh000.get(secondPointIndex); // av2.
            Triplet currOffsets2 = sh00n.get(secondPointIndex); // off2.
            // p2 = ((av2.x + off2.x) * w, (av2.y + off2.y) * w, (av2.z + off2.z) * w).
            Point3D trianglePoint2 = new Point3D(
                    currAverageFacePoint2.getFirst() + currOffsets2.getFirst() * weight, // x.
                    currAverageFacePoint2.getSecond() + currOffsets2.getSecond() * weight, // y.
                    currAverageFacePoint2.getThird() + currOffsets2.getThird() * weight); // z.

            int thirdPointIndex = (int) currTriangleTriplet.getThird() - 1; // i3.
            Triplet currAverageFacePoint3 = sh000.get(thirdPointIndex); // av3.
            Triplet currOffsets3 = sh00n.get(thirdPointIndex); // off3.
            // p3 = ((av3.x + off3.x) * w, (av3.y + off3.y) * w, (av3.z + off3.z) * w).
            Point3D trianglePoint3 = new Point3D(
                    currAverageFacePoint3.getFirst() + currOffsets3.getFirst() * weight, // x.
                    currAverageFacePoint3.getSecond() + currOffsets3.getSecond() * weight, // y.
                    currAverageFacePoint3.getThird() + currOffsets3.getThird() * weight); // z.

            // Create new triangle of 3D points using: p1, p2, and p3.
            Triangle newTriangle = new Triangle(trianglePoint1, trianglePoint2, trianglePoint3);
            shapeData.add(newTriangle); // Add triangle to shape data.

        }

        // Note that the ordering of triangles in the array list equals the ordering indexed by the mesh.
        return shapeData;

    } // getReferenceFaceShape().

    /**
     * Read in a face data file - either shape or color information.
     *
     * @param dataDir Directory containing the data files.
     * @param faceNum Number of the face to get shape/color data for (i.e., 1 through to 199 inclusive).
     * @param shape   Whether getting shape data (true), or color data (false).
     * @return Array list of triplets which represent the data retrieved from the file in CSV format.
     */
    private ArrayList<Triplet> getFaceDataFile(File dataDir, int faceNum, boolean shape) {

        // Face shape/color data structure.
        ArrayList<Triplet> faceData = new ArrayList<>();

        // Get face number prefix correctly, which is padded with zeroes when required.
        String numSuffix = String.format("%03d", faceNum);

        // Get correct face data file name according to whether shape or color data is specified.
        String faceFileName;
        if (shape) {
            faceFileName = dataDir.getAbsolutePath() + "/sh_" + numSuffix + ".csv";
        } else {
            faceFileName = dataDir.getAbsolutePath() + "/tx_" + numSuffix + ".csv";
        }

        // Read every line from the CSV file.
        try (BufferedReader meshReader = new BufferedReader(new FileReader(faceFileName))) {

            String line;
            while ((line = meshReader.readLine()) != null) {

                String[] values = line.split(",");

                // Each line of the file contains a triple: x,y,z for shape data file - r,g,b for color data file.
                double firstIndex = Double.parseDouble(values[0]);
                double secondIndex = Double.parseDouble(values[1]);
                double thirdIndex = Double.parseDouble(values[2]);

                // Add triplet from the data file to the structure in order read.
                Triplet currTriangleIndices = new Triplet(firstIndex, secondIndex, thirdIndex);
                faceData.add(currTriangleIndices);

            }

        } catch (Exception e) {
            System.out.println("P2main.getFaceDataFile() Exception: " + e.getMessage());
            System.exit(-1);
        }

        return faceData;

    } // getFaceDataFile().

    /**
     * Read in a weight file from CSV.
     *
     * @param dataDir Directory of data containing the weight file (for shape and color).
     * @param shape   Whether getting the shape weight file (sh_ev.csv) or the color file (tx_ev.csv).
     * @return Array list of double representing the weights.
     */
    private ArrayList<Double> getWeightFile(File dataDir, boolean shape) {

        // List fo weights retrieved from the file to return.
        ArrayList<Double> weights = new ArrayList<>();

        // Get weight file name according to type of weight data file requested.
        String weightFileName;
        if (shape) {
            weightFileName = dataDir.getAbsolutePath() + "/sh_ev.csv";
        } else {
            weightFileName = dataDir.getAbsolutePath() + "/tx_ev.csv";
        }

        // Read every line from the CSV file.
        try (BufferedReader meshReader = new BufferedReader(new FileReader(weightFileName))) {

            String line;
            while ((line = meshReader.readLine()) != null) {

                // Save weights in order read to the weights data structure.
                double weight = Double.parseDouble(line);
                weights.add(weight);

            }

        } catch (Exception e) {
            System.out.println("P2main.getWeightFile() Exception: " + e.getMessage());
            System.exit(-1);
        }

        return weights;

    } // getWeightFile().

    /**
     * Read the mesh from file.
     *
     * @param dataDir Directory (File object) containing all of the needed data, namely the mesh data file.
     * @return List of tuple representing the mesh.
     */
    private ArrayList<Triplet> getMesh(File dataDir) {

        ArrayList<Triplet> mesh = new ArrayList<>(); // Mesh to populate and return.
        String meshFileName = dataDir.getAbsolutePath() + "/mesh.csv"; // Get mesh file given the data directory.

        // Read every line from the CSV file and get the resulting mesh.
        try (BufferedReader meshReader = new BufferedReader(new FileReader(meshFileName))) {

            String line;
            while ((line = meshReader.readLine()) != null) {

                String[] currIndices = line.split(","); // CSV file.

                // Each line of the mesh contains three indices representing a triangle.
                double firstIndex = Double.parseDouble(currIndices[0]);
                double secondIndex = Double.parseDouble(currIndices[1]);
                double thirdIndex = Double.parseDouble(currIndices[2]);

                Triplet currTriangleIndices = new Triplet(firstIndex, secondIndex, thirdIndex);
                mesh.add(currTriangleIndices);

            }

        } catch (Exception e) {
            System.out.println("P2main.getMesh() Exception: " + e.getMessage());
            System.exit(-1);
        }

        return mesh;

    } // getMesh().


} // Face3DReader{}.
