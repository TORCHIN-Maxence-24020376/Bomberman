package com.example.bomberman.models.entities;

import com.example.bomberman.enums.Direction;
import com.example.bomberman.utils.SpriteLoader;
import com.example.bomberman.models.map.GameMap;
import javafx.beans.property.*;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

import java.util.Set;

public class Player extends Entity {
    private String name;
    private IntegerProperty health = new SimpleIntegerProperty();
    private DoubleProperty speed = new SimpleDoubleProperty();
    private IntegerProperty bombCount = new SimpleIntegerProperty();
    private IntegerProperty bombRange = new SimpleIntegerProperty();
    private BooleanProperty isAlive = new SimpleBooleanProperty();
    private Direction direction;
    private int playerId;
    private Set<KeyCode> keys;

    public Player(int x, int y, String name, int playerId, GameMap map) {
        super(x, y, map);
        this.name = name;
        this.playerId = playerId;
        this.health.set(3);
        this.speed.set(1.0);
        this.bombRange.set(1);
        this.bombCount.set(0);
        this.isAlive.set(false);
    }

    @Override
    public void update() {
    }

    @Override
    public Rectangle2D getHitBox() {
        int ts = map.getTileSize();
        return new Rectangle2D(x * ts, y * ts, ts, ts);
    }

    @Override
    public Image getSprite() {
        String path;
        Color color;
        switch (playerId) {
            case 1:
                path = "/com/example/bomberman/default/players/player1.png";
                color = Color.CYAN;
                break;
            case 2:
                path = "/com/example/bomberman/default/players/player2.png";
                color = Color.GREEN;
                break;
            default:
                // Si jamais playerId n’est pas entre 1 et 2, on prend un sprite générique
                path = "/com/example/bomberman/default/players/player_default.png";
                color = Color.BLUE;
                break;
        }
        return SpriteLoader.load(
                path,
                Color.BLUE,
                32
        );
    }

    public void move(Direction direction) {
        this.direction = direction;
        switch (direction) {
            case UP:
                y -= 1;
                break;
            case DOWN:
                y += 1;
                break;
            case LEFT:
                x -= 1;
                break;
            case RIGHT:
                x += 1;
                break;
            default:
                break;
        }
    }

    public void notifyBombExploded() {
        bombCount.set(bombCount.get() + 1);
    }


    public Bomb placeBomb(){
        if (bombCount.get() < 1) {
            int range = bombRange.get();
            Bomb bombe = new Bomb(x, y, range, this, map);
            bombCount.set(bombCount.get() + 1);
            return bombe;
        }
        return null;
    }

    public void takeDamage(int amount) {
        this.health.set(health.get() - amount);
    }

    public void collectPowerUp(PowerUp powerUp){

    }

    private boolean canMoveTo(int newX, int newY) {

    }
}