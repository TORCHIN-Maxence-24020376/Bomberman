package com.example.bomberman.tests.models.entities;

import com.example.bomberman.models.entities.PlayerProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerProfileTest {

    private PlayerProfile profile;
    
    @BeforeEach
    public void setUp() {
        profile = new PlayerProfile("Jean", "Dupont", "avatar.png", 10, 5, "default");
    }
    
    @Test
    public void testInitialState() {
        assertEquals("Jean", profile.getFirstName());
        assertEquals("Dupont", profile.getLastName());
        assertEquals("Jean Dupont", profile.getFullName());
        assertEquals("avatar.png", profile.getAvatarPath());
        assertEquals(10, profile.getGamesPlayed());
        assertEquals(5, profile.getGamesWon());
        assertEquals("default", profile.getTheme());
    }
    
    @Test
    public void testWinPercentage() {
        // Profil initial avec 10 parties jouées et 5 gagnées
        assertEquals(50.0, profile.getWinPercentage(), 0.01);
        
        // Profil avec 0 parties jouées
        PlayerProfile emptyProfile = new PlayerProfile("Test", "User", "", 0, 0, "default");
        assertEquals(0.0, emptyProfile.getWinPercentage(), 0.01);
        
        // Profil avec 100% de victoires
        PlayerProfile perfectProfile = new PlayerProfile("Pro", "Gamer", "", 20, 20, "default");
        assertEquals(100.0, perfectProfile.getWinPercentage(), 0.01);
    }
    
    @Test
    public void testAddGame() {
        // État initial
        assertEquals(10, profile.getGamesPlayed());
        assertEquals(5, profile.getGamesWon());
        assertEquals(50.0, profile.getWinPercentage(), 0.01);
        
        // Ajouter une partie gagnée
        profile.addGame(true);
        assertEquals(11, profile.getGamesPlayed());
        assertEquals(6, profile.getGamesWon());
        assertEquals(54.55, profile.getWinPercentage(), 0.01);
        
        // Ajouter une partie perdue
        profile.addGame(false);
        assertEquals(12, profile.getGamesPlayed());
        assertEquals(6, profile.getGamesWon());
        assertEquals(50.0, profile.getWinPercentage(), 0.01);
    }
    
    @Test
    public void testSettersAndGetters() {
        // Modifier les propriétés
        profile.setFirstName("Pierre");
        profile.setLastName("Martin");
        profile.setAvatarPath("new_avatar.png");
        profile.setGamesPlayed(20);
        profile.setGamesWon(15);
        profile.setTheme("dark");
        
        // Vérifier les modifications
        assertEquals("Pierre", profile.getFirstName());
        assertEquals("Martin", profile.getLastName());
        assertEquals("Pierre Martin", profile.getFullName());
        assertEquals("new_avatar.png", profile.getAvatarPath());
        assertEquals(20, profile.getGamesPlayed());
        assertEquals(15, profile.getGamesWon());
        assertEquals("dark", profile.getTheme());
        assertEquals(75.0, profile.getWinPercentage(), 0.01);
    }
    
    @Test
    public void testToString() {
        String expected = "Jean Dupont (5/10 victoires)";
        assertEquals(expected, profile.toString());
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
    }
} 