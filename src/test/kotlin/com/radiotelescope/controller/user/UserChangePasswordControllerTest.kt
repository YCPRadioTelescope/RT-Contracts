package com.radiotelescope.controller.user

import com.radiotelescope.controller.model.user.ChangePasswordForm
import com.radiotelescope.repository.log.ILogRepository
import com.radiotelescope.repository.role.UserRole
import com.radiotelescope.repository.user.User
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner

@DataJpaTest
@RunWith(SpringRunner::class)
internal class UserChangePasswordControllerTest : BaseUserRestControllerTest() {
    @Autowired
    private lateinit var logRepo: ILogRepository

    private lateinit var userChangePasswordController: UserChangePasswordController

    private lateinit var baseForm: ChangePasswordForm

    private lateinit var user: User

    private var userContext = getContext()

    @Before
    override fun init() {
        super.init()

        user = testUtil.createUserWithEncodedPassword(
                email = "rpim@ycp.edu",
                password = "Password"
        )
        // Simulate a login
        userContext.login(user.id)
        userContext.currentRoles.add(UserRole.Role.USER)

        userChangePasswordController = UserChangePasswordController(
                userWrapper = getWrapper(),
                logger = getLogger()
        )

        baseForm = ChangePasswordForm(
                currentPassword = "Password",
                id = user.id,
                password = "ValidPassword1!",
                passwordConfirm = "ValidPassword1!"
        )
    }

    @Test
    fun testSuccessResponse() {
        // Test the success scenario to ensure the result
        // is correct
        val result = userChangePasswordController.execute(baseForm)

        assertNotNull(result)
        assertEquals(user.id, result.data)
        assertEquals(HttpStatus.OK, result.status)
        assertNull(result.errors)

        // Ensure a log was created
        assertEquals(1, logRepo.count())

        logRepo.findAll().forEach {
            assertEquals(HttpStatus.OK.value(), it.status)
        }
    }

    @Test
    fun testInvalidFormResponse() {
        // Test the scenario where the form's validate
        // request method fails to ensure the result object
        // has the correct properties
        val formCopy = baseForm.copy(currentPassword = "")

        val result = userChangePasswordController.execute(formCopy)

        assertNotNull(result)
        assertNull(result.data)
        assertNotNull(result.errors)
        assertEquals(HttpStatus.BAD_REQUEST, result.status)
        assertEquals(1, result.errors!!.size)

        // Ensure a log was created
        assertEquals(1, logRepo.count())

        logRepo.findAll().forEach {
            assertEquals(HttpStatus.BAD_REQUEST.value(), it.status)
        }
    }

    @Test
    fun testValidForm_FailedValidationResponse() {
        // Test the scenario where the form is valid,
        // but validation in the command object fails

        // Create a form with the same password as the current password
        val formCopy = baseForm.copy(
                password = baseForm.currentPassword,
                passwordConfirm = baseForm.currentPassword
        )


        val result = userChangePasswordController.execute(formCopy)

        assertNotNull(result)
        assertNull(result.data)
        assertNotNull(result.errors)
        assertEquals(HttpStatus.BAD_REQUEST, result.status)
        assertEquals(1, result.errors!!.size)
    }

    @Test
    fun testValidForm_FailedAuthenticationResponse() {
        // Test the scenario where the form is valid,
        // but the authentication in the wrapper fails

        // Simulate a log out
        userContext.logout()

        val result = userChangePasswordController.execute(baseForm)

        assertNotNull(result)
        assertNull(result.data)
        assertNotNull(result.errors)
        assertEquals(HttpStatus.FORBIDDEN, result.status)
        assertEquals(1, result.errors!!.size)

        // Ensure a log was created
        assertEquals(1, logRepo.count())

        logRepo.findAll().forEach {
            assertEquals(HttpStatus.FORBIDDEN.value(), it.status)
        }
    }

    @Test
    fun testValidForm_InvalidResourceIdResponse() {
        // Test the scenario where the form is valid,
        // but the resource does not exist

        val formCopy = baseForm.copy(
                id = 420L
        )

        val result = userChangePasswordController.execute(formCopy)

        assertNotNull(result)
        assertNull(result.data)
        assertNotNull(result.errors)
        assertEquals(HttpStatus.NOT_FOUND, result.status)
        assertEquals(1, result.errors!!.size)

        // Ensure a log was created
        assertEquals(1, logRepo.count())

        logRepo.findAll().forEach {
            assertEquals(HttpStatus.NOT_FOUND.value(), it.status)
        }
    }
}