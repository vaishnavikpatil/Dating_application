package com.example.dating_app.data

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun saveUser(id: String, name: String, email: String) {
        prefs.edit().apply {
            putString("user_id", id)
            putString("user_name", name)
            putString("user_email", email)
            putBoolean("is_logged_in", true)
            apply()
        }
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean("is_logged_in", false)

    fun getUserId(): String? = prefs.getString("user_id", null)

    fun getUserName(): String? = prefs.getString("user_name", null)

    fun getUserEmail(): String? = prefs.getString("user_email", null)

    fun clear() {
        prefs.edit().clear().apply()
    }
}
