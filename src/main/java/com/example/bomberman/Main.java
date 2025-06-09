package com.example.bomberman;
import com.example.bomberman.service.ProfileManager;
import com.example.bomberman.service.SoundManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Classe principale de l'application Bomberman
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            // Initialiser les gestionnaires
            ProfileManager.getInstance();
            SoundManager soundManager = SoundManager.getInstance();

            // Essayer de charger le menu principal d'abord
            FXMLLoader menuLoader = null;
            Scene scene = null;

            try {
                menuLoader = new FXMLLoader(Main.class.getResource("/com/example/bomberman/view/menu-view.fxml"));
                scene = new Scene(menuLoader.load(), 800, 600);
                System.out.println("Menu principal chargé avec succès !");
            } catch (Exception menuError) {
                System.out.println("Impossible de charger le menu, chargement du jeu direct...");

                // Fallback sur le jeu original
                FXMLLoader gameLoader = new FXMLLoader(Main.class.getResource("/com/example/bomberman/view/game-view.fxml"));
                scene = new Scene(gameLoader.load(), 800, 600);
                System.out.println("Jeu direct chargé !");
            }

            // Ajouter le CSS si disponible
            try {
                var cssResource = getClass().getResource("/com/example/bomberman/styles.css");
                if (cssResource != null) {
                    scene.getStylesheets().add(cssResource.toExternalForm());
                    System.out.println("Styles CSS chargés !");
                }
            } catch (Exception cssError) {
                System.out.println("CSS non trouvé, utilisation du style par défaut");
            }

            // Configuration de la fenêtre principale
            primaryStage.setTitle("Super Bomberman - Version Améliorée");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);

            // Gestionnaire de fermeture propre
            primaryStage.setOnCloseRequest(event -> {
                // Sauvegarder les profils
                ProfileManager.getInstance().saveProfiles();

                // Arrêter la musique
                soundManager.stopBackgroundMusic();

                // Quitter l'application
                Platform.exit();
                System.exit(0);
            });

            // Afficher la fenêtre
            primaryStage.show();

            // Démarrer la musique du menu
            soundManager.playBackgroundMusic("/sounds/menu_music.mp3");

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

        // Nettoyage final
        ProfileManager.getInstance().saveProfiles();
        SoundManager.getInstance().stopBackgroundMusic();

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