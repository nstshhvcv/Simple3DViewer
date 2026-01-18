package com.cgvsu.model;

import com.cgvsu.math.Vector3f;
import com.cgvsu.render_engine.GraphicConveyor;

import javax.vecmath.Matrix4f;
import java.util.ArrayList;

import static com.cgvsu.render_engine.GraphicConveyor.rotateScaleTranslate;

public class SceneObject {
    private final Model model; // Иммутабельная ссылка на mesh
    private Matrix4f transform; // Матрица трансформаций (перемещение, вращение, масштаб)
    private String name; // Для удобства, e.g., "Cube1" или имя файла

    public SceneObject(Model model, String name) {
        this.model = model;
        this.name = name;
        this.transform = rotateScaleTranslate(); // Идентичная матрица по умолчанию
    }

    public Model getModel() {
        return model;
    }

    public Matrix4f getTransform() {
        return new Matrix4f(transform); // Копия, чтобы не модифицировать извне
    }

    public void applyTranslation(javax.vecmath.Vector3f delta) {
        Matrix4f translation = new Matrix4f();
        translation.setIdentity();
        translation.setTranslation(delta);
        transform.mul(translation, transform); // Пре-мультипликация (порядок важен!)
    }

    public void applyRotation(float angle, javax.vecmath.Vector3f axis) {
        Matrix4f rotation = new Matrix4f();
        rotation.setIdentity();
        rotation.rotZ(angle); // Пример для Z; добавьте методы в GraphicConveyor для общей оси
        transform.mul(rotation, transform);
    }

    public void applyScale(javax.vecmath.Vector3f scale) {
        Matrix4f scaling = new Matrix4f();
        scaling.setIdentity();
        scaling.setScale(scale.x); // Пример uniform; для non-uniform - setElement
        transform.mul(scaling, transform);
    }

    public String getName() {
        return name;
    }

    // Для сохранения: экспорт с применёнными трансформациями
    public Model getTransformedModel() {
        return copyAndTransformModel();
    }

    private Model copyAndTransformModel() {
        Model transformed = new Model();
        // Копируем вершины с трансформацией
        for (Vector3f v : model.getVertices()) {
            javax.vecmath.Vector3f vec = new javax.vecmath.Vector3f(v.getX(), v.getY(), v.getZ());
            javax.vecmath.Vector3f tv = GraphicConveyor.multiplyMatrix4ByVector3(transform, vec);
            transformed.addVertex(new Vector3f(tv.x, tv.y, tv.z));
        }
        // Копируем текстуры (не трансформируем)
        for (com.cgvsu.math.Vector2f vt : model.getTextureVertices()) {
            transformed.addTextureVertex(new com.cgvsu.math.Vector2f(vt.getX(), vt.getY()));
        }
        // Нормали: трансформируем с inverse transpose (для простоты опустим, если не нужно)
        for (Vector3f n : model.getNormals()) {
            javax.vecmath.Vector3f vec = new javax.vecmath.Vector3f(n.getX(), n.getY(), n.getZ());
            // TODO: Proper normal transform
            transformed.addNormal(new Vector3f(vec.x, vec.y, vec.z));
        }
        // Полигоны копируем как есть (индексы те же)
        for (Polygon p : model.getPolygons()) {
            Polygon newP = new Polygon();
            newP.vertexIndices.addAll(p.vertexIndices);
            newP.textureVertexIndices.addAll(p.textureVertexIndices);
            newP.normalIndices.addAll(p.normalIndices);
            transformed.addPolygon(newP);
        }
        return transformed;
    }
}