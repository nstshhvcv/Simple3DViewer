package com.cgvsu.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Polygon {

    private List<Integer> vertexIndices = new ArrayList<>();
    private List<Integer> textureVertexIndices = new ArrayList<>();
    private List<Integer> normalIndices = new ArrayList<>();

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

    // Новый метод для сдвига индексов вершин
    public void decrementVertexIndicesGreaterThan(int index) {
        for (int i = 0; i < vertexIndices.size(); i++) {
            int vi = vertexIndices.get(i);
            if (vi > index) {
                vertexIndices.set(i, vi - 1);
            }
        }
    }

    // Аналогично для текстур и нормалей, если нужно (пока только для вершин)
    public void decrementTextureIndicesGreaterThan(int index) {
        for (int i = 0; i < textureVertexIndices.size(); i++) {
            int ti = textureVertexIndices.get(i);
            if (ti > index) {
                textureVertexIndices.set(i, ti - 1);
            }
        }
    }

    public void decrementNormalIndicesGreaterThan(int index) {
        for (int i = 0; i < normalIndices.size(); i++) {
            int ni = normalIndices.get(i);
            if (ni > index) {
                normalIndices.set(i, ni - 1);
            }
        }
    }

    @Override
    public String toString() {
        return "Polygon{" +
                "vertices=" + vertexIndices +
                ", tex=" + (hasTextureCoordinates() ? textureVertexIndices : "[]") +
                ", normals=" + (hasNormals() ? normalIndices : "[]") +
                '}';
    }
    public Polygon copy(){
        return new Polygon(getVertexIndices(), getTextureVertexIndices(), getNormalIndices());
    }
    private Polygon(List<Integer> vertexIndices, List<Integer> textureVertexIndices, List<Integer> normalIndices){
        this.vertexIndices = vertexIndices;
        this.textureVertexIndices = textureVertexIndices;
        this.normalIndices = normalIndices;
    }
    public Polygon(){
        vertexIndices = new ArrayList<>();
        textureVertexIndices = new ArrayList<>();
        normalIndices = new ArrayList<>();
    }

}