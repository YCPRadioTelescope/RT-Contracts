package com.radiotelescope.controller.viewer

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
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner
import java.util.*

@DataJpaTest
@RunWith(SpringRunner::class)
internal class ViewerUnsharePrivateAppointmentTest : BaseViewerRestControllerTest() {
    @Autowired
    private lateinit var logRepo: ILogRepository

    private lateinit var viewerUnSharePrivateController: ViewerUnsharePrivateController
    private lateinit var user: User
    private lateinit var researcher: User
    private lateinit var appointment: Appointment

    @Before
    override fun init() {
        super.init()

        viewerUnSharePrivateController = ViewerUnsharePrivateController(
                viewerWrapper = getWrapper(),
                logger = getLogger()
        )

        user = testUtil.createUser("rpim@ycp.edu")
        researcher = testUtil.createUser("rpim1@ycp.edu")

        appointment = testUtil.createAppointment(
                user = researcher,
                startTime = Date(System.currentTimeMillis() + 100000L),
                endTime = Date(System.currentTimeMillis()  +  200000L),
                status = Appointment.Status.SCHEDULED,
                isPublic = false,
                priority = Appointment.Priority.PRIMARY,
                telescopeId = 1L,
                type = Appointment.Type.POINT
        )

        testUtil.createViewer(user, appointment)
    }

    @Test
    fun testSuccessResponse() {
        // Test the success scenario to ensure
        // the result object is correctly set

        // Simulate a login
        getContext().login(researcher.id)
        getContext().currentRoles.addAll(listOf(UserRole.Role.RESEARCHER, UserRole.Role.USER))

        val result = viewerUnSharePrivateController.execute(
                appointmentId = appointment.id,
                userId = user.id
        )

        assertNotNull(result)
        assertTrue(result.data is Long)
        assertEquals(HttpStatus.OK, result.status)
        assertNull(result.errors)

        // Ensure a log record was created
        assertEquals(1, logRepo.count())
    }

    @Test
    fun testFailedValidationResponse() {
        // Test the failure scenario to ensure
        // the result object is correctly set

        // Simulate a login
        getContext().login(researcher.id)
        getContext().currentRoles.addAll(listOf(UserRole.Role.RESEARCHER, UserRole.Role.USER))

        val result = viewerUnSharePrivateController.execute(
                appointmentId = appointment.id,
                userId = 123456L
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
        // Test the failure scenario to ensure
        // the result object is correctly set

        // Do not simulate a login

        val result = viewerUnSharePrivateController.execute(
                appointmentId = appointment.id,
                userId = user.id
        )

        assertNotNull(result)
        assertNull(result.data)
        assertEquals(HttpStatus.FORBIDDEN, result.status)
        assertNotNull(result.errors)

        // Ensure a log record was created
        assertEquals(1, logRepo.count())
    }

    @Test
    fun testInvalidResourceIdResponse() {
        // Test the failure scenario where the
        // appointment id does not exist
        val result = viewerUnSharePrivateController.execute(
                appointmentId = 311L,
                userId = user.id
        )

        assertNotNull(result)
        assertNull(result.data)
        assertEquals(HttpStatus.NOT_FOUND, result.status)
        assertNotNull(result.errors)

        // Ensure a log record was created
        assertEquals(1, logRepo.count())
    }
}