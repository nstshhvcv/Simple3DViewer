package com.cgvsu.model;

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

    // Безопасные геттеры (чтобы никто случайно не модифицировал коллекции извне)
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

    // Для отладки удобно
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