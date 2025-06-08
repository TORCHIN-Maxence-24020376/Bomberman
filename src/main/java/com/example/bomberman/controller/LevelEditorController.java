package com.example.bomberman.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import com.example.bomberman.utils.FileDialogManager;

/**
 * Contrôleur pour l'éditeur de niveaux
 */
public class LevelEditorController implements Initializable {

    @FXML private Canvas editorCanvas;
    @FXML private ToggleGroup toolGroup;
    @FXML private ToggleButton emptyTool;
    @FXML private ToggleButton wallTool;
    @FXML private ToggleButton destructibleWallTool;
    @FXML private ToggleButton player1SpawnTool;
    @FXML private ToggleButton player2SpawnTool;
    @FXML private Button returnButton;
    @FXML private Button newButton;
    @FXML private Button loadButton;
    @FXML private Button saveButton;
    @FXML private Button testButton;
    @FXML private Button randomWallsButton;
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
    
    // Menu contextuel
    private ContextMenu activeContextMenu = null;
    
    // Fichier temporaire pour sauvegarder l'état de l'éditeur
    private static File tempSaveFile;
    private static final String TEMP_SAVE_PATH = "levels/autosave.level";

    // Types de cases
    private static final int EMPTY = 0;
    private static final int WALL = 1;
    private static final int DESTRUCTIBLE_WALL = 2;

    // Curseurs personnalisés
    private Map<Integer, Cursor> customCursors = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gc = editorCanvas.getGraphicsContext2D();
        
        setupControls();
        
        // Essayer de charger l'état temporaire, sinon initialiser un nouveau niveau
        if (!loadTemporaryState()) {
            initializeLevel();
        }
        
        initializeCustomCursors();
        render();
        
        System.out.println("Éditeur de niveaux lancé avec succès !");
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
                updateCursor();
            }
        });

        // Sélectionner l'outil vide par défaut
        emptyTool.setSelected(true);
        updateCursor();

        // Configuration des boutons
        returnButton.setOnAction(e -> returnToMainMenu());
        newButton.setOnAction(e -> newLevel());
        loadButton.setOnAction(e -> loadLevel());
        saveButton.setOnAction(e -> saveLevel());
        testButton.setOnAction(e -> testLevel());
        randomWallsButton.setOnAction(e -> placeRandomDestructibleWalls());

        // Événements de souris sur le canvas
        editorCanvas.setOnMousePressed(this::handleMousePressed);
        editorCanvas.setOnMouseClicked(this::handleMouseClick);
        editorCanvas.setOnMouseDragged(this::handleMouseDrag);

        updateDimensionsLabel();
    }

    /**
     * Initialise les curseurs personnalisés
     */
    private void initializeCustomCursors() {
        // Pour l'instant, nous utilisons des curseurs par défaut
        // Les curseurs personnalisés seront ajoutés ultérieurement
        
        // Si aucun curseur personnalisé n'est créé, appliquer le curseur par défaut
        updateCursor();
    }
    
    /**
     * Met à jour le curseur selon l'outil sélectionné
     */
    private void updateCursor() {
        // Utiliser le curseur personnalisé s'il existe, sinon utiliser le curseur par défaut
        Cursor cursor = customCursors.getOrDefault(selectedTool, null);
        
        if (cursor == null) {
            // Curseurs par défaut si les personnalisés ne sont pas disponibles
            switch (selectedTool) {
                case EMPTY:
                    cursor = Cursor.CROSSHAIR;
                    break;
                case WALL:
                    cursor = Cursor.CLOSED_HAND;
                    break;
                case DESTRUCTIBLE_WALL:
                    cursor = Cursor.HAND;
                    break;
                case 3: // Player 1 spawn
                    cursor = Cursor.MOVE;
                    break;
                case 4: // Player 2 spawn
                    cursor = Cursor.WAIT;
                    break;
                default:
                    cursor = Cursor.DEFAULT;
            }
        }
        
        editorCanvas.setCursor(cursor);
    }

    /**
     * Initialise un nouveau niveau
     */
    public void initializeLevel() {
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
    public void resizeLevel() {
        // Créer un nouveau tableau vide avec les nouvelles dimensions
        int[][] newLevelData = new int[levelHeight][levelWidth];
        
        // Initialiser toutes les cellules à vide
        for (int y = 0; y < levelHeight; y++) {
            for (int x = 0; x < levelWidth; x++) {
                newLevelData[y][x] = EMPTY;
            }
        }
        
        // Appliquer le motif de base (bordures et murs fixes)
        for (int y = 0; y < levelHeight; y++) {
            for (int x = 0; x < levelWidth; x++) {
                if (x == 0 || y == 0 || x == levelWidth - 1 || y == levelHeight - 1) {
                    newLevelData[y][x] = WALL; // Bordures
                } else if (x % 2 == 0 && y % 2 == 0) {
                    newLevelData[y][x] = WALL; // Murs fixes en damier
                }
            }
        }
        
        // Copier les données de l'ancien niveau (seulement pour les cellules modifiables)
        for (int y = 1; y < Math.min(levelHeight - 1, levelData.length - 1); y++) {
            for (int x = 1; x < Math.min(levelWidth - 1, levelData[0].length - 1); x++) {
                // Ne pas copier les murs fixes et les bordures
                if (!(x % 2 == 0 && y % 2 == 0) && !(x == 0 || y == 0 || x == levelWidth - 1 || y == levelHeight - 1)) {
                    newLevelData[y][x] = levelData[y][x];
                }
            }
        }
        
        levelData = newLevelData;
        
        // Ajuster les positions des joueurs si nécessaire
        player1X = Math.min(player1X, levelWidth - 2);
        player1Y = Math.min(player1Y, levelHeight - 2);
        player2X = Math.min(player2X, levelWidth - 2);
        player2Y = Math.min(player2Y, levelHeight - 2);
        
        // S'assurer que les joueurs ne sont pas sur des murs fixes
        if (player1X % 2 == 0 && player1Y % 2 == 0) {
            player1X = player1X + 1;
        }
        if (player2X % 2 == 0 && player2Y % 2 == 0) {
            player2X = player2X + 1;
        }
        
        clearSpawnAreas();
        updateDimensionsLabel();
        
        // Redimensionner le canvas
        editorCanvas.setWidth(levelWidth * TILE_SIZE);
        editorCanvas.setHeight(levelHeight * TILE_SIZE);
        
        // Sauvegarder l'état temporaire après redimensionnement
        saveTemporaryState();
    }

    /**
     * Nettoie les zones de spawn
     */
    public void clearSpawnAreas() {
        // Pour éviter de supprimer les murs indestructibles, ne nettoyons que les cases où ils ne sont pas
        
        // Zone joueur 1 - uniquement la case du joueur
        levelData[player1Y][player1X] = EMPTY;
        
        // Zone joueur 2 - uniquement la case du joueur
        levelData[player2Y][player2X] = EMPTY;
    }

    /**
     * Gère les clics de souris
     */
    @FXML
    private void handleMouseClick(MouseEvent event) {
        // Ignorer les clics droits
        if (!event.isPrimaryButtonDown()) {
            return;
        }
        
        int x = (int) (event.getX() / TILE_SIZE);
        int y = (int) (event.getY() / TILE_SIZE);

        paintTile(x, y);
    }

    /**
     * Gère le glissement de souris
     */
    @FXML
    private void handleMouseDrag(MouseEvent event) {
        // Ignorer les glissements avec le bouton droit
        if (!event.isPrimaryButtonDown()) {
            return;
        }
        
        int x = (int) (event.getX() / TILE_SIZE);
        int y = (int) (event.getY() / TILE_SIZE);

        paintTile(x, y);
    }

    /**
     * Gère les événements de souris (détecte le clic droit)
     */
    private void handleMousePressed(MouseEvent event) {
        if (event.isSecondaryButtonDown()) {
            // Fermer le menu précédent s'il existe
            if (activeContextMenu != null) {
                activeContextMenu.hide();
            }
            
            // Clic droit - afficher menu contextuel
            showContextMenu(event);
            
            // Consommer l'événement pour éviter d'autres actions
            event.consume();
        } else if (event.isPrimaryButtonDown()) {
            // Clic gauche - placer une tuile immédiatement
            int x = (int) (event.getX() / TILE_SIZE);
            int y = (int) (event.getY() / TILE_SIZE);
            paintTile(x, y);
        }
    }

    /**
     * Peint une case selon l'outil sélectionné
     */
    public void paintTile(int x, int y) {
        if (x < 0 || x >= levelWidth || y < 0 || y >= levelHeight) return;

        // Ne pas modifier les bordures et les murs fixes
        if (x == 0 || y == 0 || x == levelWidth - 1 || y == levelHeight - 1) return;
        if (x % 2 == 0 && y % 2 == 0) return; // Murs fixes

        boolean modified = false;

        switch (selectedTool) {
            case EMPTY:
            case WALL:
            case DESTRUCTIBLE_WALL:
                // Vérifier si le bloc est différent avant de le modifier
                if (levelData[y][x] != selectedTool) {
                    levelData[y][x] = selectedTool;
                    modified = true;
                }
                break;
            case 3: // Player 1 spawn
                if (player1X != x || player1Y != y) {
                    player1X = x;
                    player1Y = y;
                    clearSpawnAreas();
                    modified = true;
                }
                break;
            case 4: // Player 2 spawn
                if (player2X != x || player2Y != y) {
                    player2X = x;
                    player2Y = y;
                    clearSpawnAreas();
                    modified = true;
                }
                break;
        }

        render();
        
        // Sauvegarder automatiquement l'état uniquement si une modification a été effectuée
        if (modified) {
            saveTemporaryState();
        }
    }

    /**
     * Dessine le niveau
     */
    public void render() {
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
     * Supprime le fichier de sauvegarde temporaire
     */
    private void deleteTemporaryState() {
        File tempFile = new File(TEMP_SAVE_PATH);
        if (tempFile.exists()) {
            if (tempFile.delete()) {
                System.out.println("Fichier temporaire supprimé avec succès");
            } else {
                System.err.println("Impossible de supprimer le fichier temporaire");
            }
        }
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
            // Supprimer la sauvegarde temporaire
            deleteTemporaryState();
            
            initializeLevel();
            render();
        }
    }

    /**
     * Charge un niveau depuis un fichier
     */
    @FXML
    private void loadLevel() {
        Stage stage = (Stage) loadButton.getScene().getWindow();
        String filePath = FileDialogManager.showLoadDialog(stage);

        if (filePath != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
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

                // Supprimer la sauvegarde temporaire car on a chargé un niveau existant
                deleteTemporaryState();
                
                resizeLevel();
                render();

                System.out.println("Niveau chargé avec succès !");

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
        Stage stage = (Stage) saveButton.getScene().getWindow();
        String filePath = FileDialogManager.showSaveDialog(stage);

        if (filePath != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
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

                System.out.println("Niveau sauvegardé avec succès !");

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

        try {
            // Sauvegarder l'état actuel de l'éditeur
            saveTemporaryState();
            
            // Créer un fichier temporaire pour le niveau
            File tempFile = File.createTempFile("level_test", ".level");
            tempFile.deleteOnExit();

            // Sauvegarder le niveau dans le fichier temporaire
            try (PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {
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
            }

            // Charger la scène de jeu avec le niveau créé
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bomberman/view/game-view.fxml"));
            Parent gameRoot = loader.load();

            // Obtenir le contrôleur de jeu et lui passer le niveau
            GameController gameController = loader.getController();
            
            // Configurer le contrôleur de jeu pour utiliser le niveau temporaire
            if (gameController != null) {
                gameController.loadCustomLevel(tempFile.getAbsolutePath());
                // Indiquer que c'est un test depuis l'éditeur
                gameController.setTestMode(true);
                gameController.setLevelEditorStage((Stage) testButton.getScene().getWindow());
            }

            // Changer de scène
            Stage stage = (Stage) testButton.getScene().getWindow();
            
            // Calculer la taille de la fenêtre en fonction de la taille du niveau
            int canvasWidth = Math.max(600, levelWidth * 40 + 40); // 40 = TILE_SIZE dans GameController
            int canvasHeight = Math.max(520, levelHeight * 40 + 40);
            
            // Ajuster la taille du canvas dans la scène
            Canvas gameCanvas = (Canvas) gameRoot.lookup("#gameCanvas");
            if (gameCanvas != null) {
                gameCanvas.setWidth(canvasWidth);
                gameCanvas.setHeight(canvasHeight);
            }
            
            // Calculer la taille totale de la fenêtre (canvas + panneaux latéraux)
            int sceneWidth = canvasWidth + 200; // +200 pour le panneau latéral
            int sceneHeight = canvasHeight + 100; // +100 pour les en-têtes/pieds
            
            Scene gameScene = new Scene(gameRoot, sceneWidth, sceneHeight);

            // Charger le CSS s'il existe
            try {
                var cssResource = getClass().getResource("/com/example/bomberman/style.css");
                if (cssResource != null) {
                    gameScene.getStylesheets().add(cssResource.toExternalForm());
                }
            } catch (Exception cssError) {
                System.out.println("CSS non trouvé, continuation sans styles");
            }

            stage.setScene(gameScene);
            stage.setTitle("Super Bomberman - Test de niveau");

            System.out.println("Test du niveau lancé avec succès !");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de lancer le test du niveau : " + e.getMessage(), Alert.AlertType.ERROR);
        }
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

    /**
     * Retourne au menu principal
     */
    @FXML
    private void returnToMainMenu() {
        try {
            // Confirmer si des modifications non sauvegardées existent
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Retour au menu");
            confirmAlert.setHeaderText("Retourner au menu principal ?");
            confirmAlert.setContentText("Les modifications non sauvegardées seront perdues.");

            if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                // Supprimer la sauvegarde temporaire
                deleteTemporaryState();
                
                // Charger la scène du menu principal
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bomberman/view/menu-view.fxml"));
                Parent menuRoot = loader.load();
                
                // Obtenir le contrôleur du menu pour pouvoir actualiser les éléments si nécessaire
                MainMenuController menuController = loader.getController();
                
                // Changer de scène
                Stage stage = (Stage) returnButton.getScene().getWindow();
                Scene menuScene = new Scene(menuRoot, 800, 600);
                
                // Charger le CSS s'il existe
                try {
                    var cssResource = getClass().getResource("/com/example/bomberman/style.css");
                    if (cssResource != null) {
                        menuScene.getStylesheets().add(cssResource.toExternalForm());
                    }
                } catch (Exception cssError) {
                    System.out.println("CSS non trouvé, continuation sans styles");
                }
                
                stage.setScene(menuScene);
                stage.setTitle("Super Bomberman - Menu principal");
                
                // Informer le contrôleur du menu que nous revenons de l'éditeur
                menuController.returnToMenu();
                
                System.out.println("Retour au menu principal");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le menu principal.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Affiche le menu contextuel à la position spécifiée
     */
    private void showContextMenu(MouseEvent event) {
        // Créer un nouveau menu contextuel
        ContextMenu contextMenu = new ContextMenu();
        activeContextMenu = contextMenu;
        
        // Créer les items du menu avec les outils
        MenuItem emptyItem = new MenuItem("Vide");
        emptyItem.setOnAction(e -> {
            selectedTool = EMPTY;
            emptyTool.setSelected(true);
            updateCursor();
            activeContextMenu = null;
        });
        
        MenuItem wallItem = new MenuItem("Mur indestructible");
        wallItem.setOnAction(e -> {
            selectedTool = WALL;
            wallTool.setSelected(true);
            updateCursor();
            activeContextMenu = null;
        });
        
        MenuItem destructibleWallItem = new MenuItem("Mur destructible");
        destructibleWallItem.setOnAction(e -> {
            selectedTool = DESTRUCTIBLE_WALL;
            destructibleWallTool.setSelected(true);
            updateCursor();
            activeContextMenu = null;
        });
        
        // Séparateur
        SeparatorMenuItem separator = new SeparatorMenuItem();
        
        MenuItem player1SpawnItem = new MenuItem("Spawn Joueur 1");
        player1SpawnItem.setOnAction(e -> {
            selectedTool = 3;
            player1SpawnTool.setSelected(true);
            updateCursor();
            activeContextMenu = null;
        });
        
        MenuItem player2SpawnItem = new MenuItem("Spawn Joueur 2");
        player2SpawnItem.setOnAction(e -> {
            selectedTool = 4;
            player2SpawnTool.setSelected(true);
            updateCursor();
            activeContextMenu = null;
        });
        
        // Ajouter les items au menu
        contextMenu.getItems().addAll(
            emptyItem,
            wallItem,
            destructibleWallItem,
            separator,
            player1SpawnItem,
            player2SpawnItem
        );
        
        // Ajouter un gestionnaire pour nettoyer la référence quand le menu se ferme
        contextMenu.setOnHidden(e -> activeContextMenu = null);
        
        // Utiliser directement l'événement pour afficher le menu
        contextMenu.show(editorCanvas, event.getScreenX(), event.getScreenY());
    }

    /**
     * Place des murs destructibles aléatoirement sur les cases vides
     */
    public void placeRandomDestructibleWalls() {
        int countPlaced = 0;
        int maxWalls = (levelWidth * levelHeight) / 5; // ~20% de la carte
        java.util.Random random = new java.util.Random();
        
        // Placer des murs aléatoirement
        for (int y = 1; y < levelHeight - 1; y++) {
            for (int x = 1; x < levelWidth - 1; x++) {
                // Ne placer que sur des cases vides et pas sur les murs fixes
                if (levelData[y][x] == EMPTY && !(x % 2 == 0 && y % 2 == 0)) {
                    // Vérifier que ce n'est pas une zone de spawn pour les joueurs
                    boolean isNearPlayer1 = (Math.abs(x - player1X) <= 1 && Math.abs(y - player1Y) <= 1);
                    boolean isNearPlayer2 = (Math.abs(x - player2X) <= 1 && Math.abs(y - player2Y) <= 1);
                    
                    if (!isNearPlayer1 && !isNearPlayer2 && random.nextDouble() < 0.4) { // 40% de chance
                        levelData[y][x] = DESTRUCTIBLE_WALL;
                        countPlaced++;
                        
                        if (countPlaced >= maxWalls) {
                            break;
                        }
                    }
                }
            }
            if (countPlaced >= maxWalls) {
                break;
            }
        }
        
        render();
        System.out.println("Placés " + countPlaced + " murs destructibles aléatoirement.");
        
        // Sauvegarder l'état temporaire après placement des murs aléatoires
        saveTemporaryState();
    }

    /**
     * Sauvegarder l'état temporaire de l'éditeur
     */
    private void saveTemporaryState() {
        try {
            // Créer le répertoire levels s'il n'existe pas
            File levelsDir = new File("levels");
            if (!levelsDir.exists()) {
                levelsDir.mkdirs();
            }
            
            // Créer ou écraser le fichier temporaire
            tempSaveFile = new File(TEMP_SAVE_PATH);
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(tempSaveFile))) {
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
            }
            
            System.out.println("État temporaire sauvegardé dans " + TEMP_SAVE_PATH);
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde de l'état temporaire: " + e.getMessage());
        }
    }

    /**
     * Charge l'état temporaire s'il existe
     * @return true si l'état a été chargé, false sinon
     */
    private boolean loadTemporaryState() {
        File tempFile = new File(TEMP_SAVE_PATH);
        if (!tempFile.exists()) {
            return false;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
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

            // Redimensionner le canvas
            editorCanvas.setWidth(levelWidth * TILE_SIZE);
            editorCanvas.setHeight(levelHeight * TILE_SIZE);
            
            updateDimensionsLabel();
            
            System.out.println("État temporaire chargé depuis " + TEMP_SAVE_PATH);
            return true;
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'état temporaire: " + e.getMessage());
            // En cas d'erreur, supprimer le fichier temporaire corrompu
            tempFile.delete();
            return false;
        }
    }
}