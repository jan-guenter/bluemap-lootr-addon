/*
 * SPDX-License-Identifier: MIT
 *
 * Static part dimensions and poses are interpreted from Lootr's MIT-licensed
 * exact version-labeled renderer source; no source or captured mesh is bundled.
 */

package io.github.janguenter.bluemap.lootr.model;

import java.util.ArrayList;
import java.util.List;

/** Deterministic neutral-phase mesh for Lootr's opened decorated pot. */
public final class OpenedPotModel {

    private static final float TEXTURE_WIDTH = 64F;
    private static final float TEXTURE_HEIGHT = 32F;
    private static final Pose IDENTITY = new Pose(0F, 0F, 0F, 0F, 0F, 0F);
    private static final Pose OPEN =
            new Pose(-1.75F, -5F, 0F, 0F, -0.1309F, -0.0873F);

    private OpenedPotModel() {
    }

    /** Returns the exact five source cuboids in Lootr's static opened pose. */
    public static List<Quad> forFacing(String facing) {
        float worldRotation = switch (facing) {
            case "north" -> 0F;
            case "east" -> -90F;
            case "south" -> 180F;
            case "west" -> 90F;
            default -> throw new IllegalArgumentException("invalid pot facing");
        };
        List<Quad> output = new ArrayList<>(30);
        addCube(output, 0F, 0F, -4F, -0.5F, -4F,
                8F, 3F, 8F, IDENTITY, OPEN, worldRotation);
        addCube(output, 0F, 5F, -2.8257F, 2.4924F, -3F,
                6F, 1F, 6F, IDENTITY, OPEN, worldRotation);
        addCube(output, 17F, 21F, -0.5F, -0.5F, -4.5F,
                5F, 1F, 6F,
                new Pose(3.5F, -3.5F, 1.5F, 0F, 0F, 0.829F),
                IDENTITY, worldRotation);
        addCube(output, 14F, 19F, -4.5F, -0.5F, -4.5F,
                9F, 1F, 8F,
                new Pose(-1F, -1.5F, 0.5F, 0F, 0.1309F, 0F),
                IDENTITY, worldRotation);
        addCube(output, 4F, 16F, -4.5F, -0.5F, -3.5F,
                10F, 1F, 11F,
                new Pose(-1.5F, -0.5F, -1.5F, 0F, -0.3054F, 0F),
                IDENTITY, worldRotation);
        return List.copyOf(output);
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    private static void addCube(
            List<Quad> output,
            float textureU,
            float textureV,
            float x,
            float y,
            float z,
            float width,
            float height,
            float depth,
            Pose localPose,
            Pose parentPose,
            float worldRotation
    ) {
        Position v0 = new Position(x, y, z);
        Position v1 = new Position(x + width, y, z);
        Position v2 = new Position(x + width, y + height, z);
        Position v3 = new Position(x, y + height, z);
        Position v4 = new Position(x, y, z + depth);
        Position v5 = new Position(x + width, y, z + depth);
        Position v6 = new Position(x + width, y + height, z + depth);
        Position v7 = new Position(x, y + height, z + depth);

        float u0 = textureU;
        float u1 = u0 + depth;
        float u2 = u1 + width;
        float u3 = u2 + width;
        float u4 = u2 + depth;
        float u5 = u4 + width;
        float vv0 = textureV;
        float vv1 = vv0 + depth;
        float vv2 = vv1 + height;

        add(output, Face.DOWN, new Position[]{v5, v4, v0, v1},
                u1, vv0, u2, vv1, localPose, parentPose, worldRotation);
        add(output, Face.UP, new Position[]{v2, v3, v7, v6},
                u2, vv1, u3, vv0, localPose, parentPose, worldRotation);
        add(output, Face.WEST, new Position[]{v0, v4, v7, v3},
                u0, vv1, u1, vv2, localPose, parentPose, worldRotation);
        add(output, Face.NORTH, new Position[]{v1, v0, v3, v2},
                u1, vv1, u2, vv2, localPose, parentPose, worldRotation);
        add(output, Face.EAST, new Position[]{v5, v1, v2, v6},
                u2, vv1, u4, vv2, localPose, parentPose, worldRotation);
        add(output, Face.SOUTH, new Position[]{v4, v5, v6, v7},
                u4, vv1, u5, vv2, localPose, parentPose, worldRotation);
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    private static void add(
            List<Quad> output,
            Face sourceFace,
            Position[] positions,
            float left,
            float top,
            float right,
            float bottom,
            Pose localPose,
            Pose parentPose,
            float worldRotation
    ) {
        Face transformedFace = transformedFace(
                sourceFace, localPose, parentPose, worldRotation
        );
        output.add(new Quad(transformedFace, List.of(
                vertex(positions[0], right, top, localPose, parentPose, worldRotation),
                vertex(positions[1], left, top, localPose, parentPose, worldRotation),
                vertex(positions[2], left, bottom, localPose, parentPose, worldRotation),
                vertex(positions[3], right, bottom, localPose, parentPose, worldRotation)
        )));
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    private static Vertex vertex(
            Position position,
            float textureU,
            float textureV,
            Pose localPose,
            Pose parentPose,
            float worldRotation
    ) {
        Position transformed = world(
                parentPose.apply(localPose.apply(position)), worldRotation, true
        );
        return new Vertex(
                transformed.x(), transformed.y(), transformed.z(),
                textureU / TEXTURE_WIDTH, textureV / TEXTURE_HEIGHT
        );
    }

    private static Face transformedFace(
            Face face,
            Pose localPose,
            Pose parentPose,
            float worldRotation
    ) {
        Position normal = world(
                parentPose.rotate(localPose.rotate(face.normal())),
                worldRotation,
                false
        );
        float ax = Math.abs(normal.x());
        float ay = Math.abs(normal.y());
        float az = Math.abs(normal.z());
        if (ay >= ax && ay >= az) {
            return normal.y() >= 0F ? Face.UP : Face.DOWN;
        }
        if (ax >= az) {
            return normal.x() >= 0F ? Face.EAST : Face.WEST;
        }
        return normal.z() >= 0F ? Face.SOUTH : Face.NORTH;
    }

    private static Position world(
            Position position,
            float rotationDegrees,
            boolean translate
    ) {
        float x = position.x() / 16F;
        float y = -position.y() / 16F;
        float z = -position.z() / 16F;
        float angle = (float) Math.toRadians(rotationDegrees);
        float cosine = (float) Math.cos(angle);
        float sine = (float) Math.sin(angle);
        float rotatedX = x * cosine + z * sine;
        float rotatedZ = -x * sine + z * cosine;
        return new Position(
                rotatedX + (translate ? 0.5F : 0F),
                y,
                rotatedZ + (translate ? 0.5F : 0F)
        );
    }

    public enum Face {
        DOWN(new Position(0F, -1F, 0F)),
        UP(new Position(0F, 1F, 0F)),
        NORTH(new Position(0F, 0F, -1F)),
        SOUTH(new Position(0F, 0F, 1F)),
        WEST(new Position(-1F, 0F, 0F)),
        EAST(new Position(1F, 0F, 0F));

        private final Position normal;

        Face(Position normal) {
            this.normal = normal;
        }

        private Position normal() {
            return normal;
        }
    }

    public record Vertex(float x, float y, float z, float u, float v) {
    }

    public record Quad(Face face, List<Vertex> vertices) {

        public Quad {
            vertices = List.copyOf(vertices);
            if (vertices.size() != 4) {
                throw new IllegalArgumentException("an opened-pot quad has four vertices");
            }
        }
    }

    private record Pose(
            float x,
            float y,
            float z,
            float xRotation,
            float yRotation,
            float zRotation
    ) {

        Position apply(Position position) {
            Position rotated = rotate(position);
            return new Position(rotated.x() + x, rotated.y() + y, rotated.z() + z);
        }

        Position rotate(Position position) {
            Position result = rotateX(position, xRotation);
            result = rotateY(result, yRotation);
            return rotateZ(result, zRotation);
        }

        private static Position rotateX(Position value, float angle) {
            float cosine = (float) Math.cos(angle);
            float sine = (float) Math.sin(angle);
            return new Position(
                    value.x(),
                    value.y() * cosine - value.z() * sine,
                    value.y() * sine + value.z() * cosine
            );
        }

        private static Position rotateY(Position value, float angle) {
            float cosine = (float) Math.cos(angle);
            float sine = (float) Math.sin(angle);
            return new Position(
                    value.x() * cosine + value.z() * sine,
                    value.y(),
                    -value.x() * sine + value.z() * cosine
            );
        }

        private static Position rotateZ(Position value, float angle) {
            float cosine = (float) Math.cos(angle);
            float sine = (float) Math.sin(angle);
            return new Position(
                    value.x() * cosine - value.y() * sine,
                    value.x() * sine + value.y() * cosine,
                    value.z()
            );
        }
    }

    private record Position(float x, float y, float z) {
    }
}
