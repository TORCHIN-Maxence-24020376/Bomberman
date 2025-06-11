package com.example.bomberman;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Programme de test pour vérifier le chargement des fichiers FXML
 */
public class FXMLTest extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Essayer de charger le fichier menu-view.fxml
            URL menuFxml = getClass().getResource("/com/example/bomberman/view/menu-view.fxml");
            if (menuFxml == null) {
                System.err.println("ERREUR: menu-view.fxml introuvable!");
            } else {
                System.out.println("menu-view.fxml trouvé à: " + menuFxml);
                try {
                    Parent menuRoot = FXMLLoader.load(menuFxml);
                    System.out.println("menu-view.fxml chargé avec succès!");
                } catch (Exception e) {
                    System.err.println("ERREUR lors du chargement de menu-view.fxml: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // Essayer de charger le fichier game-view.fxml
            URL gameFxml = getClass().getResource("/com/example/bomberman/view/game-view.fxml");
            if (gameFxml == null) {
                System.err.println("ERREUR: game-view.fxml introuvable!");
            } else {
                System.out.println("game-view.fxml trouvé à: " + gameFxml);
                try {
                    Parent gameRoot = FXMLLoader.load(gameFxml);
                    System.out.println("game-view.fxml chargé avec succès!");
                } catch (Exception e) {
                    System.err.println("ERREUR lors du chargement de game-view.fxml: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // Quitter le programme
            System.exit(0);
        } catch (Exception e) {
            System.err.println("Erreur générale: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
} 