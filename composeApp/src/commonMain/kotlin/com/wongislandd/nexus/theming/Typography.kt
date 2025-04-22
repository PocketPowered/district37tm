package com.wongislandd.nexus.theming

import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import district37toastmasters.composeapp.generated.resources.Res
import district37toastmasters.composeapp.generated.resources.poppins_black
import district37toastmasters.composeapp.generated.resources.poppins_bold
import district37toastmasters.composeapp.generated.resources.poppins_medium
import district37toastmasters.composeapp.generated.resources.poppins_regular

val poppinsRegular
    @Composable
    get() = FontFamily(org.jetbrains.compose.resources.Font(Res.font.poppins_regular))
val poppinsBold
    @Composable
    get() = FontFamily(org.jetbrains.compose.resources.Font(Res.font.poppins_bold))
val poppinsMedium
    @Composable
    get() = FontFamily(org.jetbrains.compose.resources.Font(Res.font.poppins_medium))
val poppinsBlack
    @Composable
    get() = FontFamily(org.jetbrains.compose.resources.Font(Res.font.poppins_black))

val PoppinsFont
    @Composable
    get() = Typography().let {

        it.copy(
            h1 = it.h1.copy(fontFamily = poppinsMedium),
            h2 = it.h2.copy(fontFamily = poppinsMedium),
            h3 = it.h3.copy(fontFamily = poppinsMedium),
            h4 = it.h4.copy(fontFamily = poppinsMedium),
            h5 = it.h5.copy(fontFamily = poppinsMedium),
            h6 = it.h6.copy(fontFamily = poppinsMedium),
            subtitle1 = it.subtitle1.copy(fontFamily = poppinsMedium),
            subtitle2 = it.subtitle2.copy(fontFamily = poppinsMedium),
            body1 = it.body1.copy(fontFamily = poppinsMedium),
            body2 = it.body2.copy(fontFamily = poppinsMedium),
            button = it.button.copy(fontFamily = poppinsMedium),
            caption = it.caption.copy(fontFamily = poppinsMedium),
            overline = it.overline.copy(fontFamily = poppinsMedium),
        )
    }