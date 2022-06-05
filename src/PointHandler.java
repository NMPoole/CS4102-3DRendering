import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * PointHandler: Handles the addition of points on the 2D canvas.
 *
 * @author 170004680
 */
class PointHandler extends MouseAdapter {


    MainUIPanel mainUiPanel; // UIPanel instance this listener tied to.


    /**
     * Constructor:
     *
     * @param mainUiPanel UIPanel instance this object is a listener for.
     */
    public PointHandler(MainUIPanel mainUiPanel) {

        this.mainUiPanel = mainUiPanel;

    } // PointLocator().

    /**
     * Mouse Press Handler:
     *
     * @param mouseEvent The mouse event that triggered this action.
     */
    @Override
    public void mousePressed(MouseEvent mouseEvent) {

        Point point = mouseEvent.getPoint(); // Get position where mouse was clicked.
        mainUiPanel.setClickedPoint(point);

    } // mousePressed().


} // PointHandler{}.
