package com.example.fitlife.auth

import android.content.Context
import org.json.JSONObject

class UserPref(context: Context) {

    private val pref = context.getSharedPreferences("user_data", Context.MODE_PRIVATE)

    private val KEY_USERS_JSON = "users_json"            // email -> {fullName, password}
    private val KEY_IS_LOGGED_IN = "isLoggedIn"
    private val KEY_LOGGED_IN_EMAIL = "loggedInEmail"

    private fun norm(email: String) = email.trim().lowercase()

    // ---------- Users DB (email -> {fullName, password}) ----------
    private fun getUsersJson(): JSONObject {
        val raw = pref.getString(KEY_USERS_JSON, null)
        return if (raw.isNullOrBlank()) JSONObject() else JSONObject(raw)
    }

    private fun saveUsersJson(obj: JSONObject) {
        pref.edit().putString(KEY_USERS_JSON, obj.toString()).apply()
    }

    fun userExists(email: String): Boolean {
        val em = norm(email)
        return getUsersJson().has(em)
    }

    fun saveUser(fullName: String, email: String, password: String) {
        val em = norm(email)
        val users = getUsersJson()

        val userObj = JSONObject().apply {
            put("fullName", fullName.trim())
            put("password", password)
        }

        users.put(em, userObj)
        saveUsersJson(users)

        // Optional: initialize basic profile for this user (so profile shows data)
        saveProfile(
            email = em,
            fullName = fullName.trim(),
            age = 0,
            heightCm = 0,
            weightKg = 0f,
            goal = ""
        )
    }

    fun validateLogin(email: String, password: String): Boolean {
        val em = norm(email)
        val users = getUsersJson()
        if (!users.has(em)) return false
        val userObj = users.getJSONObject(em)
        return userObj.optString("password", "") == password
    }

    fun getFullNameFor(email: String): String {
        val em = norm(email)
        val users = getUsersJson()
        if (!users.has(em)) return ""
        return users.getJSONObject(em).optString("fullName", "")
    }

    // ---------- Session ----------
    fun setLoggedIn(email: String, value: Boolean) {
        pref.edit()
            .putBoolean(KEY_IS_LOGGED_IN, value)
            .putString(KEY_LOGGED_IN_EMAIL, if (value) norm(email) else "")
            .apply()
    }

    fun isLoggedIn(): Boolean = pref.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getLoggedInEmail(): String = pref.getString(KEY_LOGGED_IN_EMAIL, "") ?: ""

    fun logout() {
        pref.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .putString(KEY_LOGGED_IN_EMAIL, "")
            .apply()
    }

    // ---------- Profile per user ----------
    private fun pKey(email: String, field: String) = "profile_${norm(email)}_$field"

    fun saveProfile(
        email: String,
        fullName: String,
        age: Int,
        heightCm: Int,
        weightKg: Float,
        goal: String
    ) {
        val em = norm(email)
        pref.edit()
            .putString(pKey(em, "fullName"), fullName)
            .putString(pKey(em, "email"), em)
            .putInt(pKey(em, "age"), age)
            .putInt(pKey(em, "heightCm"), heightCm)
            .putFloat(pKey(em, "weightKg"), weightKg)
            .putString(pKey(em, "goal"), goal)
            .apply()
    }

    fun getProfileFullName(email: String): String = pref.getString(pKey(email, "fullName"), "") ?: ""
    fun getProfileEmail(email: String): String = pref.getString(pKey(email, "email"), norm(email)) ?: norm(email)
    fun getProfileAge(email: String): Int = pref.getInt(pKey(email, "age"), 0)
    fun getProfileHeightCm(email: String): Int = pref.getInt(pKey(email, "heightCm"), 0)
    fun getProfileWeightKg(email: String): Float = pref.getFloat(pKey(email, "weightKg"), 0f)
    fun getProfileGoal(email: String): String = pref.getString(pKey(email, "goal"), "") ?: ""
}
