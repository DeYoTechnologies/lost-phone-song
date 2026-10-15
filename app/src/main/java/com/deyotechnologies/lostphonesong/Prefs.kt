package com.deyotechnologies.lostphonesong

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri

object Prefs {
    private const val FILE = "lost_phone_song"
    private const val KEY_TRUSTED_NUMBER = "trusted_number"
    private const val KEY_SONG_URI = "song_uri"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun trustedNumber(context: Context): String? =
        prefs(context).getString(KEY_TRUSTED_NUMBER, null)?.takeIf { it.isNotBlank() }

    fun setTrustedNumber(context: Context, number: String) {
        prefs(context).edit().putString(KEY_TRUSTED_NUMBER, number).apply()
    }

    fun songUri(context: Context): Uri? =
        prefs(context).getString(KEY_SONG_URI, null)?.let(Uri::parse)

    fun setSongUri(context: Context, uri: Uri?) {
        prefs(context).edit().apply {
            if (uri == null) remove(KEY_SONG_URI) else putString(KEY_SONG_URI, uri.toString())
        }.apply()
    }
}
