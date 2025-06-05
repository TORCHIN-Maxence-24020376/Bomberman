package com.example.bomberman;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur pour l'éditeur de niveaux
 */
public class LevelEditorController implements Initializable {

    @FXML private Canvas editorCanvas;
    @FXML private ToggleGroup toolGroup;
    @FXML private RadioButton emptyTool;
    @FXML private RadioButton wallTool;
    @FXML private RadioButton destructibleWallTool;
    @FXML private RadioButton player1SpawnTool;
    @FXML private RadioButton player2SpawnTool;
    @FXML private Button newButton;
    @FXML private Button loadButton;
    @FXML private Button saveButton;
    @FXML private Button testButton;
    @FXML private Label dimensionsLabel;
    @FXML private Spinner<Integer> widthSpinner;
    @FXML private Spinner<Integer> heightSpinner;

    // Configuration
    private static final int TILE_SIZE = 30;
    private static final int MIN_SIZE = 9;
    private static final int MAX_SIZE = 21;

    // État de l'éditeur
    private int[][] levelData;
    private int levelWidth = 15;
    private int levelHeight = 13;
    private GraphicsContext gc;
    private int selectedTool = 0; // 0=vide, 1=mur, 2=mur destructible
    private int player1X = 1, player1Y = 1;
    private int player2X = 13, player2Y = 11;

    // Types de cases
    private static final int EMPTY = 0;
    private static final int WALL = 1;
    private static final int DESTRUCTIBLE_WALL = 2;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gc = editorCanvas.getGraphicsContext2D();

        setupControls();
        initializeLevel();
        render();
    }

    /**
     * Configure les contrôles de l'interface
     */
    private void setupControls() {
        // Configuration des spinners
        widthSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_SIZE, MAX_SIZE, levelWidth, 2));
        heightSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_SIZE, MAX_SIZE, levelHeight, 2));

        // Écouteurs pour les changements de dimensions
        widthSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal % 2 == 0) newVal++; // Assurer des dimensions impaires
            levelWidth = newVal;
            resizeLevel();
            render();
        });

        heightSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal % 2 == 0) newVal++; // Assurer des dimensions impaires
            levelHeight = newVal;
            resizeLevel();
            render();
        });

        // Configuration des outils
        emptyTool.setUserData(EMPTY);
        wallTool.setUserData(WALL);
        destructibleWallTool.setUserData(DESTRUCTIBLE_WALL);
        player1SpawnTool.setUserData(3);
        player2SpawnTool.setUserData(4);

        toolGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                selectedTool = (Integer) newToggle.getUserData();
            }
        });

        // Sélectionner l'outil vide par défaut
        emptyTool.setSelected(true);

        // Configuration des boutons
        newButton.setOnAction(e -> newLevel());
        loadButton.setOnAction(e -> loadLevel());
        saveButton.setOnAction(e -> saveLevel());
        testButton.setOnAction(e -> testLevel());

        // Événements de souris sur le canvas
        editorCanvas.setOnMouseClicked(this::handleMouseClick);
        editorCanvas.setOnMouseDragged(this::handleMouseDrag);

        updateDimensionsLabel();
    }

    /**
     * Initialise un nouveau niveau
     */
    private void initializeLevel() {
        levelData = new int[levelHeight][levelWidth];

        // Créer les bordures
        for (int y = 0; y < levelHeight; y++) {
            for (int x = 0; x < levelWidth; x++) {
                if (x == 0 || y == 0 || x == levelWidth - 1 || y == levelHeight - 1) {
                    levelData[y][x] = WALL;
                } else if (x % 2 == 0 && y % 2 == 0) {
                    levelData[y][x] = WALL; // Murs fixes en damier
                } else {
                    levelData[y][x] = EMPTY;
                }
            }
        }

        // Assurer que les zones de spawn sont libres
        clearSpawnAreas();
    }

    /**
     * Redimensionne le niveau
     */
    private void resizeLevel() {
        int[][] newLevelData = new int[levelHeight][levelWidth];

        // Copier les données existantes
        for (int y = 0; y < Math.min(levelHeight, levelData.length); y++) {
            for (int x = 0; x < Math.min(levelWidth, levelData[0].length); x++) {
                newLevelData[y][x] = levelData[y][x];
            }
        }

        levelData = newLevelData;

        // Recréer les bordures si nécessaire
        for (int y = 0; y < levelHeight; y++) {
            for (int x = 0; x < levelWidth; x++) {
                if (x == 0 || y == 0 || x == levelWidth - 1 || y == levelHeight - 1) {
                    levelData[y][x] = WALL;
                } else if (x % 2 == 0 && y % 2 == 0 && levelData[y][x] == EMPTY) {
                    levelData[y][x] = WALL; // Murs fixes en damier
                }
            }
        }

        // Ajuster les positions des joueurs si nécessaire
        player1X = Math.min(player1X, levelWidth - 2);
        player1Y = Math.min(player1Y, levelHeight - 2);
        player2X = Math.min(player2X, levelWidth - 2);
        player2Y = Math.min(player2Y, levelHeight - 2);

        clearSpawnAreas();
        updateDimensionsLabel();

        // Redimensionner le canvas
        editorCanvas.setWidth(levelWidth * TILE_SIZE);
        editorCanvas.setHeight(levelHeight * TILE_SIZE);
    }

    /**
     * Nettoie les zones de spawn
     */
    private void clearSpawnAreas() {
        // Zone joueur 1
        for (int y = Math.max(0, player1Y - 1); y <= Math.min(levelHeight - 1, player1Y + 1); y++) {
            for (int x = Math.max(0, player1X - 1); x <= Math.min(levelWidth - 1, player1X + 1); x++) {
                if (!(x % 2 == 0 && y % 2 == 0)) { // Ne pas effacer les murs fixes
                    levelData[y][x] = EMPTY;
                }
            }
        }

        // Zone joueur 2
        for (int y = Math.max(0, player2Y - 1); y <= Math.min(levelHeight - 1, player2Y + 1); y++) {
            for (int x = Math.max(0, player2X - 1); x <= Math.min(levelWidth - 1, player2X + 1); x++) {
                if (!(x % 2 == 0 && y % 2 == 0)) { // Ne pas effacer les murs fixes
                    levelData[y][x] = EMPTY;
                }
            }
        }
    }

    /**
     * Gère les clics de souris
     */
    @FXML
    private void handleMouseClick(MouseEvent event) {
        int x = (int) (event.getX() / TILE_SIZE);
        int y = (int) (event.getY() / TILE_SIZE);

        paintTile(x, y);
    }

    /**
     * Gère le glissement de souris
     */
    @FXML
    private void handleMouseDrag(MouseEvent event) {
        int x = (int) (event.getX() / TILE_SIZE);
        int y = (int) (event.getY() / TILE_SIZE);

        paintTile(x, y);
    }

    /**
     * Peint une case selon l'outil sélectionné
     */
    private void paintTile(int x, int y) {
        if (x < 0 || x >= levelWidth || y < 0 || y >= levelHeight) return;

        // Ne pas modifier les bordures et les murs fixes
        if (x == 0 || y == 0 || x == levelWidth - 1 || y == levelHeight - 1) return;
        if (x % 2 == 0 && y % 2 == 0) return; // Murs fixes

        switch (selectedTool) {
            case EMPTY:
            case WALL:
            case DESTRUCTIBLE_WALL:
                levelData[y][x] = selectedTool;
                break;
            case 3: // Player 1 spawn
                player1X = x;
                player1Y = y;
                clearSpawnAreas();
                break;
            case 4: // Player 2 spawn
                player2X = x;
                player2Y = y;
                clearSpawnAreas();
                break;
        }

        render();
    }

    /**
     * Dessine le niveau
     */
    private void render() {
        gc.clearRect(0, 0, editorCanvas.getWidth(), editorCanvas.getHeight());

        for (int y = 0; y < levelHeight; y++) {
            for (int x = 0; x < levelWidth; x++) {
                int cellX = x * TILE_SIZE;
                int cellY = y * TILE_SIZE;

                // Couleur de fond
                switch (levelData[y][x]) {
                    case EMPTY:
                        gc.setFill(Color.LIGHTGREEN);
                        break;
                    case WALL:
                        gc.setFill(Color.DARKGRAY);
                        break;
                    case DESTRUCTIBLE_WALL:
                        gc.setFill(Color.BROWN);
                        break;
                }

                gc.fillRect(cellX, cellY, TILE_SIZE, TILE_SIZE);

                // Grille
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(1);
                gc.strokeRect(cellX, cellY, TILE_SIZE, TILE_SIZE);

                // Marqueurs spéciaux pour les murs fixes
                if (x % 2 == 0 && y % 2 == 0 && levelData[y][x] == WALL) {
                    gc.setFill(Color.BLACK);
                    gc.fillRect(cellX + TILE_SIZE/4, cellY + TILE_SIZE/4, TILE_SIZE/2, TILE_SIZE/2);
                }
            }
        }

        // Dessiner les positions des joueurs
        gc.setFill(Color.BLUE);
        gc.fillOval(player1X * TILE_SIZE + 5, player1Y * TILE_SIZE + 5, TILE_SIZE - 10, TILE_SIZE - 10);
        gc.setFill(Color.WHITE);
        gc.fillText("1", player1X * TILE_SIZE + TILE_SIZE/2 - 3, player1Y * TILE_SIZE + TILE_SIZE/2 + 3);

        gc.setFill(Color.RED);
        gc.fillOval(player2X * TILE_SIZE + 5, player2Y * TILE_SIZE + 5, TILE_SIZE - 10, TILE_SIZE - 10);
        gc.setFill(Color.WHITE);
        gc.fillText("2", player2X * TILE_SIZE + TILE_SIZE/2 - 3, player2Y * TILE_SIZE + TILE_SIZE/2 + 3);
    }

    /**
     * Crée un nouveau niveau vide
     */
    @FXML
    private void newLevel() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Nouveau niveau");
        confirmAlert.setHeaderText("Créer un nouveau niveau ?");
        confirmAlert.setContentText("Les modifications non sauvegardées seront perdues.");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            initializeLevel();
            render();
        }
    }

    /**
     * Charge un niveau depuis un fichier
     */
    @FXML
    private void loadLevel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Charger un niveau");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers de niveau", "*.level")
        );

        Stage stage = (Stage) loadButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                // Lire les dimensions
                String[] dimensions = reader.readLine().split(",");
                levelWidth = Integer.parseInt(dimensions[0]);
                levelHeight = Integer.parseInt(dimensions[1]);

                // Lire les positions des joueurs
                String[] players = reader.readLine().split(",");
                player1X = Integer.parseInt(players[0]);
                player1Y = Integer.parseInt(players[1]);
                player2X = Integer.parseInt(players[2]);
                player2Y = Integer.parseInt(players[3]);

                // Mettre à jour les spinners
                widthSpinner.getValueFactory().setValue(levelWidth);
                heightSpinner.getValueFactory().setValue(levelHeight);

                // Lire les données du niveau
                levelData = new int[levelHeight][levelWidth];
                for (int y = 0; y < levelHeight; y++) {
                    String[] row = reader.readLine().split(",");
                    for (int x = 0; x < levelWidth; x++) {
                        levelData[y][x] = Integer.parseInt(row[x]);
                    }
                }

                resizeLevel();
                render();

                showAlert("Succès", "Niveau chargé avec succès !", Alert.AlertType.INFORMATION);

            } catch (Exception e) {
                showAlert("Erreur", "Impossible de charger le niveau : " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    /**
     * Sauvegarde le niveau dans un fichier
     */
    @FXML
    private void saveLevel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder le niveau");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers de niveau", "*.level")
        );

        Stage stage = (Stage) saveButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                // Écrire les dimensions
                writer.println(levelWidth + "," + levelHeight);

                // Écrire les positions des joueurs
                writer.println(player1X + "," + player1Y + "," + player2X + "," + player2Y);

                // Écrire les données du niveau
                for (int y = 0; y < levelHeight; y++) {
                    for (int x = 0; x < levelWidth; x++) {
                        writer.print(levelData[y][x]);
                        if (x < levelWidth - 1) writer.print(",");
                    }
                    writer.println();
                }

                showAlert("Succès", "Niveau sauvegardé avec succès !", Alert.AlertType.INFORMATION);

            } catch (Exception e) {
                showAlert("Erreur", "Impossible de sauvegarder le niveau : " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    /**
     * Teste le niveau en lançant une partie
     */
    @FXML
    private void testLevel() {
        // Valider le niveau
        if (!validateLevel()) {
            return;
        }

        showAlert("Test", "Fonctionnalité de test en cours de développement.\n" +
                "Le niveau semble valide et peut être sauvegardé.", Alert.AlertType.INFORMATION);
    }

    /**
     * Valide le niveau
     */
    private boolean validateLevel() {
        // Vérifier que les positions des joueurs sont valides
        if (levelData[player1Y][player1X] != EMPTY) {
            showAlert("Erreur", "La position du joueur 1 doit être sur une case vide.", Alert.AlertType.ERROR);
            return false;
        }

        if (levelData[player2Y][player2X] != EMPTY) {
            showAlert("Erreur", "La position du joueur 2 doit être sur une case vide.", Alert.AlertType.ERROR);
            return false;
        }

        // Vérifier que les joueurs ne sont pas trop proches
        if (Math.abs(player1X - player2X) < 3 && Math.abs(player1Y - player2Y) < 3) {
            showAlert("Attention", "Les joueurs sont très proches. Considérez les éloigner.", Alert.AlertType.WARNING);
        }

        return true;
    }

    /**
     * Met à jour le label des dimensions
     */
    private void updateDimensionsLabel() {
        dimensionsLabel.setText("Dimensions: " + levelWidth + " x " + levelHeight);
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