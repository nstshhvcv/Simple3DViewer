package com.cgvsu.model;

import com.cgvsu.math.Vector3f;
import com.cgvsu.render_engine.GraphicConveyor;

import com.cgvsu.math.Matrix4f;
import java.util.ArrayList;

import static com.cgvsu.render_engine.GraphicConveyor.rotateScaleTranslate;

public class SceneObject {
    private final Model model;
    private Matrix4f transform;
    private String name;

    public SceneObject(Model model, String name) {
        this.model = model;
        this.name = name;
        this.transform = rotateScaleTranslate();
    }

    public Model getModel() {
        return model;
    }



//    public void applyTranslation(com.cgvsu.math.Vector3f delta) {
//        Matrix4f translation = new Matrix4f();
//        translation.setIdentity();
//        translation.setTranslation(delta);
//        transform.mul(translation);
//    }

//    public void applyRotation(float angle, com.cgvsu.math.Vector3f axis) {
//        Matrix4f rotation = new Matrix4f();
//        rotation.setIdentity();
//        rotation.rotY(angle); // Пример для Y; можно добавить параметры для разных осей
//        transform.mul(rotation);
//    }

//    public void applyScale(com.cgvsu.math.Vector3f scale) {
//        Matrix4f scaling = new Matrix4f();
//        scaling.setIdentity();
//        scaling.m00 = scale.x;
//        scaling.m11 = scale.y;
//        scaling.m22 = scale.z;
//        transform.mul(scaling);
//    }

    public String getName() {
        return name;
    }

    public Model getTransformedModel() {
        Model transformed = new Model();

        // Трансформируем вершины
        for (com.cgvsu.math.Vector3f v : model.getVertices()) {

            com.cgvsu.math.Vector3f tv = GraphicConveyor.multiplyMatrix4ByVector3(transform, v);
            transformed.addVertex(new com.cgvsu.math.Vector3f(tv.x, tv.y, tv.z));
        }

        // Копируем текстурные координаты без изменений
        for (com.cgvsu.math.Vector2f vt : model.getTextureVertices()) {
            transformed.addTextureVertex(new com.cgvsu.math.Vector2f(vt.getX(), vt.getY()));
        }

        // Копируем нормали (опционально трансформируем inverse-transpose)
        for (com.cgvsu.math.Vector3f n : model.getNormals()) {
            transformed.addNormal(new com.cgvsu.math.Vector3f(n.getX(), n.getY(), n.getZ()));
        }

        // Копируем полигоны, используя публичные геттеры и add-методы
        for (Polygon p : model.getPolygons()) {
            Polygon newP = new Polygon();

            for (int vi : p.getVertexIndices()) {
                newP.addVertex(vi);
            }

            for (int ti : p.getTextureVertexIndices()) {
                newP.addTextureVertex(ti);
            }

            for (int ni : p.getNormalIndices()) {
                newP.addNormal(ni);
            }

            transformed.addPolygon(newP);
        }

        return transformed;
    }
    public void applyTranslation(Vector3f delta) {
        Matrix4f translation = new Matrix4f();
        translation.setIdentity();
        translation.setTranslation(delta);
        transform.mul(translation); // Используем vecmath-style mul()
    }

    public void applyRotation(float angle, Vector3f axis) {
        Matrix4f rotation = new Matrix4f();
        rotation.setIdentity();
        rotation.rotY(angle); // Или rotX/rotZ в зависимости от оси
        transform.mul(rotation);
    }

    public void applyScale(Vector3f scale) {
        Matrix4f scaling = new Matrix4f();
        scaling.setIdentity();
        scaling.setScale(scale);
        transform.mul(scaling);
    }

    public Matrix4f getTransform() {
        return new Matrix4f(transform); // Используем конструктор копирования
    }
}