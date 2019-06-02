package com.mycompany.binferprojectclient;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


public class MainApp extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
       
       BorderPane root = (BorderPane)FXMLLoader.load(getClass().getResource("/fxml/Scene.fxml"));
    
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        primaryStage.setTitle("NY Best Seller Books List (latest)");
        primaryStage.setScene(scene);
        primaryStage.show();
        
    }
    

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
