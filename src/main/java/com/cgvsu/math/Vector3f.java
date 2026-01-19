package com.cgvsu.math;

public class Vector3f {
    public float x, y, z;

    public Vector3f() {
        this.x = 0.0f;
        this.y = 0.0f;
        this.z = 0.0f;
    }

    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3f(Vector3f v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    public final void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public final void set(Vector3f v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    public final void add(Vector3f v) {
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
    }

    public final void sub(Vector3f v1, Vector3f v2) {
        this.x = v1.x - v2.x;
        this.y = v1.y - v2.y;
        this.z = v1.z - v2.z;
    }

    public final void cross(Vector3f v1, Vector3f v2) {
        float x = v1.y * v2.z - v1.z * v2.y;
        float y = v1.z * v2.x - v1.x * v2.z;
        float z = v1.x * v2.y - v1.y * v2.x;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // ДОБАВЬ этот метод (возвращает новый вектор)
    public Vector3f cross(Vector3f other) {
        return new Vector3f(
                this.y * other.z - this.z * other.y,
                this.z * other.x - this.x * other.z,
                this.x * other.y - this.y * other.x
        );
    }

    public final float dot(Vector3f v) {
        return x * v.x + y * v.y + z * v.z;
    }

    public final void normalize() {
        float len = (float) Math.sqrt(x*x + y*y + z*z);
        if (len != 0.0f) {
            this.x /= len;
            this.y /= len;
            this.z /= len;
        }
    }

    // Геттеры
    public float getX() { return x; }
    public float getY() { return y; }
    public float getZ() { return z; }

    // Для совместимости
    public Vector3f addNew(Vector3f other) {
        return new Vector3f(x + other.x, y + other.y, z + other.z);
    }

    public Vector3f subtract(Vector3f other) {
        return new Vector3f(x - other.x, y - other.y, z - other.z);
    }

    public Vector3f normalized() {
        float len = (float) Math.sqrt(x*x + y*y + z*z);
        if (len == 0) return new Vector3f(0, 0, 0);
        return new Vector3f(x / len, y / len, z / len);
    }

    public boolean equals(Vector3f other) {
        final float eps = 1e-7f;
        return Math.abs(x - other.x) < eps &&
                Math.abs(y - other.y) < eps &&
                Math.abs(z - other.z) < eps;
    }
}