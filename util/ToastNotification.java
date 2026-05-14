package com.veloura.util;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ToastNotification {

    public enum ToastType {
        SUCCESS,
        ERROR,
        WARNING,
        INFO
    }

    public static void show(Stage owner, String message, ToastType type) {
        if (owner == null || owner.getScene() == null) return;

        Popup popup = new Popup();

        Label label = new Label(message);
        label.setWrapText(true);
        label.setMaxWidth(340);
        label.setStyle(getStyle(type));

        StackPane root = new StackPane(label);
        root.setAlignment(Pos.CENTER);

        popup.getContent().add(root);

        Scene scene = owner.getScene();
        double x = owner.getX() + scene.getWidth() - 400;
        double y = owner.getY() + 60;

        popup.show(owner, x, y);

        root.setOpacity(0);
        root.setTranslateY(-20);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(250), root);
        slide.setFromY(-20);
        slide.setToY(0);

        PauseTransition wait = new PauseTransition(Duration.seconds(2.5));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(350), root);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        fadeIn.play();
        slide.play();

        wait.setOnFinished(e -> fadeOut.play());
        fadeOut.setOnFinished(e -> popup.hide());
        wait.play();
    }

    private static String getStyle(ToastType type) {
        String base = """
                -fx-padding:14 20 14 20;
                -fx-background-radius:16;
                -fx-font-size:14px;
                -fx-font-weight:bold;
                -fx-effect:dropshadow(gaussian,rgba(212,166,74,0.45),22,0.35,0,0);
                """;

        return switch (type) {
            case SUCCESS -> base + """
                    -fx-background-color:rgba(5,45,28,0.96);
                    -fx-text-fill:#E7FFF2;
                    -fx-border-color:#34D399;
                    -fx-border-radius:16;
                    """;

            case ERROR -> base + """
                    -fx-background-color:rgba(65,12,12,0.96);
                    -fx-text-fill:#FFECEC;
                    -fx-border-color:#FB7185;
                    -fx-border-radius:16;
                    """;

            case WARNING -> base + """
                    -fx-background-color:rgba(70,45,8,0.96);
                    -fx-text-fill:#FFF5D6;
                    -fx-border-color:#FACC15;
                    -fx-border-radius:16;
                    """;

            case INFO -> base + """
                    -fx-background-color:rgba(3,10,18,0.96);
                    -fx-text-fill:#E7C16B;
                    -fx-border-color:#D4A64A;
                    -fx-border-radius:16;
                    """;
        };
    }
}