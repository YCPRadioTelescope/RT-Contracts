package com.radiotelescope.repository.user

import javax.persistence.*

/**
 * Entity Class representing a User for the web-application
 *
 * This Entity correlates to the User SQL table
 */
@Entity
@Table(name = "user")
data class User(
        @Column(name = "first_name", nullable = false)
        var firstName: String,
        @Column(name = "last_name", nullable = false)
        var lastName: String,
        @Column(name = "email_address", nullable = false, unique = true)
        var email: String,
        @Column(name = "password", nullable = false)
        var password: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    var id: Long = 0

    @Column(name = "company")
    var company: String? = null

    @Column(name = "phone_number")
    var phoneNumber: String? = null

    @Column(name = "active")
    var active: Boolean = false

    @Column(name = "status")
    @Enumerated(value = EnumType.STRING)
    var status: User.Status = Status.Inactive

    enum class Status {
        Inactive,
        Active,
        Banned,
        Deleted
    }
}