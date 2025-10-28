package com.example.mobiamigo.data

import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
data class AppItem(
    val label: String,
    val packageName: String,
    val icon: Drawable?,
    val resolveInfo: ResolveInfo?
)