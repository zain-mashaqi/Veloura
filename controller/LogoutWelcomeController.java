package com.veloura.controller;

import com.mycompany.velourafx.App;

import java.io.IOException;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;

import javafx.application.Platform;

import javafx.fxml.FXML;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

import javafx.util.Duration;

public class LogoutWelcomeController {

    @FXML private Label lblTitle;
    @FXML private Label lblSubtitle;
    @FXML private Label lblLogout;

    @FXML private ProgressIndicator loader;

    @FXML
    public void initialize() {

        playAnimations();

        new Thread(() -> {

            try {

                Thread.sleep(2800);

                Platform.runLater(() -> {

                    try {

                        App.setRoot(
                                "Login",
                                800,
                                620
                        );

                    } catch (IOException e) {

                        e.printStackTrace();
                    }
                });

            } catch (InterruptedException e) {

                e.printStackTrace();
            }

        }).start();
    }

    private void playAnimations() {

        animateNode(lblTitle, 0);

        animateNode(lblSubtitle, 250);

        animateNode(loader, 500);

        animateNode(lblLogout, 750);

        pulseLoader();
    }

    private void animateNode(Node node,
                             int delay) {

        if (node == null) return;

        node.setOpacity(0);

        node.setTranslateY(25);

        FadeTransition fade =
                new FadeTransition(
                        Duration.millis(900),
                        node
                );

        fade.setFromValue(0);

        fade.setToValue(1);

        fade.setDelay(
                Duration.millis(delay)
        );

        TranslateTransition slide =
                new TranslateTransition(
                        Duration.millis(900),
                        node
                );

        slide.setFromY(25);

        slide.setToY(0);

        slide.setDelay(
                Duration.millis(delay)
        );

        fade.play();

        slide.play();
    }

    private void pulseLoader() {

        ScaleTransition pulse =
                new ScaleTransition(
                        Duration.seconds(1.5),
                        loader
                );

        pulse.setFromX(1.0);

        pulse.setFromY(1.0);

        pulse.setToX(1.08);

        pulse.setToY(1.08);

        pulse.setCycleCount(
                ScaleTransition.INDEFINITE
        );

        pulse.setAutoReverse(true);

        pulse.play();
    }
}