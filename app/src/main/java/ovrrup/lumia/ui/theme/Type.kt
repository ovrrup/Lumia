package ovrrup.lumia.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import ovrrup.lumia.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val fontName = GoogleFont("Nunito")

val NunitoFontFamily = FontFamily.SansSerif

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

fun getSystemTypography(fontNameStr: String, headingFontNameStr: String? = null): Typography {
    val heading = headingFontNameStr ?: fontNameStr

    val bodyFamily = when (fontNameStr) {
        "System Default" -> FontFamily.Default
        "Lato", "Open Sans", "Inter", "Nunito" -> FontFamily.SansSerif
        "Tenor Sans" -> FontFamily.Serif
        else -> FontFamily.Default
    }

    val headingFamily = when (heading) {
        "System Default" -> FontFamily.Default
        "Nunito" -> FontFamily.SansSerif
        "Playfair Display" -> FontFamily.Serif
        "Josefin Sans" -> FontFamily.SansSerif
        "Archivo" -> FontFamily.SansSerif
        "Syne" -> FontFamily.SansSerif
        "Montserrat" -> FontFamily.SansSerif
        "Yellowtail" -> FontFamily.Cursive
        else -> FontFamily.Default
    }

    return Typography(
        bodyLarge = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        titleLarge = TextStyle(
            fontFamily = headingFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = headingFamily,
            fontWeight = FontWeight.Black,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.0.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = headingFamily,
            fontWeight = FontWeight.Black,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = headingFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.15.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp
        ),
        labelSmall = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        )
    )
}

fun getTypography(fontNameStr: String, headingFontNameStr: String? = null, isGms: Boolean = true): Typography {
    val heading = headingFontNameStr ?: fontNameStr

    if (!isGms || fontNameStr == "System Default") {
        return getSystemTypography(fontNameStr, headingFontNameStr)
    }

    val bodyFamily = try {
        val bodyFont = GoogleFont(fontNameStr)
        FontFamily(
            Font(googleFont = bodyFont, fontProvider = provider, weight = FontWeight.Normal),
            Font(googleFont = bodyFont, fontProvider = provider, weight = FontWeight.Medium),
            Font(googleFont = bodyFont, fontProvider = provider, weight = FontWeight.SemiBold),
            Font(googleFont = bodyFont, fontProvider = provider, weight = FontWeight.Bold)
        )
    } catch (e: Throwable) {
        return getSystemTypography(fontNameStr, headingFontNameStr)
    }

    val headingFamily = try {
        val headingFont = GoogleFont(heading)
        FontFamily(
            Font(googleFont = headingFont, fontProvider = provider, weight = FontWeight.Normal),
            Font(googleFont = headingFont, fontProvider = provider, weight = FontWeight.Medium),
            Font(googleFont = headingFont, fontProvider = provider, weight = FontWeight.SemiBold),
            Font(googleFont = headingFont, fontProvider = provider, weight = FontWeight.Bold),
            Font(googleFont = headingFont, fontProvider = provider, weight = FontWeight.ExtraBold),
            Font(googleFont = headingFont, fontProvider = provider, weight = FontWeight.Black)
        )
    } catch (e: Throwable) {
        return getSystemTypography(fontNameStr, headingFontNameStr)
    }

    return Typography(
        bodyLarge = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        titleLarge = TextStyle(
            fontFamily = headingFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = headingFamily,
            fontWeight = FontWeight.Black,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.0.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = headingFamily,
            fontWeight = FontWeight.Black,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = headingFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.15.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp
        ),
        labelSmall = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        )
    )
}
