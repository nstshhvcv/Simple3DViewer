package com.cgvsu;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.model.SceneObject;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.objreader.ObjReaderException;
import com.cgvsu.objwriter.ObjWriter;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.GraphicConveyor;
import com.cgvsu.render_engine.RenderEngine;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class GuiController {

    private static final float TRANSLATION_STEP = 0.5f;
    private static final float ROTATION_ANGLE = (float) Math.toRadians(5);
    private static final float SCALE_FACTOR = 1.1f;

    @FXML private AnchorPane anchorPane;
    @FXML private Canvas canvas;
    @FXML private VBox notificationContainer;
    @FXML private VBox transformPanel;
    @FXML private TextField scaleX, scaleY, scaleZ;
    @FXML private TextField rotateX, rotateY, rotateZ;
    @FXML private TextField translateX, translateY, translateZ;
    @FXML private CheckBox randomTransformationCheck;

    private final List<SceneObject> sceneObjects = new ArrayList<>();
    private int selectedModelIndex = -1;
    private Camera camera;
    private Timeline timeline;
    private boolean isDarkTheme = true;

    // Режимы редактирования
    private enum EditMode { NONE, VERTEX, POLYGON }
    private EditMode editMode = EditMode.NONE;
    private int selectedVertexIndex = -1;
    private int selectedPolygonIndex = -1;

    // Хранит оригинальные модели для возможности отката изменений
    private final Map<SceneObject, Model> originalModels = new HashMap<>();

    // Режим отображения: true - трансформированные модели, false - оригинальные
    private boolean modelMode = false;

    @FXML
    private void initialize() {
        // Инициализация камеры
        camera = new Camera(
                new Vector3f(0, 0, 40),
                new Vector3f(0, 0, 0),
                60.0F,
                1.0F,
                0.1F,
                100.0F
        );

        // Адаптация канваса под размер окна
        anchorPane.widthProperty().addListener((ov, old, nv) -> {
            canvas.setWidth(nv.doubleValue());
            updateCameraAspectRatio();
        });

        anchorPane.heightProperty().addListener((ov, old, nv) -> {
            canvas.setHeight(nv.doubleValue());
            updateCameraAspectRatio();
        });

        // Основной цикл рендера ~60 fps
        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);
        KeyFrame frame = new KeyFrame(Duration.millis(16), e -> {
            double w = canvas.getWidth();
            double h = canvas.getHeight();
            canvas.getGraphicsContext2D().clearRect(0, 0, w, h);
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
        notificationContainer.setVisible(false);
    }

    // Метод для обновления aspect ratio камеры
    private void updateCameraAspectRatio() {
        if (canvas.getHeight() > 0) {
            camera.setAspectRatio((float) (canvas.getWidth() / canvas.getHeight()));
        }
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
            selectedModelIndex = clickedModelIndex;
            selectedVertexIndex = -1;
            selectedPolygonIndex = -1;

            if (editMode != EditMode.NONE) {
                Model model = sceneObjects.get(selectedModelIndex).getModel();
                Matrix4f mvp = getMVPForSelectedModel();

                if (editMode == EditMode.VERTEX) {
                    selectedVertexIndex = findClosestVertex(model, mvp, mx, my, w, h);
                } else if (editMode == EditMode.POLYGON) {
                    selectedPolygonIndex = findClosestPolygon(model, mvp, mx, my, w, h);
                }
            }
            showNotification("Selected model: " + sceneObjects.get(selectedModelIndex).getName(), "info");
        }
    }

    private int findClosestModel(double mx, double my, int w, int h) {
        double globalMinDist = Double.MAX_VALUE;
        int bestIndex = -1;

        for (int i = 0; i < sceneObjects.size(); i++) {
            SceneObject so = sceneObjects.get(i);
            Model model = so.getModel();
            Matrix4f mvp = getMVPForModel(i);

            double modelMinDist = Double.MAX_VALUE;
            for (Vector3f vert : model.getVertices()) {
                Vector2f p = GraphicConveyor.vertexToPoint(
                        GraphicConveyor.multiplyMatrix4ByVector3(mvp, vert), w, h
                );
                double dist = Math.hypot(p.x - mx, p.y - my);
                if (dist < modelMinDist) modelMinDist = dist;
            }
            if (modelMinDist < globalMinDist) {
                globalMinDist = modelMinDist;
                bestIndex = i;
            }
        }
        return (globalMinDist < 120) ? bestIndex : -1;
    }

    private int findClosestVertex(Model model, Matrix4f mvp, double mx, double my, int w, int h) {
        double minDist = 12.0;
        int best = -1;
        for (int i = 0; i < model.getVertices().size(); i++) {
            Vector3f v = model.getVertices().get(i);
            Vector2f p = GraphicConveyor.vertexToPoint(
                    GraphicConveyor.multiplyMatrix4ByVector3(mvp, v), w, h
            );
            double dist = Math.hypot(p.x - mx, p.y - my);
            if (dist < minDist) {
                minDist = dist;
                best = i;
            }
        }
        return best;
    }

    private int findClosestPolygon(Model model, Matrix4f mvp, double mx, double my, int w, int h) {
        double minDist = 40.0;
        int best = -1;
        for (int i = 0; i < model.getPolygons().size(); i++) {
            Polygon poly = model.getPolygons().get(i);
            double cx = 0, cy = 0;
            int count = 0;
            for (int idx : poly.getVertexIndices()) {
                Vector3f v = model.getVertices().get(idx);
                Vector2f p = GraphicConveyor.vertexToPoint(
                        GraphicConveyor.multiplyMatrix4ByVector3(mvp, v), w, h
                );
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
        if (selectedModelIndex < 0) return;

        if (code == KeyCode.DELETE || code == KeyCode.BACK_SPACE) {
            Model model = sceneObjects.get(selectedModelIndex).getModel();
            if (editMode == EditMode.VERTEX && selectedVertexIndex >= 0) {
                model.removeVertex(selectedVertexIndex);
                selectedVertexIndex = -1;
                showNotification("Vertex deleted successfully", "success");
            } else if (editMode == EditMode.POLYGON && selectedPolygonIndex >= 0) {
                model.removePolygon(selectedPolygonIndex);
                selectedPolygonIndex = -1;
                showNotification("Polygon deleted successfully", "success");
            }
        }
    }

    // ---------------------------------------------------------------
    //              Переключение режимов редактирования
    // ---------------------------------------------------------------
    @FXML private void setEditModeNone() {
        editMode = EditMode.NONE;
        selectedVertexIndex = -1;
        selectedPolygonIndex = -1;
        showNotification("Object selection mode activated", "info");
    }

    @FXML private void setEditModeVertex() {
        editMode = EditMode.VERTEX;
        selectedPolygonIndex = -1;
        showNotification("Vertex selection mode activated", "info");
    }

    @FXML private void setEditModePolygon() {
        editMode = EditMode.POLYGON;
        selectedVertexIndex = -1;
        showNotification("Polygon selection mode activated", "info");
    }

    // ---------------------------------------------------------------
    //                    Загрузка / Сохранение
    // ---------------------------------------------------------------
    @FXML private void onOpenModelMenuItemClick() {
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
            showNotification("Model loaded: " + file.getName(), "success");
        } catch (IOException | ObjReaderException e) {
            showError("Ошибка загрузки модели", e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    //                     Камера (из меню Camera Options)
    // ---------------------------------------------------------------
    @FXML public void handleCameraForward(ActionEvent e) {
        camera.movePosition(new Vector3f(0, 0, -TRANSLATION_STEP));
    }
    @FXML public void handleCameraBackward(ActionEvent e) {
        camera.movePosition(new Vector3f(0, 0, TRANSLATION_STEP));
    }
    @FXML public void handleCameraLeft(ActionEvent e) {
        camera.movePosition(new Vector3f(-TRANSLATION_STEP, 0, 0));
    }
    @FXML public void handleCameraRight(ActionEvent e) {
        camera.movePosition(new Vector3f(TRANSLATION_STEP, 0, 0));
    }
    @FXML public void handleCameraUp(ActionEvent e) {
        camera.movePosition(new Vector3f(0, TRANSLATION_STEP, 0));
    }
    @FXML public void handleCameraDown(ActionEvent e) {
        camera.movePosition(new Vector3f(0, -TRANSLATION_STEP, 0));
    }

    @FXML public void resetCamera() {
        camera = new Camera(
                new Vector3f(0, 0, 5),
                new Vector3f(0, 0, 0),
                60.0F,
                (float) (canvas.getWidth() / canvas.getHeight()),
                0.1F,
                100.0F
        );
        showNotification("Camera reset to default position", "info");
    }

    // ---------------------------------------------------------------
    //                     Вспомогательные методы
    // ---------------------------------------------------------------
    private Matrix4f getMVPForModel(int modelIndex) {
        if (modelIndex < 0 || modelIndex >= sceneObjects.size()) return Matrix4f.identity();

        SceneObject so = sceneObjects.get(modelIndex);
        Matrix4f modelMatrix = so.getTransform();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projMatrix = camera.getProjectionMatrix();

        // Порядок умножения матриц: projection * view * model
        return projMatrix.multiply(viewMatrix).multiply(modelMatrix);
    }

    private Matrix4f getMVPForSelectedModel() {
        if (selectedModelIndex < 0) return Matrix4f.identity();
        return getMVPForModel(selectedModelIndex);
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
        showNotification("Selected model: " + sceneObjects.get(selectedModelIndex).getName(), "info");
    }

    @FXML private void toggleTheme() {
        isDarkTheme = !isDarkTheme;
        anchorPane.getStyleClass().remove("anchor-pane-dark");
        anchorPane.getStyleClass().remove("anchor-pane-light");
        if (isDarkTheme) {
            anchorPane.getStyleClass().add("anchor-pane-dark");
            showNotification("Dark theme enabled", "info");
        } else {
            anchorPane.getStyleClass().add("anchor-pane-light");
            showNotification("Light theme enabled", "info");
        }
    }

    // ---------------------------------------------------------------
    //                     Система уведомлений
    // ---------------------------------------------------------------
    private void showNotification(String message, String type) {
        Label notification = new Label(message);
        notification.getStyleClass().addAll("notification", type);
        notification.setMaxWidth(250);
        notification.setWrapText(true);

        notificationContainer.getChildren().add(notification);
        notificationContainer.setVisible(true);

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> {
            notificationContainer.getChildren().remove(notification);
            if (notificationContainer.getChildren().isEmpty()) {
                notificationContainer.setVisible(false);
            }
        });
        delay.play();
    }

    // ---------------------------------------------------------------
    //                     Дополнительные методы
    // ---------------------------------------------------------------
    @FXML private void clearAllModels() {
        sceneObjects.clear();
        originalModels.clear();
        selectedModelIndex = -1;
        selectedVertexIndex = -1;
        selectedPolygonIndex = -1;
        showNotification("All models cleared", "info");
    }

    @FXML private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("3D Model Editor");
        alert.setContentText("A simple 3D model editor for viewing and editing OBJ files.\n\n" +
                "Features:\n" +
                "• Load and save OBJ models\n" +
                "• Vertex and polygon selection\n" +
                "• Basic editing operations\n" +
                "• Camera navigation\n" +
                "• Dark/Light theme support");
        alert.showAndWait();
    }

    // ---------------------------------------------------------------
    //                     Трансформация модели
    // ---------------------------------------------------------------

    // Показать панель трансформации
    @FXML
    private void onShowTransformPanel() {
        if (selectedModelIndex < 0) {
            showNotification("No model selected", "error");
            return;
        }

        transformPanel.setVisible(true);
        transformPanel.setManaged(true);

        // Устанавливаем значения по умолчанию
        scaleX.setText("1.0");
        scaleY.setText("1.0");
        scaleZ.setText("1.0");
        rotateX.setText("0.0");
        rotateY.setText("0.0");
        rotateZ.setText("0.0");
        translateX.setText("0.0");
        translateY.setText("0.0");
        translateZ.setText("0.0");

        // Сохраняем оригинальную модель при первом открытии панели
        if (!originalModels.containsKey(sceneObjects.get(selectedModelIndex))) {
            Model originalModel = sceneObjects.get(selectedModelIndex).getModel().clone();
            originalModels.put(sceneObjects.get(selectedModelIndex), originalModel);
        }
    }

    // Применение трансформации
    @FXML
    private void onApplyTransformation() {
        if (selectedModelIndex < 0) {
            showNotification("No model selected", "error");
            return;
        }

        try {
            float sx = Float.parseFloat(scaleX.getText());
            float sy = Float.parseFloat(scaleY.getText());
            float sz = Float.parseFloat(scaleZ.getText());

            float rx = Float.parseFloat(rotateX.getText());
            float ry = Float.parseFloat(rotateY.getText());
            float rz = Float.parseFloat(rotateZ.getText());

            float tx = Float.parseFloat(translateX.getText());
            float ty = Float.parseFloat(translateY.getText());
            float tz = Float.parseFloat(translateZ.getText());

            // Получаем текущую матрицу трансформации объекта
            SceneObject selectedObject = sceneObjects.get(selectedModelIndex);
            Matrix4f currentTransform = selectedObject.getTransform();

            // Создаем матрицы для каждого типа трансформации
            Matrix4f scaleMatrix = AffineTransformation.scale(sx, sy, sz);
            Matrix4f rotateXMatrix = AffineTransformation.rotationX((float) Math.toRadians(rx));
            Matrix4f rotateYMatrix = AffineTransformation.rotationY((float) Math.toRadians(ry));
            Matrix4f rotateZMatrix = AffineTransformation.rotationZ((float) Math.toRadians(rz));
            Matrix4f translateMatrix = AffineTransformation.translation(tx, ty, tz);

            // Комбинируем матрицы в правильном порядке: масштаб -> вращение -> перенос
            Matrix4f rotationMatrix = rotateZMatrix.multiply(rotateYMatrix).multiply(rotateXMatrix);
            Matrix4f transformMatrix = translateMatrix.multiply(rotationMatrix).multiply(scaleMatrix);

            // Применяем трансформацию к объекту
            selectedObject.setTransform(transformMatrix.multiply(currentTransform));

            showNotification("Transformation applied", "success");
        } catch (NumberFormatException e) {
            showNotification("Invalid input format", "error");
        }
    }

    // Откат трансформации
    @FXML
    private void onOriginalModel() {
        if (selectedModelIndex < 0) {
            showNotification("No model selected", "error");
            return;
        }

        if (originalModels.containsKey(sceneObjects.get(selectedModelIndex))) {
            Model originalModel = originalModels.get(sceneObjects.get(selectedModelIndex));
            sceneObjects.get(selectedModelIndex).setModel(originalModel.clone());
            sceneObjects.get(selectedModelIndex).setTransform(Matrix4f.identity());
            showNotification("Transformation undone", "success");
        } else {
            showNotification("No original model saved", "error");
        }
    }

    // Сохранение оригинальной модели
    @FXML
    private void onSaveOriginalModelMenuItemClick() {
        if (selectedModelIndex < 0) {
            showNotification("No model selected", "error");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("OBJ (*.obj)", "*.obj"));
        fc.setInitialFileName("original_model.obj");
        File file = fc.showSaveDialog(getStage());
        if (file == null) return;

        try {
            Model modelToSave = originalModels.getOrDefault(
                    sceneObjects.get(selectedModelIndex),
                    sceneObjects.get(selectedModelIndex).getModel()
            );
            ObjWriter.write(modelToSave, file.getAbsolutePath());
            showNotification("Original model saved: " + file.getName(), "success");
        } catch (Exception e) {
            showError("Ошибка сохранения", e.getMessage());
        }
    }

    // Сохранение трансформированной модели
    @FXML
    private void onSaveTransformedModelMenuItemClick() {
        if (selectedModelIndex < 0) {
            showNotification("No model selected", "error");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("OBJ (*.obj)", "*.obj"));
        fc.setInitialFileName("transformed_model.obj");
        File file = fc.showSaveDialog(getStage());
        if (file == null) return;

        try {
            Model modelToSave = sceneObjects.get(selectedModelIndex).getModel();
            ObjWriter.write(modelToSave, file.getAbsolutePath());
            showNotification("Transformed model saved: " + file.getName(), "success");
        } catch (Exception e) {
            showError("Ошибка сохранения", e.getMessage());
        }
    }

    // Скрыть панель трансформации
    @FXML
    private void onHideTransformPanel() {
        transformPanel.setVisible(false);
        transformPanel.setManaged(false);
    }

    // Удаление выбранной модели
    @FXML
    private void onRemoveModelClick() {
        if (selectedModelIndex < 0) {
            showNotification("No model selected", "error");
            return;
        }

        sceneObjects.remove(selectedModelIndex);
        originalModels.remove(selectedModelIndex);

        if (sceneObjects.isEmpty()) {
            selectedModelIndex = -1;
        } else {
            selectedModelIndex = Math.max(0, selectedModelIndex - 1);
        }

        showNotification("Model removed", "success");
    }

    // ---------------------------------------------------------------
    //                     Переключение режима отображения
    // ---------------------------------------------------------------
    @FXML
    public void toggleMode() {
        modelMode = !modelMode;
        if (modelMode) {
            showNotification("Displaying transformed models", "info");
        } else {
            showNotification("Displaying original models", "info");
        }
    }
}
