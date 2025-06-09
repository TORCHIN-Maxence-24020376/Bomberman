package com.example.bomberman.tests;

import com.example.bomberman.Main;
import com.example.bomberman.service.ProfileManager;
import com.example.bomberman.service.SoundManager;
import javafx.application.Application;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MainTest {

    @Mock
    private Stage mockStage;
    
    @Mock
    private ProfileManager mockProfileManager;
    
    @Mock
    private SoundManager mockSoundManager;
    
    private Main main;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        main = new Main();
    }
    
    @Test
    public void testMainMethodDoesNotThrowException() {
        // Utiliser MockedStatic pour simuler le comportement de Application.launch
        try (MockedStatic<Application> mockedStatic = Mockito.mockStatic(Application.class)) {
            // Configurer le mock pour ne rien faire quand launch est appelé
            mockedStatic.when(() -> Application.launch(Main.class)).thenReturn(null);
            
            // Appeler la méthode main
            assertDoesNotThrow(() -> Main.main(new String[]{}));
            
            // Vérifier que Application.launch a été appelé
            mockedStatic.verify(() -> Application.launch(Main.class));
        }
    }
    
    @Test
    public void testStopMethodSaveProfiles() {
        // Utiliser MockedStatic pour simuler le comportement de ProfileManager.getInstance
        try (MockedStatic<ProfileManager> mockedStatic = Mockito.mockStatic(ProfileManager.class)) {
            // Configurer le mock pour retourner notre mockProfileManager
            mockedStatic.when(ProfileManager::getInstance).thenReturn(mockProfileManager);
            
            // Appeler la méthode stop
            assertDoesNotThrow(() -> main.stop());
            
            // Vérifier que saveProfiles a été appelé
            verify(mockProfileManager).saveProfiles();
        }
    }
    
    @Test
    public void testStopMethodStopsMusic() {
        // Utiliser MockedStatic pour simuler le comportement de SoundManager.getInstance
        try (MockedStatic<SoundManager> mockedStatic = Mockito.mockStatic(SoundManager.class)) {
            // Configurer le mock pour retourner notre mockSoundManager
            mockedStatic.when(SoundManager::getInstance).thenReturn(mockSoundManager);
            
            // Appeler la méthode stop
            assertDoesNotThrow(() -> main.stop());
            
            // Vérifier que stopBackgroundMusic a été appelé
            verify(mockSoundManager).stopBackgroundMusic();
        }
    }
} 