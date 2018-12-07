package com.radiotelescope.controller.admin.role

import com.radiotelescope.TestUtil
import com.radiotelescope.controller.model.role.ValidateForm
import com.radiotelescope.controller.user.BaseUserRoleControllerTest
import com.radiotelescope.repository.log.ILogRepository
import com.radiotelescope.repository.role.UserRole
import com.radiotelescope.repository.user.User
import liquibase.integration.spring.SpringLiquibase
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@DataJpaTest
@RunWith(SpringRunner::class)
@ActiveProfiles(value = ["test"])
internal class AdminUserRoleValidateControllerTest : BaseUserRoleControllerTest() {
    @TestConfiguration
    class UtilTestContextConfiguration {
        @Bean
        fun utilService(): TestUtil { return TestUtil() }

        @Bean
        fun liquibase(): SpringLiquibase {
            val liquibase = SpringLiquibase()
            liquibase.setShouldRun(false)
            return liquibase
        }
    }

    @Autowired
    private lateinit var testUtil: TestUtil

    @Autowired
    private lateinit var logRepo: ILogRepository

    private lateinit var adminUserRoleValidateController: AdminUserRoleValidateController
    private lateinit var admin: User
    private lateinit var roles: List<UserRole>

    @Before
    override fun init() {
        super.init()

        adminUserRoleValidateController = AdminUserRoleValidateController(
                roleWrapper = getWrapper(),
                logger = getLogger()
        )

        admin = testUtil.createUser("rpim@ycp.edu")
        testUtil.createUserRolesForUser(
                userId = admin.id,
                role = UserRole.Role.ADMIN,
                isApproved = true
        )

        val user = testUtil.createUser("rpim2@ycp.edu")
        roles = testUtil.createUserRolesForUser(
                userId = user.id,
                role = UserRole.Role.GUEST,
                isApproved = false
        )
    }

    @Test
    fun testSuccessResponse() {
        // Test the success scenario to ensure
        // the result object is correctly set

        // Simulate a login
        getContext().login(admin.id)
        getContext().currentRoles.addAll(listOf(UserRole.Role.ADMIN, UserRole.Role.USER))

        val result = adminUserRoleValidateController.execute(
                validateForm = ValidateForm(
                        id = roles[1].id,
                        role = roles[1].role
                )
        )

        assertNotNull(result)
        assertEquals(roles[1].id, result.data)
        assertEquals(HttpStatus.OK, result.status)
        assertNull(result.errors)

        // Ensure a log record was created
        assertEquals(1, logRepo.count())
    }

    @Test
    fun testFailedRequiredFieldResponse() {
        // Simulate a login
        getContext().login(admin.id)
        getContext().currentRoles.addAll(listOf(UserRole.Role.ADMIN, UserRole.Role.USER))

        val result = adminUserRoleValidateController.execute(
                validateForm = ValidateForm(
                        id = null,
                        role = roles[1].role
                )
        )
        assertNotNull(result)
        assertNull(result.data)
        assertEquals(HttpStatus.BAD_REQUEST, result.status)
        assertNotNull(result.errors)

        // Ensure a log record was created
        assertEquals(1, logRepo.count())
    }

    @Test
    fun testFailedValidationResponse() {
        // Simulate a login
        getContext().login(admin.id)
        getContext().currentRoles.addAll(listOf(UserRole.Role.ADMIN, UserRole.Role.USER))

        val result = adminUserRoleValidateController.execute(
                validateForm = ValidateForm(
                        id = 123456789,
                        role = roles[1].role
                )
        )
        assertNotNull(result)
        assertNull(result.data)
        assertEquals(HttpStatus.BAD_REQUEST, result.status)
        assertNotNull(result.errors)

        // Ensure a log record was created
        assertEquals(1, logRepo.count())
    }

    @Test
    fun testFailedAuthenticationResponse() {
        // Do not log the user in
        val result = adminUserRoleValidateController.execute(
                validateForm = ValidateForm(
                        id = roles[1].id,
                        role = roles[1].role
                )
        )
        assertNotNull(result)
        assertNull(result.data)
        assertEquals(HttpStatus.FORBIDDEN, result.status)
        assertNotNull(result.errors)

        // Ensure a log record was created
        assertEquals(1, logRepo.count())
    }
}