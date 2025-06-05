package com.example.bomberman.utils;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.SnapshotParameters;

public class SpriteLoader {

    /**
     * Charge une image à partir d’un chemin, ou génère une case colorée si l’image est introuvable.
     * @param resourcePath Chemin vers l’image (ex: "/com/example/bomberman/default/tiles/wall.png")
     * @param fallbackColor Couleur à afficher en cas d’erreur
     * @param size Taille de la case (ex: 32)
     * @return Image chargée ou image colorée
     */
    public static Image load(String resourcePath, Color fallbackColor, int size) {
        try {
            Image image = new Image(SpriteLoader.class.getResourceAsStream(resourcePath));
            if (image.isError() || image.getWidth() <= 0) {
                throw new Exception("Image invalide");
            }
            return image;
        } catch (Exception e) {
            return createColoredTile(fallbackColor, size);
        }
    }

    private static Image createColoredTile(Color color, int size) {
        Canvas canvas = new Canvas(size, size);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(color);
        gc.fillRect(0, 0, size, size);
        WritableImage image = new WritableImage(size, size);
        canvas.snapshot(new SnapshotParameters(), image);
        return image;
    }
}
