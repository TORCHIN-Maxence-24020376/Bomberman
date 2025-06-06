// TestGameMapFX.java
package com.example.bomberman.test;

import com.example.bomberman.models.map.GameMap;
import com.example.bomberman.models.map.Tile;
import com.example.bomberman.models.map.WallTile;
import com.example.bomberman.models.map.BreakableWall;
import com.example.bomberman.models.map.FloorTile;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class TestGameMapFX extends Application {

    private static final int TILE_SIZE = 32;
    private static final int MAP_WIDTH = 20; // Max 150
    private static final int MAP_HEIGHT = 20;

    @Override
    public void start(Stage primaryStage) {
        // Instancie la map
        GameMap map = new GameMap(MAP_WIDTH, MAP_HEIGHT);

        // Crée un Canvas à la taille de la map
        Canvas canvas = new Canvas(MAP_WIDTH * TILE_SIZE, MAP_HEIGHT * TILE_SIZE);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Dessine chaque tuile en couleur selon son type
        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                Tile tile = map.getTile(x, y);
                Color color;

                if (tile instanceof WallTile) {
                    color = Color.DARKGRAY;
                } else if (tile instanceof BreakableWall) {
                    color = Color.SADDLEBROWN;
                } else if (tile instanceof FloorTile) {
                    color = Color.LIGHTGRAY;
                } else {
                    color = Color.MAGENTA; // Cas inattendu
                }

                gc.setFill(color);
                gc.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                // Optional : tracer le contour des cases
                gc.setStroke(Color.BLACK);
                gc.strokeRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        // Ajoute le canvas à la scène
        Group root = new Group(canvas);
        Scene scene = new Scene(root);

        primaryStage.setTitle("Test GameMap");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
