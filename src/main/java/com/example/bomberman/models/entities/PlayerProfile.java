package com.example.bomberman.models.entities;

import com.example.bomberman.utils.ResourceManager;
import javafx.scene.input.KeyCode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe représentant le profil d'un joueur
 */
public class PlayerProfile implements Serializable {
    private static final long serialVersionUID = 1L; // Revenir à 1 pour compatibilité

    private String firstName;
    private String lastName;
    private String avatarPath;
    private int gamesPlayed;
    private int gamesWon;
    private String theme;
    
    // Contrôles personnalisés
    private transient Map<String, KeyCode> controls;
    
    // Actions disponibles
    public static final String ACTION_UP = "up";
    public static final String ACTION_DOWN = "down";
    public static final String ACTION_LEFT = "left";
    public static final String ACTION_RIGHT = "right";
    public static final String ACTION_BOMB = "bomb";
    public static final String ACTION_SPECIAL = "special";

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
        initializeDefaultControls();
    }
    
    /**
     * Méthode appelée lors de la désérialisation pour initialiser les champs transients
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // Lire les champs normaux
        in.defaultReadObject();
        
        // Initialiser les contrôles par défaut
        initializeDefaultControls();
    }
    
    /**
     * Initialise les contrôles par défaut
     */
    private void initializeDefaultControls() {
        controls = new HashMap<>();
        
        // Contrôles par défaut pour le joueur 1
        if (firstName.equals("Joueur") && lastName.equals("1")) {
            controls.put(ACTION_UP, KeyCode.Z);
            controls.put(ACTION_DOWN, KeyCode.S);
            controls.put(ACTION_LEFT, KeyCode.Q);
            controls.put(ACTION_RIGHT, KeyCode.D);
            controls.put(ACTION_BOMB, KeyCode.SPACE);
            controls.put(ACTION_SPECIAL, KeyCode.E);
        } 
        // Contrôles par défaut pour le joueur 2
        else if (firstName.equals("Joueur") && lastName.equals("2")) {
            controls.put(ACTION_UP, KeyCode.UP);
            controls.put(ACTION_DOWN, KeyCode.DOWN);
            controls.put(ACTION_LEFT, KeyCode.LEFT);
            controls.put(ACTION_RIGHT, KeyCode.RIGHT);
            controls.put(ACTION_BOMB, KeyCode.ENTER);
            controls.put(ACTION_SPECIAL, KeyCode.PAGE_DOWN);
        }
        // Contrôles par défaut pour tout autre joueur
        else {
            controls.put(ACTION_UP, KeyCode.UP);
            controls.put(ACTION_DOWN, KeyCode.DOWN);
            controls.put(ACTION_LEFT, KeyCode.LEFT);
            controls.put(ACTION_RIGHT, KeyCode.RIGHT);
            controls.put(ACTION_BOMB, KeyCode.SPACE);
            controls.put(ACTION_SPECIAL, KeyCode.CONTROL);
        }
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
    
    /**
     * Définit un contrôle pour une action
     * @param action L'action à configurer
     * @param keyCode La touche à associer
     */
    public void setControl(String action, KeyCode keyCode) {
        controls.put(action, keyCode);
    }
    
    /**
     * Récupère la touche associée à une action
     * @param action L'action dont on veut la touche
     * @return La touche associée, ou null si aucune
     */
    public KeyCode getControl(String action) {
        return controls.getOrDefault(action, null);
    }
    
    /**
     * Vérifie si la touche est utilisée pour une action
     * @param keyCode La touche à vérifier
     * @return true si la touche est déjà utilisée
     */
    public boolean isKeyUsed(KeyCode keyCode) {
        return controls.containsValue(keyCode);
    }
    
    /**
     * Réinitialise les contrôles aux valeurs par défaut
     */
    public void resetControls() {
        initializeDefaultControls();
    }
    
    /**
     * Récupère tous les contrôles
     * @return Une copie de la map des contrôles
     */
    public Map<String, KeyCode> getAllControls() {
        return new HashMap<>(controls);
    }

    // Getters et Setters
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public int getGamesWon() {
        return gamesWon;
    }

    public void setGamesWon(int gamesWon) {
        this.gamesWon = gamesWon;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    @Override
    public String toString() {
        return getFullName() + " (" + gamesWon + "/" + gamesPlayed + " victoires)";
    }
}