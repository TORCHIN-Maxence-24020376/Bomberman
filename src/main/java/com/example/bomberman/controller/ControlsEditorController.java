package com.example.bomberman.controller;

import com.example.bomberman.models.entities.PlayerProfile;
import com.example.bomberman.service.ProfileManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.util.Pair;
import javafx.scene.Node;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controleur pour l'editeur de controles
 */
public class ControlsEditorController implements Initializable {

    @FXML
    private ComboBox<PlayerProfile> playerCombo;
    
    @FXML
    private GridPane controlsGrid;
    
    @FXML
    private Button upButton;
    
    @FXML
    private Button downButton;
    
    @FXML
    private Button leftButton;
    
    @FXML
    private Button rightButton;
    
    @FXML
    private Button bombButton;
    
    @FXML
    private Button specialButton;
    
    @FXML
    private Button resetButton;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;
    
    private ProfileManager profileManager;
    private PlayerProfile currentProfile;
    private Map<String, Button> controlButtons;
    private Map<String, KeyCode> tempControls;
    private Button activeButton;
    private Stage keyListenerStage;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        profileManager = ProfileManager.getInstance();
        
        // Initialiser la map des boutons de controle
        controlButtons = new HashMap<>();
        controlButtons.put(PlayerProfile.ACTION_UP, upButton);
        controlButtons.put(PlayerProfile.ACTION_DOWN, downButton);
        controlButtons.put(PlayerProfile.ACTION_LEFT, leftButton);
        controlButtons.put(PlayerProfile.ACTION_RIGHT, rightButton);
        controlButtons.put(PlayerProfile.ACTION_BOMB, bombButton);
        controlButtons.put(PlayerProfile.ACTION_SPECIAL, specialButton);
        
        // Configurer les gestionnaires d'evenements pour les boutons de controle
        for (Map.Entry<String, Button> entry : controlButtons.entrySet()) {
            String action = entry.getKey();
            Button button = entry.getValue();
            
            button.setOnAction(e -> startKeyCapture(action, button));
        }
        
        // Configurer la ComboBox des profils
        setupProfileCombo();
    }
    
    /**
     * Configure la ComboBox des profils
     */
    private void setupProfileCombo() {
        // Configurer l'affichage des profils dans la ComboBox
        playerCombo.setCellFactory(param -> new ListCell<PlayerProfile>() {
            @Override
            protected void updateItem(PlayerProfile item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getFullName());
                }
            }
        });
        
        playerCombo.setButtonCell(new ListCell<PlayerProfile>() {
            @Override
            protected void updateItem(PlayerProfile item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getFullName());
                }
            }
        });
        
        // Charger les profils
        playerCombo.getItems().clear();
        playerCombo.getItems().addAll(profileManager.getAllProfiles());
        
        // Selectionner le premier profil s'il y en a
        if (!playerCombo.getItems().isEmpty()) {
            playerCombo.setValue(playerCombo.getItems().get(0));
            loadProfile(playerCombo.getValue());
        }
        
        // Configurer le gestionnaire d'evenements pour le changement de profil
        playerCombo.setOnAction(e -> {
            PlayerProfile selectedProfile = playerCombo.getValue();
            if (selectedProfile != null) {
                loadProfile(selectedProfile);
            }
        });
    }
    
    /**
     * Charge un profil et met a jour l'interface
     */
    private void loadProfile(PlayerProfile profile) {
        currentProfile = profile;
        tempControls = new HashMap<>(profile.getAllControls());
        
        // Mettre a jour les boutons avec les controles du profil
        updateControlButtons();
    }
    
    /**
     * Met a jour les boutons de controle avec les valeurs actuelles
     */
    private void updateControlButtons() {
        for (Map.Entry<String, Button> entry : controlButtons.entrySet()) {
            String action = entry.getKey();
            Button button = entry.getValue();
            
            KeyCode keyCode = tempControls.get(action);
            button.setText(keyCode != null ? keyCode.getName() : "?");
        }
    }
    
    /**
     * Demarre la capture d'une touche pour une action
     */
    private void startKeyCapture(String action, Button button) {
        // Marquer le bouton actif
        if (activeButton != null) {
            activeButton.getStyleClass().remove("active-key-button");
        }
        
        activeButton = button;
        activeButton.getStyleClass().add("active-key-button");
        activeButton.setText("...");
        
        // Creer une fenetre invisible pour capturer les touches
        if (keyListenerStage == null) {
            keyListenerStage = new Stage();
            keyListenerStage.setTitle("Capture de touche");
            keyListenerStage.setOpacity(0);
            keyListenerStage.setWidth(1);
            keyListenerStage.setHeight(1);
            keyListenerStage.setX(-100);
            keyListenerStage.setY(-100);
            
            // Creer une scene vide
            Scene scene = new Scene(new Label());
            keyListenerStage.setScene(scene);
            
            // Configurer le gestionnaire d'evenements pour la capture de touches
            scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (activeButton != null) {
                    KeyCode keyCode = event.getCode();
                    
                    // Vérifier si la touche est déjà utilisée par une autre action
                    if (currentProfile.isKeyUsed(keyCode, action)) {
                        // Chercher quelle action utilise cette touche
                        final String[] conflictActionRef = {null};
                        for (Map.Entry<String, KeyCode> entry : tempControls.entrySet()) {
                            if (entry.getValue() == keyCode && !entry.getKey().equals(action)) {
                                conflictActionRef[0] = entry.getKey();
                                break;
                            }
                        }
                        
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Conflit de touche");
                        alert.setHeaderText("Cette touche est deja utilisee");
                        alert.setContentText("La touche " + keyCode.getName() + " est deja utilisee pour l'action " + 
                                getActionName(conflictActionRef[0]) + ". Voulez-vous la reassigner ?");
                        
                        ButtonType yesButton = new ButtonType("Oui", ButtonBar.ButtonData.YES);
                        ButtonType noButton = new ButtonType("Non", ButtonBar.ButtonData.NO);
                        alert.getButtonTypes().setAll(yesButton, noButton);
                        
                        alert.showAndWait().ifPresent(buttonType -> {
                            if (buttonType == yesButton) {
                                // Assigner la touche à l'action
                                tempControls.put(action, keyCode);
                                activeButton.setText(keyCode.getName());
                                
                                // Mettre à jour le bouton de l'action en conflit
                                Button conflictButton = controlButtons.get(conflictActionRef[0]);
                                if (conflictButton != null) {
                                    conflictButton.setText("?");
                                }
                            }
                        });
                    } else {
                        // Assigner la touche à l'action
                        tempControls.put(action, keyCode);
                        activeButton.setText(keyCode.getName());
                    }
                    
                    // Désactiver le mode de capture
                    activeButton.getStyleClass().remove("active-key-button");
                    activeButton = null;
                    
                    // Fermer la fenêtre de capture
                    keyListenerStage.hide();
                }
                
                event.consume();
            });
        }
        
        // Afficher la fenetre de capture
        keyListenerStage.show();
        keyListenerStage.requestFocus();
    }
    
    /**
     * Obtient le nom d'une action
     */
    private String getActionName(String action) {
        switch (action) {
            case PlayerProfile.ACTION_UP:
                return "Haut";
            case PlayerProfile.ACTION_DOWN:
                return "Bas";
            case PlayerProfile.ACTION_LEFT:
                return "Gauche";
            case PlayerProfile.ACTION_RIGHT:
                return "Droite";
            case PlayerProfile.ACTION_BOMB:
                return "Bombe";
            case PlayerProfile.ACTION_SPECIAL:
                return "Special";
            default:
                return action;
        }
    }
    
    /**
     * Reinitialise les controles aux valeurs par defaut
     */
    @FXML
    private void resetControls() {
        if (currentProfile != null) {
            // Creer une copie temporaire du profil
            PlayerProfile tempProfile = new PlayerProfile(
                    currentProfile.getFirstName(),
                    currentProfile.getLastName(),
                    currentProfile.getAvatarPath(),
                    currentProfile.getGamesPlayed(),
                    currentProfile.getGamesWon(),
                    currentProfile.getTheme()
            );
            
            // Reinitialiser les controles
            tempProfile.resetControls();
            
            // Copier les controles reinitialises
            tempControls = new HashMap<>(tempProfile.getAllControls());
            
            // Mettre a jour l'interface
            updateControlButtons();
        }
    }
    
    /**
     * Sauvegarde les controles modifies
     */
    @FXML
    private void saveControls() {
        if (currentProfile != null) {
            // Appliquer les controles temporaires au profil
            for (Map.Entry<String, KeyCode> entry : tempControls.entrySet()) {
                currentProfile.setControl(entry.getKey(), entry.getValue());
            }
            
            // Sauvegarder le profil
            profileManager.updateProfile(currentProfile);
            
            // Sauvegarder explicitement tous les profils
            profileManager.saveProfiles();
            
            // Afficher un message de confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sauvegarde reussie");
            alert.setHeaderText(null);
            alert.setContentText("Les controles ont ete sauvegardes avec succes !");
            alert.showAndWait();
            
            // Fermer la fenetre
            closeWindow();
        }
    }
    
    /**
     * Annule les modifications et ferme la fenetre
     */
    @FXML
    private void cancel() {
        closeWindow();
    }
    
    /**
     * Ferme la fenetre
     */
    private void closeWindow() {
        // Fermer la fenetre de capture si elle est ouverte
        if (keyListenerStage != null) {
            keyListenerStage.close();
        }
        
        // Fermer la fenetre principale
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Definit le profil a editer
     */
    public void setProfile(PlayerProfile profile) {
        // Selectionner le profil dans la ComboBox
        playerCombo.setValue(profile);
        
        // Charger le profil
        loadProfile(profile);
    }

    /**
     * Cree un nouveau profil
     */
    @FXML
    private void createNewProfile() {
        // Creer une boite de dialogue personnalisee
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Nouveau profil");
        dialog.setHeaderText("Creation d'un nouveau profil");

        // Ajouter les boutons
        ButtonType createButtonType = new ButtonType("Creer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Creer les champs pour le prenom et le nom
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("Prenom");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Nom (optionnel)");

        grid.add(new Label("Prenom:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Nom:"), 0, 1);
        grid.add(lastNameField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Activer/desactiver le bouton selon si les champs sont vides
        Node createButton = dialog.getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(true);

        // Validation des champs
        firstNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            createButton.setDisable(newValue.trim().isEmpty());
        });

        // Convertir le resultat
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return new Pair<>(firstNameField.getText(), lastNameField.getText());
            }
            return null;
        });

        // Afficher la boite de dialogue et traiter le resultat
        Optional<Pair<String, String>> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            String firstName = result.get().getKey().trim();
            String lastName = result.get().getValue().trim();
            
            // Si le nom est vide, utiliser "1" par defaut
            if (lastName.isEmpty()) {
                lastName = "1";
            }
            
            // Verifier si le profil existe deja
            if (profileManager.profileExists(firstName, lastName)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Profil existant");
                alert.setContentText("Un profil avec ce nom existe deja.");
                alert.showAndWait();
                return;
            }
            
            // Creer le nouveau profil
            PlayerProfile newProfile = new PlayerProfile(firstName, lastName, "", 0, 0, "default");
            
            // Si un profil est selectionne, copier ses controles
            if (currentProfile != null) {
                newProfile.copyControlsFrom(currentProfile);
            } else {
                // Sinon, initialiser les controles par defaut
                newProfile.resetControls();
            }
            
            // Ajouter le profil
            profileManager.addProfile(newProfile);
            
            // Mettre a jour la liste des profils
            playerCombo.getItems().clear();
            playerCombo.getItems().addAll(profileManager.getAllProfiles());
            
            // Selectionner le nouveau profil
            playerCombo.setValue(newProfile);
            loadProfile(newProfile);
            
            // Afficher un message de confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Profil cree");
            alert.setHeaderText(null);
            alert.setContentText("Le profil a ete cree avec succes !");
            alert.showAndWait();
        }
    }

    /**
     * Renomme le profil actuel
     */
    @FXML
    private void renameProfile() {
        // Verifier qu'un profil est selectionne
        if (currentProfile == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Attention");
            alert.setHeaderText("Aucun profil selectionne");
            alert.setContentText("Veuillez selectionner un profil a renommer.");
            alert.showAndWait();
            return;
        }

        // Creer une boite de dialogue personnalisee
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Renommer le profil");
        dialog.setHeaderText("Renommer le profil " + currentProfile.getFullName());

        // Ajouter les boutons
        ButtonType renameButtonType = new ButtonType("Renommer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(renameButtonType, ButtonType.CANCEL);

        // Creer les champs pour le prenom et le nom
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField firstNameField = new TextField(currentProfile.getFirstName());
        TextField lastNameField = new TextField(currentProfile.getLastName());

        grid.add(new Label("Prenom:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Nom:"), 0, 1);
        grid.add(lastNameField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Activer/desactiver le bouton selon si les champs sont vides
        Node renameButton = dialog.getDialogPane().lookupButton(renameButtonType);
        renameButton.setDisable(false);

        // Validation des champs
        firstNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            renameButton.setDisable(newValue.trim().isEmpty());
        });

        // Convertir le resultat
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == renameButtonType) {
                return new Pair<>(firstNameField.getText(), lastNameField.getText());
            }
            return null;
        });

        // Afficher la boite de dialogue et traiter le resultat
        Optional<Pair<String, String>> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            String newFirstName = result.get().getKey().trim();
            String newLastName = result.get().getValue().trim();
            
            // Si le nom est vide, utiliser "1" par defaut
            if (newLastName.isEmpty()) {
                newLastName = "1";
            }
            
            // Verifier si le nouveau nom est identique a l'ancien
            if (newFirstName.equals(currentProfile.getFirstName()) && newLastName.equals(currentProfile.getLastName())) {
                return; // Aucun changement
            }
            
            // Verifier si un profil avec ce nom existe deja
            if (profileManager.profileExists(newFirstName, newLastName)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Profil existant");
                alert.setContentText("Un profil avec ce nom existe deja.");
                alert.showAndWait();
                return;
            }
            
            // Sauvegarder l'ancien nom pour le message de confirmation
            String oldName = currentProfile.getFullName();
            
            // Mettre a jour le profil
            currentProfile.setFirstName(newFirstName);
            currentProfile.setLastName(newLastName);
            profileManager.updateProfile(currentProfile);
            
            // Mettre a jour la liste des profils
            playerCombo.getItems().clear();
            playerCombo.getItems().addAll(profileManager.getAllProfiles());
            playerCombo.setValue(currentProfile);
            
            // Afficher un message de confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Profil renomme");
            alert.setHeaderText(null);
            alert.setContentText("Le profil a ete renomme de \"" + oldName + "\" a \"" + currentProfile.getFullName() + "\".");
            alert.showAndWait();
        }
    }
}
