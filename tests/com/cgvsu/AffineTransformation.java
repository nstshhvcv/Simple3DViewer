package com.cgvsu;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AffineTransformationTest {

    @Test
    public void testScale() {
        Matrix4f scaleMatrix = AffineTransformation.scale(2, 3, 4);
        Vector3f testVector = new Vector3f(1, 1, 1);
        Vector3f result = scaleMatrix.transform(testVector);

        assertEquals(2, result.x, 1e-5);
        assertEquals(3, result.y, 1e-5);
        assertEquals(4, result.z, 1e-5);
    }

    @Test
    public void testRotationX() {
        Matrix4f rotationMatrix = AffineTransformation.rotationX((float) Math.PI / 2);
        Vector3f testVector = new Vector3f(0, 1, 0);
        Vector3f result = rotationMatrix.transform(testVector);

        assertEquals(0, result.x, 1e-5);
        assertEquals(0, result.y, 1e-5);
        assertEquals(1, result.z, 1e-5);
    }

    @Test
    public void testRotationY() {
        Matrix4f rotationMatrix = AffineTransformation.rotationY((float) Math.PI / 2);
        Vector3f testVector = new Vector3f(1, 0, 0);
        Vector3f result = rotationMatrix.transform(testVector);

        assertEquals(0, result.x, 1e-5);
        assertEquals(0, result.y, 1e-5);
        assertEquals(-1, result.z, 1e-5);
    }

    @Test
    public void testRotationZ() {
        Matrix4f rotationMatrix = AffineTransformation.rotationZ((float) Math.PI / 2);
        Vector3f testVector = new Vector3f(1, 0, 0);
        Vector3f result = rotationMatrix.transform(testVector);

        assertEquals(0, result.x, 1e-5);
        assertEquals(1, result.y, 1e-5);
        assertEquals(0, result.z, 1e-5);
    }

    @Test
    public void testTranslation() {
        Matrix4f translationMatrix = AffineTransformation.translation(1, 2, 3);
        Vector3f testVector = new Vector3f(0, 0, 0);
        Vector3f result = translationMatrix.transform(testVector);

        assertEquals(1, result.x, 1e-5);
        assertEquals(2, result.y, 1e-5);
        assertEquals(3, result.z, 1e-5);
    }

    @Test
    public void testCombine() {
        Matrix4f scale = AffineTransformation.scale(2, 2, 2);
        Matrix4f translation = AffineTransformation.translation(1, 1, 1);
        Matrix4f combined = AffineTransformation.combine(translation, scale);

        Vector3f testVector = new Vector3f(1, 1, 1);
        Vector3f result = combined.transform(testVector);

        assertEquals(3, result.x, 1e-5);
        assertEquals(3, result.y, 1e-5);
        assertEquals(3, result.z, 1e-5);
    }

    @Test
    public void testTransformation() {
        Model model = new Model();
        model.addVertex(new Vector3f(1, 0, 0));
        model.addVertex(new Vector3f(0, 1, 0));
        model.addVertex(new Vector3f(0, 0, 1));
        model.addPolygon(new Polygon(new int[]{0, 1, 2}));

        AffineTransformation.transformation(model, 1, 1, 1, 0, 0, 0, 1, 1, 1);

        Vector3f vertex = model.getVertices().get(0);
        assertEquals(2, vertex.x, 1e-5);
        assertEquals(1, vertex.y, 1e-5);
        assertEquals(1, vertex.z, 1e-5);
    }
}
