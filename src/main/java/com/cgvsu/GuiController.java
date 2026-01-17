package com.cgvsu;

import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.objreader.ObjReaderException;
import com.cgvsu.objwriter.ObjWriter;           // ← убедитесь, что импорт есть
import com.cgvsu.objwriter.ObjWriterException;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.RenderEngine;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.vecmath.Vector3f;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GuiController {

    final private float TRANSLATION = 0.5F;

    @FXML private AnchorPane anchorPane;
    @FXML private Canvas canvas;

    private Model mesh = null;

    private Camera camera = new Camera(
            new Vector3f(0, 0, 100),
            new Vector3f(0, 0, 0),
            1.0F, 1, 0.01F, 100);

    private Timeline timeline;

    @FXML
    private void initialize() {
        anchorPane.prefWidthProperty().addListener((ov, old, nv) -> canvas.setWidth(nv.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, old, nv) -> canvas.setHeight(nv.doubleValue()));

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(16), event -> {
            double w = canvas.getWidth();
            double h = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, w, h);
            camera.setAspectRatio((float) (w / h));

            if (mesh != null) {
                RenderEngine.render(canvas.getGraphicsContext2D(), camera, mesh, (int) w, (int) h);
            }
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();
    }

    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("OBJ models (*.obj)", "*.obj"));
        fc.setTitle("Open OBJ Model");

        File file = fc.showOpenDialog(getStage());
        if (file == null) return;

        try {
            String content = Files.readString(file.toPath());
            mesh = ObjReader.read(content);
            // можно вывести имя файла в заголовок окна или label, если хотите
        } catch (IOException | ObjReaderException e) {
            showError("Cannot load model", e.getMessage());
        }
    }

    @FXML
    private void onSaveModelMenuItemClick() {
        if (mesh == null) {
            showError("Nothing to save", "No model is currently loaded.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("OBJ models (*.obj)", "*.obj"));
        fc.setTitle("Save OBJ Model");
        fc.setInitialFileName("model.obj");

        File file = fc.showSaveDialog(getStage());
        if (file == null) return;

        try {
            // Если у вас есть класс ObjWriter с методом write(model, path)
            ObjWriter.write(mesh, file.getAbsolutePath());
            // или если у вас только modelToString:
            // String content = ObjWriter.modelToString(mesh, "Saved from CGVSU viewer");
            // Files.writeString(file.toPath(), content);
        } catch (IOException | ObjWriterException e) {
            showError("Cannot save model", e.getMessage());
        }
    }

    // Вспомогательный метод для показа ошибок (можно улучшить с Alert)
    private void showError(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Stage getStage() {
        return (Stage) canvas.getScene().getWindow();
    }

    // ── Камера ────────────────────────────────────────────────
    @FXML public void handleCameraForward (ActionEvent e) { camera.movePosition(new Vector3f(0, 0, -TRANSLATION)); }
    @FXML public void handleCameraBackward(ActionEvent e) { camera.movePosition(new Vector3f(0, 0,  TRANSLATION)); }
    @FXML public void handleCameraLeft    (ActionEvent e) { camera.movePosition(new Vector3f( TRANSLATION, 0, 0)); }
    @FXML public void handleCameraRight   (ActionEvent e) { camera.movePosition(new Vector3f(-TRANSLATION, 0, 0)); }
    @FXML public void handleCameraUp      (ActionEvent e) { camera.movePosition(new Vector3f(0,  TRANSLATION, 0)); }
    @FXML public void handleCameraDown    (ActionEvent e) { camera.movePosition(new Vector3f(0, -TRANSLATION, 0)); }
}