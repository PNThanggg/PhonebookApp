package com.app.phonebook.base.helpers

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import java.util.Locale

class MyContextWrapper(context: Context) : ContextWrapper(context) {
    fun wrap(context: Context, language: String): ContextWrapper {
        var newContext = context
        val config = newContext.resources.configuration

        val sysLocale: Locale? = getSystemLocaleLegacy(config)

        if (language != "" && sysLocale!!.language != language) {
            val locale = Locale(language)
            Locale.setDefault(locale)
            setSystemLocaleLegacy(config, locale)
        }

        newContext = newContext.createConfigurationContext(config)
        return MyContextWrapper(newContext)
    }

    private fun getSystemLocaleLegacy(config: Configuration) = config.locales.get(0)

    private fun getSystemLocale(config: Configuration) = config.locales.get(0)

    private fun setSystemLocaleLegacy(config: Configuration, locale: Locale) {
        config.setLocale(locale)
    }

    private fun setSystemLocale(config: Configuration, locale: Locale) {
        config.setLocale(locale)
    }
}
