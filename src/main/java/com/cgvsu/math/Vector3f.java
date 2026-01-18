package com.cgvsu.math;

public class Vector3f {
    public float x, y, z;

    // Конструкторы как в javax.vecmath
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

    // Методы javax.vecmath (которые используются в коде)
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

    // Этот add() изменяет текущий вектор (используется в Camera.movePosition())
    public final void add(Vector3f v) {
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
    }

    // Этот sub используется в GraphicConveyor.lookAt()
    public final void sub(Vector3f v1, Vector3f v2) {
        this.x = v1.x - v2.x;
        this.y = v1.y - v2.y;
        this.z = v1.z - v2.z;
    }

    // Этот cross используется в GraphicConveyor.lookAt()
    public final void cross(Vector3f v1, Vector3f v2) {
        float x = v1.y * v2.z - v1.z * v2.y;
        float y = v1.z * v2.x - v1.x * v2.z;
        float z = v1.x * v2.y - v1.y * v2.x;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Этот dot используется в GraphicConveyor.lookAt()
    public final float dot(Vector3f v) {
        return x * v.x + y * v.y + z * v.z;
    }

    // Этот normalize используется в GraphicConveyor.lookAt()
    public final void normalize() {
        float len = (float) Math.sqrt(x*x + y*y + z*z);
        if (len != 0.0f) {
            this.x /= len;
            this.y /= len;
            this.z /= len;
        }
    }
    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    // Можно добавить и сеттеры для полноты
    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setZ(float z) {
        this.z = z;
    }

    // equals для тестов
    public boolean equals(Vector3f other) {
        final float eps = 1e-7f;
        return Math.abs(x - other.x) < eps &&
                Math.abs(y - other.y) < eps &&
                Math.abs(z - other.z) < eps;
    }
}