package com.cgvsu.render_engine;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector4f;

public class GraphicConveyor {
    public static Matrix4f rotateScaleTranslate() {
        float[][] matrix = {
                {1, 0, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        };
        return new Matrix4f(matrix);
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target) {
        return lookAt(eye, target, new Vector3f(0, -1, 0));
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target, Vector3f up) {
        Vector3f zAxis = target.subtract(eye).normalized();
        Vector3f xAxis = up.cross(zAxis).normalized();
        Vector3f yAxis = zAxis.cross(xAxis).normalized(); // Нормализация добавлена

        float[][] matrix = {
                {xAxis.x, xAxis.y, xAxis.z, 0},
                {yAxis.x, yAxis.y, yAxis.z, 0},
                {zAxis.x, zAxis.y, zAxis.z, 0},
                {-xAxis.dot(eye), -yAxis.dot(eye), -zAxis.dot(eye), 1}
        };

        return new Matrix4f(matrix);
    }


    public static Matrix4f perspective(
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {

        float tanHalfFov = (float) Math.tan(Math.toRadians(fov / 2.0f));
        float range = farPlane - nearPlane;

        float[][] matrix = {
                {1.0f / (tanHalfFov * aspectRatio), 0, 0, 0},
                {0, 1.0f / tanHalfFov, 0, 0},
                {0, 0, -(farPlane + nearPlane) / range, -1},
                {0, 0, -2.0f * farPlane * nearPlane / range, 0}
        };

        return new Matrix4f(matrix);
    }


    public static Vector3f multiplyMatrix4ByVector3(Matrix4f matrix, Vector3f vertex) {
        float m00 = matrix.get(0, 0), m01 = matrix.get(0, 1), m02 = matrix.get(0, 2), m03 = matrix.get(0, 3);
        float m10 = matrix.get(1, 0), m11 = matrix.get(1, 1), m12 = matrix.get(1, 2), m13 = matrix.get(1, 3);
        float m20 = matrix.get(2, 0), m21 = matrix.get(2, 1), m22 = matrix.get(2, 2), m23 = matrix.get(2, 3);
        float m30 = matrix.get(3, 0), m31 = matrix.get(3, 1), m32 = matrix.get(3, 2), m33 = matrix.get(3, 3);

        float x = vertex.x * m00 + vertex.y * m10 + vertex.z * m20 + m30;
        float y = vertex.x * m01 + vertex.y * m11 + vertex.z * m21 + m31;
        float z = vertex.x * m02 + vertex.y * m12 + vertex.z * m22 + m32;
        float w = vertex.x * m03 + vertex.y * m13 + vertex.z * m23 + m33;

        if (Math.abs(w) > 1e-7f) {
            return new Vector3f(x / w, y / w, z / w);
        }
        return new Vector3f(x, y, z);
    }

    public static Vector2f vertexToPoint(Vector3f vertex, int width, int height) {
        float x = (vertex.x + 1.0f) * 0.5f * width;
        float y = (1.0f - (vertex.y + 1.0f) * 0.5f) * height;
        return new Vector2f(x, y);
    }

    public static Matrix4f scaleMatrix(float sx, float sy, float sz) {
        float[][] matrix = {
                {sx, 0, 0, 0},
                {0, sy, 0, 0},
                {0, 0, sz, 0},
                {0, 0, 0, 1}
        };
        return new Matrix4f(matrix);
    }

    public static Matrix4f rotationXMatrix(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        float[][] matrix = {
                {1, 0, 0, 0},
                {0, cos, -sin, 0},
                {0, sin, cos, 0},
                {0, 0, 0, 1}
        };
        return new Matrix4f(matrix);
    }

    public static Matrix4f rotationYMatrix(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        float[][] matrix = {
                {cos, 0, sin, 0},
                {0, 1, 0, 0},
                {-sin, 0, cos, 0},
                {0, 0, 0, 1}
        };
        return new Matrix4f(matrix);
    }

    public static Matrix4f rotationZMatrix(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        float[][] matrix = {
                {cos, -sin, 0, 0},
                {sin, cos, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        };
        return new Matrix4f(matrix);
    }

    public static Matrix4f translationMatrix(float tx, float ty, float tz) {
        float[][] matrix = {
                {1, 0, 0, tx},
                {0, 1, 0, ty},
                {0, 0, 1, tz},
                {0, 0, 0, 1}
        };
        return new Matrix4f(matrix);
    }

    public static Matrix4f createModelMatrix(Vector3f translation, Vector3f rotation, Vector3f scale) {
        Matrix4f scaleMat = scaleMatrix(scale.x, scale.y, scale.z);
        Matrix4f rotX = rotationXMatrix(rotation.x);
        Matrix4f rotY = rotationYMatrix(rotation.y);
        Matrix4f rotZ = rotationZMatrix(rotation.z);
        Matrix4f transMat = translationMatrix(translation.x, translation.y, translation.z);

        Matrix4f rotationMat = rotX.multiply(rotY).multiply(rotZ);
        return transMat.multiply(rotationMat).multiply(scaleMat);
    }

    public static Vector3f screenToWorld(Vector2f screenPoint, int width, int height,
                                         Matrix4f viewMatrix, Matrix4f projectionMatrix) {
        float x = (2.0f * screenPoint.x) / width - 1.0f;
        float y = 1.0f - (2.0f * screenPoint.y) / height;

        Vector4f clipCoords = new Vector4f(x, y, -1.0f, 1.0f);

        Matrix4f invProjection = projectionMatrix.inverted();
        Vector4f eyeCoords = invProjection.multiply(clipCoords);
        eyeCoords.z = -1.0f;
        eyeCoords.w = 0.0f;

        Matrix4f invView = viewMatrix.inverted();
        Vector4f worldCoords = invView.multiply(eyeCoords);

        return new Vector3f(worldCoords.x, worldCoords.y, worldCoords.z);
    }
}
