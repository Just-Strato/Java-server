package graphic;

import javafx.scene.shape.Polygon;
import network.ClientSession;
import org.w3c.dom.*;

/**
 * Maillon de la chaine pour les formes du type Shape, Segment et Rectangle.
 *
 * @author Thomas
 */
public class ShapeInterface extends DrawableInterface {

    public ShapeInterface(DrawableInterface di) {
        super(di);
    }
    @Override
    public void executeRequest(ClientSession cs) {
        final Polygon polygon = init(cs, Polygon.class);    
        
        //Points
        final Node verticeNode = DrawableInterface.doc.getElementsByTagName("vertices").item(0);
        final NodeList vertices = verticeNode.getChildNodes();
        
        //Si des points ont déja été définis, on les reset.
        if (!polygon.getPoints().isEmpty()) polygon.getPoints().clear();
        
        for (int i = 0; i<vertices.getLength(); i++) {
            if(vertices.item(i).getNodeType() == Node.ELEMENT_NODE) {
                final Element e = (Element) vertices.item(i);
                polygon.getPoints().add(new Double(e.getElementsByTagName("x").item(0).getTextContent()));
                polygon.getPoints().add(new Double(e.getElementsByTagName("y").item(0).getTextContent()));
            }				
        }
        
        updateCanvas(cs, polygon);
        
        polygon.setStroke(polygon.getFill());
        if (polygon.getPoints().size() < 3) polygon.setStrokeWidth(1); //Si c'est un segment, on affiche le bord
        else polygon.setStrokeWidth(0);
    }

    @Override
    public String getDescription() {
        return "shape";
    }
}
