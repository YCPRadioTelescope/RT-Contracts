package com.radiotelescope.controller.admin.role

import com.radiotelescope.controller.user.role.BaseUserRoleControllerTest
import com.radiotelescope.repository.appointment.Appointment
import com.radiotelescope.repository.log.ILogRepository
import com.radiotelescope.repository.role.UserRole
import com.radiotelescope.repository.user.User
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner

@DataJpaTest
@RunWith(SpringRunner::class)
internal class AdminUnapprovedUserRoleListControllerTest : BaseUserRoleControllerTest() {
    @Autowired
    private lateinit var logRepo: ILogRepository

    private lateinit var adminUnapprovedUserRoleListController: AdminUnapprovedUserRoleListController
    private lateinit var admin: User

    @Before
    override fun init() {
        super.init()

        adminUnapprovedUserRoleListController = AdminUnapprovedUserRoleListController(
                roleWrapper = getWrapper(),
                logger = getLogger()
        )

        admin = testUtil.createUser("rpim@ycp.edu")
        testUtil.createUserRolesForUser(
                user = admin,
                role = UserRole.Role.ADMIN,
                isApproved = true
        )
        testUtil.createAllottedTimeCapForUser(
                user = admin,
                allottedTime = null
        )

        val user = testUtil.createUser("rpim2@ycp.edu")
        testUtil.createUserRolesForUser(
                user = user,
                role = UserRole.Role.GUEST,
                isApproved = false
        )
        testUtil.createAllottedTimeCapForUser(
                user = user,
                allottedTime = Appointment.GUEST_APPOINTMENT_TIME_CAP
        )
    }

    @Test
    fun testSuccessResponse() {
        // Test the success scenario to ensure
        // the result object is correctly set

        // Simulate a login
        getContext().login(admin.id)
        getContext().currentRoles.addAll(listOf(UserRole.Role.ADMIN, UserRole.Role.USER))

        val result = adminUnapprovedUserRoleListController.execute(
                pageNumber = 0,
                pageSize = 10
        )

        assertNotNull(result)
        assertTrue(result.data is Page<*>)
        assertEquals(HttpStatus.OK, result.status)
        assertNull(result.errors)

        // Ensure a log record was created
        assertEquals(1, logRepo.count())

        logRepo.findAll().forEach {
            assertEquals(HttpStatus.OK.value(), it.status)
        }
    }

    @Test
    fun testFailedRequiredFieldResponse() {
        // Simulate a login
        getContext().login(admin.id)
        getContext().currentRoles.addAll(listOf(UserRole.Role.ADMIN, UserRole.Role.USER))

        val result = adminUnapprovedUserRoleListController.execute(
                pageNumber = null,
                pageSize = null
        )
        assertNotNull(result)
        assertNull(result.data)
        assertEquals(HttpStatus.BAD_REQUEST, result.status)
        assertNotNull(result.errors)

        // Ensure a log record was created
        assertEquals(1, logRepo.count())

        logRepo.findAll().forEach {
            assertEquals(HttpStatus.BAD_REQUEST.value(), it.status)
        }
    }

    @Test
    fun testFailedValidationResponse() {
        // Simulate a login
        getContext().login(admin.id)
        getContext().currentRoles.addAll(listOf(UserRole.Role.ADMIN, UserRole.Role.USER))

        val result = adminUnapprovedUserRoleListController.execute(
                pageNumber = -1,
                pageSize = 0
        )
        assertNotNull(result)
        assertNull(result.data)
        assertEquals(HttpStatus.BAD_REQUEST, result.status)
        assertNotNull(result.errors)

        // Ensure a log record was created
        assertEquals(1, logRepo.count())

        logRepo.findAll().forEach {
            assertEquals(HttpStatus.BAD_REQUEST.value(), it.status)
        }
    }

    @Test
    fun testFailedAuthenticationResponse() {
        // Do not log the user in
        val result = adminUnapprovedUserRoleListController.execute(
                pageNumber = 0,
                pageSize = 10
        )
        assertNotNull(result)
        assertNull(result.data)
        assertEquals(HttpStatus.FORBIDDEN, result.status)
        assertNotNull(result.errors)

        // Ensure a log record was created
        assertEquals(1, logRepo.count())

        logRepo.findAll().forEach {
            assertEquals(HttpStatus.FORBIDDEN.value(), it.status)
        }
    }
}