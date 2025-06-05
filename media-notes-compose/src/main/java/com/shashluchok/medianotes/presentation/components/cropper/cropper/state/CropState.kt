package com.shashluchok.medianotes.presentation.components.cropper.cropper.state

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.positionChangeIgnoreConsumed
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.core.graphics.rotationMatrix
import androidx.core.graphics.scaleMatrix
import com.shashluchok.medianotes.presentation.components.cropper.cropper.TouchRegion
import com.shashluchok.medianotes.presentation.components.cropper.cropper.cropdefaults.CropProperties
import com.shashluchok.medianotes.presentation.components.cropper.cropper.model.CropData
import com.shashluchok.medianotes.presentation.utils.contains
import com.shashluchok.medianotes.presentation.utils.transform
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
internal fun rememberCropState(
    cropProperties: CropProperties,
    imageBitmap: ImageBitmap,
    contentScale: ContentScale,
    onCropDataChange: (CropData) -> Unit
): CropState {
    return remember(contentScale) {
        CropState(
            cropProperties = cropProperties,
            imageBitmap = imageBitmap,
            contentScale = contentScale,
            onCropDataChange = onCropDataChange
        )
    }
}

internal class CropState internal constructor(
    private val onCropDataChange: (CropData) -> Unit,
    private val contentScale: ContentScale,
    imageBitmap: ImageBitmap,
    cropProperties: CropProperties
) {

    var imageBitmap = imageBitmap
        private set

    var cropProperties = cropProperties
        private set

    // Flag to determine that all image calculations with content scale are completed
    var isInitialized: Boolean by mutableStateOf(false)
        private set

    var touchRegion by mutableStateOf(TouchRegion.None)
        private set

    var isCropping: Boolean by mutableStateOf(false)
        private set

    val pan: Offset
        get() = Offset(animatablePanX.value, animatablePanY.value)

    val zoom: Float
        get() = animatableZoom.value

    val rotation: Float
        get() = animatableRotation.value

    val isAnimationRunning: Boolean
        get() = animatableZoom.isRunning || animatablePanX.isRunning ||
            animatablePanY.isRunning || animatableRotation.isRunning

    val isRotationRunning: Boolean
        get() = animatableRotation.isRunning

    val cropData: CropData
        get() = CropData(
            zoom = animatableZoom.targetValue,
            pan = Offset(animatablePanX.targetValue, animatablePanY.targetValue),
            rotation = animatableRotation.targetValue,
            overlayRect = overlayRect,
            cropRect = cropRect,
            cropShape = cropProperties.cropShape,
            imageBitmap = imageBitmap.transform(
                matrix = rotationMatrix(rotation)
            )
        )

    val overlayRect: Rect
        get() = animatableRectOverlay.value

    private val animatablePanX = Animatable(0f)
    private val animatablePanY = Animatable(0f)
    private val animatableZoom = Animatable(1f)
    private val animatableRotation = Animatable(0f)
    private val animatableRectOverlay = Animatable(
        Rect.Zero,
        Rect.VectorConverter
    )
    private var distanceToEdgeFromTouch = Offset.Zero
    private var gestureInvoked = false

    private var rectTemp = Rect.Zero
    private var drawAreaRect: Rect = Rect.Zero
    private var containerRect: Rect = Rect.Zero

    private val cropRect: Rect
        get() {
            val isVertical = rotation % 180 == 0f
            val width = if (isVertical) imageBitmap.width else imageBitmap.height
            val height = if (isVertical) imageBitmap.height else imageBitmap.width
            return getCropRectangle(
                width,
                height,
                drawAreaRect,
                animatableRectOverlay.targetValue
            )
        }

    suspend fun initWithConstraints(
        constraints: Constraints
    ) {
        isInitialized = false

        val hasBoundedDimens = constraints.hasBoundedWidth && constraints.hasBoundedHeight
        val hasFixedDimens = constraints.hasFixedWidth && constraints.hasFixedHeight

        val containerWidth: Int = if (hasBoundedDimens || hasFixedDimens) {
            constraints.maxWidth
        } else {
            constraints.minWidth.coerceAtLeast(imageBitmap.width)
        }
        val containerHeight: Int = if (hasBoundedDimens || hasFixedDimens) {
            constraints.maxHeight
        } else {
            constraints.minHeight.coerceAtLeast(imageBitmap.height)
        }

        containerRect = Rect(
            offset = Offset.Zero,
            size = Size(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())
        )

        val srcSize = Size(imageBitmap.width.toFloat(), imageBitmap.height.toFloat())

        val dstSize = Size(containerRect.width, containerRect.height)

        val scaleFactor = contentScale.computeScaleFactor(srcSize, dstSize)

        imageBitmap = imageBitmap.transform(
            matrix = scaleMatrix(sx = scaleFactor.scaleX, sy = scaleFactor.scaleY)
        )

        drawAreaRect =
            Rect(
                offset = Offset(
                    x = ((containerRect.width - imageBitmap.width) / 2),
                    y = ((containerRect.height - imageBitmap.height) / 2)
                ),
                size = Size(
                    imageBitmap.width.coerceAtMost(containerWidth).toFloat(),
                    imageBitmap.height.coerceAtMost(containerHeight).toFloat()
                )
            )
        resetTransformations()
        isInitialized = true
    }

    suspend fun setCropping(isCropping: Boolean) {
        this.isCropping = isCropping
        resetTransformations()
        onCropDataChange(cropData)
    }

    suspend fun rotate(rotation: Float) {
        if (isAnimationRunning || this.rotation == rotation) return
        animateRotationTo(rotation)
        updateImageDrawRectFromTransformation()
        validateOverlayRectPosition()
    }

    fun onDown(change: PointerInputChange) {
        if (isAnimationRunning) return

        rectTemp = overlayRect.copy()

        val position = change.position
        val touchPositionScreenX = position.x
        val touchPositionScreenY = position.y

        val touchPositionOnScreen = Offset(touchPositionScreenX, touchPositionScreenY)

        // Get whether user touched outside, handles of rectangle or inner region or overlay
        // rectangle. Depending on where is touched we can move or scale overlay
        touchRegion = getTouchRegion(
            position = touchPositionOnScreen,
            rect = overlayRect,
            threshold = cropProperties.handleSize
        )

        // This is the difference between touch position and edge
        // This is required for not moving edge of draw rect to touch position on move
        distanceToEdgeFromTouch =
            getDistanceToEdgeFromTouch(touchRegion, rectTemp, touchPositionOnScreen)
    }

    suspend fun onMove(changes: List<PointerInputChange>) {
        if (isAnimationRunning) return
        if (changes.isEmpty()) {
            touchRegion = TouchRegion.None
            return
        }

        gestureInvoked = changes.size > 1 && (touchRegion == TouchRegion.Inside)

        // If overlay is touched and pointer size is one update
        // or pointer size is bigger than one but touched any handles update
        if (touchRegion != TouchRegion.None && changes.size == 1 && !gestureInvoked) {
            val change = changes.first()

            val defaultMinDimension = cropProperties.minSize

            // update overlay rectangle based on where its touched and touch position to corners
            // This function moves and/or scales overlay rectangle

            val newRect = updateOverlayRect(
                distanceToEdgeFromTouch = distanceToEdgeFromTouch,
                touchRegion = touchRegion,
                minDimension = defaultMinDimension,
                overlayRect = overlayRect,
                change = change
            )

            snapOverlayRectTo(newRect)
        }
    }

    suspend fun onGesture(
        panChange: Offset,
        zoomChange: Float
    ) {
        if (touchRegion == TouchRegion.None || gestureInvoked) {
            val newPan = if (gestureInvoked) Offset.Zero else panChange

            updateTransformState(
                zoomChange = zoomChange,
                panChange = newPan
            )

            // Update image draw rectangle based on pan, zoom or rotation change
            updateImageDrawRectFromTransformation()
        }
    }

    suspend fun onGestureEnd(onBoundsCalculated: () -> Unit) {
        onCropDataChange(cropData)
        if (touchRegion == TouchRegion.None || gestureInvoked) {
            onBoundsCalculated()
            animateTransformationToOverlayBounds(overlayRect)
        }
    }

    suspend fun onUp() = coroutineScope {
        onCropDataChange(cropData)
        if (isAnimationRunning) return@coroutineScope
        if (touchRegion != TouchRegion.None) {
            validateOverlayRectPosition()
        }
        gestureInvoked = false
    }

    private suspend fun updateTransformState(
        panChange: Offset,
        zoomChange: Float
    ) {
        val newZoom = (this.zoom * zoomChange).coerceIn(1f, cropProperties.maxZoom)

        snapZoomTo(newZoom)

        val newPan = this.pan + panChange
        snapPanXto(newPan.x)
        snapPanYto(newPan.y)
    }

    private suspend fun animateTransformations(
        pan: Offset = Offset.Zero,
        zoom: Float = 1f,
        rotation: Float = 0f
    ) = coroutineScope {
        launch { animateRotationTo(rotation) }
        launch { animatePanXto(pan.x) }
        launch { animatePanYto(pan.y) }
        launch { animateZoomTo(zoom) }
    }

    private suspend fun animatePanXto(
        panX: Float
    ) {
        if (pan.x != panX) {
            animatablePanX.animateTo(panX, tween())
        }
    }

    private suspend fun animatePanYto(
        panY: Float
    ) {
        if (pan.y != panY) {
            animatablePanY.animateTo(panY, tween())
        }
    }

    private suspend fun animateZoomTo(
        zoom: Float
    ) {
        if (this.zoom != zoom) {
            val newZoom = zoom.coerceIn(1f, cropProperties.maxZoom)
            animatableZoom.animateTo(newZoom, tween())
        }
    }

    private suspend fun animateRotationTo(
        rotation: Float
    ) {
        if (this.rotation != rotation) {
            animatableRotation.animateTo(rotation, tween())
        }
    }

    private suspend fun snapPanXto(panX: Float) {
        animatablePanX.snapTo(panX)
    }

    private suspend fun snapPanYto(panY: Float) {
        animatablePanY.snapTo(panY)
    }

    private suspend fun snapZoomTo(zoom: Float) {
        animatableZoom.snapTo(zoom.coerceIn(1f, cropProperties.maxZoom))
    }

    private suspend fun animateOverlayRectTo(
        rect: Rect
    ) {
        animatableRectOverlay.animateTo(
            targetValue = rect,
            animationSpec = tween()
        )
    }

    private suspend fun snapOverlayRectTo(rect: Rect) {
        animatableRectOverlay.snapTo(rect)
    }

    private fun updateImageDrawRectFromTransformation() {
        val containerWidth = containerRect.width
        val containerHeight = containerRect.height

        val originalDrawWidth = imageBitmap.width
        val originalDrawHeight = imageBitmap.height

        val panX = animatablePanX.targetValue
        val panY = animatablePanY.targetValue

        val left = (containerWidth - originalDrawWidth) / 2
        val top = (containerHeight - originalDrawHeight) / 2

        val zoom = animatableZoom.targetValue

        val isVerticalOrientation = rotation % 180 == 0f

        val newWidth = if (isVerticalOrientation) {
            originalDrawWidth * zoom
        } else {
            originalDrawHeight * zoom
        }
        val newHeight = if (isVerticalOrientation) {
            originalDrawHeight * zoom
        } else {
            originalDrawWidth * zoom
        }

        drawAreaRect = Rect(
            offset = Offset(
                left - (newWidth - originalDrawWidth) / 2 + panX,
                top - (newHeight - originalDrawHeight) / 2 + panY
            ),
            size = Size(newWidth, newHeight)
        )
    }

    private suspend fun animateTransformationToOverlayBounds(
        overlayRect: Rect
    ) {
        val zoom = zoom.coerceAtLeast(1f)

        // Calculate new pan based on overlay
        val newDrawAreaRect = calculateValidImageDrawRect(overlayRect, drawAreaRect)

        val newZoom =
            calculateNewZoom(oldRect = drawAreaRect, newRect = newDrawAreaRect, zoom = zoom)

        val leftChange = newDrawAreaRect.left - drawAreaRect.left
        val topChange = newDrawAreaRect.top - drawAreaRect.top

        val widthChange = newDrawAreaRect.width - drawAreaRect.width
        val heightChange = newDrawAreaRect.height - drawAreaRect.height

        val panXChange = leftChange + widthChange / 2
        val panYChange = topChange + heightChange / 2

        val newPanX = pan.x + panXChange
        val newPanY = pan.y + panYChange

        animateTransformations(
            pan = Offset(newPanX, newPanY),
            zoom = newZoom,
            rotation = rotation
        )

        updateImageDrawRectFromTransformation()
    }

    private suspend fun resetTransformations() = coroutineScope {
        if (isInitialized.not()) return@coroutineScope
        launch {
            if (isCropping) {
                launch { snapOverlayRectTo(getInitOverlayRect()) }
                launch { animateTransformationToOverlayBounds(getInitOverlayRect()) }
            } else {
                launch { animateTransformations() }
            }
        }.invokeOnCompletion {
            updateImageDrawRectFromTransformation()
        }
    }

    private fun calculateNewZoom(oldRect: Rect, newRect: Rect, zoom: Float): Float {
        if (oldRect.size == Size.Zero || newRect.size == Size.Zero) return zoom

        val widthChange = (newRect.width / oldRect.width)
            .coerceAtLeast(1f)
        val heightChange = (newRect.height / oldRect.height)
            .coerceAtLeast(1f)

        return widthChange.coerceAtLeast(heightChange) * zoom
    }

    private fun calculateValidImageDrawRect(rectOverlay: Rect, rectDrawArea: Rect): Rect {
        var width = rectDrawArea.width
        var height = rectDrawArea.height

        if (width < rectOverlay.width) {
            width = rectOverlay.width
        }

        if (height < rectOverlay.height) {
            height = rectOverlay.height
        }

        var rectImageArea = Rect(offset = rectDrawArea.topLeft, size = Size(width, height))

        if (rectImageArea.left > rectOverlay.left) {
            rectImageArea = rectImageArea.translate(rectOverlay.left - rectImageArea.left, 0f)
        }

        if (rectImageArea.right < rectOverlay.right) {
            rectImageArea = rectImageArea.translate(rectOverlay.right - rectImageArea.right, 0f)
        }

        if (rectImageArea.top > rectOverlay.top) {
            rectImageArea = rectImageArea.translate(0f, rectOverlay.top - rectImageArea.top)
        }

        if (rectImageArea.bottom < rectOverlay.bottom) {
            rectImageArea = rectImageArea.translate(0f, rectOverlay.bottom - rectImageArea.bottom)
        }

        return rectImageArea
    }

    private fun getInitOverlayRect(): Rect {
        var width = imageBitmap.width.coerceIn(
            cropProperties.minSize.width..containerRect.width.toInt()
        ).toFloat()
        var height = imageBitmap.height.coerceIn(
            cropProperties.minSize.height..containerRect.height.toInt()
        ).toFloat()

        cropProperties.aspectRatio?.let {
            if (width / it.value > drawAreaRect.height) {
                height = drawAreaRect.height.coerceAtLeast(cropProperties.minSize.height.toFloat())
                width = it.value * height
            } else {
                height = width / it.value
            }
        }

        val offsetX = (containerRect.width - width) / 2f
        val offsetY = (containerRect.height - height) / 2f

        return Rect(
            offset = Offset(offsetX, offsetY),
            size = Size(width, height)
        )
    }

    private fun getCropRectangle(
        bitmapWidth: Int,
        bitmapHeight: Int,
        drawAreaRect: Rect,
        overlayRect: Rect
    ): Rect {
        // Calculate latest image draw area based on overlay position
        // This is valid rectangle that contains crop area inside overlay
        val newRect = calculateValidImageDrawRect(overlayRect, drawAreaRect)

        val overlayWidth = overlayRect.width
        val overlayHeight = overlayRect.height

        val drawAreaWidth = newRect.width
        val drawAreaHeight = newRect.height

        val widthRatio = overlayWidth / drawAreaWidth
        val heightRatio = overlayHeight / drawAreaHeight

        val diffLeft = overlayRect.left - newRect.left
        val diffTop = overlayRect.top - newRect.top

        val croppedBitmapLeft = (diffLeft * (bitmapWidth / drawAreaWidth))
        val croppedBitmapTop = (diffTop * (bitmapHeight / drawAreaHeight))

        val croppedBitmapWidth = bitmapWidth * widthRatio
        val croppedBitmapHeight = bitmapHeight * heightRatio

        return Rect(
            offset = Offset(croppedBitmapLeft, croppedBitmapTop),
            size = Size(croppedBitmapWidth, croppedBitmapHeight)
        )
    }

    private suspend fun validateOverlayRectPosition() {
        val isOverlayInContainerBounds =
            containerRect.contains(overlayRect) && drawAreaRect.contains(overlayRect)
        var rect = overlayRect.copy()
        if (!isOverlayInContainerBounds) {
            // Calculate new overlay since it's out of bounds
            rect = calculateOverlayRectInBounds()
        }
        coroutineScope {
            // Animate overlay to new bounds inside container
            launch { animateOverlayRectTo(rect) }
            // Update and animate pan, zoom and image draw area after overlay position is updated
            launch { animateTransformationToOverlayBounds(rect) }
        }
    }

    private fun calculateOverlayRectInBounds(): Rect {
        val width = overlayRect.width.coerceAtMost(
            arrayOf(containerRect.width, imageBitmap.width * cropProperties.maxZoom).min()
        )
        val height = overlayRect.height.coerceAtMost(
            arrayOf(containerRect.height, imageBitmap.height * cropProperties.maxZoom).min()
        )
        var rect = Rect(offset = overlayRect.topLeft, size = Size(width, height))

        if (containerRect.contains(overlayRect).not()) {
            val leftConstraint = containerRect.left
            val topConstraint = containerRect.top
            val rightConstraint = containerRect.right
            val bottomConstraint = containerRect.bottom

            if (rect.left < leftConstraint) {
                rect = rect.translate(leftConstraint - rect.left, 0f)
            }

            if (rect.top < topConstraint) {
                rect = rect.translate(0f, topConstraint - rect.top)
            }

            if (rect.right > rightConstraint) {
                rect = rect.translate(rightConstraint - rect.right, 0f)
            }

            if (rect.bottom > bottomConstraint) {
                rect = rect.translate(0f, bottomConstraint - rect.bottom)
            }
        }
        return rect
    }

    private fun updateOverlayRect(
        distanceToEdgeFromTouch: Offset,
        touchRegion: TouchRegion,
        minDimension: IntSize,
        overlayRect: Rect,
        change: PointerInputChange
    ): Rect {
        val position = change.position
        // Get screen coordinates from touch position inside composable
        // and add how far it's from corner to not jump edge to user's touch position
        val screenPositionX = position.x + distanceToEdgeFromTouch.x
        val screenPositionY = position.y + distanceToEdgeFromTouch.y

        return when (touchRegion) {
            // Corners
            TouchRegion.TopLeft -> {
                // Set position of top left while moving with top left handle and
                // limit position to not intersect other handles
                val left = screenPositionX.coerceAtMost(rectTemp.right - minDimension.width)
                val top = cropProperties.aspectRatio?.let {
                    // If aspect ratio is fixed we need to calculate top position based on
                    // left position and aspect ratio
                    val width = rectTemp.right - left
                    val height = width / it.value
                    rectTemp.bottom - height
                } ?: screenPositionY.coerceAtMost(rectTemp.bottom - minDimension.height)

                Rect(
                    left = left,
                    top = top,
                    right = rectTemp.right,
                    bottom = rectTemp.bottom
                )
            }

            TouchRegion.BottomLeft -> {
                // Set position of top left while moving with bottom left handle and
                // limit position to not intersect other handles
                val left = screenPositionX.coerceAtMost(rectTemp.right - minDimension.width)
                val bottom = cropProperties.aspectRatio?.let {
                    // If aspect ratio is fixed we need to calculate bottom position based on
                    // left position and aspect ratio
                    val width = rectTemp.right - left
                    val height = width / it.value
                    rectTemp.top + height
                } ?: screenPositionY.coerceAtLeast(rectTemp.top + minDimension.height)
                Rect(
                    left = left,
                    top = rectTemp.top,
                    right = rectTemp.right,
                    bottom = bottom
                )
            }

            TouchRegion.TopRight -> {
                // Set position of top left while moving with top right handle and
                // limit position to not intersect other handles
                val right = screenPositionX.coerceAtLeast(rectTemp.left + minDimension.width)
                val top = cropProperties.aspectRatio?.let {
                    // If aspect ratio is fixed we need to calculate top position based on
                    // right position and aspect ratio
                    val width = right - rectTemp.left
                    val height = width / it.value
                    rectTemp.bottom - height
                } ?: screenPositionY.coerceAtMost(rectTemp.bottom - minDimension.height)

                Rect(
                    left = rectTemp.left,
                    top = top,
                    right = right,
                    bottom = rectTemp.bottom
                )
            }

            TouchRegion.BottomRight -> {
                // Set position of top left while moving with bottom right handle and
                // limit position to not intersect other handles
                val right = screenPositionX.coerceAtLeast(rectTemp.left + minDimension.width)
                val bottom = cropProperties.aspectRatio?.let {
                    // If aspect ratio is fixed we need to calculate bottom position based on
                    // right position and aspect ratio
                    val width = right - rectTemp.left
                    val height = width / it.value
                    rectTemp.top + height
                } ?: screenPositionY.coerceAtLeast(rectTemp.top + minDimension.height)

                Rect(
                    left = rectTemp.left,
                    top = rectTemp.top,
                    right = right,
                    bottom = bottom
                )
            }

            TouchRegion.Inside -> {
                val drag = change.positionChangeIgnoreConsumed()
                val scaledDragX = drag.x
                val scaledDragY = drag.y
                overlayRect.translate(scaledDragX, scaledDragY)
            }

            else -> overlayRect
        }
    }

    private fun getTouchRegion(
        position: Offset,
        rect: Rect,
        threshold: Float
    ): TouchRegion {
        val closedTouchRange = -threshold / 2..threshold

        return when {
            position.x - rect.left in closedTouchRange &&
                position.y - rect.top in closedTouchRange -> TouchRegion.TopLeft

            rect.right - position.x in closedTouchRange &&
                position.y - rect.top in closedTouchRange -> TouchRegion.TopRight

            rect.right - position.x in closedTouchRange &&
                rect.bottom - position.y in closedTouchRange -> TouchRegion.BottomRight

            position.x - rect.left in closedTouchRange &&
                rect.bottom - position.y in closedTouchRange -> TouchRegion.BottomLeft

            rect.contains(offset = position) -> TouchRegion.Inside
            else -> TouchRegion.None
        }
    }

    private fun getDistanceToEdgeFromTouch(
        touchRegion: TouchRegion,
        rect: Rect,
        touchPosition: Offset
    ) = when (touchRegion) {
        TouchRegion.TopLeft -> {
            rect.topLeft - touchPosition
        }

        TouchRegion.TopRight -> {
            rect.topRight - touchPosition
        }

        TouchRegion.BottomLeft -> {
            rect.bottomLeft - touchPosition
        }

        TouchRegion.BottomRight -> {
            rect.bottomRight - touchPosition
        }

        else -> {
            Offset.Zero
        }
    }
}
