package com.radiotelescope.repository.user

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit4.SpringRunner

@DataJpaTest
@RunWith(SpringRunner::class)
internal class UserTest {

    @Autowired
    private lateinit var userRepo: IUserRepository

    private var email: String = ""

    @Before
    fun setUp() {
        // Instantiate and persist a User Entity Object
        val user = User("David", "Bowie", "blackstar@bowie.com", "Password1234")
        userRepo.save(user)

        // Set the email variable to be used used in the IUserRepository existsByEmail query
        email = user.email
    }

    @Test
    fun testExistByEmail() {
        // Use the variable set in the set up method
        val exists: Boolean = userRepo.existsByEmail(email)

        // The User Entity should exist
        Assert.assertTrue(exists)
    }

    @Test
    fun testFindByEmail() {
        // Use the variable set in the set up method
        val user = userRepo.findByEmail(email)

        // The user val should not be null
        Assert.assertNotNull(user)
    }

    @Test
    fun testPasswordRegex() {
        // All lowercase, under 8
        Assert.assertFalse("password".matches(User.passwordRegex))

        // One uppercase, one lowercase, under 8
        Assert.assertFalse("Password".matches(User.passwordRegex))

        // One of each, under 8
        Assert.assertFalse("1qW#".matches(User.passwordRegex))

        // One uppercase, one lowercase, one digit, over 8
        Assert.assertTrue("Password1".matches(User.passwordRegex))

        // One uppercase, one lowercase, one digit, over 8
        Assert.assertTrue("GoodPassword1".matches(User.passwordRegex))

        // One uppercase, one lowercase, one special character, over 8
        Assert.assertTrue("GoodPassword?".matches(User.passwordRegex))

        // One lowercase, one special character, one digit, over 8
        Assert.assertTrue("goodpassword?1".matches(User.passwordRegex))

        // All four, over 8
        Assert.assertTrue("GoodPassword!?3".matches(User.passwordRegex))
    }
}