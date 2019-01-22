package graphic;

import javafx.scene.shape.Circle;
import network.ClientSession;
import org.w3c.dom.Node;

/**
 * Maillon de la chaine pour les formes de type Circle.
 *
 * @author Thomas
 */
public class CircleInterface extends DrawableInterface {
    
    public CircleInterface(DrawableInterface di) {
        super(di);
    }
    
    @Override
    public void executeRequest(ClientSession cs) {
        final Circle circle = init(cs, Circle.class);
        
        //Center
        final Node center = DrawableInterface.doc.getElementsByTagName("vertices").item(0).getFirstChild();
        circle.setCenterX(Double.parseDouble(center.getFirstChild().getTextContent()));
        circle.setCenterY(Double.parseDouble(center.getLastChild().getTextContent()));
        
        //Radius
        circle.setRadius(Double.parseDouble(DrawableInterface.doc.getElementsByTagName("radius").item(0).getTextContent()));
        
        updateCanvas(cs, circle);
    }

    @Override
    public String getDescription() {
        return "circle";
    }
}
