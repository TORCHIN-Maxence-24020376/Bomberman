package com.example.bomberman.models.entities;

import com.example.bomberman.utils.ResourceManager;

import java.io.Serializable;

/**
 * Classe représentant le profil d'un joueur
 */
public class PlayerProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    private String firstName;
    private String lastName;
    private String avatarPath;
    private int gamesPlayed;
    private int gamesWon;
    private String theme;

    /**
     * Constructeur par défaut
     */
    public PlayerProfile() {
        this("", "", "", 0, 0, "default");
    }

    /**
     * Constructeur complet
     */
    public PlayerProfile(String firstName, String lastName, String avatarPath,
                         int gamesPlayed, int gamesWon, String theme) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.avatarPath = avatarPath;
        this.gamesPlayed = gamesPlayed;
        this.gamesWon = gamesWon;
        this.theme = theme;
    }

    /**
     * Ajoute une partie jouée
     * @param won true si la partie a été gagnée
     */
    public void addGame(boolean won) {
        gamesPlayed++;
        if (won) {
            gamesWon++;
        }
    }

    /**
     * Calcule le pourcentage de victoires
     * @return Pourcentage de victoires (0-100)
     */
    public double getWinPercentage() {
        if (gamesPlayed == 0) return 0.0;
        return (double) gamesWon / gamesPlayed * 100.0;
    }

    /**
     * Retourne le nom complet du joueur
     * @return Prénom + Nom
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    /**
     * Retourne le thème sous forme d'énumération
     * @return Le thème sous forme d'énumération
     */
    public ResourceManager.Theme getThemeEnum() {
        return ResourceManager.themeFromString(theme);
    }

    // Getters et Setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getAvatarPath() { return avatarPath; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }

    public int getGamesPlayed() { return gamesPlayed; }
    public void setGamesPlayed(int gamesPlayed) { this.gamesPlayed = gamesPlayed; }

    public int getGamesWon() { return gamesWon; }
    public void setGamesWon(int gamesWon) { this.gamesWon = gamesWon; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    @Override
    public String toString() {
        return getFullName() + " (" + gamesWon + "/" + gamesPlayed + " victoires)";
    }
}