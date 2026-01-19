package com.cgvsu.model;

import com.cgvsu.AffineTransformation;
import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;

public class SceneObject {
    private Model model;
    private final String name;
    private Matrix4f transformMatrix;

    public SceneObject(Model model, String name) {
        this.model = model;
        this.name = name;
        this.transformMatrix = Matrix4f.identity();
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public String getName() {
        return name;
    }

    public Matrix4f getTransform() {
        return transformMatrix;
    }

    public void setTransform(Matrix4f transformMatrix) {
        this.transformMatrix = transformMatrix;
    }

    // Применение масштабирования
    public void scale(float sx, float sy, float sz) {
        Matrix4f scaleMatrix = AffineTransformation.scale(sx, sy, sz);
        transformMatrix = transformMatrix.multiply(scaleMatrix);
    }

    // Применение вращения
    public void rotate(float rx, float ry, float rz) {
        Matrix4f rotX = AffineTransformation.rotationX((float) Math.toRadians(rx));
        Matrix4f rotY = AffineTransformation.rotationY((float) Math.toRadians(ry));
        Matrix4f rotZ = AffineTransformation.rotationZ((float) Math.toRadians(rz));
        Matrix4f rotationMatrix = rotZ.multiply(rotY).multiply(rotX);
        transformMatrix = transformMatrix.multiply(rotationMatrix);
    }

    // Применение переноса
    public void translate(float tx, float ty, float tz) {
        Matrix4f translationMatrix = AffineTransformation.translation(tx, ty, tz);
        transformMatrix = transformMatrix.multiply(translationMatrix);
    }

    // Применение произвольного аффинного преобразования
    public void applyTransformation(Matrix4f transformation) {
        transformMatrix = transformMatrix.multiply(transformation);
    }
}
