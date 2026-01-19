package com.cgvsu.render_engine;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.model.SceneObject;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

import static com.cgvsu.render_engine.GraphicConveyor.*;

public class RenderEngine {
    public static void render(
            GraphicsContext gc,
            Camera camera,
            List<SceneObject> sceneObjects,
            int width,
            int height,
            int selectedModelIndex,
            int selectedVertexIndex,
            int selectedPolygonIndex) {

        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        for (int i = 0; i < sceneObjects.size(); i++) {
            SceneObject so = sceneObjects.get(i);
            Model model = so.getModel();
            Matrix4f modelMatrix = so.getTransform();

            Matrix4f mvp = modelMatrix.multiply(viewMatrix).multiply(projectionMatrix);

            if (i == selectedModelIndex) {
                gc.setStroke(Color.RED);
                gc.setLineWidth(1.5);
            } else {
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(1.0);
            }

            for (int p = 0; p < model.getPolygons().size(); p++) {
                Polygon poly = model.getPolygons().get(p);
                boolean isSelectedPoly = (i == selectedModelIndex && p == selectedPolygonIndex);

                if (isSelectedPoly) {
                    gc.setStroke(Color.LIME);
                    gc.setLineWidth(3.5);
                }

                ArrayList<Vector2f> screenPoints = new ArrayList<>();

                for (int vi : poly.getVertexIndices()) {
                    Vector3f vertex = model.getVertices().get(vi);
                    Vector2f screen = vertexToPoint(
                            multiplyMatrix4ByVector3(mvp, vertex),
                            width, height);
                    screenPoints.add(screen);
                }

                for (int j = 0; j < screenPoints.size(); j++) {
                    Vector2f p1 = screenPoints.get(j);
                    Vector2f p2 = screenPoints.get((j + 1) % screenPoints.size());
                    gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
                }

                if (isSelectedPoly) {
                    gc.setStroke(i == selectedModelIndex ? Color.RED : Color.BLACK);
                    gc.setLineWidth(i == selectedModelIndex ? 1.5 : 1.0);
                }
            }

            if (i == selectedModelIndex && selectedVertexIndex >= 0 && selectedVertexIndex < model.getVertices().size()) {
                Vector3f vertex = model.getVertices().get(selectedVertexIndex);
                Vector2f screen = vertexToPoint(multiplyMatrix4ByVector3(mvp, vertex), width, height);

                gc.setFill(Color.BLUE);
                gc.fillOval(screen.x - 5, screen.y - 5, 10, 10);
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(1.8);
                gc.strokeOval(screen.x - 5, screen.y - 5, 10, 10);
            }
        }
    }
}
