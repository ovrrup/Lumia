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

val NunitoFontFamily = FontFamily(
    Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.ExtraBold),
    Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.Black)
)

val QuicksandFontFamily = FontFamily(
    Font(googleFont = GoogleFont("Quicksand"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Quicksand"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Quicksand"), fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Quicksand"), fontProvider = provider, weight = FontWeight.Bold)
)

val MontserratFontFamily = FontFamily(
    Font(googleFont = GoogleFont("Montserrat"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Montserrat"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Montserrat"), fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Montserrat"), fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = GoogleFont("Montserrat"), fontProvider = provider, weight = FontWeight.Black)
)

val InterFontFamily = FontFamily(
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.Bold)
)

val PoppinsFontFamily = FontFamily(
    Font(googleFont = GoogleFont("Poppins"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Poppins"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Poppins"), fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Poppins"), fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = GoogleFont("Poppins"), fontProvider = provider, weight = FontWeight.Black)
)

fun getTypography(fontFamily: FontFamily): Typography {
    return Typography(
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Black,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Black,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.15.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp
        ),
        labelSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        ),
        labelLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        )
    )
}

val Typography = getTypography(NunitoFontFamily)
