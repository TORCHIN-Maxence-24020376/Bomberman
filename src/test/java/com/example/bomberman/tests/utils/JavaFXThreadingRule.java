package com.example.bomberman.tests.utils;

import java.util.concurrent.CountDownLatch;

import javax.swing.SwingUtilities;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Classe utilitaire pour initialiser l'environnement JavaFX dans les tests
 */
public class JavaFXThreadingRule implements BeforeAllCallback {

    private static boolean jfxIsSetup;

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        setupJavaFX();
    }

    public static void setupJavaFX() throws InterruptedException {
        if (jfxIsSetup) return;
        
        final CountDownLatch latch = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> {
            // Initialiser l'environnement JavaFX
            new JFXPanel();
            latch.countDown();
        });
        
        latch.await();
        jfxIsSetup = true;
    }

    /**
     * Exécute une action sur le thread JavaFX
     */
    public static void runOnJavaFXThread(Runnable action) throws Exception {
        setupJavaFX();
        
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });
        
        latch.await();
    }
} 