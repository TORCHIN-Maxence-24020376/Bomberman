package com.example.bomberman;

import com.example.bomberman.ProfileManager;
import com.example.bomberman.SoundManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Classe principale de l'application Bomberman améliorée
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            // Initialiser les gestionnaires
            ProfileManager.getInstance();
            SoundManager soundManager = SoundManager.getInstance();

            // Charger l'interface du menu principal
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("menu-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);

            // Ajouter le CSS
            var cssResource = getClass().getResource("styles.css");
            if (cssResource != null) {
                scene.getStylesheets().add(cssResource.toExternalForm());
            }

            // Configuration de la fenêtre principale
            primaryStage.setTitle("Super Bomberman - Version Améliorée");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);

            // Ajouter une icône si disponible
            try {
                var iconResource = getClass().getResourceAsStream("icon.png");
                if (iconResource != null) {
                    primaryStage.getIcons().add(new Image(iconResource));
                }
            } catch (Exception e) {
                System.out.println("Icône non trouvée, utilisation de l'icône par défaut");
            }

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
            System.out.println("Fonctionnalités disponibles :");
            System.out.println("- Jeu 2 joueurs avec power-ups");
            System.out.println("- Gestion des profils joueurs");
            System.out.println("- Éditeur de niveaux");
            System.out.println("- Système de sons");
            System.out.println("- Interface utilisateur améliorée");

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
        // Configuration des propriétés système si nécessaire
        System.setProperty("javafx.preloader", "com.example.bomberman.utils.Preloader");

        // Vérifier la version de Java
        checkJavaVersion();

        // Lancer l'application JavaFX
        launch(args);
    }

    /**
     * Vérifie la version de Java
     */
    private static void checkJavaVersion() {
        String javaVersion = System.getProperty("java.version");
        System.out.println("Version Java détectée: " + javaVersion);

        // Avertir si la version de Java est trop ancienne
        String[] versionParts = javaVersion.split("\\.");
        try {
            int majorVersion = Integer.parseInt(versionParts[0]);
            if (majorVersion < 11) {
                System.err.println("ATTENTION: Java 11 ou supérieur est recommandé pour cette application.");
                System.err.println("Certaines fonctionnalités peuvent ne pas fonctionner correctement.");
            }
        } catch (Exception e) {
            System.err.println("Impossible de déterminer la version de Java.");
        }
    }
}