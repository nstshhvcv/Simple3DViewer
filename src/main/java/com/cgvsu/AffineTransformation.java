package com.cgvsu;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.model.Model;
import java.util.Random;

public class AffineTransformation {

    // Матрица масштабирования
    public static Matrix4f scale(float sx, float sy, float sz) {
        return new Matrix4f(
                sx, 0, 0, 0,
                0, sy, 0, 0,
                0, 0, sz, 0,
                0, 0, 0, 1
        );
    }

    // Матрица поворота вокруг оси X
    public static Matrix4f rotationX(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        return new Matrix4f(
                1, 0, 0, 0,
                0, cos, -sin, 0,
                0, sin, cos, 0,
                0, 0, 0, 1
        );
    }

    // Матрица поворота вокруг оси Y
    public static Matrix4f rotationY(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        return new Matrix4f(
                cos, 0, sin, 0,
                0, 1, 0, 0,
                -sin, 0, cos, 0,
                0, 0, 0, 1
        );
    }

    // Матрица поворота вокруг оси Z
    public static Matrix4f rotationZ(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        return new Matrix4f(
                cos, -sin, 0, 0,
                sin, cos, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        );
    }

    // Матрица переноса
    public static Matrix4f translation(float tx, float ty, float tz) {
        return new Matrix4f(
                1, 0, 0, tx,
                0, 1, 0, ty,
                0, 0, 1, tz,
                0, 0, 0, 1
        );
    }

    // Комбинирование матриц
    public static Matrix4f combine(Matrix4f... matrices) {
        if (matrices.length == 0) {
            return Matrix4f.identity();
        }

        Matrix4f result = matrices[0];
        for (int i = 1; i < matrices.length; i++) {
            result = result.multiply(matrices[i]);
        }
        return result;
    }

    // Создание матрицы модели
    public static Matrix4f createModelMatrix(float tx, float ty, float tz, float rx, float ry, float rz, float sx, float sy, float sz) {
        rx = (float) Math.toRadians(rx);
        ry = (float) Math.toRadians(ry);
        rz = (float) Math.toRadians(rz);

        Matrix4f scale = scale(sx, sy, sz);
        Matrix4f rotationX = rotationX(rx);
        Matrix4f rotationY = rotationY(ry);
        Matrix4f rotationZ = rotationZ(rz);
        Matrix4f translation = translation(tx, ty, tz);

        return combine(translation, rotationZ, rotationY, rotationX, scale);
    }

    // Применение аффинного преобразования к модели
    public static void transformation(Model model, float tx, float ty, float tz, float rx, float ry, float rz, float sx, float sy, float sz) {
        if (model == null) return;

        rx = (float) Math.toRadians(rx);
        ry = (float) Math.toRadians(ry);
        rz = (float) Math.toRadians(rz);

        Matrix4f current = model.getModelMatrix();
        Matrix4f scale = scale(sx, sy, sz);
        Matrix4f rotationX = rotationX(rx);
        Matrix4f rotationY = rotationY(ry);
        Matrix4f rotationZ = rotationZ(rz);
        Matrix4f translation = translation(tx, ty, tz);

        Matrix4f transform = combine(translation, rotationZ, rotationY, rotationX, scale);
        model.setModelMatrix(transform.multiply(current));
    }

    // Случайное аффинное преобразование
    public static void randomTransformation(Model model) {
        Random random = new Random();

        float tx = (random.nextFloat() - 0.5f) * 0.2f;
        float ty = (random.nextFloat() - 0.5f) * 0.2f;
        float tz = (random.nextFloat() - 0.5f) * 0.2f;

        float rx = (random.nextFloat() - 0.5f) * 10.0f;
        float ry = (random.nextFloat() - 0.5f) * 10.0f;
        float rz = (random.nextFloat() - 0.5f) * 10.0f;

        float sx = 1.0f + (random.nextFloat() - 0.5f) * 0.1f;
        float sy = 1.0f + (random.nextFloat() - 0.5f) * 0.1f;
        float sz = 1.0f + (random.nextFloat() - 0.5f) * 0.1f;

        transformation(model, tx, ty, tz, rx, ry, rz, sx, sy, sz);
    }
}
