package com.radiotelescope.controller.user

import com.radiotelescope.contracts.user.Register
import com.radiotelescope.controller.model.Profile
import com.radiotelescope.controller.model.user.RegisterForm
import com.radiotelescope.repository.log.ILogRepository
import com.radiotelescope.repository.role.UserRole
import com.radiotelescope.repository.user.IUserRepository
import com.radiotelescope.services.ses.MockAwsSesSendService
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
internal class UserRegisterControllerTest : BaseUserRestControllerTest() {
    @Autowired
    private lateinit var logRepo: ILogRepository

    private lateinit var userRegisterController: UserRegisterController

    @Autowired
    private lateinit var userRepo: IUserRepository

    private val baseForm = RegisterForm(
            firstName = "Cody",
            lastName = "Spath",
            email = "cspath1@ycp.edu",
            emailConfirm = "cspath1@ycp.edu",
            password = "Password1@",
            passwordConfirm = "Password1@",
            phoneNumber = "717-823-2216",
            company = "York College of PA",
            categoryOfService = UserRole.Role.GUEST
    )

    @Before
    override fun init() {
        super.init()

        userRegisterController = UserRegisterController(
                userWrapper = getWrapper(),
                awsSesSendService = MockAwsSesSendService(true),
                userRepo = userRepo,
                logger = getLogger()
        )


    }

    @Test
    fun testSuccessResponse() {
        // Test the success scenario to ensure the result
        // object is correctly set
        val result = userRegisterController.execute(baseForm)

        assertNotNull(result)
        assertTrue(result.data is Register.Response)
        assertEquals(HttpStatus.OK, result.status)
        assertNull(result.errors)

        // Ensure a log record was created
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
        val formCopy = baseForm.copy(firstName = "")

        val result = userRegisterController.execute(formCopy)

        assertNotNull(result)
        assertNull(result.data)
        assertNotNull(result.errors)
        assertEquals(HttpStatus.BAD_REQUEST, result.status)
        assertEquals(1, result.errors!!.size)

        // Ensure a log record was created
        assertEquals(1, logRepo.count())

        logRepo.findAll().forEach {
            assertEquals(HttpStatus.BAD_REQUEST.value(), it.status)
        }
    }

    @Test
    fun testValidForm_FailedValidationResponse() {
        // Test the scenario where the form is valid,
        // but validation in the command object fails

        // Create a user with the same email as the form
        testUtil.createUser(
                email = baseForm.email!!
        )

        val result = userRegisterController.execute(baseForm)

        assertNotNull(result)
        assertNull(result.data)
        assertNotNull(result.errors)
        assertEquals(HttpStatus.BAD_REQUEST, result.status)
        assertEquals(1, result.errors!!.size)

        // Ensure a log record was created
        assertEquals(1, logRepo.count())

        logRepo.findAll().forEach {
            assertEquals(HttpStatus.BAD_REQUEST.value(), it.status)
        }
    }
}