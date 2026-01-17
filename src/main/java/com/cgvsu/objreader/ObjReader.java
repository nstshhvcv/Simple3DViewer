package com.cgvsu.objreader;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class ObjReader {

    private static final String VERTEX_TOKEN = "v";
    private static final String TEXTURE_TOKEN = "vt";
    private static final String NORMAL_TOKEN = "vn";
    private static final String FACE_TOKEN = "f";

    public static Model read(String fileContent) {
        Model model = new Model();

        int lineNumber = 0;
        Scanner scanner = new Scanner(fileContent);

        while (scanner.hasNextLine()) {
            lineNumber++;
            String line = scanner.nextLine().trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            ArrayList<String> words = new ArrayList<>(Arrays.asList(line.split("\\s+")));
            String token = words.remove(0);

            try {
                switch (token) {
                    case VERTEX_TOKEN:
                        model.addVertex(parseVertex(words, lineNumber));
                        break;
                    case TEXTURE_TOKEN:
                        model.addTextureVertex(parseTextureVertex(words, lineNumber));
                        break;
                    case NORMAL_TOKEN:
                        model.addNormal(parseNormal(words, lineNumber));
                        break;
                    case FACE_TOKEN:
                        model.addPolygon(parseFace(words, lineNumber));
                        break;
                    default:
                        // игнорируем неизвестные строки (o, g, s, usemtl, mtllib и т.д.)
                        break;
                }
            } catch (ObjReaderException e) {
                throw e;  // пробрасываем дальше с номером строки
            }
        }

        return model;
    }

    private static Vector3f parseVertex(ArrayList<String> parts, int line) {
        if (parts.size() < 3) throw new ObjReaderException("Too few vertex components", line);
        try {
            float x = Float.parseFloat(parts.get(0));
            float y = Float.parseFloat(parts.get(1));
            float z = Float.parseFloat(parts.get(2));
            return new Vector3f(x, y, z);
        } catch (NumberFormatException e) {
            throw new ObjReaderException("Invalid float in vertex", line);
        }
    }

    private static Vector2f parseTextureVertex(ArrayList<String> parts, int line) {
        if (parts.size() < 2) throw new ObjReaderException("Too few texture coordinates", line);
        try {
            float u = Float.parseFloat(parts.get(0));
            float v = Float.parseFloat(parts.get(1));
            return new Vector2f(u, v);
        } catch (NumberFormatException e) {
            throw new ObjReaderException("Invalid float in texture coordinate", line);
        }
    }

    private static Vector3f parseNormal(ArrayList<String> parts, int line) {
        if (parts.size() < 3) throw new ObjReaderException("Too few normal components", line);
        try {
            float x = Float.parseFloat(parts.get(0));
            float y = Float.parseFloat(parts.get(1));
            float z = Float.parseFloat(parts.get(2));
            return new Vector3f(x, y, z);
        } catch (NumberFormatException e) {
            throw new ObjReaderException("Invalid float in normal", line);
        }
    }

    private static Polygon parseFace(ArrayList<String> faceParts, int line) {
        if (faceParts.size() < 3) {
            throw new ObjReaderException("Face must have at least 3 vertices", line);
        }

        Polygon polygon = new Polygon();

        for (String vertexRef : faceParts) {
            String[] indices = vertexRef.split("/");

            // vertex index (обязательный)
            int v = parseIndex(indices[0], line);
            polygon.addVertex(v);

            // texture (может отсутствовать)
            int t = -1;
            if (indices.length > 1 && !indices[1].isEmpty()) {
                t = parseIndex(indices[1], line);
            }
            polygon.addTextureVertex(t);

            // normal (может отсутствовать)
            int n = -1;
            if (indices.length > 2 && !indices[2].isEmpty()) {
                n = parseIndex(indices[2], line);
            }
            polygon.addNormal(n);
        }

        return polygon;
    }

    private static int parseIndex(String s, int line) {
        try {
            int idx = Integer.parseInt(s);
            if (idx == 0) {
                throw new ObjReaderException("Zero index is not allowed in .obj", line);
            }
            // .obj использует индексацию с 1 → преобразуем в 0-based
            return idx - 1;
        } catch (NumberFormatException e) {
            throw new ObjReaderException("Invalid index format: " + s, line);
        }
    }
}