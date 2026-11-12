package com.mobilespace.xrnavi

internal enum class PrimarySectionSwipeDirection {
    Next,
    Previous,
}

private val primarySections = listOf(
    DemoSection.Map,
    DemoSection.Garage,
    DemoSection.Settings,
    DemoSection.Profile,
)

internal fun primarySectionAfterSwipe(
    current: DemoSection,
    direction: PrimarySectionSwipeDirection,
): DemoSection? {
    val index = primarySections.indexOf(current)
    if (index < 0) return null
    val nextIndex = when (direction) {
        PrimarySectionSwipeDirection.Next -> (index + 1) % primarySections.size
        PrimarySectionSwipeDirection.Previous -> (index - 1 + primarySections.size) % primarySections.size
    }
    return primarySections[nextIndex]
}

internal fun HorizontalSwipe.direction(): PrimarySectionSwipeDirection =
    if (deltaX > 0) PrimarySectionSwipeDirection.Next
    else PrimarySectionSwipeDirection.Previous

internal fun HorizontalSwipe.startedAtEdge(edgeSize: Float): Boolean =
    startX <= edgeSize || startX >= width - edgeSize
