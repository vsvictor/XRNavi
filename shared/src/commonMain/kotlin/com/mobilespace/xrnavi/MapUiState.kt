package com.mobilespace.xrnavi

internal enum class SheetLevel(val fraction: Float) {
    Collapsed(0.1815f),
    Partial(0.3777f),
    Expanded(0.4905f)
}

internal data class MapSheetAnchors(
    val collapsed: Float,
    val partial: Float,
    val expanded: Float,
    val attributionBand: Float
)

internal fun MapSheetAnchors.heightFor(level: SheetLevel): Float = when (level) {
    SheetLevel.Collapsed -> collapsed
    SheetLevel.Partial -> partial
    SheetLevel.Expanded -> expanded
}

internal fun calculateMapSheetAnchors(
    mapHeightPx: Float,
    density: Float,
    fontScale: Float
): MapSheetAnchors {
    val safeDensity = density.coerceAtLeast(0.1f)
    val safeFontScale = fontScale.coerceAtLeast(1f)
    val attributionBand = maxOf(24f, 36f * safeFontScale + 8f) * safeDensity
    val availableHeight = (mapHeightPx - attributionBand).coerceAtLeast(1f)
    val actionHeight = maxOf(44f, 14f * safeFontScale + 16f)
    val collapsedBodyHeight = 24f * safeFontScale + 10f * safeFontScale + 32f * safeFontScale
    val collapsedContentHeight =
        44f + collapsedBodyHeight + 7f + actionHeight + 12f
    val collapsed = maxOf(
        mapHeightPx * SheetLevel.Collapsed.fraction,
        collapsedContentHeight * safeDensity
    ).coerceAtMost(availableHeight * 0.68f)
    val partial = maxOf(
        mapHeightPx * SheetLevel.Partial.fraction,
        collapsed + 80f * safeDensity
    ).coerceAtMost(availableHeight * 0.9f)
    val expanded = maxOf(
        mapHeightPx * SheetLevel.Expanded.fraction,
        partial + 60f * safeDensity
    ).coerceAtMost(availableHeight)

    return MapSheetAnchors(
        collapsed = collapsed.coerceAtMost(partial),
        partial = partial.coerceAtMost(expanded),
        expanded = expanded,
        attributionBand = attributionBand
    )
}

internal fun nextSheetLevel(level: SheetLevel): SheetLevel = when (level) {
    SheetLevel.Collapsed -> SheetLevel.Partial
    SheetLevel.Partial -> SheetLevel.Expanded
    SheetLevel.Expanded -> SheetLevel.Collapsed
}

internal fun previousSheetLevel(level: SheetLevel): SheetLevel = when (level) {
    SheetLevel.Collapsed -> SheetLevel.Collapsed
    SheetLevel.Partial -> SheetLevel.Collapsed
    SheetLevel.Expanded -> SheetLevel.Partial
}
