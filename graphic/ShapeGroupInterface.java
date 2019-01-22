package graphic;

import static graphic.DrawableInterface.doc;
import java.io.StringWriter;
import javafx.scene.Group;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import network.ClientSession;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Maillon de la chaine pour les formes complexes.
 *
 * @author Thomas
 */
public class ShapeGroupInterface extends DrawableInterface {
    
    public ShapeGroupInterface(DrawableInterface di) {
        super(di);
    }
    
    @Override
    public void executeRequest(ClientSession cs) {
        final Group group = init(cs, Group.class);
        Document originalDoc = doc;
        
        Node shape = originalDoc.getDocumentElement().getFirstChild();
        
        StringWriter sw;
        Transformer t;
        
        while(shape != null) {
            if (shape instanceof Element) {
                sw = new StringWriter();

                try {
                 t = TransformerFactory.newInstance().newTransformer();
                 t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                 t.setOutputProperty(OutputKeys.INDENT, "no");
                 t.transform(new DOMSource(shape), new StreamResult(sw));
                 System.out.println(">> " + sw.toString());
                 interpretXML(sw.toString(), cs, group);

                } catch (TransformerException te) {
                 System.out.println("nodeToString Transformer Exception");
                }
            }
            shape = shape.getNextSibling();
        }
        defaultGroup = cs.getGroup();
        updateCanvas(cs, group);
    }

    @Override
    public String getDescription() {
        return "shapegroup";
    }
}
