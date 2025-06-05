package com.example.bomberman;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la gestion des profils joueurs
 */
public class ProfilesController implements Initializable {

    @FXML
    private TableView<PlayerProfile> profilesTable;
    @FXML
    private TableColumn<PlayerProfile, String> firstNameColumn;
    @FXML
    private TableColumn<PlayerProfile, String> lastNameColumn;
    @FXML
    private TableColumn<PlayerProfile, Integer> gamesPlayedColumn;
    @FXML
    private TableColumn<PlayerProfile, Integer> gamesWonColumn;
    @FXML
    private TableColumn<PlayerProfile, String> winPercentageColumn;

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
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button browseAvatarButton;
    @FXML
    private Button closeButton;

    private ProfileManager profileManager;
    private MainMenuController mainMenuController;
    private PlayerProfile selectedProfile;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        profileManager = ProfileManager.getInstance();

        setupTable();
        setupEventHandlers();
        loadProfiles();
        setupThemeCombo();
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
    @FXML
    private void resetStats() {
        if (selectedProfile == null) return;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Réinitialiser les statistiques");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir réinitialiser les statistiques de " +
                selectedProfile.getFullName() + " ?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            selectedProfile.setGamesPlayed(0);
            selectedProfile.setGamesWon(0);
            profileManager.updateProfile(selectedProfile);
            loadProfiles();

            showAlert("Succès", "Statistiques réinitialisées !", Alert.AlertType.INFORMATION);
        }
    }

    /**
     * Configure le tableau des profils
     */
    private void setupTable() {
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        gamesPlayedColumn.setCellValueFactory(new PropertyValueFactory<>("gamesPlayed"));
        gamesWonColumn.setCellValueFactory(new PropertyValueFactory<>("gamesWon"));

        // Colonne personnalisée pour le pourcentage de victoires
        winPercentageColumn.setCellValueFactory(cellData -> {
            PlayerProfile profile = cellData.getValue();
            String percentage = String.format("%.1f%%", profile.getWinPercentage());
            return new javafx.beans.property.SimpleStringProperty(percentage);
        });

        // Sélection dans le tableau
        profilesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    selectedProfile = newSelection;
                    updateFormFields();
                    updateButtonStates();
                }
        );
    }

    /**
     * Configure les gestionnaires d'événements
     */
    private void setupEventHandlers() {
        addButton.setOnAction(e -> addProfile());
        editButton.setOnAction(e -> editProfile());
        deleteButton.setOnAction(e -> deleteProfile());
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
     * Charge les profils dans le tableau
     */
    private void loadProfiles() {
        profilesTable.getItems().clear();
        profilesTable.getItems().addAll(profileManager.getAllProfiles());
    }

    /**
     * Met à jour les champs du formulaire
     */
    private void updateFormFields() {
        if (selectedProfile != null) {
            firstNameField.setText(selectedProfile.getFirstName());
            lastNameField.setText(selectedProfile.getLastName());
            avatarField.setText(selectedProfile.getAvatarPath());
            themeCombo.setValue(selectedProfile.getTheme());
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
     * Met à jour l'état des boutons
     */
    private void updateButtonStates() {
        boolean hasSelection = selectedProfile != null;
        editButton.setDisabled(!hasSelection);
        deleteButton.setDisabled(!hasSelection);
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
            if (profileManager.profileExists(firstName, lastName)) {
                showAlert("Erreur", "Un profil avec ce nom existe déjà.", Alert.AlertType.ERROR);
                return;
            }

            PlayerProfile newProfile = new PlayerProfile(firstName, lastName, avatarPath, 0, 0, theme);
            profileManager.addProfile(newProfile);

            loadProfiles();
            clearFormFields();

            showAlert("Succès", "Profil ajouté avec succès !", Alert.AlertType.INFORMATION);
        }
    }

    /**
     * Modifie le profil sélectionné
     */
    @FXML
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
                showAlert("Erreur", "Un profil avec ce nom existe déjà.", Alert.AlertType.ERROR);
                return;
            }

            selectedProfile.setFirstName(firstName);
            selectedProfile.setLastName(lastName);
            selectedProfile.setAvatarPath(avatarPath);
            selectedProfile.setTheme(theme);

            profileManager.updateProfile(selectedProfile);

            loadProfiles();

            showAlert("Succès", "Profil modifié avec succès !", Alert.AlertType.INFORMATION);
        }
    }

    /**
     * Supprime le profil sélectionné
     */
    @FXML
    private void deleteProfile() {
        if (selectedProfile == null) return;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer le profil");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer le profil de " +
                selectedProfile.getFullName() + " ?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            profileManager.removeProfile(selectedProfile);
            loadProfiles();
            clearFormFields();
            selectedProfile = null;
            updateButtonStates();

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
        if (firstName.isEmpty() || lastName.isEmpty()) {
            showAlert("Erreur", "Le prénom et le nom sont obligatoires.", Alert.AlertType.ERROR);
            return false;
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
}

