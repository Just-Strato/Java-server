package network;

import graphic.ShapeInterface;
import graphic.CircleInterface;
import graphic.ShapeGroupInterface;
import graphic.DrawableInterface;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javaserver.*;


/**
 * Représente une session client sur le serveur.
 * <p>
 * Il est défini par le socket de connexion ainsi que l'identifiant de connexion
 * tout deux délivrés par le serveur. Chaque session dispose d'un Group JavaFX
 * qui deviendra la racine des formes insérées. L'identifiant permettant le
 * dispatch dans le bon Group. Son parent direct est le Group root du Serveur.
 *
 * @author Thomas
 */
public class ClientSession extends Thread {
    ///Membres spécifiques au réseau
    private final Socket socket;
    private final int noConnexion;
    
    //Groupe racine
    private Group SceneGroup = new Group();
    
    //Chaine de responsabilité
    private static DrawableInterface chain = null;
    
    //Buffer d'interception des data envoyées depuis le client
    private final BufferedReader inputStream;
    
    public ClientSession(Socket s, ThreadGroup tg, int i) throws IOException {
        //Initialisation des membres
        super(tg, "ClientSession");
        this.socket = s;
        this.noConnexion = i;
        
        //Initialisation du buffer sur le flux d'entrée du socket
        this.inputStream = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        
        //Initialisation de la chaine de responsabilité
        chain = new ShapeInterface(chain);
        chain = new CircleInterface(chain);
        chain = new ShapeGroupInterface(chain);
        
        //Ajout du Group racine client au Group racine serveur
        Platform.runLater(new Runnable() {
            @Override public void run() {
                JavaServer.Root.getChildren().add(SceneGroup);
            }
        });
    }
    
    /**
     * Accesseur sur le Group racine du client.
     *
     * @return Le membre SceneGroup.
     */
    public Group getGroup() { return SceneGroup; }
    
    /**
     * Récupère l'ensemble des noeuds fils de la racine client.
     *
     * @return ArrayList contenant les noeuds.
     */
    private ArrayList<Node> getAllNodes() {
        ArrayList<Node> nodes = new ArrayList<Node>();
        addAllDescendents(SceneGroup, nodes);
        return nodes;
    }
    
    /**
     * Methode récursive d'insertion de l'ensemble des noeuds fils d'un noeud.
     *
     * @param parent Le noeud racine de la recherche.
     * @param nodes  La collection de noeuds recipient de la recherche.
     */
    private void addAllDescendents(Parent parent, ArrayList<Node> nodes) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            nodes.add(node);
            if (node instanceof Parent) addAllDescendents((Parent)node, nodes);
        }
    }
    
    /**
     * Récupère un noeud selon un identifiant.
     *
     * @param id L'identifiant XML du noeud.
     * @return   Le noeud s'il est trouvé, null sinon.
     */
    public Node retrieveNode(String id) {
        for (javafx.scene.Node node : getAllNodes()) {
            if (node != null && node.getId().equals(id)) return node;
        }
        return null;
    }
    
    @Override
    public void run() {
        String ligne; //ligne traitée dans les données envoyées
        try {
            while(!isInterrupted()) {
                ligne = inputStream.readLine(); //Data envoyé par le client (par ligne)
                if (ligne != null) {
                    System.out.println("Recieved data from client ["+noConnexion+"] : "+ligne);
                    chain.interpretXML(ligne, this);
                }
                sleep(5);
            }
        }
        catch(InterruptedException ie){/*FIN DU THREAD*/}
        catch(IOException ioe){}
    }
}
