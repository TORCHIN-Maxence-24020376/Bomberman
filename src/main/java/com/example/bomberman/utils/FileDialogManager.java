package com.example.bomberman.utils;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestionnaire de dialogues pour la sauvegarde et le chargement de fichiers
 */
public class FileDialogManager {
    
    private static final String LEVELS_DIR = "levels";
    
    /**
     * Crée le répertoire des niveaux s'il n'existe pas
     */
    private static void createLevelsDirectory() {
        File dir = new File(LEVELS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    /**
     * Applique les styles aux boîtes de dialogue
     * @param dialog Le dialogue à styliser
     * @param dialogType Le type de dialogue ("save", "load" ou "new")
     */
    private static void applyDialogStyles(Dialog<?> dialog, String dialogType) {
        // Appliquer la classe CSS spécifique
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add(dialogType + "-dialog");
        
        // Charger le CSS de l'éditeur de niveau
        Scene scene = dialogPane.getScene();
        if (scene != null) {
            try {
                String cssPath = FileDialogManager.class.getResource("/com/example/bomberman/view/level-editor-styles.css").toExternalForm();
                scene.getStylesheets().add(cssPath);
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement du CSS: " + e.getMessage());
            }
        }
        
        // Appliquer des styles spécifiques aux boutons
        Button okButton = (Button) dialogPane.lookupButton(
                dialogPane.getButtonTypes().stream()
                        .filter(type -> type.getButtonData().isDefaultButton())
                        .findFirst()
                        .orElse(null)
        );
        
        if (okButton != null) {
            okButton.getStyleClass().add("ok-button");
        }
        
        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        if (cancelButton != null) {
            cancelButton.getStyleClass().add("cancel-button");
        }
    }
    
    /**
     * Affiche un dialogue de sauvegarde personnalisé
     * @param owner Fenêtre parente
     * @return Le chemin du fichier sélectionné ou null si annulé
     */
    public static String showSaveDialog(Stage owner) {
        createLevelsDirectory();
        
        // Créer le dialogue
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Sauvegarder le niveau");
        dialog.setHeaderText("Entrez un nom pour votre niveau");
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        
        // Boutons
        ButtonType saveButtonType = new ButtonType("Sauvegarder", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Champ de texte pour le nom du fichier
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 20));
        
        TextField fileName = new TextField();
        fileName.setPromptText("nom_du_niveau");
        
        Label nameLabel = new Label("Nom du niveau:");
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        grid.add(nameLabel, 0, 0);
        grid.add(fileName, 1, 0);
        
        // Liste des fichiers existants
        ListView<String> fileList = new ListView<>();
        loadExistingFiles(fileList);
        
        Label existingLabel = new Label("Niveaux existants:");
        existingLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(10, 0, 10, 0));
        content.getChildren().addAll(
            grid,
            existingLabel,
            fileList
        );
        
        dialog.getDialogPane().setContent(content);
        
        // Appliquer les styles
        applyDialogStyles(dialog, "save");
        
        // Sélection d'un fichier existant
        fileList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fileName.setText(newVal.replace(".level", ""));
            }
        });
        
        // Activer/désactiver le bouton de sauvegarde selon que le nom est vide ou non
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);
        
        fileName.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty());
        });
        
        // Convertir le résultat
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String name = fileName.getText().trim();
                if (!name.isEmpty()) {
                    return LEVELS_DIR + File.separator + name + ".level";
                }
            }
            return null;
        });
        
        // Afficher le dialogue et retourner le résultat
        return dialog.showAndWait().orElse(null);
    }
    
    /**
     * Affiche un dialogue de chargement personnalisé
     * @param owner Fenêtre parente
     * @return Le chemin du fichier sélectionné ou null si annulé
     */
    public static String showLoadDialog(Stage owner) {
        createLevelsDirectory();
        
        // Créer le dialogue
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Charger un niveau");
        dialog.setHeaderText("Sélectionnez un niveau à charger");
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        
        // Boutons
        ButtonType loadButtonType = new ButtonType("Charger", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loadButtonType, ButtonType.CANCEL);
        
        // Liste des fichiers existants
        ListView<String> fileList = new ListView<>();
        loadExistingFiles(fileList);
        
        Label existingLabel = new Label("Niveaux disponibles:");
        existingLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(10, 0, 10, 0));
        content.getChildren().addAll(
            existingLabel,
            fileList
        );
        
        dialog.getDialogPane().setContent(content);
        
        // Appliquer les styles
        applyDialogStyles(dialog, "load");
        
        // Activer/désactiver le bouton de chargement selon qu'un fichier est sélectionné ou non
        Button loadButton = (Button) dialog.getDialogPane().lookupButton(loadButtonType);
        loadButton.setDisable(true);
        
        fileList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            loadButton.setDisable(newVal == null);
        });
        
        // Convertir le résultat
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loadButtonType) {
                String selectedFile = fileList.getSelectionModel().getSelectedItem();
                if (selectedFile != null) {
                    return LEVELS_DIR + File.separator + selectedFile;
                }
            }
            return null;
        });
        
        // Afficher le dialogue et retourner le résultat
        return dialog.showAndWait().orElse(null);
    }
    
    /**
     * Charge la liste des fichiers existants
     */
    private static void loadExistingFiles(ListView<String> fileList) {
        createLevelsDirectory();
        
        File dir = new File(LEVELS_DIR);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".level"));
        
        List<String> fileNames = new ArrayList<>();
        boolean hasAutoSave = false;
        
        if (files != null) {
            for (File file : files) {
                if (file.getName().equals("autosave.level")) {
                    hasAutoSave = true;
                    continue; // On ne l'ajoute pas ici, on le traitera séparément
                }
                fileNames.add(file.getName());
            }
        }
        
        // Trier les noms de fichiers par ordre alphabétique
        fileNames.sort(String::compareTo);
        
        // Ajouter l'autosave au début s'il existe
        if (hasAutoSave) {
            fileList.getItems().clear();
            
            // Créer une cellule personnalisée pour la ListView
            fileList.setCellFactory(lv -> new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else if (item.equals("autosave.level")) {
                        setText("Dernière session (autosave.level)");
                        setStyle("-fx-font-weight: bold;");
                    } else {
                        setText(item);
                        setStyle("");
                    }
                }
            });
            
            // Ajouter l'autosave en premier
            fileList.getItems().add("autosave.level");
            
            // Ajouter le reste des fichiers
            fileList.getItems().addAll(fileNames);
        } else {
            fileList.getItems().setAll(fileNames);
        }
    }
} 