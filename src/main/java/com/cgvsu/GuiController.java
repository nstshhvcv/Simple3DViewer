package com.cgvsu;

import com.cgvsu.model.Model;
import com.cgvsu.model.SceneObject;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.objreader.ObjReaderException;
import com.cgvsu.objwriter.ObjWriter;
import com.cgvsu.objwriter.ObjWriterException;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.RenderEngine;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.vecmath.Vector3f;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class GuiController {

    final private float TRANSLATION = 0.5F;
    final private float ROTATION_ANGLE = (float) Math.toRadians(5); // 5 градусов
    final private float SCALE_FACTOR = 1.1F;

    @FXML private AnchorPane anchorPane;
    @FXML private Canvas canvas;

    private List<SceneObject> sceneObjects = new ArrayList<>();
    private int selectedIndex = -1;

    private Camera camera = new Camera(
            new Vector3f(0, 0, 100),
            new Vector3f(0, 0, 0),
            1.0F, 1, 0.01F, 100);

    private Timeline timeline;

    private boolean modelMode = false; // false: camera mode, true: model mode

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

            RenderEngine.render(canvas.getGraphicsContext2D(), camera, sceneObjects, (int) w, (int) h, selectedIndex);
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();
    }

    void handleKey(KeyCode code) {
        Vector3f delta = new Vector3f(0, 0, 0);
        float angle = 0;
        Vector3f axis = new Vector3f(0, 0, 0);
        Vector3f scale = new Vector3f(1, 1, 1);

        switch (code) {
            case W: // Up/Forward
                if (modelMode) delta.y = TRANSLATION; else delta.z = -TRANSLATION;
                break;
            case S: // Down/Backward
                if (modelMode) delta.y = -TRANSLATION; else delta.z = TRANSLATION;
                break;
            case A: // Left
                delta.x = -TRANSLATION;
                break;
            case D: // Right
                delta.x = TRANSLATION;
                break;
            case Q: // Rotate left (Y axis)
                angle = ROTATION_ANGLE;
                axis = new Vector3f(0, 1, 0);
                break;
            case E: // Rotate right (Y axis)
                angle = -ROTATION_ANGLE;
                axis = new Vector3f(0, 1, 0);
                break;
            case PLUS: // Scale up
                scale = new Vector3f(SCALE_FACTOR, SCALE_FACTOR, SCALE_FACTOR);
                break;
            case MINUS: // Scale down
                scale = new Vector3f(1/SCALE_FACTOR, 1/SCALE_FACTOR, 1/SCALE_FACTOR);
                break;
            default:
                return;
        }

        if (modelMode && selectedIndex >= 0) {
            SceneObject so = sceneObjects.get(selectedIndex);
            if (angle != 0) {
                so.applyRotation(angle, axis);
            } else if (!scale.equals(new Vector3f(1,1,1))) {
                so.applyScale(scale);
            } else {
                so.applyTranslation(delta);
            }
        } else {
            camera.movePosition(delta);
        }
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
            Model model = ObjReader.read(content);
            String name = file.getName();
            SceneObject obj = new SceneObject(model, name);
            sceneObjects.add(obj);
            selectedIndex = sceneObjects.size() - 1;
        } catch (IOException | ObjReaderException e) {
            showError("Cannot load model", e.getMessage());
        }
    }

    @FXML
    private void onSaveModelMenuItemClick() {
        if (selectedIndex < 0) {
            showError("No selection", "Select a model first.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("OBJ models (*.obj)", "*.obj"));
        fc.setTitle("Save OBJ Model");
        fc.setInitialFileName("model.obj");

        File file = fc.showSaveDialog(getStage());
        if (file == null) return;

        try {
            SceneObject selected = sceneObjects.get(selectedIndex);
            Model toSave = selected.getTransformedModel();
            ObjWriter.write(toSave, file.getAbsolutePath());
        } catch (IOException | ObjWriterException e) {
            showError("Cannot save model", e.getMessage());
        }
    }

    // Вспомогательный метод для показа ошибок
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

    // Камера handlers (для меню, если нужно)
    @FXML public void handleCameraForward (ActionEvent e) { camera.movePosition(new Vector3f(0, 0, -TRANSLATION)); }
    @FXML public void handleCameraBackward(ActionEvent e) { camera.movePosition(new Vector3f(0, 0,  TRANSLATION)); }
    @FXML public void handleCameraLeft    (ActionEvent e) { camera.movePosition(new Vector3f( TRANSLATION, 0, 0)); }
    @FXML public void handleCameraRight   (ActionEvent e) { camera.movePosition(new Vector3f(-TRANSLATION, 0, 0)); }
    @FXML public void handleCameraUp      (ActionEvent e) { camera.movePosition(new Vector3f(0,  TRANSLATION, 0)); }
    @FXML public void handleCameraDown    (ActionEvent e) { camera.movePosition(new Vector3f(0, -TRANSLATION, 0)); }

    // Публичные методы для доступа из Simple3DViewer
    public void toggleMode() {
        modelMode = !modelMode;
    }

    public List<SceneObject> getSceneObjects() {
        return sceneObjects;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int index) {
        selectedIndex = index;
    }
}