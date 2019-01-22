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
 * Maillon abstrait de la chaine de responsabilité.
 * <p>
 * Il dispose de membres nécessaires à l'interprétation de l'XML reçu.
 * L'XML (String) est transformé en DOM XMLDocument pour être parcourable avec 
 * des méthodes DOM fournies par le package org.w3c.dom
 *
 * @author Thomas
 */
public abstract class DrawableInterface {
    //Maillon suivant de la chaine de responsabilité
    private DrawableInterface next = null;
    
    //Membres nécéssaires au parsage de l'XML
    private DocumentBuilderFactory factory;
    private StringBuilder xmlStringBuilder;
    private ByteArrayInputStream input = null;
    private DocumentBuilder builder = null;
    
    //Le DOM XMLDocument 
    static protected Document doc = null;
    
    //La racine à partir de laquelle les formes vont etre insérées
    //Généralement, la racine du client
    static protected Group defaultGroup;
    
    public DrawableInterface(DrawableInterface di) {
        this.next = di;
    }
    
    /**
     * Accesseur sur le maillon suivant
     *
     * @return Le maillon suivant s'il existe, null sinon;
     */
    public DrawableInterface getNext() { return next; }
    
    /**
     * Traitement de la requete.
     * <p>
     * La méthode est appellé par son maillon selon la forme à interpréter.
     * La forme, simple ou complexe est insérée dans le Group defaultGroup.
     *
     * @param cs La session du client.
     */
    public abstract void executeRequest(ClientSession cs);

    /**
     * Methode d'identification du maillon à utiliser.
     *
     * @return La String d'identification.
     */
    public abstract String getDescription();
    
    /**
     * Routine de concordance maillon/forme.
     * <p>
     * Le nom de la balise XML est mis en comparaison avec la String 
     * d'identification du maillon.
     *
     * @param choix Le tagName correspondant à une forme.
     * @return      Booléen sur l'égalité tagName = identificateur
     */
    public boolean canInterpret(String choix) {
        return this.getDescription().equals(choix);
    }
    
    /**
     * Vérifie un XML recu par son schéma XSD correspondant.
     *
     * @param xsdName Nom du document XSD. (le tagName de la racine de l'XML.)
     * @param xml     XML au format String à faire valider.
     * @return        true si l'XML est valide, false sinon.
     */
    public static boolean validateXMLSchema(String xsdName, String xml){
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File("src/ressources/"+ xsdName +".xsd"));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));
        } catch (IOException | SAXException e) {
            System.out.println("Exception: "+e.getMessage());
            return false;
        }
        return true;
    }
    
    /**
     * Interprète l'XML recu en XMLDocument.
     * <p>
     * L'XML recu dans le buffer est interprété ligne par ligne.
     *
     * @param ligne L'XML à interpréter
     * @param cs    La session client
     */
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
        //interact(doc.getDocumentElement().getTagName(), cs);
        if (validateXMLSchema(doc.getDocumentElement().getTagName(), ligne)) interact(doc.getDocumentElement().getTagName(), cs);
        else System.out.println("XML recu n'est pas validé par l'XSD correspondant");
    }
    
    /**
     * Interprète l'XML recu en XMLDocument.
     * <p>
     * L'XML recu dans le buffer est interprété ligne par ligne. Un Group JavaFX
     * racine dans lequel les formes vont etres insérées est aussi spécifié.
     *
     * @param ligne L'XML à interpréter
     * @param cs    La session client
     * @param g     Le Group racine d'insertion
     */
    public void interpretXML(String ligne, ClientSession cs, Group g) {
        defaultGroup = g;
        interpretXML(ligne, cs);
        defaultGroup = cs.getGroup();
    }
    
    /**
     * Parcours de la chaine de responsabilité.
     *
     * @param choix Lnom de la forme à comparer.
     * @param cs    La session client.
     */
    public void interact(String choix, ClientSession cs) {
        if (this.canInterpret(choix)) executeRequest(cs);
        else if (this.next != null) this.next.interact(choix, cs);
    }
    
    /**
     * Initialise la forme à insérer dans l'arboressence.
     *
     * @param <T> La Class de la forme.
     * @param cs  La session client.
     * @param T   Le type de l'instance de classe à renvoyer.
     * @return    Le noeud s'il a été retrouvé dans l'arboresence. Une nouvelle instance sinon.
     */
    protected<T extends javafx.scene.Node> T init(ClientSession cs, Class T) {
        //Structure T {Shape - Circle - Group} a dessiner
        T element = null;
        
        String id = doc.getDocumentElement().getAttribute("id");
        Node temp = cs.retrieveNode(id);
        
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
    
    /**
     * Met à jour la fenetre de dessin.
     * <p>
     * La forme spécifiée en paramètre est ensuite insérée dans l'arboresence
     * si elle n'y figurait pas avant.
     *
     * @param <T>       Le type de Noeud à insérer.
     * @param cs        La session client.
     * @param element   Le Noeud à mettre à jour.
     */
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
    
    /**
     * Récupère une couleur contenue dans un XML
     *
     * @return La couleur récupérée.
     */
    protected Color retrieveColor() {
        return new Color(   Double.parseDouble(doc.getElementsByTagName("r").item(0).getTextContent()), 
                            Double.parseDouble(doc.getElementsByTagName("g").item(0).getTextContent()), 
                            Double.parseDouble(doc.getElementsByTagName("b").item(0).getTextContent()), 
                            1);
    }
}
