package com.cgvsu;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Simple3DViewer extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("fxml/gui.fxml")));
        AnchorPane viewport = loader.load();
        GuiController controller = loader.getController();

        Scene scene = new Scene(viewport);
        stage.setMinWidth(1600);
        stage.setMinHeight(900);
        viewport.prefWidthProperty().bind(scene.widthProperty());
        viewport.prefHeightProperty().bind(scene.heightProperty());

        stage.setTitle("Simple3DViewer");
        stage.setScene(scene);
        stage.show();

        // Установка обработчика клавиш после прикрепления сцены
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.TAB) {
                controller.toggleMode();
            } else if (event.getCode() == javafx.scene.input.KeyCode.PAGE_UP) {
                if (!controller.getSceneObjects().isEmpty()) {
                    controller.setSelectedIndex((controller.getSelectedIndex() + 1) % controller.getSceneObjects().size());
                }
            } else if (event.getCode() == javafx.scene.input.KeyCode.PAGE_DOWN) {
                if (!controller.getSceneObjects().isEmpty()) {
                    controller.setSelectedIndex((controller.getSelectedIndex() - 1 + controller.getSceneObjects().size()) % controller.getSceneObjects().size());
                }
            } else {
                controller.handleKey(event.getCode());
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }
}