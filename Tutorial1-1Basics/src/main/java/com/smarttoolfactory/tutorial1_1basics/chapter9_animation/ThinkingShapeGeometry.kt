package com.smarttoolfactory.tutorial1_1basics.chapter9_animation

import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import kotlin.math.cos
import kotlin.math.sin

internal enum class ThinkingShapeFamily {
    Ai,
    Polygon,
    Star
}

internal enum class ThinkingShapeProfile {
    Base,
    Soft,
    Sharp
}

internal data class ThinkingShapeSpec(
    val family: ThinkingShapeFamily,
    val sides: Int,
    val profile: ThinkingShapeProfile = ThinkingShapeProfile.Base
)

internal fun buildThinkingMorphPolygon(
    spec: ThinkingShapeSpec
): RoundedPolygon {
    return when (spec.family) {
        ThinkingShapeFamily.Ai -> alternatingThinkingPolygon(
            sides = 4,
            innerRadiusRatio = 0.34f,
            outerRoundRadius = 0.06f,
            outerSmoothing = 0.86f,
            innerRoundRadius = 0.14f,
            innerSmoothing = 0.76f
        )
        ThinkingShapeFamily.Polygon -> buildThinkingPolygonVariant(
            vertices = spec.sides,
            profile = spec.profile
        )
        ThinkingShapeFamily.Star -> alternatingThinkingPolygon(
            sides = spec.sides,
            innerRadiusRatio = starInnerRadiusForProfile(
                sides = spec.sides,
                profile = spec.profile
            ),
            outerRoundRadius = starOuterRoundRadius(spec.profile),
            outerSmoothing = starOuterSmoothing(spec.profile),
            innerRoundRadius = starInnerRoundRadius(spec.profile),
            innerSmoothing = starInnerSmoothing(spec.profile)
        )
    }
}

internal fun buildThinkingPolygon(
    vertices: Int,
    roundingRadius: Float = thinkingRoundingRadius(vertices),
    smoothing: Float = thinkingSmoothing(vertices)
): RoundedPolygon {
    return RoundedPolygon(
        numVertices = vertices.coerceIn(3, ThinkingShapeMaxVertices),
        radius = 1f,
        centerX = 0f,
        centerY = 0f,
        rounding = CornerRounding(
            radius = roundingRadius.coerceIn(0f, 0.48f),
            smoothing = smoothing.coerceIn(0f, 1f)
        )
    )
}

internal fun buildThinkingPolygonVariant(
    vertices: Int,
    profile: ThinkingShapeProfile = ThinkingShapeProfile.Base
): RoundedPolygon {
    val clampedVertices = vertices.coerceIn(3, ThinkingShapeMaxVertices)
    return if (clampedVertices == 3) {
        buildRoundedThinkingTriangle()
    } else {
        buildThinkingPolygon(
            vertices = clampedVertices,
            roundingRadius = polygonRoundingRadius(
                vertices = clampedVertices,
                profile = profile
            ),
            smoothing = polygonSmoothing(
                vertices = clampedVertices,
                profile = profile
            )
        )
    }
}

private fun buildRoundedThinkingTriangle(): RoundedPolygon {
    return RoundedPolygon(
        vertices = floatArrayOf(
            0f, -1f,
            -0.94f, 0.72f,
            0.94f, 0.72f
        ),
        perVertexRounding = listOf(
            CornerRounding(
                radius = 0.4f,
                smoothing = 0.74f
            ),
            CornerRounding(
                radius = 0.32f,
                smoothing = 0.9f
            ),
            CornerRounding(
                radius = 0.32f,
                smoothing = 0.9f
            )
        ),
        centerX = 0f,
        centerY = 0f
    )
}

private fun alternatingThinkingPolygon(
    sides: Int,
    innerRadiusRatio: Float,
    outerRoundRadius: Float,
    outerSmoothing: Float,
    innerRoundRadius: Float,
    innerSmoothing: Float
): RoundedPolygon {
    val pointCount = sides.coerceIn(ThinkingShapeMinVertices, ThinkingShapeMaxVertices) * 2
    val vertices = polarVertices(
        radii = List(pointCount) { index ->
            if (index % 2 == 0) {
                1f
            } else {
                innerRadiusRatio.coerceIn(0.2f, 1f)
            }
        },
        rotationDegrees = -90f
    )
    val perVertexRounding = List(pointCount) { index ->
        if (index % 2 == 0) {
            CornerRounding(
                radius = outerRoundRadius.coerceIn(0f, 0.48f),
                smoothing = outerSmoothing.coerceIn(0f, 1f)
            )
        } else {
            CornerRounding(
                radius = innerRoundRadius.coerceIn(0f, 0.48f),
                smoothing = innerSmoothing.coerceIn(0f, 1f)
            )
        }
    }
    return RoundedPolygon(
        vertices = vertices,
        perVertexRounding = perVertexRounding,
        centerX = 0f,
        centerY = 0f
    )
}

private fun polarVertices(
    radii: List<Float>,
    rotationDegrees: Float
): FloatArray {
    val result = FloatArray(radii.size * 2)
    val angleStep = 360.0 / radii.size.toDouble()
    radii.forEachIndexed { index, radius ->
        val angleRadians = Math.toRadians(rotationDegrees + (index * angleStep))
        result[index * 2] = (cos(angleRadians) * radius).toFloat()
        result[(index * 2) + 1] = (sin(angleRadians) * radius).toFloat()
    }
    return result
}

private fun thinkingStarInnerRadius(sides: Int): Float {
    return when (sides) {
        4 -> 0.28f
        5 -> 0.3f
        6 -> 0.33f
        7 -> 0.36f
        8 -> 0.4f
        9 -> 0.44f
        else -> 0.48f
    }
}

private fun starInnerRadiusForProfile(
    sides: Int,
    profile: ThinkingShapeProfile
): Float {
    val base = thinkingStarInnerRadius(sides)
    return when (profile) {
        ThinkingShapeProfile.Base -> base
        ThinkingShapeProfile.Soft -> (base + 0.08f).coerceAtMost(0.72f)
        ThinkingShapeProfile.Sharp -> (base - 0.08f).coerceAtLeast(0.2f)
    }
}

private fun starOuterRoundRadius(profile: ThinkingShapeProfile): Float {
    return when (profile) {
        ThinkingShapeProfile.Base -> 0.14f
        ThinkingShapeProfile.Soft -> 0.2f
        ThinkingShapeProfile.Sharp -> 0.08f
    }
}

private fun starOuterSmoothing(profile: ThinkingShapeProfile): Float {
    return when (profile) {
        ThinkingShapeProfile.Base -> 0.1f
        ThinkingShapeProfile.Soft -> 0.32f
        ThinkingShapeProfile.Sharp -> 0f
    }
}

private fun starInnerRoundRadius(profile: ThinkingShapeProfile): Float {
    return when (profile) {
        ThinkingShapeProfile.Base -> 0.08f
        ThinkingShapeProfile.Soft -> 0.12f
        ThinkingShapeProfile.Sharp -> 0.02f
    }
}

private fun starInnerSmoothing(profile: ThinkingShapeProfile): Float {
    return when (profile) {
        ThinkingShapeProfile.Base -> 0f
        ThinkingShapeProfile.Soft -> 0.16f
        ThinkingShapeProfile.Sharp -> 0f
    }
}

private fun thinkingRoundingRadius(vertices: Int): Float {
    return when (vertices) {
        3 -> 0.08f
        4 -> 0.11f
        5 -> 0.14f
        6 -> 0.18f
        7 -> 0.22f
        else -> 0.26f
    }
}

private fun thinkingSmoothing(vertices: Int): Float {
    return when (vertices) {
        3 -> 0.36f
        4 -> 0.48f
        5 -> 0.58f
        6 -> 0.68f
        7 -> 0.78f
        else -> 0.86f
    }
}

internal fun polygonRoundingRadius(
    vertices: Int,
    profile: ThinkingShapeProfile = ThinkingShapeProfile.Base
): Float {
    val base = when (vertices) {
        3 -> 0.34f
        4 -> 0.1f
        5 -> 0.1f
        6 -> 0.09f
        7 -> 0.08f
        else -> 0.07f
    }
    return when (profile) {
        ThinkingShapeProfile.Base -> base
        ThinkingShapeProfile.Soft -> (base + 0.08f).coerceAtMost(0.3f)
        ThinkingShapeProfile.Sharp -> (base - 0.04f).coerceAtLeast(0.02f)
    }
}

internal fun polygonSmoothing(
    vertices: Int,
    profile: ThinkingShapeProfile = ThinkingShapeProfile.Base
): Float {
    val base = when (vertices) {
        3 -> 0.84f
        4 -> 0.18f
        5 -> 0.16f
        6 -> 0.14f
        7 -> 0.12f
        else -> 0.1f
    }
    return when (profile) {
        ThinkingShapeProfile.Base -> base
        ThinkingShapeProfile.Soft -> (base + 0.36f).coerceAtMost(0.78f)
        ThinkingShapeProfile.Sharp -> (base - 0.08f).coerceAtLeast(0f)
    }
}
