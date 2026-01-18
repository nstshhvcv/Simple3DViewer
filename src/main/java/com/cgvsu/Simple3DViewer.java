package com.cgvsu;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class Simple3DViewer extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Более надёжный способ получения ресурса FXML
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("fxml/gui.fxml")));

        AnchorPane viewport = loader.load();

        GuiController controller = loader.getController();

        Scene scene = new Scene(viewport);

        // Минимальный размер окна
        stage.setMinWidth(1280);
        stage.setMinHeight(800);

        // Привязка размеров root-элемента к сцене
        viewport.prefWidthProperty().bind(scene.widthProperty());
        viewport.prefHeightProperty().bind(scene.heightProperty());

        // Хорошая практика: отключаем масштабирование по умолчанию
//        scene.getRoot().setStyle("-fx-background-color: #;");

        stage.setTitle("Simple 3D Viewer");
        stage.setScene(scene);
        stage.centerOnScreen();           // ← приятный штрих
        stage.show();

        // Обработчик клавиш
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case TAB -> controller.toggleMode();

                case PAGE_UP -> {
                    if (!controller.getSceneObjects().isEmpty()) {
                        int next = (controller.getSelectedIndex() + 1) % controller.getSceneObjects().size();
                        controller.setSelectedIndex(next);
                    }
                }

                case PAGE_DOWN -> {
                    if (!controller.getSceneObjects().isEmpty()) {
                        int size = controller.getSceneObjects().size();
                        int prev = (controller.getSelectedIndex() - 1 + size) % size;
                        controller.setSelectedIndex(prev);
                    }
                }

                default -> controller.handleKey(event.getCode());
            }
        });

        // Опционально: фокус на сцене после показа окна
        stage.setOnShown(e -> scene.getRoot().requestFocus());
    }

    public static void main(String[] args) {
        launch(args);
    }
}