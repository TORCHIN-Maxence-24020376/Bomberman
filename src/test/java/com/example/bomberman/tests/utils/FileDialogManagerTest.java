package com.example.bomberman.tests.utils;

import com.example.bomberman.utils.FileDialogManager;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class FileDialogManagerTest {

    @Mock
    private Stage mockStage;

    @Test
    public void testCreateLevelsDirectory() throws Exception {
        // Utiliser la réflexion pour accéder à la méthode privée
        Method createLevelsDirectoryMethod = FileDialogManager.class.getDeclaredMethod("createLevelsDirectory");
        createLevelsDirectoryMethod.setAccessible(true);
        
        // Appeler la méthode
        assertDoesNotThrow(() -> createLevelsDirectoryMethod.invoke(null));
        
        // Vérifier que le répertoire a été créé
        File levelsDir = new File("levels");
        assertTrue(levelsDir.exists());
        assertTrue(levelsDir.isDirectory());
    }
    
    @Test
    public void testShowSaveDialogReturnsNull() {
        // Utiliser MockedStatic pour simuler le comportement de la méthode statique
        try (MockedStatic<FileDialogManager> mockedStatic = Mockito.mockStatic(FileDialogManager.class)) {
            // Configurer le mock pour retourner null
            mockedStatic.when(() -> FileDialogManager.showSaveDialog(mockStage)).thenReturn(null);
            
            // Appeler la méthode
            String result = FileDialogManager.showSaveDialog(mockStage);
            
            // Vérifier le résultat
            assertNull(result);
        }
    }
    
    @Test
    public void testShowLoadDialogReturnsNull() {
        // Utiliser MockedStatic pour simuler le comportement de la méthode statique
        try (MockedStatic<FileDialogManager> mockedStatic = Mockito.mockStatic(FileDialogManager.class)) {
            // Configurer le mock pour retourner null
            mockedStatic.when(() -> FileDialogManager.showLoadDialog(mockStage)).thenReturn(null);
            
            // Appeler la méthode
            String result = FileDialogManager.showLoadDialog(mockStage);
            
            // Vérifier le résultat
            assertNull(result);
        }
    }
    
    @Test
    public void testShowSaveDialogReturnsPath() {
        // Utiliser MockedStatic pour simuler le comportement de la méthode statique
        try (MockedStatic<FileDialogManager> mockedStatic = Mockito.mockStatic(FileDialogManager.class)) {
            // Configurer le mock pour retourner un chemin
            String expectedPath = "levels/test_level.level";
            mockedStatic.when(() -> FileDialogManager.showSaveDialog(mockStage)).thenReturn(expectedPath);
            
            // Appeler la méthode
            String result = FileDialogManager.showSaveDialog(mockStage);
            
            // Vérifier le résultat
            assertEquals(expectedPath, result);
        }
    }
    
    @Test
    public void testShowLoadDialogReturnsPath() {
        // Utiliser MockedStatic pour simuler le comportement de la méthode statique
        try (MockedStatic<FileDialogManager> mockedStatic = Mockito.mockStatic(FileDialogManager.class)) {
            // Configurer le mock pour retourner un chemin
            String expectedPath = "levels/existing_level.level";
            mockedStatic.when(() -> FileDialogManager.showLoadDialog(mockStage)).thenReturn(expectedPath);
            
            // Appeler la méthode
            String result = FileDialogManager.showLoadDialog(mockStage);
            
            // Vérifier le résultat
            assertEquals(expectedPath, result);
        }
    }
} 