package com.example.bomberman.tests.service;

import com.example.bomberman.models.entities.PlayerProfile;
import com.example.bomberman.service.ProfileManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ProfileManagerTest {

    private ProfileManager profileManager;
    private String testProfilesDir;
    private String testProfilesFile;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    public void setUp() throws Exception {
        // Utiliser un répertoire temporaire pour les tests
        testProfilesDir = tempDir.resolve("test_profiles").toString();
        testProfilesFile = tempDir.resolve("test_profiles/players.dat").toString();
        
        // Réinitialiser l'instance singleton pour les tests
        resetSingleton();
        
        // Créer un répertoire de test
        File dir = new File(testProfilesDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        // Configurer le gestionnaire de profils pour utiliser le répertoire de test
        profileManager = ProfileManager.getInstance();
        
        // Utiliser la réflexion pour modifier les chemins de fichiers
        setPrivateField(profileManager, "PROFILES_DIR", testProfilesDir);
        setPrivateField(profileManager, "PROFILES_FILE", testProfilesFile);
        
        // Réinitialiser les profils pour les tests
        List<PlayerProfile> emptyProfiles = new ArrayList<>();
        setPrivateField(profileManager, "profiles", emptyProfiles);
    }
    
    @AfterEach
    public void tearDown() {
        // Réinitialiser l'instance singleton pour éviter les interférences entre les tests
        try {
            resetSingleton();
        } catch (Exception e) {
            System.err.println("Erreur lors de la réinitialisation du singleton: " + e.getMessage());
        }
    }
    
    @Test
    public void testGetInstance() {
        // Vérifier que getInstance retourne toujours la même instance
        ProfileManager instance1 = ProfileManager.getInstance();
        ProfileManager instance2 = ProfileManager.getInstance();
        
        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }
    
    @Test
    public void testAddProfile() {
        // Ajouter un profil
        PlayerProfile profile = new PlayerProfile("Test", "User", "avatar.png", 0, 0, "default");
        profileManager.addProfile(profile);
        
        // Vérifier que le profil a été ajouté
        List<PlayerProfile> profiles = profileManager.getAllProfiles();
        assertEquals(1, profiles.size());
        assertEquals("Test User", profiles.get(0).getFullName());
    }
    
    @Test
    public void testRemoveProfile() {
        // Ajouter un profil
        PlayerProfile profile = new PlayerProfile("Test", "User", "avatar.png", 0, 0, "default");
        profileManager.addProfile(profile);
        
        // Vérifier que le profil a été ajouté
        assertEquals(1, profileManager.getProfileCount());
        
        // Supprimer le profil
        profileManager.removeProfile(profile);
        
        // Vérifier que le profil a été supprimé
        assertEquals(0, profileManager.getProfileCount());
    }
    
    @Test
    public void testUpdateProfile() {
        // Ajouter un profil
        PlayerProfile profile = new PlayerProfile("Test", "User", "avatar.png", 0, 0, "default");
        profileManager.addProfile(profile);
        
        // Modifier le profil
        profile.setFirstName("Updated");
        profile.setGamesPlayed(5);
        profile.setGamesWon(3);
        
        // Mettre à jour le profil
        profileManager.updateProfile(profile);
        
        // Vérifier que le profil a été mis à jour
        PlayerProfile updatedProfile = profileManager.findProfile("Updated User");
        assertNotNull(updatedProfile);
        assertEquals(5, updatedProfile.getGamesPlayed());
        assertEquals(3, updatedProfile.getGamesWon());
    }
    
    @Test
    public void testFindProfile() {
        // Ajouter des profils
        PlayerProfile profile1 = new PlayerProfile("John", "Doe", "avatar1.png", 0, 0, "default");
        PlayerProfile profile2 = new PlayerProfile("Jane", "Smith", "avatar2.png", 0, 0, "default");
        profileManager.addProfile(profile1);
        profileManager.addProfile(profile2);
        
        // Rechercher un profil existant
        PlayerProfile found = profileManager.findProfile("John Doe");
        assertNotNull(found);
        assertEquals("John", found.getFirstName());
        
        // Rechercher un profil inexistant
        PlayerProfile notFound = profileManager.findProfile("Unknown User");
        assertNull(notFound);
    }
    
    @Test
    public void testProfileExists() {
        // Ajouter un profil
        PlayerProfile profile = new PlayerProfile("Test", "User", "avatar.png", 0, 0, "default");
        profileManager.addProfile(profile);
        
        // Vérifier qu'un profil existe
        assertTrue(profileManager.profileExists("Test", "User"));
        
        // Vérifier qu'un profil n'existe pas
        assertFalse(profileManager.profileExists("Unknown", "User"));
    }
    
    @Test
    public void testSaveAndLoadProfiles() throws Exception {
        // Ajouter des profils
        PlayerProfile profile1 = new PlayerProfile("Save", "Test", "avatar1.png", 5, 2, "default");
        PlayerProfile profile2 = new PlayerProfile("Load", "Test", "avatar2.png", 10, 7, "dark");
        profileManager.addProfile(profile1);
        profileManager.addProfile(profile2);
        
        // Sauvegarder les profils
        profileManager.saveProfiles();
        
        // Réinitialiser les profils
        List<PlayerProfile> emptyProfiles = new ArrayList<>();
        setPrivateField(profileManager, "profiles", emptyProfiles);
        
        // Vérifier que les profils sont vides
        assertEquals(0, profileManager.getProfileCount());
        
        // Charger les profils
        profileManager.loadProfiles();
        
        // Vérifier que les profils ont été chargés
        assertEquals(2, profileManager.getProfileCount());
        assertNotNull(profileManager.findProfile("Save Test"));
        assertNotNull(profileManager.findProfile("Load Test"));
    }
    
    // Méthodes utilitaires pour la réflexion
    
    private void resetSingleton() throws Exception {
        Field instance = ProfileManager.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }
    
    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
} 