/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphic;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import network.ClientSession;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;






/**
 *
 * @author Thomas
 */
public abstract class DrawableInterface {
    private DrawableInterface next = null;
    
    private DocumentBuilderFactory factory;
    private StringBuilder xmlStringBuilder;
    private ByteArrayInputStream input = null;
    private DocumentBuilder builder = null;
    static protected Document doc = null;
    
    static protected Group defaultGroup;
    
    public DrawableInterface(DrawableInterface di) {
        this.next = di;
    }
    
    public DrawableInterface getNext() {
        return next;
    }
    
    public abstract void executeRequest(ClientSession cs);
    public abstract String getDescription();
    
    public boolean canInterpret(String choix) {
        return this.getDescription().equals(choix);
    }
    
    public static boolean validateXMLSchema(String xsdName, String xml){
        
        try {
            SchemaFactory factory =   SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File("src/ressources/"+ xsdName +".xsd"));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));
        } catch (IOException | SAXException e) {
            System.out.println("Exception: "+e.getMessage());
            return false;
        }
        return true;
    }
    
    public void interpretXML(String ligne, ClientSession cs) {
        if (defaultGroup == null) defaultGroup = cs.getGroup();

        //Interpretation de l'xml recu
        factory = DocumentBuilderFactory.newInstance();
        xmlStringBuilder = new StringBuilder();
        
        xmlStringBuilder.append(ligne);
        
        try {
            builder = factory.newDocumentBuilder();
            input = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
            doc = builder.parse(input);
        }
        catch(UnsupportedEncodingException uee) {/* UTF-8 est codé en dur, donc aucune exeption ne devrait etre levée */}
        catch(SAXException saxe) { System.out.println("Erreur : XML corrompu"); }
        catch(IOException ioe) { System.out.println("ioe"); }
        catch(ParserConfigurationException pce) { System.out.println("pce"); }
        interact(doc.getDocumentElement().getTagName(), cs);
        //if (validateXMLSchema(doc.getDocumentElement().getTagName(), ligne)) interact(doc.getDocumentElement().getTagName(), cs);
        //else System.out.println("XML recu n'est pas validé par l'XSD correspondant");
    }
    
    public void interpretXML(String ligne, ClientSession cs, Group g) {
        defaultGroup = g;
        interpretXML(ligne, cs);
        defaultGroup = cs.getGroup();
    }
    
    public void interact(String choix, ClientSession cs) {
        if (this.canInterpret(choix)) executeRequest(cs);
        else if (this.next != null) this.next.interact(choix, cs);
    }
    
    protected<T extends javafx.scene.Node> T init(ClientSession cs, Class T) {
        //Structure T {Shape - Circle - Group} a dessiner
        T element = null;
        
        String id = doc.getDocumentElement().getAttribute("id");
        Object temp = cs.retrieveShape(id);
        
        if (temp != null) element = (T) temp;
        else {
            try {
                element = (T) T.newInstance();
                element.setId(id);
            }
            catch(Exception e) {}
        }
        
        return element;
    }
    
    protected<T extends Node> void updateCanvas(ClientSession cs, T element) {
        if (element instanceof Shape) { ((Shape) element).setFill(retrieveColor()); }
        
        if (element.getParent() == null) {
            Platform.runLater(new Runnable() {
                @Override public void run() {
                    defaultGroup.getChildren().add(element);
                }
            });
        }
    }
    
    protected Color retrieveColor() {
        return new Color(   Double.parseDouble(doc.getElementsByTagName("r").item(0).getTextContent()), 
                            Double.parseDouble(doc.getElementsByTagName("g").item(0).getTextContent()), 
                            Double.parseDouble(doc.getElementsByTagName("b").item(0).getTextContent()), 
                            1);
    }
}
