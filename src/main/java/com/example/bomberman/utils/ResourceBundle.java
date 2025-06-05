package com.example.bomberman.utils;

import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import java.util.Map;
import java.util.HashMap;
import java.io.InputStream;

public class ResourceBundle {
    private Map<String, Image> images;
    private Map<String, String> strings;
    private Map<String, AudioClip> audioClips;
    
    private static final String IMAGE_PATH = "/com/example/bomberman/default/images/";
    private static final String AUDIO_PATH = "/com/example/bomberman/default/audio/";
    
    public ResourceBundle() {
        images = new HashMap<>();
        strings = new HashMap<>();
        audioClips = new HashMap<>();
        loadResources();
    }
    
    private void loadResources() {
        // Load player images
        loadImage("player1_down", "player1_down.png");
        loadImage("player1_up", "player1_up.png");
        loadImage("player1_left", "player1_left.png");
        loadImage("player1_right", "player1_right.png");
        
        // Load enemy images
        loadImage("enemy_basic", "enemy_basic.png");
        loadImage("enemy_smart", "enemy_smart.png");
        
        // Load tile images
        loadImage("wall", "wall.png");
        loadImage("breakable_wall", "breakable_wall.png");
        loadImage("floor", "floor.png");
        
        // Load bomb and explosion images
        loadImage("bomb", "bomb.png");
        loadImage("explosion_center", "explosion_center.png");
        loadImage("explosion_horizontal", "explosion_horizontal.png");
        loadImage("explosion_vertical", "explosion_vertical.png");
        
        // Load powerup images
        loadImage("powerup_bomb", "powerup_bomb.png");
        loadImage("powerup_fire", "powerup_fire.png");
        loadImage("powerup_speed", "powerup_speed.png");
    }
    
    private void loadImage(String key, String filename) {
        try {
            String path = IMAGE_PATH + filename;
            InputStream is = getClass().getResourceAsStream(path);
            if (is != null) {
                Image image = new Image(is);
                images.put(key, image);
            } else {
                System.err.println("Could not find image: " + path);
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + filename);
            e.printStackTrace();
        }
    }
    
    private void loadAudio(String key, String filename) {
        try {
            String path = AUDIO_PATH + filename;
            AudioClip clip = new AudioClip(getClass().getResource(path).toString());
            audioClips.put(key, clip);
        } catch (Exception e) {
            System.err.println("Error loading audio: " + filename);
            e.printStackTrace();
        }
    }
    
    public Image getImage(String key) {
        return images.get(key);
    }
    
    public String getString(String key) {
        return strings.get(key);
    }
    
    public AudioClip getAudioClip(String key) {
        return audioClips.get(key);
    }
    
    public void addImage(String key, Image image) {
        images.put(key, image);
    }
    
    public void addString(String key, String value) {
        strings.put(key, value);
    }
    
    public void addAudioClip(String key, AudioClip clip) {
        audioClips.put(key, clip);
    }
} 