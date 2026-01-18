package com.cgvsu.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Polygon {

    public final ArrayList<Integer> vertexIndices = new ArrayList<>();
    public final ArrayList<Integer> textureVertexIndices = new ArrayList<>();
    public final ArrayList<Integer> normalIndices = new ArrayList<>();

    // Добавление одной вершины (самый удобный способ при парсинге)
    public void addVertex(int vertexIndex) {
        vertexIndices.add(vertexIndex);
    }

    public void addTextureVertex(int textureIndex) {
        textureVertexIndices.add(textureIndex);
    }

    public void addNormal(int normalIndex) {
        normalIndices.add(normalIndex);
    }

    // Геттеры — возвращаем неизменяемые представления
    public List<Integer> getVertexIndices() {
        return Collections.unmodifiableList(vertexIndices);
    }

    public List<Integer> getTextureVertexIndices() {
        return Collections.unmodifiableList(textureVertexIndices);
    }

    public List<Integer> getNormalIndices() {
        return Collections.unmodifiableList(normalIndices);
    }

    public int getVertexCount() {
        return vertexIndices.size();
    }

    public boolean hasTextureCoordinates() {
        return !textureVertexIndices.isEmpty();
    }

    public boolean hasNormals() {
        return !normalIndices.isEmpty();
    }

    @Override
    public String toString() {
        return "Polygon{" +
                "vertices=" + vertexIndices +
                ", tex=" + (hasTextureCoordinates() ? textureVertexIndices : "[]") +
                ", normals=" + (hasNormals() ? normalIndices : "[]") +
                '}';
    }
}