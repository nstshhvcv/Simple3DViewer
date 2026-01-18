package com.cgvsu.math;

public class Matrix4f {
    public float m00, m01, m02, m03,
            m10, m11, m12, m13,
            m20, m21, m22, m23,
            m30, m31, m32, m33;

    private float[][] matrix;

    public Matrix4f() {
        this.matrix = new float[4][4];
        setIdentity();
    }

    public Matrix4f(float[][] values) {
        if (values.length != 4 || values[0].length != 4) {
            throw new IllegalArgumentException("Matrix must be 4x4");
        }
        this.matrix = new float[4][4];
        for (int i = 0; i < 4; i++) {
            System.arraycopy(values[i], 0, this.matrix[i], 0, 4);
        }
        copyToFields();
    }

    public Matrix4f(float a11, float a12, float a13, float a14,
                    float a21, float a22, float a23, float a24,
                    float a31, float a32, float a33, float a34,
                    float a41, float a42, float a43, float a44) {
        this.matrix = new float[][]{
                {a11, a12, a13, a14},
                {a21, a22, a23, a24},
                {a31, a32, a33, a34},
                {a41, a42, a43, a44}
        };
        copyToFields();
    }

    // Конструктор копирования (ВАЖНО: для new Matrix4f(other))
    public Matrix4f(Matrix4f other) {
        this.matrix = new float[4][4];
        for (int i = 0; i < 4; i++) {
            System.arraycopy(other.matrix[i], 0, this.matrix[i], 0, 4);
        }
        copyToFields();
    }

    private void copyToFields() {
        m00 = matrix[0][0]; m01 = matrix[0][1]; m02 = matrix[0][2]; m03 = matrix[0][3];
        m10 = matrix[1][0]; m11 = matrix[1][1]; m12 = matrix[1][2]; m13 = matrix[1][3];
        m20 = matrix[2][0]; m21 = matrix[2][1]; m22 = matrix[2][2]; m23 = matrix[2][3];
        m30 = matrix[3][0]; m31 = matrix[3][1]; m32 = matrix[3][2]; m33 = matrix[3][3];
    }

    private void copyFromFields() {
        matrix[0][0] = m00; matrix[0][1] = m01; matrix[0][2] = m02; matrix[0][3] = m03;
        matrix[1][0] = m10; matrix[1][1] = m11; matrix[1][2] = m12; matrix[1][3] = m13;
        matrix[2][0] = m20; matrix[2][1] = m21; matrix[2][2] = m22; matrix[2][3] = m23;
        matrix[3][0] = m30; matrix[3][1] = m31; matrix[3][2] = m32; matrix[3][3] = m33;
    }

    // Методы javax.vecmath
    public final void setIdentity() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                matrix[i][j] = (i == j) ? 1.0f : 0.0f;
            }
        }
        copyToFields();
    }

    public final void mul(Matrix4f m1) {
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = 0;
                for (int k = 0; k < 4; k++) {
                    result[i][j] += this.matrix[i][k] * m1.matrix[k][j];
                }
            }
        }
        // Копируем результат
        for (int i = 0; i < 4; i++) {
            System.arraycopy(result[i], 0, this.matrix[i], 0, 4);
        }
        copyToFields();
    }

    public final void set(Matrix4f m1) {
        for (int i = 0; i < 4; i++) {
            System.arraycopy(m1.matrix[i], 0, this.matrix[i], 0, 4);
        }
        copyToFields();
    }

    // Статические методы
    public static Matrix4f identity() {
        return new Matrix4f(
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        );
    }

    // Твои оригинальные методы
    public Matrix4f multiply(Matrix4f other) {
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = 0;
                for (int k = 0; k < 4; k++) {
                    result[i][j] += this.matrix[i][k] * other.matrix[k][j];
                }
            }
        }
        return new Matrix4f(result);
    }

    public boolean equals(Matrix4f other) {
        final float eps = 1e-7f;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (Math.abs(this.matrix[i][j] - other.matrix[i][j]) > eps) {
                    return false;
                }
            }
        }
        return true;
    }

    public Matrix4f transpose() {
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = this.matrix[j][i];
            }
        }
        return new Matrix4f(result);
    }

    // Для совместимости с GraphicConveyor
    public float get(int row, int col) {
        return matrix[row][col];
    }

    public void set(int row, int col, float value) {
        matrix[row][col] = value;
        copyToFields();
    }
}