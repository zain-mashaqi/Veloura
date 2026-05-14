package com.veloura.controller;

import com.mycompany.velourafx.App;
import com.veloura.security.UserSession;
import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.util.Duration;

public class WelcomeController {

    @FXML private AnchorPane rootPane;
    @FXML private ImageView imgBackground;
    @FXML private VBox contentBox;
    @FXML private Label lblWelcome;
    @FXML private Label lblUser;
    @FXML private Label lblRole;
    @FXML private Label lblPercent;
    @FXML private ProgressBar progressBar;
    @FXML private Line goldLine;

    @FXML
    public void initialize() {
        lblUser.setText("Welcome, " + UserSession.getUsername());
        lblRole.setText("Role: " + UserSession.getRole());

        playAnimations();
    }

    private void playAnimations() {
        rootPane.setOpacity(0);
        contentBox.setOpacity(0);
        contentBox.setTranslateY(35);

        FadeTransition fadeRoot = new FadeTransition(Duration.millis(700), rootPane);
        fadeRoot.setFromValue(0);
        fadeRoot.setToValue(1);
        fadeRoot.play();

        FadeTransition fadeContent = new FadeTransition(Duration.millis(900), contentBox);
        fadeContent.setFromValue(0);
        fadeContent.setToValue(1);

        javafx.animation.TranslateTransition slideContent =
                new javafx.animation.TranslateTransition(Duration.millis(900), contentBox);
        slideContent.setFromY(35);
        slideContent.setToY(0);

        ScaleTransition bgZoom = new ScaleTransition(Duration.seconds(3), imgBackground);
        bgZoom.setFromX(1.04);
        bgZoom.setFromY(1.04);
        bgZoom.setToX(1.0);
        bgZoom.setToY(1.0);

        ScaleTransition titlePulse = new ScaleTransition(Duration.millis(1200), lblWelcome);
        titlePulse.setFromX(1.0);
        titlePulse.setFromY(1.0);
        titlePulse.setToX(1.04);
        titlePulse.setToY(1.04);
        titlePulse.setAutoReverse(true);
        titlePulse.setCycleCount(2);

        Timeline progressAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(progressBar.progressProperty(), 0)),
                new KeyFrame(Duration.seconds(2.4),
                        new KeyValue(progressBar.progressProperty(), 1))
        );

        progressAnimation.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            double percent = Math.min(100, (newTime.toMillis() / 2400.0) * 100);
            lblPercent.setText(String.format("%.0f%%", percent));
        });

        fadeContent.play();
        slideContent.play();
        bgZoom.play();
        titlePulse.play();
        progressAnimation.play();

        PauseTransition wait = new PauseTransition(Duration.seconds(2.8));
        wait.setOnFinished(e -> {
            try {
                App.setRoot("Dashboard", 980, 640);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        wait.play();
    }
}