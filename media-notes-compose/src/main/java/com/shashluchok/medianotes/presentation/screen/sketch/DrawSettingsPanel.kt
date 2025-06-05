package com.shashluchok.medianotes.presentation.screen.sketch

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shashluchok.medianotes.presentation.screen.sketch.SketchViewModel.DrawSettings

private val contentVerticalArrangement = Arrangement.spacedBy(24.dp)

private val saturationPanelHeight = 300.dp
private val saturationPanelCornerRadius = 28.dp

private val huePanelHeight = 20.dp
private val thicknessSliderThumbHeight = 30.dp
private val thicknessSliderTrackHeight = 20.dp

private val previewBoxSize = 90.dp
private val previewBoxPadding = PaddingValues(start = 24.dp)
private val previewBoxShape = RoundedCornerShape(6.dp)

@Composable
internal fun PenSettingsPanel(
    penSettings: DrawSettings.Pen,
    onChangePenThickness: (Dp) -> Unit,
    onChangePenColorHue: (Float) -> Unit,
    onChangePenColorSaturationAndValue: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = contentVerticalArrangement
    ) {
        val hsv = penSettings.hsv
        SaturationPanel(
            modifier = Modifier
                .fillMaxWidth()
                .height(saturationPanelHeight),
            radius = saturationPanelCornerRadius,
            hue = hsv.hue,
            currentSaturation = hsv.saturation,
            currentValue = hsv.value,
            onSaturationAndValueChange = onChangePenColorSaturationAndValue
        )

        Row {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                HuePanel(
                    modifier = Modifier
                        .height(huePanelHeight)
                        .fillMaxWidth(),
                    currentHue = hsv.hue,
                    onHueChange = onChangePenColorHue
                )
                ThicknessSlider(
                    modifier = Modifier.fillMaxWidth(),
                    onThicknessChange = onChangePenThickness,
                    currentThickness = penSettings.currentThickness,
                    thicknessRange = penSettings.thicknessRange
                )
            }
            DrawPreviewBox(
                modifier = Modifier
                    .padding(previewBoxPadding)
                    .size(previewBoxSize)
                    .background(
                        color = Color.White,
                        shape = previewBoxShape
                    ),
                drawColor = penSettings.color,
                drawThickness = penSettings.currentThickness
            )
        }
    }
}

@Composable
internal fun EraserSettingsPanel(
    eraserSettings: DrawSettings.Eraser,
    onChangeEraserThickness: (Dp) -> Unit,
    modifier: Modifier = Modifier
) {
    ThicknessSlider(
        modifier = modifier,
        onThicknessChange = onChangeEraserThickness,
        currentThickness = eraserSettings.currentThickness,
        thicknessRange = eraserSettings.thicknessRange
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThicknessSlider(
    currentThickness: Dp,
    thicknessRange: ClosedRange<Dp>,
    onThicknessChange: (Dp) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    with(density) {
        Slider(
            modifier = modifier,
            onValueChange = { onThicknessChange(it.toDp()) },
            value = currentThickness.toPx(),
            valueRange = thicknessRange.start.toPx()..thicknessRange.endInclusive.toPx(),
            thumb = {
                SliderDefaults.Thumb(
                    modifier = Modifier.height(thicknessSliderThumbHeight),
                    interactionSource = remember { MutableInteractionSource() }
                )
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    modifier = Modifier.height(thicknessSliderTrackHeight)
                )
            }
        )
    }
}
