package com.example.bomberman.controller;

import com.example.bomberman.models.entities.PlayerProfile;
import com.example.bomberman.service.ProfileManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import com.example.bomberman.utils.UIUtils;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la gestion des profils joueurs
 */
public class ProfilesController implements Initializable {

    @FXML
    private VBox profilesListContainer;
    
    @FXML
    private Label formTitleLabel;

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField avatarField;
    @FXML
    private ComboBox<String> themeCombo;

    @FXML
    private Button addButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button browseAvatarButton;
    @FXML
    private Button closeButton;

    private ProfileManager profileManager;
    private MainMenuController mainMenuController;
    private PlayerProfile selectedProfile;
    private boolean editMode = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initialisation du contrôleur des profils");
        profileManager = ProfileManager.getInstance();

        // Vérifier si le conteneur est correctement initialisé
        if (profilesListContainer == null) {
            System.err.println("ERREUR: profilesListContainer est null!");
        } else {
            System.out.println("profilesListContainer est correctement initialisé");
        }

        setupEventHandlers();
        setupThemeCombo();
        loadProfiles();
        updateFormMode(false);
        
        // Programmer un rechargement différé pour s'assurer que l'interface est mise à jour
        scheduleDelayedReload();
        
        // Afficher un message pour confirmer que l'initialisation est terminée
        System.out.println("Initialisation du contrôleur des profils terminée");
    }

    /**
     * Définit le contrôleur du menu principal
     */
    public void setMainMenuController(MainMenuController mainMenuController) {
        this.mainMenuController = mainMenuController;
    }

    /**
     * Réinitialise les statistiques d'un profil
     */
    private void resetStats(PlayerProfile profile) {
        if (profile == null) return;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Réinitialiser les statistiques");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir réinitialiser les statistiques de " +
                profile.getFullName() + " ?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            profile.setGamesPlayed(0);
            profile.setGamesWon(0);
            profileManager.updateProfile(profile);
            loadProfiles();

            showAlert("Succès", "Statistiques réinitialisées !", Alert.AlertType.INFORMATION);
        }
    }

    /**
     * Configure les gestionnaires d'événements
     */
    private void setupEventHandlers() {
        addButton.setOnAction(e -> {
            if (editMode) {
                editProfile();
            } else {
                addProfile();
            }
        });
        cancelButton.setOnAction(e -> cancelEdit());
        browseAvatarButton.setOnAction(e -> browseAvatar());
        closeButton.setOnAction(e -> closeWindow());
    }

    /**
     * Configure la ComboBox des thèmes
     */
    private void setupThemeCombo() {
        themeCombo.getItems().addAll(
                "default", "pokemon", "mario", "zelda", "classic"
        );
        themeCombo.setValue("default");
    }

    /**
     * Charge les profils dans la liste scrollable
     */
    private void loadProfiles() {
        profilesListContainer.getChildren().clear();
        
        // Afficher un message de débogage
        System.out.println("Chargement des profils : " + profileManager.getAllProfiles().size() + " profils trouvés");
        
        for (PlayerProfile profile : profileManager.getAllProfiles()) {
            profilesListContainer.getChildren().add(createProfileCard(profile));
            System.out.println("Profil ajouté : " + profile.getFullName());
        }
        
        // Si aucun profil n'est disponible, afficher un message
        if (profilesListContainer.getChildren().isEmpty()) {
            Label emptyLabel = new Label("Aucun profil disponible. Créez un nouveau profil.");
            emptyLabel.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");
            profilesListContainer.getChildren().add(emptyLabel);
            System.out.println("Aucun profil disponible");
        }
        
        // Forcer la mise à jour de l'interface
        profilesListContainer.requestLayout();
    }
    
    /**
     * Crée une carte pour un profil
     */
    private Node createProfileCard(PlayerProfile profile) {
        VBox card = new VBox();
        card.getStyleClass().add("profile-card");
        card.setSpacing(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        // Nom du profil
        Label nameLabel = new Label(profile.getFullName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        // Statistiques
        Label statsLabel = new Label(String.format("Parties: %d | Victoires: %d | Taux: %.1f%%", 
                profile.getGamesPlayed(), profile.getGamesWon(), profile.getWinPercentage()));
        
        // Thème
        Label themeLabel = new Label("Thème: " + profile.getTheme());
        
        // Boutons d'action
        HBox actions = new HBox();
        actions.setSpacing(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        Button editBtn = new Button("Modifier");
        editBtn.getStyleClass().add("action-button");
        editBtn.setOnAction(e -> startEditProfile(profile));
        
        Button deleteBtn = new Button("Supprimer");
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> deleteProfile(profile));
        
                 Button statsBtn = new Button("Stats");
         statsBtn.getStyleClass().add("warning-button");
         statsBtn.setOnAction(e -> resetStats(profile));
         
         Button controlsBtn = new Button("Contrôles");
         controlsBtn.getStyleClass().add("info-button");
         controlsBtn.setOnAction(e -> showControlsDialog(profile));
         
         actions.getChildren().addAll(controlsBtn, statsBtn, editBtn, deleteBtn);
        
        // Ajouter tous les éléments à la carte
        card.getChildren().addAll(nameLabel, statsLabel, themeLabel, actions);
        
        return card;
    }

    /**
     * Met à jour les champs du formulaire
     */
    private void updateFormFields(PlayerProfile profile) {
        if (profile != null) {
            firstNameField.setText(profile.getFirstName());
            lastNameField.setText(profile.getLastName());
            avatarField.setText(profile.getAvatarPath());
            themeCombo.setValue(profile.getTheme());
        } else {
            clearFormFields();
        }
    }

    /**
     * Vide les champs du formulaire
     */
    private void clearFormFields() {
        firstNameField.clear();
        lastNameField.clear();
        avatarField.clear();
        themeCombo.setValue("default");
    }

    /**
     * Met à jour le mode du formulaire (ajout ou édition)
     */
    private void updateFormMode(boolean isEditMode) {
        this.editMode = isEditMode;
        
        if (isEditMode) {
            formTitleLabel.setText("Modifier le profil");
            addButton.setText("Enregistrer les modifications");
        } else {
            formTitleLabel.setText("Ajouter un nouveau profil");
            addButton.setText("Ajouter Profil");
            selectedProfile = null;
        }
    }

    /**
     * Prépare la création d'un nouveau profil
     */
    @FXML
    private void newProfile() {
        selectedProfile = null;
        clearFormFields();
        updateFormMode(false);
        firstNameField.requestFocus();
    }
    
    /**
     * Démarre l'édition d'un profil
     */
    private void startEditProfile(PlayerProfile profile) {
        selectedProfile = profile;
        updateFormFields(profile);
        updateFormMode(true);
        firstNameField.requestFocus();
    }
    
    /**
     * Annule l'édition en cours
     */
    @FXML
    private void cancelEdit() {
        clearFormFields();
        updateFormMode(false);
    }

    /**
     * Recharge les profils depuis le fichier
     */
    @FXML
    private void reloadProfiles() {
        // Forcer le rechargement des profils depuis le fichier
        profileManager.loadProfiles();
        
        // Recharger la liste
        loadProfiles();
        
        // Vider les champs du formulaire et réinitialiser le mode
        clearFormFields();
        updateFormMode(false);
        
        showAlert("Rechargement", "Les profils ont été rechargés depuis le fichier.", Alert.AlertType.INFORMATION);
    }

    /**
     * Affiche une boîte de dialogue pour saisir un nouveau nom de profil
     */
    private Pair<String, String> showNameInputDialog(String title, String headerText, String buttonText,
                                                  String initialFirstName, String initialLastName) {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);

        // Définir les boutons
        ButtonType confirmButtonType = new ButtonType(buttonText, ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        // Créer les champs pour le prénom et le nom
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField newFirstNameField = new TextField(initialFirstName);
        TextField newLastNameField = new TextField(initialLastName);

        grid.add(new Label("Prénom:"), 0, 0);
        grid.add(newFirstNameField, 1, 0);
        grid.add(new Label("Nom:"), 0, 1);
        grid.add(newLastNameField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Activer/désactiver le bouton selon si les champs sont vides
        Node confirmButton = dialog.getDialogPane().lookupButton(confirmButtonType);
        confirmButton.setDisable(true);

        // Validation des champs
        newFirstNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            confirmButton.setDisable(newValue.trim().isEmpty() || newLastNameField.getText().trim().isEmpty());
        });
        newLastNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            confirmButton.setDisable(newValue.trim().isEmpty() || newFirstNameField.getText().trim().isEmpty());
        });

        // Convertir le résultat
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return new Pair<>(newFirstNameField.getText(), newLastNameField.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();
        return result.orElse(null);
    }

    /**
     * Ajoute un nouveau profil
     */
    @FXML
    private void addProfile() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String avatarPath = avatarField.getText().trim();
        String theme = themeCombo.getValue();

        if (validateInput(firstName, lastName)) {
            // Vérifier si un profil avec le même nom existe déjà
            PlayerProfile existingProfile = profileManager.findProfile(firstName + " " + lastName);
            if (existingProfile != null) {
                // Demander à l'utilisateur s'il souhaite créer un nouveau profil avec un nom différent
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Profil existant");
                confirmAlert.setHeaderText("Un profil avec ce nom existe déjà");
                confirmAlert.setContentText("Souhaitez-vous créer un nouveau profil avec un nom différent ?");

                Optional<ButtonType> result = confirmAlert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    // Afficher la boîte de dialogue pour saisir un nouveau nom
                    Pair<String, String> newName = showNameInputDialog(
                            "Nouveau nom de profil",
                            "Entrez un nouveau nom pour le profil",
                            "Créer",
                            firstName,
                            lastName
                    );
                    
                    if (newName != null) {
                        String newFirstName = newName.getKey().trim();
                        String newLastName = newName.getValue().trim();
                        
                        // Vérifier si le nouveau nom existe déjà
                        if (profileManager.profileExists(newFirstName, newLastName)) {
                            showAlert("Erreur", "Un profil avec ce nom existe également.", Alert.AlertType.ERROR);
                            return;
                        }
                        
                        // Créer le profil avec le nouveau nom et copier les contrôles du profil existant
                        PlayerProfile newProfile = new PlayerProfile(newFirstName, newLastName, avatarPath, 0, 0, theme);
                        
                        // Si on crée un profil basé sur un profil existant, copier ses contrôles
                        if (selectedProfile != null) {
                            newProfile.copyControlsFrom(selectedProfile);
                        } else if (existingProfile != null) {
                            // Sinon, copier les contrôles du profil existant avec le même nom
                            newProfile.copyControlsFrom(existingProfile);
                        }
                        
                        profileManager.addProfile(newProfile);
                        
                        loadProfiles();
                        clearFormFields();
                        updateFormMode(false);
                        
                        showAlert("Succès", "Profil ajouté avec succès !", Alert.AlertType.INFORMATION);
                    }
                }
                return;
            }

            // Créer un nouveau profil
            PlayerProfile newProfile = new PlayerProfile(firstName, lastName, avatarPath, 0, 0, theme);
            
            // Si on crée un profil basé sur un profil sélectionné, copier ses contrôles
            if (selectedProfile != null) {
                newProfile.copyControlsFrom(selectedProfile);
            }
            
            profileManager.addProfile(newProfile);
            loadProfiles();
            clearFormFields();
            updateFormMode(false);

            showAlert("Succès", "Profil ajouté avec succès !", Alert.AlertType.INFORMATION);
        }
    }

    /**
     * Modifie le profil sélectionné
     */
    private void editProfile() {
        if (selectedProfile == null) return;

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String avatarPath = avatarField.getText().trim();
        String theme = themeCombo.getValue();

        if (validateInput(firstName, lastName)) {
            // Vérifier si le nouveau nom n'existe pas déjà (sauf pour le profil actuel)
            if (!selectedProfile.getFullName().equals(firstName + " " + lastName) &&
                    profileManager.profileExists(firstName, lastName)) {
                
                // Demander à l'utilisateur s'il souhaite choisir un autre nom
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Nom de profil existant");
                confirmAlert.setHeaderText("Un profil avec ce nom existe déjà");
                confirmAlert.setContentText("Souhaitez-vous choisir un autre nom ?");

                Optional<ButtonType> result = confirmAlert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    // Afficher la boîte de dialogue pour saisir un nouveau nom
                    Pair<String, String> newName = showNameInputDialog(
                            "Modifier le nom du profil",
                            "Choisissez un nom unique pour ce profil",
                            "Enregistrer",
                            firstName,
                            lastName
                    );
                    
                    if (newName != null) {
                        firstName = newName.getKey().trim();
                        lastName = newName.getValue().trim();
                        
                        // Vérifier si le nouveau nom existe toujours
                        if (!selectedProfile.getFullName().equals(firstName + " " + lastName) &&
                                profileManager.profileExists(firstName, lastName)) {
                            showAlert("Erreur", "Ce nom de profil existe également.", Alert.AlertType.ERROR);
                            return;
                        }
                    } else {
                        // L'utilisateur a annulé, on ne fait rien
                        return;
                    }
                } else {
                    // L'utilisateur a refusé de changer le nom, on ne fait rien
                return;
                }
            }

            // Conserver les contrôles actuels avant de mettre à jour le profil
            Map<String, KeyCode> currentControls = selectedProfile.getAllControls();

            selectedProfile.setFirstName(firstName);
            selectedProfile.setLastName(lastName);
            selectedProfile.setAvatarPath(avatarPath);
            selectedProfile.setTheme(theme);
            
            // Restaurer les contrôles personnalisés
            for (Map.Entry<String, KeyCode> entry : currentControls.entrySet()) {
                selectedProfile.setControl(entry.getKey(), entry.getValue());
            }

            profileManager.updateProfile(selectedProfile);

            loadProfiles();
            clearFormFields();
            updateFormMode(false);

            showAlert("Succès", "Profil modifié avec succès !", Alert.AlertType.INFORMATION);
        }
    }

    /**
     * Supprime un profil
     */
    private void deleteProfile(PlayerProfile profile) {
        if (profile == null) return;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer le profil");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer le profil de " +
                profile.getFullName() + " ?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            profileManager.removeProfile(profile);
            loadProfiles();
            
            // Si le profil supprimé était en cours d'édition, réinitialiser le formulaire
            if (selectedProfile == profile) {
            clearFormFields();
                updateFormMode(false);
            }

            showAlert("Succès", "Profil supprimé avec succès !", Alert.AlertType.INFORMATION);
        }
    }

    /**
     * Ouvre un dialogue pour choisir un avatar
     */
    @FXML
    private void browseAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un avatar");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        Stage stage = (Stage) browseAvatarButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            avatarField.setText(selectedFile.getName());
        }
    }

    /**
     * Valide les données saisies
     */
    private boolean validateInput(String firstName, String lastName) {
        if (firstName.isEmpty()) {
            showAlert("Erreur", "Le prénom est obligatoire.", Alert.AlertType.ERROR);
            firstNameField.requestFocus();
            return false;
        }

        if (lastName.isEmpty()) {
            // Pour faciliter la création de profils, on peut utiliser un nom par défaut
            lastName = "1";
            lastNameField.setText(lastName);
        }

        if (firstName.length() > 20 || lastName.length() > 20) {
            showAlert("Erreur", "Le prénom et le nom ne peuvent pas dépasser 20 caractères.", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    /**
     * Ferme la fenêtre
     */
    @FXML
    private void closeWindow() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Affiche une alerte
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Affiche la boîte de dialogue de personnalisation des contrôles
     */
    private void showControlsDialog(PlayerProfile profile) {
        if (profile == null) return;
        
        // Utiliser notre classe utilitaire pour ouvrir l'éditeur de contrôles
        boolean success = UIUtils.openControlsEditor(profile);
        
        // Recharger les profils si l'édition a réussi
        if (success) {
            loadProfiles();
        }
    }

    /**
     * Programme un rechargement différé de l'interface
     */
    private void scheduleDelayedReload() {
        // Créer un timer pour recharger l'interface après 500ms
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.millis(500));
        delay.setOnFinished(event -> {
            System.out.println("Rechargement différé de l'interface");
            loadProfiles();
            
            // Forcer une mise à jour visuelle
            if (profilesListContainer != null) {
                profilesListContainer.setVisible(false);
                profilesListContainer.setVisible(true);
            }
        });
        delay.play();
    }

    /**
     * Affiche la boîte de dialogue de personnalisation des contrôles pour le profil sélectionné
     * ou demande à l'utilisateur de sélectionner un profil
     */
    @FXML
    private void showControlsDialogForSelected() {
        // Si un profil est sélectionné, afficher la boîte de dialogue pour ce profil
        if (selectedProfile != null) {
            showControlsDialog(selectedProfile);
            return;
        }
        
        // Sinon, demander à l'utilisateur de sélectionner un profil
        List<PlayerProfile> profiles = profileManager.getAllProfiles();
        if (profiles.isEmpty()) {
            showAlert("Aucun profil", "Aucun profil n'est disponible. Veuillez créer un profil d'abord.", Alert.AlertType.WARNING);
            return;
        }
        
        // Créer une boîte de dialogue pour sélectionner un profil
        ChoiceDialog<PlayerProfile> dialog = new ChoiceDialog<>(profiles.get(0), profiles);
        dialog.setTitle("Sélectionner un profil");
        dialog.setHeaderText("Sélectionnez un profil pour personnaliser ses contrôles");
        dialog.setContentText("Profil:");
        
        // Configurer l'affichage des profils dans la liste déroulante
        dialog.getItems().setAll(profiles);
        
        // Afficher la boîte de dialogue et traiter le résultat
        Optional<PlayerProfile> result = dialog.showAndWait();
        result.ifPresent(this::showControlsDialog);
    }
}

