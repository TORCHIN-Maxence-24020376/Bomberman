package com.example.bomberman.utils;

import com.example.bomberman.controller.ControlsEditorController;
import com.example.bomberman.models.entities.PlayerProfile;
import com.example.bomberman.service.ProfileManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Classe utilitaire pour l'interface utilisateur
 */
public class UIUtils {

    /**
     * Ouvre l'éditeur de contrôles pour un profil spécifique
     * @param profile Le profil à éditer
     * @return true si l'édition a réussi, false sinon
     */
    public static boolean openControlsEditor(PlayerProfile profile) {
        if (profile == null) return false;
        
        try {
            // Charger le fichier FXML de l'éditeur de contrôles
            FXMLLoader loader = new FXMLLoader(UIUtils.class.getResource("/com/example/bomberman/view/controls-editor.fxml"));
            Parent root = loader.load();
            
            // Récupérer le contrôleur
            ControlsEditorController controller = loader.getController();
            controller.setProfile(profile);
            
            // Créer la scène
            Scene scene = new Scene(root);
            
            // Ajouter les styles
            scene.getStylesheets().add(UIUtils.class.getResource("/com/example/bomberman/view/styles.css").toExternalForm());
            
            // Créer et configurer la fenêtre
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Personnalisation des contrôles");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            
            // Afficher la fenêtre
            dialogStage.showAndWait();
            
            // S'assurer que les profils sont sauvegardés
            ProfileManager.getInstance().saveProfiles();
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur est survenue lors de l'ouverture de l'éditeur de contrôles: " + e.getMessage(), Alert.AlertType.ERROR);
            return false;
        }
    }
    
    /**
     * Affiche une alerte
     */
    public static void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 