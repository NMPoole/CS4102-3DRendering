import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.*;

/**
 * Implementation for interpolated shading of a triangle based on colours at the vertices within Java Paint context.
 * Code has been heavily adapted from the given source...
 *
 * @author David Haegele - https://gist.github.com/hageldave/391bacc787f31d2fb2c7a10d1446c5f6
 * @author 170004680
 */
public class TriangleInterpPaintContext implements PaintContext {


    private final float x1, x2, x3, y1, y2, y3; // For barycentric co-ordinates.
    private final float x23, x13, y23, y13;
    private final int c1, c2, c3; // Color information as integers (i.e., using getRGB()).
    private final float denominator;
    private final boolean antialiasing; // If using anti-aliasing - RECOMMENDED due to artefact prevalence without.
    private static final float[] MSAA_SAMPLES = new float[8]; // For anti-aliasing.

    // Color model to use: ARGB.
    private final DirectColorModel cm = new DirectColorModel(32,
            0x00ff0000,       // Red.
            0x0000ff00,       // Green.
            0x000000ff,       // Blue.
            0xff000000        // Alpha.
    );


    /**
     * Constructor:
     *
     * @param p1           Point 1 of triangle.
     * @param p2           Point 2 of triangle.
     * @param p3           Point 3 of triangle.
     * @param color1       Colour of point 1 of triangle.
     * @param color2       Colour of point 2 of triangle.
     * @param color3       Colour of point 3 of triangle.
     * @param xform        Affine transformation.
     * @param antialiasing Whether to use anti-aliasing or not.
     */
    public TriangleInterpPaintContext(Point2D.Float p1, Point2D.Float p2, Point2D.Float p3,
                                      Color color1, Color color2, Color color3,
                                      AffineTransform xform, boolean antialiasing) {

        c1 = color1.getRGB();
        c2 = color2.getRGB();
        c3 = color3.getRGB();

        p1 = (Point2D.Float) xform.transform(p1, new Point2D.Float());
        p2 = (Point2D.Float) xform.transform(p2, new Point2D.Float());
        p3 = (Point2D.Float) xform.transform(p3, new Point2D.Float());

        // Constants for barycentric co-ordinates.
        x1 = p1.x;
        x2 = p2.x;
        x3 = p3.x;
        y1 = p1.y;
        y2 = p2.y;
        y3 = p3.y;

        x23 = x2 - x3;
        x13 = x1 - x3;
        y23 = y2 - y3;
        y13 = y1 - y3;

        denominator = 1f / ((y23 * x13) - (x23 * y13));

        this.antialiasing = antialiasing;

    } // BarycentricGradientPaintContext().

    // Overridden Methods:

    /**
     * Overridden but not needed.
     */
    @Override
    public void dispose() {
        // Do nothing.
    } // dispose().

    /**
     * @return Colour model.
     */
    @Override
    public ColorModel getColorModel() {
        return cm;
    } // getColorModel().

    // Utility Functions:

    /**
     * Returns a Raster containing the colors generated for the graphics operation:
     * Interpolation of three colours at the vertices of a triangle.
     *
     * @param xA The x coordinate of the area in device space for which colors are generated.
     * @param yA The y coordinate of the area in device space for which colors are generated.
     * @param w  The width of the area in device space.
     * @param h  The width of the area in device space.
     * @return Returns a Raster containing the colors generated for the graphics operation.
     */
    @Override
    public Raster getRaster(int xA, int yA, int w, int h) {

        WritableRaster rast;
        rast = createRaster(w, h, new int[w * h]);

        // Fill data array with interpolated colors (barycentric co-ordinates).
        int[] data = dataFromRaster(rast);

        if (antialiasing) { // Recommended.
            fillRasterMSAA(xA, yA, w, h, data);
        } else {
            fillRaster(xA, yA, w, h, data);
        }

        return rast;

    } // getRaster().

    /**
     * Create raster for colour data.
     *
     * @param w    Raster width.
     * @param h    Raster height.
     * @param data Raster data.
     * @return WritableRaster object.
     */
    private WritableRaster createRaster(int w, int h, int[] data) {

        DataBufferInt buffer = new DataBufferInt(data, w * h);
        WritableRaster raster = Raster.createPackedRaster(buffer, w, h, w, cm.getMasks(), null);
        return raster;

    } // createRaster().

    /**
     * Used for filling data array with interpolated colors (barycentric co-ordinates). Get raster data.
     *
     * @param wr WritableRaster object.
     * @return Array of data from raster.
     */
    private static int[] dataFromRaster(WritableRaster wr) {

        return ((DataBufferInt) wr.getDataBuffer()).getData();

    } // dataFromRaster().

    /**
     * Fill raster data array with interpolated colour information.
     *
     * @param xA   The x co-ordinate of the area in device space for which colors are generated.
     * @param yA   The y co-ordinate of the area in device space for which colors are generated.
     * @param w    The width of the area in device space.
     * @param h    The height of the area in device space.
     * @param data Raster data to fill.
     */
    private void fillRaster(int xA, int yA, int w, int h, int[] data) {

        for (int i = 0; i < h; i++) {

            float y = yA + i + .5f;
            float ypart11 = -x23 * (y - y3);
            float ypart21 = x13 * (y - y3);

            for (int j = 0; j < w; j++) {

                float x = xA + j + .5f;

                // Calculate barycentric coordinates for (x, y).
                float l1 = (y23 * (x - x3) + ypart11) * denominator;
                float l2 = (-y13 * (x - x3) + ypart21) * denominator;
                float l3 = 1f - l1 - l2;

                // Determine color/
                int mix1;
                if (l1 < 0 || l2 < 0 || l3 < 0) {
                    mix1 = 0;
                } else {
                    mix1 = mixColor3(c1, c2, c3, l1, l2, l3);
                }

                data[i * w + j] = mix1;

            }

        }

    } // fillRaster().

    /**
     * Fill raster data array with interpolated colour information, with interpolation (4x multi-sampling).
     *
     * @param xA   The x co-ordinate of the area in device space for which colors are generated.
     * @param yA   The y co-ordinate of the area in device space for which colors are generated.
     * @param w    The width of the area in device space.
     * @param h    The height of the area in device space.
     * @param data Raster data to fill.
     */
    private void fillRasterMSAA(int xA, int yA, int w, int h, int[] data) {

        for (int i = 0; i < h; i++) {

            float y = yA + i + MSAA_SAMPLES[1];
            float ypart11 = -x23 * (y - y3);
            float ypart21 = x13 * (y - y3);
            y = yA + i + MSAA_SAMPLES[3];
            float ypart12 = -x23 * (y - y3);
            float ypart22 = x13 * (y - y3);
            y = yA + i + MSAA_SAMPLES[5];
            float ypart13 = -x23 * (y - y3);
            float ypart23 = x13 * (y - y3);
            y = yA + i + MSAA_SAMPLES[7];
            float ypart14 = -x23 * (y - y3);
            float ypart24 = x13 * (y - y3);

            for (int j = 0; j < w; j++) {

                float x = xA + j + MSAA_SAMPLES[0];
                float xpart11 = y23 * (x - x3);
                float xpart21 = -y13 * (x - x3);
                x = xA + j + MSAA_SAMPLES[2];
                float xpart12 = y23 * (x - x3);
                float xpart22 = -y13 * (x - x3);
                x = xA + j + MSAA_SAMPLES[4];
                float xpart13 = y23 * (x - x3);
                float xpart23 = -y13 * (x - x3);
                x = xA + j + MSAA_SAMPLES[6];
                float xpart14 = y23 * (x - x3);
                float xpart24 = -y13 * (x - x3);

                // Calculate barycentric coordinates for the 4 sub pixel samples.
                float l11 = (xpart11 + ypart11) * denominator;
                float l21 = (xpart21 + ypart21) * denominator;
                float l31 = 1f - l11 - l21;

                float l12 = (xpart12 + ypart12) * denominator;
                float l22 = (xpart22 + ypart22) * denominator;
                float l32 = 1f - l12 - l22;

                float l13 = (xpart13 + ypart13) * denominator;
                float l23 = (xpart23 + ypart23) * denominator;
                float l33 = 1f - l13 - l23;

                float l14 = (xpart14 + ypart14) * denominator;
                float l24 = (xpart24 + ypart24) * denominator;
                float l34 = 1f - l14 - l24;

                // Determine sample colors and weights (out of triangle samples have 0 weight).
                int mix1, mix2, mix3, mix4;
                float w1, w2, w3, w4;

                if (l11 < 0 || l21 < 0 || l31 < 0) {
                    mix1 = 0;
                    w1 = 0f;
                } else {
                    mix1 = mixColor3(c1, c2, c3, l11, l21, l31);
                    w1 = 1f;
                }

                if (l12 < 0 || l22 < 0 || l32 < 0) {
                    mix2 = 0;
                    w2 = 0f;
                } else {
                    mix2 = mixColor3(c1, c2, c3, l12, l22, l32);
                    w2 = 1f;
                }

                if (l13 < 0 || l23 < 0 || l33 < 0) {
                    mix3 = 0;
                    w3 = 0f;
                } else {
                    mix3 = mixColor3(c1, c2, c3, l13, l23, l33);
                    w3 = 1f;
                }

                if (l14 < 0 || l24 < 0 || l34 < 0) {
                    mix4 = 0;
                    w4 = 0f;
                } else {
                    mix4 = mixColor3(c1, c2, c3, l14, l24, l34);
                    w4 = 1f;
                }

                int color = mixColor4(mix1, mix2, mix3, mix4, w1, w2, w3, w4);
                data[i * w + j] = scaleColorAlpha(color, (w1 + w2 + w3 + w4) * .25f);

            }

        }

    } // fillRasterMSAA().

    /**
     * Mix 3 colors according to given weights.
     *
     * @param c1 Color 1.
     * @param c2 Color 2.
     * @param c3 Color 3.
     * @param m1 Weight 1.
     * @param m2 Weight 2.
     * @param m3 Weight 3.
     * @return Mixed colour integer representation.
     */
    private static int mixColor3(int c1, int c2, int c3, float m1, float m2, float m3) {

        float normalize = 1f / (m1 + m2 + m3);
        float a = (a(c1) * m1 + a(c2) * m2 + a(c3) * m3) * normalize;
        float r = (r(c1) * m1 + r(c2) * m2 + r(c3) * m3) * normalize;
        float g = (g(c1) * m1 + g(c2) * m2 + g(c3) * m3) * normalize;
        float b = (b(c1) * m1 + b(c2) * m2 + b(c3) * m3) * normalize;
        return argb((int) a, (int) r, (int) g, (int) b);

    } // mixColor3().

    /**
     * Mix 4 colors according to given weights.
     *
     * @param c1 Color 1.
     * @param c2 Color 2.
     * @param c3 Color 3.
     * @param c4 Color 4.
     * @param m1 Weight 1.
     * @param m2 Weight 2.
     * @param m3 Weight 3.
     * @param m4 Weight 4.
     * @return Mixed colour integer representation.
     */
    private static int mixColor4(int c1, int c2, int c3, int c4, float m1, float m2, float m3, float m4) {

        float normalize = 1f / (m1 + m2 + m3 + m4);
        float a = (a(c1) * m1 + a(c2) * m2 + a(c3) * m3 + a(c4) * m4) * normalize;
        float r = (r(c1) * m1 + r(c2) * m2 + r(c3) * m3 + r(c4) * m4) * normalize;
        float g = (g(c1) * m1 + g(c2) * m2 + g(c3) * m3 + g(c4) * m4) * normalize;
        float b = (b(c1) * m1 + b(c2) * m2 + b(c3) * m3 + b(c4) * m4) * normalize;
        return argb((int) a, (int) r, (int) g, (int) b);

    } // mixColor4().

    /**
     * Scale alpha component of color.
     *
     * @param color Color to scale alpha of.
     * @param m Float.
     * @return Integer.
     */
    private static int scaleColorAlpha(int color, float m) {

        float normalize = 1f / 255f;
        float af = a(color) * normalize * m;
        int a = (((int) (af * 255f)) & 0xff) << 24;
        return (color & 0x00ffffff) | a;

    } // scaleColorAlpha().

    /**
     * Get alpha component of ARGB.
     *
     * @param argb ARGB integer.
     * @return Alpha component.
     */
    private static int a(int argb) {
        return (argb >> 24) & 0xff;
    } // a().

    /**
     * Get red component of ARGB.
     *
     * @param argb ARGB integer.
     * @return Alpha component.
     */
    private static int r(int argb) {
        return (argb >> 16) & 0xff;
    } // r().

    /**
     * Get green component of ARGB.
     *
     * @param argb ARGB integer.
     * @return Alpha component.
     */
    private static int g(int argb) {
        return (argb >> 8) & 0xff;
    } // g().

    /**
     * Get blue component of ARGB.
     *
     * @param argb ARGB integer.
     * @return Alpha component.
     */
    private static int b(int argb) {
        return (argb) & 0xff;
    } // b().

    /**
     * Form ARGB integer given a, r, g, and b component integers separately.
     *
     * @param a Alpha component.
     * @param r Red component.
     * @param g Green component.
     * @param b Blue component.
     * @return ARGB integer.
     */
    private static int argb(final int a, final int r, final int g, final int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    } // argb().


} // TriangleInterpPaintContext{}.
