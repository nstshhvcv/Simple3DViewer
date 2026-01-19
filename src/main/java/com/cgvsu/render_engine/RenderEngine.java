package com.cgvsu.render_engine;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.model.SceneObject;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import com.cgvsu.math.Matrix4f;
import java.util.ArrayList;
import java.util.List;

import static com.cgvsu.render_engine.GraphicConveyor.*;

public class RenderEngine {

    public static void render(
            final GraphicsContext gc,
            final Camera camera,
            final List<SceneObject> sceneObjects,
            final int width,
            final int height,
            final int selectedModelIndex,
            final int selectedVertexIndex,
            final int selectedPolygonIndex) {

        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        for (int i = 0; i < sceneObjects.size(); i++) {
            SceneObject so = sceneObjects.get(i);
            Model model = so.getModel();
            Matrix4f modelMatrix = so.getTransform();

            Matrix4f mvp = new Matrix4f(modelMatrix);
            mvp.mul(viewMatrix);
            mvp.mul(projectionMatrix);

            // Цвет и толщина линий для модели
            if (i == selectedModelIndex) {
                gc.setStroke(Color.RED);
                gc.setLineWidth(1.5);
            } else {
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(1.0);
            }

            // Отрисовка всех полигонов модели
            for (int p = 0; p < model.getPolygons().size(); p++) {
                Polygon poly = model.getPolygons().get(p);

                boolean isSelectedPoly = (i == selectedModelIndex && p == selectedPolygonIndex);

                // Подсветка выбранного полигона
                if (isSelectedPoly) {
                    gc.setStroke(Color.LIME);
                    gc.setLineWidth(3.5);
                }

                ArrayList<Vector2f> screenPoints = new ArrayList<>();

                for (int vi : poly.getVertexIndices()) {
                    Vector3f vertex = model.getVertices().get(vi);

                    // Важно: здесь используется правильная проекция с инверсией Y
                    Vector2f screen = vertexToPoint(
                            multiplyMatrix4ByVector3(mvp, vertex),
                            width, height);

                    screenPoints.add(screen);
                }

                // Рисуем замкнутый полигон
                for (int j = 0; j < screenPoints.size(); j++) {
                    Vector2f p1 = screenPoints.get(j);
                    Vector2f p2 = screenPoints.get((j + 1) % screenPoints.size());

                    gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
                }

                // Восстанавливаем стиль после выбранного полигона
                if (isSelectedPoly) {
                    gc.setStroke(i == selectedModelIndex ? Color.RED : Color.BLACK);
                    gc.setLineWidth(i == selectedModelIndex ? 1.5 : 1.0);
                }
            }

            // Подсветка выбранной вершины (синий круг + белая обводка)
            if (i == selectedModelIndex &&
                    selectedVertexIndex >= 0 &&
                    selectedVertexIndex < model.getVertices().size()) {

                Vector3f vertex = model.getVertices().get(selectedVertexIndex);

                Vector2f screen = vertexToPoint(
                        multiplyMatrix4ByVector3(mvp, vertex),
                        width, height);

                // Синий круг
                gc.setFill(Color.BLUE);
                gc.fillOval(screen.x - 5, screen.y - 5, 10, 10);

                // Белая обводка для лучшей видимости
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(1.8);
                gc.strokeOval(screen.x - 5, screen.y - 5, 10, 10);
            }
        }
    }
}