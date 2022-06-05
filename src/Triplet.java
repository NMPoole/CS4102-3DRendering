import java.util.ArrayList;

/**
 * Custom data object to represent a triplet.
 *
 * @author 170004680
 */
public class Triplet {


    private final double first; // First tuple member.
    private final double second; // Second tuple member.
    private final double third; // Third tuple member.

    /**
     * Constructor:
     *
     * @param first  First tuple member.
     * @param second Second tuple member.
     * @param third  Third tuple member.
     */
    public Triplet(double first, double second, double third) {

        this.first = first;
        this.second = second;
        this.third = third;

    } // Triplet().

    // Getters and Setters:

    /**
     * @return Array list of the triple values (ordered from first to last element).
     */
    public ArrayList<Double> getTriplet() {

        ArrayList<Double> triplet = new ArrayList<>();

        triplet.add(first);
        triplet.add(second);
        triplet.add(third);

        return triplet;

    } // getTriplet().

    /**
     * @return first.
     */
    public double getFirst() {
        return first;
    } // getFirst().

    /**
     * @return second.
     */
    public double getSecond() {
        return second;
    } // getSecond().

    /**
     * @return third.
     */
    public double getThird() {
        return third;
    } // getThird().


} // Triplet{}.
