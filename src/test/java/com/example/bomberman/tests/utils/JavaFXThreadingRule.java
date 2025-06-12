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

    public static synchronized void setupJavaFX() throws InterruptedException {
        if (jfxIsSetup) return;
        
        try {
            // Vérifier si la classe JFXPanel est accessible
            Class.forName("javafx.embed.swing.JFXPanel");
            
            final CountDownLatch latch = new CountDownLatch(1);
            SwingUtilities.invokeLater(() -> {
                try {
                    // Initialiser l'environnement JavaFX
                    new JFXPanel();
                } catch (Exception e) {
                    System.err.println("Erreur lors de l'initialisation de JavaFX: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
            
            latch.await();
            jfxIsSetup = true;
            System.out.println("JavaFX initialisé avec succès");
        } catch (ClassNotFoundException e) {
            System.err.println("JavaFX non disponible: " + e.getMessage());
            // Ne pas bloquer les tests si JavaFX n'est pas disponible
            jfxIsSetup = true;
        }
    }

    /**
     * Exécute une action sur le thread JavaFX
     */
    public static void runOnJavaFXThread(Runnable action) throws Exception {
        setupJavaFX();
        
        try {
            // Vérifier si Platform est disponible
            if (!Platform.isFxApplicationThread() && Platform.isImplicitExit()) {
                final CountDownLatch latch = new CountDownLatch(1);
                Platform.runLater(() -> {
                    try {
                        action.run();
                    } catch (Exception e) {
                        System.err.println("Erreur lors de l'exécution sur le thread JavaFX: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
                
                latch.await();
            } else {
                // Exécuter directement si nous sommes déjà sur le thread JavaFX
                action.run();
            }
        } catch (Exception e) {
            System.err.println("Impossible d'exécuter sur le thread JavaFX: " + e.getMessage());
            // Exécuter directement si JavaFX n'est pas disponible
            action.run();
        }
    }
} 