package com.radiotelescope.repository.model.user

import com.radiotelescope.repository.user.User

/**
 * Enum class that acts as a search filter for the [User] entity
 *
 * @param field the corresponding entity field. Note: NOT the corresponding SQL column
 */
enum class Filter(val field: String) {
    FIRST_NAME("firstName"),
    LAST_NAME("lastName"),
    EMAIL("email"),
    COMPANY("company");

    companion object {
        /**
         * Takes a string value and will adapt it into the corresponding
         * [Filter] value. If the string supplied does not match with
         * any of the values, it will return null
         *
         * @param field the [User] field
         * @return a [Filter] object or null
         */
        fun fromField(field: String): Filter? {
            return when (field) {
                "firstName" -> FIRST_NAME
                "lastName" -> LAST_NAME
                "email" -> EMAIL
                "company" -> COMPANY
                else -> {
                    // Handle the case where an invalid parameter
                    // is supplied
                    null
                }
            }
        }
    }
}