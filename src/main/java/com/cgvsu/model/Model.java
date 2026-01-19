package com.cgvsu.model;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Model {
    private final ArrayList<Vector3f> vertices = new ArrayList<>();
    private final ArrayList<Vector2f> textureVertices = new ArrayList<>();
    private final ArrayList<Vector3f> normals = new ArrayList<>();
    private final ArrayList<Polygon> polygons = new ArrayList<>();
    private Matrix4f modelMatrix = Matrix4f.identity();

    public void setModelMatrix(Matrix4f matrix) {
        this.modelMatrix = matrix;
    }

    public void addVertex(Vector3f vertex) {
        vertices.add(vertex);
    }

    public void addTextureVertex(Vector2f tex) {
        textureVertices.add(tex);
    }

    public void addNormal(Vector3f normal) {
        normals.add(normal);
    }

    public void addPolygon(Polygon polygon) {
        polygons.add(polygon);
    }

    public List<Vector3f> getVertices() {
        return Collections.unmodifiableList(vertices);
    }

    public List<Vector2f> getTextureVertices() {
        return Collections.unmodifiableList(textureVertices);
    }

    public List<Vector3f> getNormals() {
        return Collections.unmodifiableList(normals);
    }

    public List<Polygon> getPolygons() {
        return Collections.unmodifiableList(polygons);
    }

    public void removeVertex(int index) {
        if (index < 0 || index >= vertices.size()) {
            throw new IllegalArgumentException("Invalid vertex index: " + index);
        }
        vertices.remove(index);

        polygons.removeIf(p -> p.getVertexIndices().contains(index));

        for (Polygon p : polygons) {
            p.decrementVertexIndicesGreaterThan(index);
        }
    }

    public void removePolygon(int index) {
        if (index < 0 || index >= polygons.size()) {
            throw new IllegalArgumentException("Invalid polygon index: " + index);
        }
        polygons.remove(index);
    }

    public Matrix4f getModelMatrix() {
        return modelMatrix;
    }

    // Метод для клонирования модели
    public Model clone() {
        Model clonedModel = new Model();
        clonedModel.vertices.addAll(this.vertices);
        clonedModel.textureVertices.addAll(this.textureVertices);
        clonedModel.normals.addAll(this.normals);

        // Клонируем полигоны
        for (Polygon polygon : this.polygons) {
            clonedModel.polygons.add(polygon.copy());
        }

        clonedModel.modelMatrix = new Matrix4f(this.modelMatrix);
        return clonedModel;
    }

    @Override
    public String toString() {
        return "Model{" +
                "vertices=" + vertices.size() +
                ", texCoords=" + textureVertices.size() +
                ", normals=" + normals.size() +
                ", polygons=" + polygons.size() +
                '}';
    }
}
