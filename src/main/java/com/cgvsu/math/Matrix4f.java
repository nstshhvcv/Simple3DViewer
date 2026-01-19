package com.cgvsu.math;

public class Matrix4f {
    private float[][] matrix;

    public Matrix4f() {
        this.matrix = new float[4][4];
    }

    public Matrix4f(Matrix4f m) {
        this.matrix = new float[4][4];
        for (int i = 0; i < 4; i++) {
            System.arraycopy(m.matrix[i], 0, this.matrix[i], 0, 4);
        }
    }

    public Matrix4f(float[][] values) {
        if (values.length != 4 || values[0].length != 4) {
            throw new IllegalArgumentException("Matrix must be 4x4");
        }
        this.matrix = new float[4][4];
        for (int i = 0; i < 4; i++) {
            System.arraycopy(values[i], 0, this.matrix[i], 0, 4);
        }
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
    }

    public static Matrix4f identity() {
        return new Matrix4f(
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        );
    }

    public static Matrix4f zero() {
        return new Matrix4f(
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0
        );
    }

    public float get(int row, int col) {
        if (row < 0 || row >= 4 || col < 0 || col >= 4) {
            throw new IllegalArgumentException("Index out of bounds");
        }
        return matrix[row][col];
    }

    public void set(int row, int col, float value) {
        if (row < 0 || row >= 4 || col < 0 || col >= 4) {
            throw new IllegalArgumentException("Index out of bounds");
        }
        matrix[row][col] = value;
    }

    public boolean equals(Matrix4f other) {
        if (other == null) return false;
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

    public Matrix4f add(Matrix4f other) {
        if (other == null) throw new IllegalArgumentException("Other matrix must not be null");
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = this.matrix[i][j] + other.matrix[i][j];
            }
        }
        return new Matrix4f(result);
    }

    public Matrix4f subtract(Matrix4f other) {
        if (other == null) throw new IllegalArgumentException("Other matrix must not be null");
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = this.matrix[i][j] - other.matrix[i][j];
            }
        }
        return new Matrix4f(result);
    }

    public Vector4f multiply(Vector4f vector) {
        if (vector == null) throw new IllegalArgumentException("Vector must not be null");
        float x = matrix[0][0] * vector.x + matrix[0][1] * vector.y +
                matrix[0][2] * vector.z + matrix[0][3] * vector.w;
        float y = matrix[1][0] * vector.x + matrix[1][1] * vector.y +
                matrix[1][2] * vector.z + matrix[1][3] * vector.w;
        float z = matrix[2][0] * vector.x + matrix[2][1] * vector.y +
                matrix[2][2] * vector.z + matrix[2][3] * vector.w;
        float w = matrix[3][0] * vector.x + matrix[3][1] * vector.y +
                matrix[3][2] * vector.z + matrix[3][3] * vector.w;
        return new Vector4f(x, y, z, w);
    }

    public Matrix4f multiply(Matrix4f other) {
        if (other == null) throw new IllegalArgumentException("Other matrix must not be null");
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append("[");
            for (int j = 0; j < 4; j++) {
                sb.append(String.format("%.4f", matrix[i][j]));
                if (j < 3) sb.append(", ");
            }
            sb.append("]\n");
        }
        return sb.toString();
    }

    public Matrix4f inverted() {
        float[] m = new float[16];
        int idx = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                m[idx++] = this.matrix[i][j];
            }
        }

        float[][] inv = new float[4][4];

        inv[0][0] = m[5] * m[10] * m[15] - m[5] * m[11] * m[14] - m[9] * m[6] * m[15] + m[9] * m[7] * m[14] + m[13] * m[6] * m[11] - m[13] * m[7] * m[10];
        inv[1][0] = -m[4] * m[10] * m[15] + m[4] * m[11] * m[14] + m[8] * m[6] * m[15] - m[8] * m[7] * m[14] - m[12] * m[6] * m[11] + m[12] * m[7] * m[10];
        inv[2][0] = m[4] * m[9] * m[15] - m[4] * m[11] * m[13] - m[8] * m[5] * m[15] + m[8] * m[7] * m[13] + m[12] * m[5] * m[11] - m[12] * m[7] * m[9];
        inv[3][0] = -m[4] * m[9] * m[14] + m[4] * m[10] * m[13] + m[8] * m[5] * m[14] - m[8] * m[6] * m[13] - m[12] * m[5] * m[10] + m[12] * m[6] * m[9];

        inv[0][1] = -m[1] * m[10] * m[15] + m[1] * m[11] * m[14] + m[9] * m[2] * m[15] - m[9] * m[3] * m[14] - m[13] * m[2] * m[11] + m[13] * m[3] * m[10];
        inv[1][1] = m[0] * m[10] * m[15] - m[0] * m[11] * m[14] - m[8] * m[2] * m[15] + m[8] * m[3] * m[14] + m[12] * m[2] * m[11] - m[12] * m[3] * m[10];
        inv[2][1] = -m[0] * m[9] * m[15] + m[0] * m[11] * m[13] + m[8] * m[1] * m[15] - m[8] * m[3] * m[13] - m[12] * m[1] * m[11] + m[12] * m[3] * m[9];
        inv[3][1] = m[0] * m[9] * m[14] - m[0] * m[10] * m[13] - m[8] * m[1] * m[14] + m[8] * m[2] * m[13] + m[12] * m[1] * m[10] - m[12] * m[2] * m[9];

        inv[0][2] = m[1] * m[6] * m[15] - m[1] * m[7] * m[14] - m[5] * m[2] * m[15] + m[5] * m[3] * m[14] + m[13] * m[2] * m[7] - m[13] * m[3] * m[6];
        inv[1][2] = -m[0] * m[6] * m[15] + m[0] * m[7] * m[14] + m[4] * m[2] * m[15] - m[4] * m[3] * m[14] - m[12] * m[2] * m[7] + m[12] * m[3] * m[6];
        inv[2][2] = m[0] * m[5] * m[15] - m[0] * m[7] * m[13] - m[4] * m[1] * m[15] + m[4] * m[3] * m[13] + m[12] * m[1] * m[7] - m[12] * m[3] * m[5];
        inv[3][2] = -m[0] * m[5] * m[14] + m[0] * m[6] * m[13] + m[4] * m[1] * m[14] - m[4] * m[2] * m[13] - m[12] * m[1] * m[6] + m[12] * m[2] * m[5];

        inv[0][3] = -m[1] * m[6] * m[11] + m[1] * m[7] * m[10] + m[5] * m[2] * m[11] - m[5] * m[3] * m[10] - m[9] * m[2] * m[7] + m[9] * m[3] * m[6];
        inv[1][3] = m[0] * m[6] * m[11] - m[0] * m[7] * m[10] - m[4] * m[2] * m[11] + m[4] * m[3] * m[10] + m[8] * m[2] * m[7] - m[8] * m[3] * m[6];
        inv[2][3] = -m[0] * m[5] * m[11] + m[0] * m[7] * m[9] + m[4] * m[1] * m[11] - m[4] * m[3] * m[9] - m[8] * m[1] * m[7] + m[8] * m[3] * m[5];
        inv[3][3] = m[0] * m[5] * m[10] - m[0] * m[6] * m[9] - m[4] * m[1] * m[10] + m[4] * m[2] * m[9] + m[8] * m[1] * m[6] - m[8] * m[2] * m[5];

        float det = m[0] * inv[0][0] + m[1] * inv[1][0] + m[2] * inv[2][0] + m[3] * inv[3][0];

        if (Math.abs(det) < 1e-7f) {
            throw new ArithmeticException("Matrix is singular and cannot be inverted");
        }

        det = 1.0f / det;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                inv[i][j] *= det;
            }
        }

        return new Matrix4f(inv);
    }

    public final void mul(Matrix4f other) {
        if (other == null) throw new IllegalArgumentException("Other matrix must not be null");
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = 0;
                for (int k = 0; k < 4; k++) {
                    result[i][j] += this.matrix[i][k] * other.matrix[k][j];
                }
            }
        }
        for (int i = 0; i < 4; i++) {
            System.arraycopy(result[i], 0, this.matrix[i], 0, 4);
        }
    }

    public final void mul(Matrix4f m1, Matrix4f m2) {
        if (m1 == null || m2 == null) throw new IllegalArgumentException("Matrices must not be null");
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = 0;
                for (int k = 0; k < 4; k++) {
                    result[i][j] += m1.matrix[i][k] * m2.matrix[k][j];
                }
            }
        }
        for (int i = 0; i < 4; i++) {
            System.arraycopy(result[i], 0, this.matrix[i], 0, 4);
        }
    }

    public final void setIdentity() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                matrix[i][j] = (i == j) ? 1.0f : 0.0f;
            }
        }
    }

    public final void setTranslation(Vector3f translation) {
        if (translation == null) throw new IllegalArgumentException("Translation vector must not be null");
        setIdentity();
        matrix[0][3] = translation.x;
        matrix[1][3] = translation.y;
        matrix[2][3] = translation.z;
    }

    public final void rotX(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        setIdentity();
        matrix[1][1] = cos;
        matrix[1][2] = -sin;
        matrix[2][1] = sin;
        matrix[2][2] = cos;
    }

    public final void rotY(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        setIdentity();
        matrix[0][0] = cos;
        matrix[0][2] = sin;
        matrix[2][0] = -sin;
        matrix[2][2] = cos;
    }

    public final void rotZ(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        setIdentity();
        matrix[0][0] = cos;
        matrix[0][1] = -sin;
        matrix[1][0] = sin;
        matrix[1][1] = cos;
    }

    public final void setScale(Vector3f scale) {
        if (scale == null) throw new IllegalArgumentException("Scale vector must not be null");
        setIdentity();
        matrix[0][0] = scale.x;
        matrix[1][1] = scale.y;
        matrix[2][2] = scale.z;
    }

    public Vector3f transform(Vector3f point) {
        if (point == null) throw new IllegalArgumentException("Point must not be null");
        float x = matrix[0][0] * point.x + matrix[0][1] * point.y + matrix[0][2] * point.z + matrix[0][3];
        float y = matrix[1][0] * point.x + matrix[1][1] * point.y + matrix[1][2] * point.z + matrix[1][3];
        float z = matrix[2][0] * point.x + matrix[2][1] * point.y + matrix[2][2] * point.z + matrix[2][3];
        float w = matrix[3][0] * point.x + matrix[3][1] * point.y + matrix[3][2] * point.z + matrix[3][3];
        if (Math.abs(w) > 1e-7f) {
            return new Vector3f(x / w, y / w, z / w);
        }
        return new Vector3f(x, y, z);
    }

    public final void set(Matrix4f other) {
        if (other == null) throw new IllegalArgumentException("Other matrix must not be null");
        for (int i = 0; i < 4; i++) {
            System.arraycopy(other.matrix[i], 0, this.matrix[i], 0, 4);
        }
    }

    public boolean isIdentity() {
        final float eps = 1e-6f;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                float expected = (i == j) ? 1.0f : 0.0f;
                if (Math.abs(matrix[i][j] - expected) > eps) {
                    return false;
                }
            }
        }
        return true;
    }

    public final void mul(float scalar) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                matrix[i][j] *= scalar;
            }
        }
    }

    public Vector3f getScale() {
        float sx = (float) Math.sqrt(matrix[0][0] * matrix[0][0] + matrix[1][0] * matrix[1][0] + matrix[2][0] * matrix[2][0]);
        float sy = (float) Math.sqrt(matrix[0][1] * matrix[0][1] + matrix[1][1] * matrix[1][1] + matrix[2][1] * matrix[2][1]);
        float sz = (float) Math.sqrt(matrix[0][2] * matrix[0][2] + matrix[1][2] * matrix[1][2] + matrix[2][2] * matrix[2][2]);
        return new Vector3f(sx, sy, sz);
    }

    public final void invert() {
        Matrix4f inverted = this.inverted();
        this.set(inverted);
    }

    public final void transpose() {
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = this.matrix[j][i];
            }
        }
        for (int i = 0; i < 4; i++) {
            System.arraycopy(result[i], 0, this.matrix[i], 0, 4);
        }
    }
}
