package com.example.bomberman.utils;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
        
        // Boutons
        ButtonType saveButtonType = new ButtonType("Sauvegarder", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Champ de texte pour le nom du fichier
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField fileName = new TextField();
        fileName.setPromptText("nom_du_niveau");
        
        grid.add(new Label("Nom du niveau:"), 0, 0);
        grid.add(fileName, 1, 0);
        
        // Liste des fichiers existants
        ListView<String> fileList = new ListView<>();
        loadExistingFiles(fileList);
        
        VBox content = new VBox(10);
        content.getChildren().addAll(
            grid,
            new Label("Niveaux existants:"),
            fileList
        );
        
        dialog.getDialogPane().setContent(content);
        
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
        
        // Boutons
        ButtonType loadButtonType = new ButtonType("Charger", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loadButtonType, ButtonType.CANCEL);
        
        // Liste des fichiers existants
        ListView<String> fileList = new ListView<>();
        loadExistingFiles(fileList);
        
        VBox content = new VBox(10);
        content.getChildren().addAll(
            new Label("Niveaux disponibles:"),
            fileList
        );
        
        dialog.getDialogPane().setContent(content);
        
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
                        setText(item);
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