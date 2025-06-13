package com.example.bomberman;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Classe principale de l'application Bomberman
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            // Essayer de charger le menu principal d'abord
            FXMLLoader menuLoader = null;
            Scene scene = null;

            try {
                menuLoader = new FXMLLoader(Main.class.getResource("/com/example/bomberman/view/menu-view.fxml"));
                scene = new Scene(menuLoader.load(), 800, 600);
                
                // Charger le CSS du menu
                URL cssResource = getClass().getResource("/com/example/bomberman/view/menu-styles.css");
                if (cssResource != null) {
                    scene.getStylesheets().add(cssResource.toExternalForm());
                    System.out.println("Styles CSS du menu chargés !");
                }
                
                System.out.println("Menu principal chargé avec succès !");
            } catch (Exception menuError) {
                System.out.println("Impossible de charger le menu, chargement du jeu direct...");
                menuError.printStackTrace();

                // Fallback sur le jeu original
                FXMLLoader gameLoader = new FXMLLoader(Main.class.getResource("/com/example/bomberman/view/game-view.fxml"));
                scene = new Scene(gameLoader.load(), 1000, 900);
                
                // Charger le CSS du jeu
                URL cssResource = getClass().getResource("/com/example/bomberman/view/game-styles.css");
                if (cssResource != null) {
                    scene.getStylesheets().add(cssResource.toExternalForm());
                    System.out.println("Styles CSS du jeu chargés !");
                }
                
                System.out.println("Jeu direct chargé !");
            }

            // Configuration de la fenêtre principale
            primaryStage.setTitle("Super Bomberman");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);

            // Gestionnaire de fermeture propre
            primaryStage.setOnCloseRequest(event -> {
                Platform.exit();
                System.exit(0);
            });

            // Afficher la fenêtre
            primaryStage.show();

            System.out.println("Super Bomberman lancé avec succès !");

        } catch (Exception e) {
            System.err.println("Erreur lors du lancement de l'application: " + e.getMessage());
            e.printStackTrace();

            // Afficher une alerte d'erreur
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR
            );
            alert.setTitle("Erreur de démarrage");
            alert.setHeaderText("Impossible de démarrer l'application");
            alert.setContentText("Erreur: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        System.out.println("Application fermée proprement.");
    }

    /**
     * Point d'entrée principal
     */
    public static void main(String[] args) {
        System.out.println("Démarrage de Super Bomberman...");
        launch(args);
    }
}