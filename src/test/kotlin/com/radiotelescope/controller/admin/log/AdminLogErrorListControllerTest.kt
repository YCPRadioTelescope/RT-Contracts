package com.radiotelescope.controller.admin.log

import com.google.common.collect.HashMultimap
import com.radiotelescope.contracts.log.ErrorTag
import com.radiotelescope.repository.log.ILogRepository
import com.radiotelescope.repository.log.Log
import com.radiotelescope.repository.role.UserRole
import com.radiotelescope.repository.user.User
import com.radiotelescope.toStringMap
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner
import java.util.*

@DataJpaTest
@RunWith(SpringRunner::class)
internal class AdminLogErrorListControllerTest : BaseLogRestControllerTest() {
    @Autowired
    private lateinit var logRepo: ILogRepository

    private lateinit var adminLogErrorListController: AdminLogErrorListController
    private lateinit var admin: User
    private lateinit var log: Log

    @Before
    override fun init() {
        super.init()

        adminLogErrorListController = AdminLogErrorListController(
                logWrapper = getWrapper(),
                logger = getLogger()
        )

        admin = testUtil.createUser("rpim@ycp.edu")
        testUtil.createUserRolesForUser(
                user = admin,
                role = UserRole.Role.ADMIN,
                isApproved = true
        )

        val errors = HashMultimap.create<ErrorTag, String>()
        errors.put(ErrorTag.SUCCESS, "Create Error Log")
        log = testUtil.createErrorLog(
                user = admin,
                action = "Creating log",
                affectedRecordId = null,
                affectedTable = Log.AffectedTable.LOG,
                timestamp = Date(System.currentTimeMillis()),
                isSuccess = false,
                errors = errors.toStringMap()
        )
    }

    @Test
    fun testSuccessResponse() {
        // Test the success scenario to ensure
        // the result object is correctly set

        // Simulate a login
        getContext().login(admin.id)
        getContext().currentRoles.addAll(listOf(UserRole.Role.ADMIN, UserRole.Role.USER))

        val result = adminLogErrorListController.execute(
                logId = log.id
        )

        assertNotNull(result)
        assertTrue(result.data is List<*>)
        assertEquals(HttpStatus.OK, result.status)
        assertNull(result.errors)
    }

    @Test
    fun testFailedValidationResponse() {
        // Simulate a login
        getContext().login(admin.id)
        getContext().currentRoles.addAll(listOf(UserRole.Role.ADMIN, UserRole.Role.USER))

        val result = adminLogErrorListController.execute(
                logId = 123456789
        )
        assertNotNull(result)
        assertNull(result.data)
        assertEquals(HttpStatus.BAD_REQUEST, result.status)
        assertNotNull(result.errors)
    }

    @Test
    fun testFailedAuthenticationResponse() {
        // Do not log the user in
        val result = adminLogErrorListController.execute(
                logId = log.id
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