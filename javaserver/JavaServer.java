package javaserver;

import network.Server;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Classe de l'application.
 * <p>
 * Elle sert à la création de la fenetre JavaFX et à l'instanciation du serveur.
 * 
 * @author Thomas
 */
public class JavaServer extends Application {
    //Group racine de l'arboresence
    public static volatile Group Root = null;
    
    @Override
    public void start(Stage primaryStage) {
        Group root = new Group();                       //Main group
        Scene scene = new Scene(root, 600, 300);        //Main scene
        Root = root;                                    //Affectation du groupe principal à la variable globale
        
        primaryStage.setTitle("JavaFX Paint Window");   //Titre de la fenetre
        primaryStage.setScene(scene);                   //Affectation de la scene principale
        primaryStage.show();                            //Affichage sur la fenetre
    }

    /**
     * Fonction main.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception{
        Server server = new Server();   //Instancie le serveur
        server.start();                 //Boot le serveur
        launch(args);                   //Lancemant de l'application JavaFX
    }   
}