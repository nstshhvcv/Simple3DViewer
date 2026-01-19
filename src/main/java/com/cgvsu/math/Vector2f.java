package com.cgvsu.math;

public class Vector2f {
    public float x, y;

    public Vector2f() {
        this.x = 0;
        this.y = 0;
    }

    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2f(Vector2f v) {
        this.x = v.x;
        this.y = v.y;
    }

    public boolean equals(Vector2f other) {
        final float eps = 1e-7f;
        return Math.abs(x - other.x) < eps &&
                Math.abs(y - other.y) < eps;
    }

    public float getX() { return x; }
    public float getY() { return y; }

    // Остальные методы без изменений
    public Vector2f add(Vector2f other) {
        return new Vector2f(this.x + other.x, this.y + other.y);
    }

    public Vector2f subtract(Vector2f other) {
        return new Vector2f(this.x - other.x, this.y - other.y);
    }

    public Vector2f multiply(float scalar) {
        return new Vector2f(this.x * scalar, this.y * scalar);
    }

    public Vector2f divide(float scalar) {
        if (scalar == 0) throw new IllegalArgumentException("Division by zero");
        return new Vector2f(this.x / scalar, this.y / scalar);
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public Vector2f normalized() {
        float len = length();
        if (len == 0) return new Vector2f(0, 0);
        return new Vector2f(x / len, y / len);
    }

    public float dot(Vector2f other) {
        return this.x * other.x + this.y * other.y;
    }
    public static Vector2f vertexToPoint(final Vector3f vertex, final int width, final int height) {
        // vertex - точка в NDC пространстве [-1, 1]^3
        // где: ( -1, -1) - левый нижний угол
        //      (  1,  1) - правый верхний угол

        // Преобразование NDC → экранные координаты:
        // 1. Приводим из [-1, 1] к [0, 1]
        // 2. Умножаем на размеры экрана
        // 3. Инвертируем Y (JavaFX: Y вниз, NDC: Y вверх)

        float ndcX = vertex.x; // [-1, 1]
        float ndcY = vertex.y; // [-1, 1]

        // Преобразование в [0, 1]
        float normalizedX = (ndcX + 1.0f) * 0.5f;
        float normalizedY = (ndcY + 1.0f) * 0.5f;

        // Преобразование в экранные координаты
        float screenX = normalizedX * width;
        float screenY = height - (normalizedY * height); // Инверсия Y для JavaFX!

        return new Vector2f(screenX, screenY);
    }
}