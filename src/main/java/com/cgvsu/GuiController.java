package com.cgvsu;

import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.model.SceneObject;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.objreader.ObjReaderException;
import com.cgvsu.objwriter.ObjWriter;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.GraphicConveyor;
import com.cgvsu.render_engine.RenderEngine;
import com.cgvsu.math.Vector2f;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class GuiController {

    private static final float TRANSLATION = 0.5f;
    private static final float ROTATION_ANGLE = (float) Math.toRadians(5);
    private static final float SCALE_FACTOR = 1.1f;

    @FXML private AnchorPane anchorPane;
    @FXML private Canvas canvas;

    private final List<SceneObject> sceneObjects = new ArrayList<>();
    private int selectedModelIndex = -1;

    private Camera camera = new Camera(
            new Vector3f(0, 0, 100),
            new Vector3f(0, 0, 0),
            1.0F, 1, 0.01F, 100);

    private Timeline timeline;

    // Режимы редактирования
    private enum EditMode { NONE, VERTEX, POLYGON }
    private EditMode editMode = EditMode.NONE;

    private int selectedVertexIndex = -1;
    private int selectedPolygonIndex = -1;

    @FXML
    private void initialize() {
        // Адаптация канваса под размер окна
        anchorPane.widthProperty().addListener((ov, old, nv) -> canvas.setWidth(nv.doubleValue()));
        anchorPane.heightProperty().addListener((ov, old, nv) -> canvas.setHeight(nv.doubleValue()));

        // Основной цикл рендера ~60 fps
        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);
        KeyFrame frame = new KeyFrame(Duration.millis(16), e -> {
            double w = canvas.getWidth();
            double h = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, w, h);
            camera.setAspectRatio((float) (w / h));

            RenderEngine.render(
                    canvas.getGraphicsContext2D(),
                    camera,
                    sceneObjects,
                    (int) w, (int) h,
                    selectedModelIndex,
                    selectedVertexIndex,
                    selectedPolygonIndex
            );
        });
        timeline.getKeyFrames().add(frame);
        timeline.play();

        // Выбор вершин/полигонов по клику
        canvas.setOnMouseClicked(this::handleMouseClick);
    }

    // ---------------------------------------------------------------
    //                   Выбор по клику мыши
    // ---------------------------------------------------------------
    private void handleMouseClick(MouseEvent event) {
        double mx = event.getX();
        double my = event.getY();
        int w = (int) canvas.getWidth();
        int h = (int) canvas.getHeight();

        // Шаг 1: Пытаемся выбрать модель по клику
        int clickedModelIndex = findClosestModel(mx, my, w, h);

        if (clickedModelIndex >= 0) {
            // Выбираем новую модель
            selectedModelIndex = clickedModelIndex;
            // Сбрасываем внутреннее выделение
            selectedVertexIndex = -1;
            selectedPolygonIndex = -1;

            // Если режим редактирования включён — пробуем выбрать вершину/полигон внутри модели
            if (editMode != EditMode.NONE) {
                Model model = sceneObjects.get(selectedModelIndex).getModel();
                Matrix4f mvp = getMVPForSelectedModel();

                if (editMode == EditMode.VERTEX) {
                    selectedVertexIndex = findClosestVertex(model, mvp, mx, my, w, h);
                    selectedPolygonIndex = -1;
                } else if (editMode == EditMode.POLYGON) {
                    selectedPolygonIndex = findClosestPolygon(model, mvp, mx, my, w, h);
                    selectedVertexIndex = -1;
                }
            }
        }
        // Опционально: сброс модели, если клик в пустоту
        // else {
        //     selectedModelIndex = -1;
        //     selectedVertexIndex = -1;
        //     selectedPolygonIndex = -1;
        // }
    }
    private int findClosestModel(double mx, double my, int w, int h) {
        double globalMinDist = Double.MAX_VALUE;
        int bestIndex = -1;

        for (int i = 0; i < sceneObjects.size(); i++) {
            SceneObject so = sceneObjects.get(i);
            Model model = so.getModel();
            Matrix4f mvp = getMVPForModel(i);

            double modelMinDist = Double.MAX_VALUE;

            // Ищем минимальное расстояние до любой вершины модели
            for (com.cgvsu.math.Vector3f vert : model.getVertices()) {


                Vector2f p = GraphicConveyor.vertexToPoint(
                        GraphicConveyor.multiplyMatrix4ByVector3(mvp, vert), w, h);

                double dist = Math.hypot(p.x - mx, p.y - my);
                if (dist < modelMinDist) modelMinDist = dist;
            }

            // Бонус для уже выбранной модели


            if (modelMinDist < globalMinDist) {
                globalMinDist = modelMinDist;
                bestIndex = i;
            }
        }

        // Порог теперь может быть маленьким — клик в пределах 80–150 пикселей от любой вершины
        return (globalMinDist < 120) ? bestIndex : -1;
    }
    private int findClosestVertex(Model model, Matrix4f mvp, double mx, double my, int w, int h) {
        double minDist = 12.0;
        int best = -1;

        for (int i = 0; i < model.getVertices().size(); i++) {
            com.cgvsu.math.Vector3f v = model.getVertices().get(i);

            Vector2f p = GraphicConveyor.vertexToPoint(
                    GraphicConveyor.multiplyMatrix4ByVector3(mvp, v), w, h);

            double dist = Math.hypot(p.x - mx, p.y - my);
            if (dist < minDist) {
                minDist = dist;
                best = i;
            }
        }
        return best;
    }
    private Matrix4f getMVPForModel(int modelIndex) {
        if (modelIndex < 0 || modelIndex >= sceneObjects.size()) return new Matrix4f();

        SceneObject so = sceneObjects.get(modelIndex);
        Matrix4f modelMatrix = so.getTransform();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projMatrix = camera.getProjectionMatrix();

        Matrix4f mvp = new Matrix4f(modelMatrix);
        mvp.mul(viewMatrix);
        mvp.mul(projMatrix);
        return mvp;
    }

    private int findClosestPolygon(Model model, Matrix4f mvp, double mx, double my, int w, int h) {
        double minDist = 40.0;
        int best = -1;

        for (int i = 0; i < model.getPolygons().size(); i++) {
            Polygon poly = model.getPolygons().get(i);
            double cx = 0, cy = 0;
            int count = 0;

            for (int idx : poly.getVertexIndices()) {
                com.cgvsu.math.Vector3f v = model.getVertices().get(idx);

                Vector2f p = GraphicConveyor.vertexToPoint(
                        GraphicConveyor.multiplyMatrix4ByVector3(mvp, v), w, h);
                cx += p.x;
                cy += p.y;
                count++;
            }

            if (count == 0) continue;
            cx /= count;
            cy /= count;

            double dist = Math.hypot(cx - mx, cy - my);
            if (dist < minDist) {
                minDist = dist;
                best = i;
            }
        }
        return best;
    }

    // ---------------------------------------------------------------
    //                     Обработка клавиатуры
    // ---------------------------------------------------------------

    public void handleKey(KeyCode code) {

        // Удаление выбранного элемента
        if ((code == KeyCode.DELETE || code == KeyCode.BACK_SPACE) && selectedModelIndex >= 0) {
            Model model = sceneObjects.get(selectedModelIndex).getModel();

            if (editMode == EditMode.VERTEX && selectedVertexIndex >= 0) {
                model.removeVertex(selectedVertexIndex);
                selectedVertexIndex = -1;
            }
            else if (editMode == EditMode.POLYGON && selectedPolygonIndex >= 0) {
                model.removePolygon(selectedPolygonIndex);
                selectedPolygonIndex = -1;
            }
            return;
        }

        // Управление камерой (всегда доступно)

    }

    // ---------------------------------------------------------------
    //              Переключение режимов редактирования
    // ---------------------------------------------------------------

    @FXML private void setEditModeNone() {
        editMode = EditMode.NONE;
        selectedVertexIndex = -1;
        selectedPolygonIndex = -1;
    }

    @FXML private void setEditModeVertex() {
        editMode = EditMode.VERTEX;
        selectedPolygonIndex = -1;
    }

    @FXML private void setEditModePolygon() {
        editMode = EditMode.POLYGON;
        selectedVertexIndex = -1;
    }

    // ---------------------------------------------------------------
    //                    Загрузка / Сохранение (из твоего FXML)
    // ---------------------------------------------------------------

    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("OBJ (*.obj)", "*.obj"));
        File file = fc.showOpenDialog(getStage());
        if (file == null) return;

        try {
            String content = Files.readString(file.toPath());
            Model model = ObjReader.read(content);
            SceneObject obj = new SceneObject(model, file.getName());
            sceneObjects.add(obj);
            selectedModelIndex = sceneObjects.size() - 1;
        } catch (IOException | ObjReaderException e) {
            showError("Ошибка загрузки модели", e.getMessage());
        }
    }

    @FXML
    private void onSaveModelMenuItemClick() {
        if (selectedModelIndex < 0) {
            showError("Нет выбранной модели", "Выберите модель для сохранения");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("OBJ (*.obj)", "*.obj"));
        fc.setInitialFileName("model.obj");
        File file = fc.showSaveDialog(getStage());
        if (file == null) return;

        try {
            SceneObject so = sceneObjects.get(selectedModelIndex);
            Model toSave = so.getModel();  // или getTransformedModel(), если нужно
            ObjWriter.write(toSave, file.getAbsolutePath());
        } catch (Exception e) {
            showError("Ошибка сохранения", e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    //                     Камера (из меню Camera Options)
    // ---------------------------------------------------------------

    @FXML public void handleCameraForward (ActionEvent e) { camera.movePosition(new Vector3f(0, 0, -TRANSLATION)); }
    @FXML public void handleCameraBackward(ActionEvent e) { camera.movePosition(new Vector3f(0, 0,  TRANSLATION)); }
    @FXML public void handleCameraLeft    (ActionEvent e) { camera.movePosition(new Vector3f( TRANSLATION, 0, 0)); }
    @FXML public void handleCameraRight   (ActionEvent e) { camera.movePosition(new Vector3f(-TRANSLATION, 0, 0)); }
    @FXML public void handleCameraUp      (ActionEvent e) { camera.movePosition(new Vector3f(0,  TRANSLATION, 0)); }
    @FXML public void handleCameraDown    (ActionEvent e) { camera.movePosition(new Vector3f(0, -TRANSLATION, 0)); }
    // ---------------------------------------------------------------
    //                     Вспомогательные методы
    // ---------------------------------------------------------------

    private Matrix4f getMVPForSelectedModel() {
        if (selectedModelIndex < 0) return new Matrix4f();

        SceneObject so = sceneObjects.get(selectedModelIndex);
        Matrix4f modelMatrix = so.getTransform();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projMatrix = camera.getProjectionMatrix();

        Matrix4f mvp = new Matrix4f(modelMatrix);
        mvp.mul(viewMatrix);
        mvp.mul(projMatrix);
        return mvp;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Stage getStage() {
        return (Stage) canvas.getScene().getWindow();
    }

    // Методы для работы с переключением режимов и выбором модели (если будут использоваться позже)
    private boolean modelMode = false;

    @FXML
    public void toggleMode() {
        modelMode = !modelMode;
    }

    public List<SceneObject> getSceneObjects() {
        return sceneObjects;
    }

    public int getSelectedIndex() {
        return selectedModelIndex;
    }

    public void setSelectedIndex(int index) {
        int size = sceneObjects.size();
        if (size == 0) {
            selectedModelIndex = -1;
            return;
        }
        selectedModelIndex = (index + size) % size;
        selectedVertexIndex = -1;
        selectedPolygonIndex = -1;
    }

}