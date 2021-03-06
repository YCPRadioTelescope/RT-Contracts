package com.radiotelescope.controller.admin.log

import com.radiotelescope.repository.log.ILogRepository
import com.radiotelescope.repository.log.Log
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
import java.util.*

@DataJpaTest
@RunWith(SpringRunner::class)
internal class AdminLogListControllerTest : BaseLogRestControllerTest() {
    @Autowired
    private lateinit var logRepo: ILogRepository

    private lateinit var adminLogListController: AdminLogListController
    private lateinit var admin: User
    private lateinit var log: Log

    @Before
    override fun init() {
        super.init()

        adminLogListController = AdminLogListController(
                logWrapper = getWrapper(),
                logger = getLogger()
        )

        admin = testUtil.createUser("rpim@ycp.edu")
        testUtil.createUserRolesForUser(
                user = admin,
                role = UserRole.Role.ADMIN,
                isApproved = true
        )

        log = testUtil.createLog(
                user = admin,
                action = "Creating log",
                affectedRecordId = null,
                affectedTable = Log.AffectedTable.LOG,
                timestamp = Date(System.currentTimeMillis()),
                isSuccess = true
        )
    }

    @Test
    fun testSuccessResponse() {
        // Test the success scenario to ensure
        // the result object is correctly set

        // Simulate a login
        getContext().login(admin.id)
        getContext().currentRoles.addAll(listOf(UserRole.Role.ADMIN, UserRole.Role.USER))

        val result = adminLogListController.execute(
                pageNumber = 0,
                pageSize = 10
        )

        assertNotNull(result)
        assertTrue(result.data is Page<*>)
        assertEquals(HttpStatus.OK, result.status)
        assertNull(result.errors)
    }

    @Test
    fun testFailedRequiredFieldResponse() {
        // Simulate a login
        getContext().login(admin.id)
        getContext().currentRoles.addAll(listOf(UserRole.Role.ADMIN, UserRole.Role.USER))

        val result = adminLogListController.execute(
                pageNumber = null,
                pageSize = 10
        )
        assertNotNull(result)
        assertNull(result.data)
        assertEquals(HttpStatus.BAD_REQUEST, result.status)
        assertNotNull(result.errors)

        // Ensure a log record was created
        assertEquals(2, logRepo.count())

        logRepo.findAll().forEach {
            if (it.id != log.id) {
                assertEquals(HttpStatus.BAD_REQUEST.value(), it.status)
            }
        }
    }

    @Test
    fun testFailedValidationResponse() {
        // Simulate a login
        getContext().login(admin.id)
        getContext().currentRoles.addAll(listOf(UserRole.Role.ADMIN, UserRole.Role.USER))

        val result = adminLogListController.execute(
                pageNumber = -1,
                pageSize = 10
        )
        assertNotNull(result)
        assertNull(result.data)
        assertEquals(HttpStatus.BAD_REQUEST, result.status)
        assertNotNull(result.errors)

        // Ensure a log record was created
        assertEquals(2, logRepo.count())

        logRepo.findAll().forEach {
            if (it.id != log.id) {
                assertEquals(HttpStatus.BAD_REQUEST.value(), it.status)
            }
        }
    }

    @Test
    fun testFailedAuthenticationResponse() {
        // Do not log the user in
        val result = adminLogListController.execute(
                pageNumber = 0,
                pageSize = 10
        )
        assertNotNull(result)
        assertNull(result.data)
        assertEquals(HttpStatus.FORBIDDEN, result.status)
        assertNotNull(result.errors)

        // Ensure a log record was created
        assertEquals(2, logRepo.count())

        logRepo.findAll().forEach {
            if (it.id != log.id) {
                assertEquals(HttpStatus.FORBIDDEN.value(), it.status)
            }
        }
    }
}