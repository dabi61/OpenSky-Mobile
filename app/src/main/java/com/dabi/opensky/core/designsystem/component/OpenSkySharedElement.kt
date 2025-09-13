package com.dabi.opensky.core.designsystem.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

//Tác dụng của file này là để cung cấp các hàm mở rộng cho việc sử dụng các thành phần chuyển tiếp chia sẻ trong ứng dụng TrueCleaner.

context(SharedTransitionScope)
fun Modifier.openSkySharedElement(
    isLocalInspectionMode: Boolean,
    sharedContentState: SharedTransitionScope.SharedContentState, // đổi tên ở đây
    animatedVisibilityScope: AnimatedVisibilityScope,
    boundsTransform: BoundsTransform = DefaultBoundsTransform,
    placeHolderSize: SharedTransitionScope.PlaceHolderSize =
        SharedTransitionScope.PlaceHolderSize.contentSize,
    renderInOverlayDuringTransition: Boolean = true,
    zIndexInOverlay: Float = 0f,
    clipInOverlayDuringTransition: SharedTransitionScope.OverlayClip = ParentClip,
): Modifier {
    return if (isLocalInspectionMode) {
        this
    } else {
        this.sharedElement(
            state = sharedContentState,
            animatedVisibilityScope = animatedVisibilityScope,
            boundsTransform = boundsTransform,
            placeHolderSize = placeHolderSize,
            renderInOverlayDuringTransition = renderInOverlayDuringTransition,
            zIndexInOverlay = zIndexInOverlay,
            clipInOverlayDuringTransition = clipInOverlayDuringTransition,
        )
    }
}


// Tác dụng của ParentClip là để cắt clip theo bo góc/shape của parent trong overlay. Nếu không có phần tử có thể bị tràn khỏi bo góc
private val ParentClip: SharedTransitionScope.OverlayClip =
    object : SharedTransitionScope.OverlayClip {
        override fun getClipPath(
            state: SharedTransitionScope.SharedContentState,
            bounds: Rect,
            layoutDirection: LayoutDirection,
            density: Density,
        ): Path? {
            return state.parentSharedContentState?.clipPathInOverlay
        }
    }

private val DefaultBoundsTransform =
    BoundsTransform { _, _ -> DefaultSpring }

// Tác dụng của DefaultSpring là để cung cấp độ cứng và ngưỡng hiển thị mặc định cho các chuyển động chia sẻ
private val DefaultSpring = spring(
    stiffness = Spring.StiffnessMediumLow, // Độ cứng của lò xo trung bình thấp
    visibilityThreshold = Rect.VisibilityThreshold, // Ngưỡng hiển thị của hình chữ nhật
)