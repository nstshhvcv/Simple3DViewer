package com.cgvsu.objwriter;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public class ObjWriter {

    public static void write(Model model, String filePath) throws IOException {
        String content = modelToString(model);
        Files.writeString(Path.of(filePath), content);
    }

    public static String modelToString(Model model) {
        return modelToString(model, "Exported by CGVSU ObjWriter");
    }

    public static String modelToString(Model model, String comment) {
        if (model == null) {
            throw new ObjWriterException("Model cannot be null");
        }

        StringBuilder sb = new StringBuilder();

        // Комментарий в начале файла
        if (comment != null && !comment.isBlank()) {
            sb.append("# ").append(comment).append("\n\n");
        }

        // Вершины
        List<Vector3f> vertices = model.getVertices();
        for (int i = 0; i < vertices.size(); i++) {
            Vector3f v = vertices.get(i);
            validateVertex(v, i);
            sb.append("v ")
                    .append(formatFloatCompact(v.getX())).append(" ")
                    .append(formatFloatCompact(v.getY())).append(" ")
                    .append(formatFloatCompact(v.getZ()))
                    .append("\n");
        }

        if (!vertices.isEmpty()) {
            sb.append("\n");
        }

        // Текстурные координаты
        List<Vector2f> texCoords = model.getTextureVertices();
        for (int i = 0; i < texCoords.size(); i++) {
            Vector2f vt = texCoords.get(i);
            validateTextureVertex(vt, i);
            sb.append("vt ")
                    .append(formatFloatCompact(vt.getX())).append(" ")
                    .append(formatFloatCompact(vt.getY()))
                    .append("\n");
        }

        if (!texCoords.isEmpty()) {
            sb.append("\n");
        }

        // Нормали
        List<Vector3f> normals = model.getNormals();
        for (int i = 0; i < normals.size(); i++) {
            Vector3f vn = normals.get(i);
            validateNormal(vn, i);
            sb.append("vn ")
                    .append(formatFloatCompact(vn.getX())).append(" ")
                    .append(formatFloatCompact(vn.getY())).append(" ")
                    .append(formatFloatCompact(vn.getZ()))
                    .append("\n");
        }

        if (!normals.isEmpty()) {
            sb.append("\n");
        }

        // Полигоны (грани)
        List<Polygon> polygons = model.getPolygons();
        for (int i = 0; i < polygons.size(); i++) {
            Polygon p = polygons.get(i);
            validatePolygon(p, i, vertices.size(), texCoords.size(), normals.size());

            sb.append("f");

            List<Integer> vIdx = p.getVertexIndices();
            List<Integer> tIdx = p.getTextureVertexIndices();
            List<Integer> nIdx = p.getNormalIndices();

            boolean hasTex = p.hasTextureCoordinates();
            boolean hasNorm = p.hasNormals();

            for (int j = 0; j < vIdx.size(); j++) {
                sb.append(" ").append(vIdx.get(j) + 1);  // .obj — индексы с 1

                if (hasTex || hasNorm) {
                    sb.append("/");

                    if (hasTex) {
                        int ti = tIdx.get(j);
                        sb.append(ti >= 0 ? (ti + 1) : "");
                    }

                    if (hasNorm) {
                        sb.append("/");
                        int ni = nIdx.get(j);
                        sb.append(ni >= 0 ? (ni + 1) : "");
                    }
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    protected static String formatFloatCompact(float value) {
        if (Float.isNaN(value)) {
            throw new ObjWriterException("Cannot format NaN value");
        }
        if (Float.isInfinite(value)) {
            throw new ObjWriterException("Cannot format infinite value");
        }

        String s = String.format(Locale.ROOT, "%.7f", value);  // чуть больше точности
        s = s.replaceAll("0+$", "");                           // убираем лишние нули справа
        if (s.endsWith(".")) {
            s = s.substring(0, s.length() - 1);                // убираем точку, если целое
        }
        if (s.isEmpty() || s.equals("-0")) {
            s = "0";
        }
        return s;
    }

    // Валидация вершин
    protected static void validateVertex(Vector3f v, int index) {
        if (v == null) throw new ObjWriterException("Vertex #" + (index + 1) + " is null");
        checkFinite(v.getX(), v.getY(), v.getZ(), "Vertex", index);
    }

    protected static void validateTextureVertex(Vector2f vt, int index) {
        if (vt == null) throw new ObjWriterException("Texture coord #" + (index + 1) + " is null");
        checkFinite(vt.getX(), vt.getY(), 0, "Texture coord", index);
    }

    protected static void validateNormal(Vector3f vn, int index) {
        if (vn == null) throw new ObjWriterException("Normal #" + (index + 1) + " is null");
        checkFinite(vn.getX(), vn.getY(), vn.getZ(), "Normal", index);
    }

    private static void checkFinite(float x, float y, float z, String type, int index) {
        if (Float.isNaN(x) || Float.isNaN(y) || Float.isNaN(z)) {
            throw new ObjWriterException(type + " #" + (index + 1) + " contains NaN");
        }
        if (Float.isInfinite(x) || Float.isInfinite(y) || Float.isInfinite(z)) {
            throw new ObjWriterException(type + " #" + (index + 1) + " contains infinity");
        }
    }

    protected static void validatePolygon(Polygon p, int polyIndex,
                                          int vertCount, int texCount, int normCount) {
        if (p == null) {
            throw new ObjWriterException("Polygon #" + (polyIndex + 1) + " is null");
        }

        List<Integer> verts = p.getVertexIndices();
        if (verts == null || verts.isEmpty()) {
            throw new ObjWriterException("Polygon #" + (polyIndex + 1) + " has no vertices");
        }
        if (verts.size() < 3) {
            throw new ObjWriterException("Polygon #" + (polyIndex + 1) + " has < 3 vertices");
        }

        // Проверка диапазона индексов вершин
        for (int vi : verts) {
            if (vi < 0 || vi >= vertCount) {
                throw new ObjWriterException(
                        "Polygon #" + (polyIndex + 1) + " has invalid vertex index: " + vi);
            }
        }

        // Текстуры — если есть, то должны быть у всех или ни у кого (по стандарту лучше у всех)
        List<Integer> texs = p.getTextureVertexIndices();
        if (!texs.isEmpty()) {
            if (texs.size() != verts.size()) {
                throw new ObjWriterException(
                        "Polygon #" + (polyIndex + 1) + ": texture indices count mismatch");
            }
            for (int ti : texs) {
                if (ti < -1 || ti >= texCount) {  // -1 допустим (отсутствует)
                    throw new ObjWriterException(
                            "Polygon #" + (polyIndex + 1) + " invalid tex index: " + ti);
                }
            }
        }

        // Нормали — аналогично
        List<Integer> norms = p.getNormalIndices();
        if (!norms.isEmpty()) {
            if (norms.size() != verts.size()) {
                throw new ObjWriterException(
                        "Polygon #" + (polyIndex + 1) + ": normal indices count mismatch");
            }
            for (int ni : norms) {
                if (ni < -1 || ni >= normCount) {
                    throw new ObjWriterException(
                            "Polygon #" + (polyIndex + 1) + " invalid normal index: " + ni);
                }
            }
        }
    }
}