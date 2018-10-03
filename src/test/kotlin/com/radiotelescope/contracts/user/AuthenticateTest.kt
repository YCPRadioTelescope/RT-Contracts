package com.radiotelescope.contracts.user

import com.radiotelescope.repository.user.IUserRepository
import com.radiotelescope.repository.user.User
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder
import org.springframework.test.context.junit4.SpringRunner

@DataJpaTest
@RunWith(SpringRunner::class)
class AuthenticateTest {
    @Autowired
    private lateinit var userRepo: IUserRepository

    private val baseRequest = Authenticate.Request(
            email = "cspath1@ycp.edu",
            password = "Password"
    )

    @Before
    fun setUp() {
        // We will need to hash the password before persisting
        val passwordEncoder = Pbkdf2PasswordEncoder(
                "YCAS2018",
                50,
                256
        )

        // Persist the User with the hashed password
        val user = userRepo.save(User(
                firstName = "Cody",
                lastName = "Spath",
                email = "cspath1@ycp.edu",
                password = passwordEncoder.encode("Password")
        ))

        // Make sure this was correctly executed
        assertEquals(1, userRepo.count())
        assertNotNull(user)
    }

    @Test
    fun testValidConstraints_Success() {
        // Execute the command
        val (info, errors) = Authenticate(
                request = baseRequest,
                userRepo = userRepo
        ).execute()

        // The info class should not be null
        assertNotNull(info)

        // The errors should be
        assertNull(errors)
    }

    @Test
    fun testBlankEmail_Failure() {
        // Create a copy of the request with a blank email
        val requestCopy = baseRequest.copy(
                email = ""
        )

        // Execute the command
        val (info, errors) = Authenticate(
                request = requestCopy,
                userRepo = userRepo
        ).execute()

        // The errors should not be null
        assertNotNull(errors)

        // The data class should be
        assertNull(info)
    }

    @Test
    fun testBlankPassword_Failure() {
        // Create a copy of the request with a blank password
        val requestCopy = baseRequest.copy(
                password = ""
        )

        // Execute the command
        val (info, errors) = Authenticate(
                request = requestCopy,
                userRepo = userRepo
        ).execute()

        // The errors should not be null
        assertNotNull(errors)

        // The data class should be
        assertNull(info)
    }

    @Test
    fun testNonExistentEmail_Failure() {
        // Create a copy of the request with a non-existent email
        val requestCopy = baseRequest.copy(
                email = "spathcody@gmail.com"
        )

        // Execute the command
        val (info, errors) = Authenticate(
                request = requestCopy,
                userRepo = userRepo
        ).execute()

        // The errors should not be null
        assertNotNull(errors)

        // The data class should be
        assertNull(info)
    }

    @Test
    fun testPasswordsDoNotMatch_Failure() {
        // Create a copy of the request with a bad password
        val requestCopy = baseRequest.copy(
                password = "Passwrod"
        )

        // Execute the command
        val (info, errors) = Authenticate(
                request = requestCopy,
                userRepo = userRepo
        ).execute()

        // The errors should not be null
        assertNotNull(errors)

        // The data class should be
        assertNull(info)
    }
}