package com.example.bomberman.tests.models.entities;

import com.example.bomberman.models.entities.PlayerProfile;
import com.example.bomberman.utils.ResourceManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerProfileTest {

    private PlayerProfile profile;
    
    @BeforeEach
    public void setUp() {
        profile = new PlayerProfile("John", "Doe", "avatar.png", 10, 5, "default");
    }
    
    @Test
    public void testInitialState() {
        assertEquals("John", profile.getFirstName());
        assertEquals("Doe", profile.getLastName());
        assertEquals("avatar.png", profile.getAvatarPath());
        assertEquals(10, profile.getGamesPlayed());
        assertEquals(5, profile.getGamesWon());
        assertEquals("default", profile.getTheme());
        assertEquals("John Doe", profile.getFullName());
        assertEquals(ResourceManager.Theme.DEFAULT, profile.getThemeEnum());
    }
    
    @Test
    public void testWinPercentage() {
        // 5 victoires sur 10 parties = 50%
        assertEquals(50.0, profile.getWinPercentage(), 0.01);
        
        // Cas où il n'y a pas de parties jouées
        PlayerProfile newProfile = new PlayerProfile();
        assertEquals(0.0, newProfile.getWinPercentage(), 0.01);
    }
    
    @Test
    public void testAddGame() {
        // État initial: 10 parties, 5 victoires
        assertEquals(10, profile.getGamesPlayed());
        assertEquals(5, profile.getGamesWon());
        
        // Ajouter une victoire
        profile.addGame(true);
        assertEquals(11, profile.getGamesPlayed());
        assertEquals(6, profile.getGamesWon());
        assertEquals(54.54, profile.getWinPercentage(), 0.01);
        
        // Ajouter une défaite
        profile.addGame(false);
        assertEquals(12, profile.getGamesPlayed());
        assertEquals(6, profile.getGamesWon());
        assertEquals(50.0, profile.getWinPercentage(), 0.01);
    }
    
    @Test
    public void testSettersAndGetters() {
        profile.setFirstName("Jane");
        assertEquals("Jane", profile.getFirstName());
        
        profile.setLastName("Smith");
        assertEquals("Smith", profile.getLastName());
        
        profile.setAvatarPath("new_avatar.png");
        assertEquals("new_avatar.png", profile.getAvatarPath());
        
        profile.setGamesPlayed(20);
        assertEquals(20, profile.getGamesPlayed());
        
        profile.setGamesWon(15);
        assertEquals(15, profile.getGamesWon());
        
        profile.setTheme("jungle");
        assertEquals("jungle", profile.getTheme());
        assertEquals(ResourceManager.Theme.JUNGLE, profile.getThemeEnum());
    }
    
    @Test
    public void testToString() {
        assertEquals("John Doe (5/10 victoires)", profile.toString());
    }
    
    @Test
    public void testDefaultConstructor() {
        PlayerProfile defaultProfile = new PlayerProfile();
        assertEquals("", defaultProfile.getFirstName());
        assertEquals("", defaultProfile.getLastName());
        assertEquals("", defaultProfile.getAvatarPath());
        assertEquals(0, defaultProfile.getGamesPlayed());
        assertEquals(0, defaultProfile.getGamesWon());
        assertEquals("default", defaultProfile.getTheme());
        assertEquals(ResourceManager.Theme.DEFAULT, defaultProfile.getThemeEnum());
    }
    
    @Test
    public void testThemeEnum() {
        // Tester différents thèmes
        profile.setTheme("desert");
        assertEquals(ResourceManager.Theme.DESERT, profile.getThemeEnum());
        
        profile.setTheme("JUNGLE");
        assertEquals(ResourceManager.Theme.JUNGLE, profile.getThemeEnum());
        
        // Thème invalide doit retourner DEFAULT
        profile.setTheme("invalid_theme");
        assertEquals(ResourceManager.Theme.DEFAULT, profile.getThemeEnum());
    }
} 