package com.app.phonebook.base.helpers

import android.annotation.TargetApi
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import com.app.phonebook.base.utils.isNougatPlus
import java.util.*

class MyContextWrapper(context: Context) : ContextWrapper(context) {
    fun wrap(context: Context, language: String): ContextWrapper {
        var newContext = context
        val config = newContext.resources.configuration
        val sysLocale: Locale?

        sysLocale = if (isNougatPlus()) {
            getSystemLocale(config)
        } else {
            getSystemLocaleLegacy(config)
        }

        if (language != "" && sysLocale!!.language != language) {
            val locale = Locale(language)
            Locale.setDefault(locale)
            if (isNougatPlus()) {
                setSystemLocale(config, locale)
            } else {
                setSystemLocaleLegacy(config, locale)
            }
        }

        newContext = newContext.createConfigurationContext(config)
        return MyContextWrapper(newContext)
    }

    private fun getSystemLocaleLegacy(config: Configuration) = config.locale

    @TargetApi(Build.VERSION_CODES.N)
    private fun getSystemLocale(config: Configuration) = config.locales.get(0)

    private fun setSystemLocaleLegacy(config: Configuration, locale: Locale) {
        config.locale = locale
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun setSystemLocale(config: Configuration, locale: Locale) {
        config.setLocale(locale)
    }
}
