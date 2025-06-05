package com.example.bomberman;

import com.example.bomberman.PlayerProfile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestionnaire pour la sauvegarde et le chargement des profils joueurs
 */
public class ProfileManager {
    private static final String PROFILES_DIR = "profiles";
    private static final String PROFILES_FILE = "profiles/players.dat";
    private static ProfileManager instance;
    private List<PlayerProfile> profiles;

    private ProfileManager() {
        profiles = new ArrayList<>();
        createProfilesDirectory();
        loadProfiles();
    }

    /**
     * Retourne l'instance unique du gestionnaire de profils
     */
    public static ProfileManager getInstance() {
        if (instance == null) {
            instance = new ProfileManager();
        }
        return instance;
    }

    /**
     * Crée le répertoire des profils s'il n'existe pas
     */
    private void createProfilesDirectory() {
        File dir = new File(PROFILES_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Charge tous les profils depuis le fichier
     */
    @SuppressWarnings("unchecked")
    public void loadProfiles() {
        File file = new File(PROFILES_FILE);
        if (!file.exists()) {
            // Créer des profils par défaut
            createDefaultProfiles();
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            profiles = (List<PlayerProfile>) ois.readObject();
            System.out.println("Profils chargés: " + profiles.size());
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des profils: " + e.getMessage());
            createDefaultProfiles();
        }
    }

    /**
     * Sauvegarde tous les profils dans le fichier
     */
    public void saveProfiles() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PROFILES_FILE))) {
            oos.writeObject(profiles);
            System.out.println("Profils sauvegardés: " + profiles.size());
        } catch (Exception e) {
            System.err.println("Erreur lors de la sauvegarde des profils: " + e.getMessage());
        }
    }

    /**
     * Crée des profils par défaut
     */
    private void createDefaultProfiles() {
        profiles.clear();
        profiles.add(new PlayerProfile("Joueur", "1", "avatar1.png", 0, 0, "default"));
        profiles.add(new PlayerProfile("Joueur", "2", "avatar2.png", 0, 0, "default"));
        saveProfiles();
    }

    /**
     * Ajoute un nouveau profil
     */
    public void addProfile(PlayerProfile profile) {
        profiles.add(profile);
        saveProfiles();
    }

    /**
     * Supprime un profil
     */
    public void removeProfile(PlayerProfile profile) {
        profiles.remove(profile);
        saveProfiles();
    }

    /**
     * Met à jour un profil existant
     */
    public void updateProfile(PlayerProfile profile) {
        // Le profil est déjà dans la liste par référence
        saveProfiles();
    }

    /**
     * Trouve un profil par nom complet
     */
    public PlayerProfile findProfile(String fullName) {
        return profiles.stream()
                .filter(p -> p.getFullName().equalsIgnoreCase(fullName))
                .findFirst()
                .orElse(null);
    }

    /**
     * Retourne tous les profils
     */
    public List<PlayerProfile> getAllProfiles() {
        return new ArrayList<>(profiles);
    }

    /**
     * Retourne le nombre de profils
     */
    public int getProfileCount() {
        return profiles.size();
    }

    /**
     * Vérifie si un nom de profil existe déjà
     */
    public boolean profileExists(String firstName, String lastName) {
        String fullName = firstName + " " + lastName;
        return profiles.stream()
                .anyMatch(p -> p.getFullName().equalsIgnoreCase(fullName));
    }
}