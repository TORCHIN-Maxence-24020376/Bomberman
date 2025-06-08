package com.example.bomberman.tests.controller;

import com.example.bomberman.controller.LevelEditorController;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LevelEditorControllerTest {

    @InjectMocks
    private LevelEditorController levelEditorController;
    
    @Mock
    private Canvas editorCanvas;
    
    @Mock
    private GraphicsContext graphicsContext;
    
    @Mock
    private ToggleGroup toolGroup;
    
    @Mock
    private ToggleButton emptyTool;
    
    @Mock
    private ToggleButton wallTool;
    
    @Mock
    private ToggleButton destructibleWallTool;
    
    @Mock
    private ToggleButton player1SpawnTool;
    
    @Mock
    private ToggleButton player2SpawnTool;
    
    @Mock
    private Button returnButton;
    
    @Mock
    private Button newButton;
    
    @Mock
    private Button loadButton;
    
    @Mock
    private Button saveButton;
    
    @Mock
    private Button testButton;
    
    @Mock
    private Button randomWallsButton;
    
    @Mock
    private Label dimensionsLabel;
    
    @Mock
    private Spinner<Integer> widthSpinner;
    
    @Mock
    private Spinner<Integer> heightSpinner;
    
    @Mock
    private SpinnerValueFactory.IntegerSpinnerValueFactory widthValueFactory;
    
    @Mock
    private SpinnerValueFactory.IntegerSpinnerValueFactory heightValueFactory;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // Configurer les mocks
        when(editorCanvas.getGraphicsContext2D()).thenReturn(graphicsContext);
        when(widthSpinner.getValueFactory()).thenReturn(widthValueFactory);
        when(heightSpinner.getValueFactory()).thenReturn(heightValueFactory);
        
        // Initialiser le tableau levelData
        int[][] levelData = new int[13][15];
        setPrivateField(levelEditorController, "levelData", levelData);
        setPrivateField(levelEditorController, "levelWidth", 15);
        setPrivateField(levelEditorController, "levelHeight", 13);
        setPrivateField(levelEditorController, "player1X", 1);
        setPrivateField(levelEditorController, "player1Y", 1);
        setPrivateField(levelEditorController, "player2X", 13);
        setPrivateField(levelEditorController, "player2Y", 11);
        setPrivateField(levelEditorController, "selectedTool", 0);
    }
    
    @Test
    public void testInitializeLevel() throws Exception {
        // Accéder à la méthode privée
        Method initializeLevelMethod = LevelEditorController.class.getDeclaredMethod("initializeLevel");
        initializeLevelMethod.setAccessible(true);
        
        // Appeler la méthode
        initializeLevelMethod.invoke(levelEditorController);
        
        // Récupérer le tableau levelData
        int[][] levelData = (int[][]) getPrivateField(levelEditorController, "levelData");
        
        // Vérifier que les bordures sont des murs
        assertEquals(1, levelData[0][0]); // Coin supérieur gauche
        assertEquals(1, levelData[0][14]); // Coin supérieur droit
        assertEquals(1, levelData[12][0]); // Coin inférieur gauche
        assertEquals(1, levelData[12][14]); // Coin inférieur droit
        
        // Vérifier que les murs fixes sont en damier
        assertEquals(1, levelData[2][2]);
        assertEquals(1, levelData[4][4]);
        
        // Vérifier que les zones de spawn sont vides
        assertEquals(0, levelData[1][1]); // Spawn joueur 1
        assertEquals(0, levelData[11][13]); // Spawn joueur 2
    }
    
    @Test
    public void testResizeLevel() throws Exception {
        // Accéder à la méthode privée
        Method resizeLevelMethod = LevelEditorController.class.getDeclaredMethod("resizeLevel");
        resizeLevelMethod.setAccessible(true);
        
        // Modifier les dimensions
        setPrivateField(levelEditorController, "levelWidth", 11);
        setPrivateField(levelEditorController, "levelHeight", 9);
        
        // Appeler la méthode
        resizeLevelMethod.invoke(levelEditorController);
        
        // Récupérer le tableau levelData
        int[][] levelData = (int[][]) getPrivateField(levelEditorController, "levelData");
        
        // Vérifier les nouvelles dimensions
        assertEquals(9, levelData.length);
        assertEquals(11, levelData[0].length);
        
        // Vérifier que les bordures sont des murs
        assertEquals(1, levelData[0][0]); // Coin supérieur gauche
        assertEquals(1, levelData[0][10]); // Coin supérieur droit
        assertEquals(1, levelData[8][0]); // Coin inférieur gauche
        assertEquals(1, levelData[8][10]); // Coin inférieur droit
    }
    
    @Test
    public void testClearSpawnAreas() throws Exception {
        // Accéder à la méthode privée
        Method clearSpawnAreasMethod = LevelEditorController.class.getDeclaredMethod("clearSpawnAreas");
        clearSpawnAreasMethod.setAccessible(true);
        
        // Initialiser le tableau avec des murs partout
        int[][] levelData = new int[13][15];
        for (int y = 0; y < 13; y++) {
            for (int x = 0; x < 15; x++) {
                levelData[y][x] = 1;
            }
        }
        setPrivateField(levelEditorController, "levelData", levelData);
        
        // Appeler la méthode
        clearSpawnAreasMethod.invoke(levelEditorController);
        
        // Vérifier que les zones de spawn sont vides
        assertEquals(0, levelData[1][1]); // Spawn joueur 1
        assertEquals(0, levelData[11][13]); // Spawn joueur 2
    }
    
    @Test
    public void testPlaceRandomDestructibleWalls() throws Exception {
        // Accéder à la méthode privée
        Method placeRandomWallsMethod = LevelEditorController.class.getDeclaredMethod("placeRandomDestructibleWalls");
        placeRandomWallsMethod.setAccessible(true);
        
        // Initialiser le tableau avec des cases vides
        int[][] levelData = new int[13][15];
        setPrivateField(levelEditorController, "levelData", levelData);
        
        // Appeler la méthode
        placeRandomWallsMethod.invoke(levelEditorController);
        
        // Compter le nombre de murs destructibles
        int wallCount = 0;
        for (int y = 0; y < 13; y++) {
            for (int x = 0; x < 15; x++) {
                if (levelData[y][x] == 2) {
                    wallCount++;
                }
            }
        }
        
        // Vérifier qu'au moins quelques murs ont été placés
        assertTrue(wallCount > 0);
        
        // Vérifier que les zones de spawn sont toujours vides
        assertEquals(0, levelData[1][1]); // Spawn joueur 1
        assertEquals(0, levelData[11][13]); // Spawn joueur 2
    }
    
    @Test
    public void testPaintTile() throws Exception {
        // Accéder à la méthode privée
        Method paintTileMethod = LevelEditorController.class.getDeclaredMethod("paintTile", int.class, int.class);
        paintTileMethod.setAccessible(true);
        
        // Initialiser le tableau avec des cases vides
        int[][] levelData = new int[13][15];
        setPrivateField(levelEditorController, "levelData", levelData);
        
        // Sélectionner l'outil mur
        setPrivateField(levelEditorController, "selectedTool", 1);
        
        // Peindre une case
        paintTileMethod.invoke(levelEditorController, 3, 3);
        
        // Vérifier que la case a été modifiée
        assertEquals(1, levelData[3][3]);
        
        // Essayer de peindre une bordure (ne devrait pas changer)
        levelData[0][0] = 1;
        paintTileMethod.invoke(levelEditorController, 0, 0);
        assertEquals(1, levelData[0][0]);
        
        // Essayer de peindre un mur fixe (ne devrait pas changer)
        levelData[2][2] = 1;
        paintTileMethod.invoke(levelEditorController, 2, 2);
        assertEquals(1, levelData[2][2]);
    }
    
    // Méthodes utilitaires pour la réflexion
    
    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        Field field = LevelEditorController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
    
    private Object getPrivateField(Object object, String fieldName) throws Exception {
        Field field = LevelEditorController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }
} 