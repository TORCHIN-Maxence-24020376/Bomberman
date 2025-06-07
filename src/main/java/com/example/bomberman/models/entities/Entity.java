package com.example.bomberman.models.entities;

import com.example.bomberman.models.map.GameMap;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * Classe de base pour toutes les entités (joueur, ennemis, etc.).
 */

public abstract class Entity {
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected boolean active;
    protected GameMap map;

    public Entity(int x, int y, GameMap map) {
        this.x = x;
        this.y = y;
        this.map = map;
    }

    public abstract void update();
    public abstract Rectangle2D getHitBox();
    public abstract Image getSprite();

    public boolean isActive(){
        return active;
    }

    public void setActive(boolean active){
        this.active = active;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}