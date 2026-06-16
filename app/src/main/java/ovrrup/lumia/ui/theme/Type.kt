package ovrrup.lumia.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ovrrup.lumia.R

val NunitoFamily = FontFamily(
    Font(resId = R.font.nunito_regular, weight = FontWeight.Normal),
    Font(resId = R.font.nunito_bold, weight = FontWeight.Bold)
)

val LatoFamily = FontFamily(
    Font(resId = R.font.lato_regular, weight = FontWeight.Normal),
    Font(resId = R.font.lato_bold, weight = FontWeight.Bold)
)

val OpenSansFamily = FontFamily(
    Font(resId = R.font.opensans_regular, weight = FontWeight.Normal),
    Font(resId = R.font.opensans_bold, weight = FontWeight.Bold)
)

val InterFamily = FontFamily(
    Font(resId = R.font.inter_regular, weight = FontWeight.Normal),
    Font(resId = R.font.inter_bold, weight = FontWeight.Bold)
)

val TenorSansFamily = FontFamily(
    Font(resId = R.font.tenorsans_regular, weight = FontWeight.Normal)
)

val PlayfairDisplayFamily = FontFamily(
    Font(resId = R.font.playfairdisplay_regular, weight = FontWeight.Normal),
    Font(resId = R.font.playfairdisplay_bold, weight = FontWeight.Bold)
)

val JosefinSansFamily = FontFamily(
    Font(resId = R.font.josefinsans_regular, weight = FontWeight.Normal),
    Font(resId = R.font.josefinsans_bold, weight = FontWeight.Bold)
)

val ArchivoFamily = FontFamily(
    Font(resId = R.font.archivo_regular, weight = FontWeight.Normal),
    Font(resId = R.font.archivo_bold, weight = FontWeight.Bold)
)

val SyneFamily = FontFamily(
    Font(resId = R.font.syne_regular, weight = FontWeight.Normal),
    Font(resId = R.font.syne_bold, weight = FontWeight.Bold)
)

val MontserratFamily = FontFamily(
    Font(resId = R.font.montserrat_regular, weight = FontWeight.Normal),
    Font(resId = R.font.montserrat_bold, weight = FontWeight.Bold)
)

val YellowtailFamily = FontFamily(
    Font(resId = R.font.yellowtail_regular, weight = FontWeight.Normal)
)

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.Black,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.Black,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    labelSmall = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

fun getLocalFontFamily(name: String): FontFamily {
    return when (name) {
        "Nunito" -> NunitoFamily
        "Lato" -> LatoFamily
        "Open Sans" -> OpenSansFamily
        "Inter" -> InterFamily
        "Tenor Sans" -> TenorSansFamily
        "Playfair Display" -> PlayfairDisplayFamily
        "Josefin Sans" -> JosefinSansFamily
        "Archivo" -> ArchivoFamily
        "Syne" -> SyneFamily
        "Montserrat" -> MontserratFamily
        "Yellowtail" -> YellowtailFamily
        else -> FontFamily.Default
    }
}

fun getSystemTypography(fontNameStr: String, headingFontNameStr: String? = null): Typography {
    return getTypography(fontNameStr, headingFontNameStr, isGms = false)
}

fun getTypography(fontNameStr: String, headingFontNameStr: String? = null, isGms: Boolean = true): Typography {
    val heading = headingFontNameStr ?: fontNameStr

    val bodyFamily = getLocalFontFamily(fontNameStr)
    val headingFamily = getLocalFontFamily(heading)

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
