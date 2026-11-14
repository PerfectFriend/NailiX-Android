package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text as M3Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun Color.isDarkColor(): Boolean {
    if (this == Color.Unspecified) return false
    val luminance = 0.299f * this.red + 0.587f * this.green + 0.114f * this.blue
    return luminance < 0.5f
}

@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current
) {
    val resolvedColor = if (color != Color.Unspecified) {
        color
    } else if (style.color != Color.Unspecified) {
        style.color
    } else {
        androidx.compose.material3.LocalContentColor.current
    }

    val isDark = resolvedColor.isDarkColor()
    val borderColor = if (isDark) {
        Color.White
    } else {
        Color(0xFF0F2C59)
    }

    val resolvedFontSize = if (fontSize != TextUnit.Unspecified) {
        fontSize
    } else if (style.fontSize != TextUnit.Unspecified) {
        style.fontSize
    } else {
        14.sp
    }

    val strokeWidthDp = when {
        resolvedFontSize.value < 14f -> 1.0.dp
        resolvedFontSize.value < 20f -> 1.5.dp
        resolvedFontSize.value < 30f -> 2.5.dp
        else -> 3.5.dp
    }

    val density = LocalDensity.current
    val strokeWidthPx = with(density) { strokeWidthDp.toPx() }

    Box(modifier = modifier, propagateMinConstraints = true) {
        M3Text(
            text = text,
            modifier = Modifier.matchParentSize(),
            color = borderColor,
            fontSize = fontSize,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign,
            lineHeight = lineHeight,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            minLines = minLines,
            onTextLayout = onTextLayout,
            style = style.copy(
                drawStyle = Stroke(
                    width = strokeWidthPx,
                    join = StrokeJoin.Round
                )
            )
        )
        M3Text(
            text = text,
            modifier = Modifier,
            color = resolvedColor,
            fontSize = fontSize,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign,
            lineHeight = lineHeight,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            minLines = minLines,
            onTextLayout = onTextLayout,
            style = style.copy(
                drawStyle = Fill
            )
        )
    }
}
