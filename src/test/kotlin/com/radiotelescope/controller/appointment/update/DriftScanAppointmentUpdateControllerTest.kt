package com.radiotelescope.controller.appointment.update

import com.radiotelescope.controller.appointment.BaseAppointmentRestControllerTest
import com.radiotelescope.controller.model.appointment.update.DriftScanAppointmentUpdateForm
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
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.junit4.SpringRunner
import java.util.*

@DataJpaTest
@RunWith(SpringRunner::class)
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = ["classpath:sql/seedTelescope.sql"])
internal class DriftScanAppointmentUpdateControllerTest : BaseAppointmentRestControllerTest() {

    @Autowired
    private lateinit var logRepo: ILogRepository

    private lateinit var driftScanAppointmentUpdateController: DriftScanAppointmentUpdateController
    private lateinit var user: User
    private lateinit var appointment: Appointment

    private lateinit var driftScanAppointmentUpdateForm: DriftScanAppointmentUpdateForm

    @Before
    override fun init() {
        super.init()

        driftScanAppointmentUpdateController = DriftScanAppointmentUpdateController(
                autoAppointmentWrapper = getDriftScanCreateWrapper(),
                logger = getLogger()
        )

        user = testUtil.createUser("rpim@ycp.edu")

        testUtil.createUserRolesForUser(
                user = user,
                role = UserRole.Role.RESEARCHER,
                isApproved = true
        )

        testUtil.createAllottedTimeCapForUser(
                user = user,
                allottedTime = null
        )

        appointment = testUtil.createAppointment(
                user = user,
                telescopeId = 1L,
                status = Appointment.Status.SCHEDULED,
                startTime = Date(System.currentTimeMillis() + 50000L),
                endTime = Date(System.currentTimeMillis() + 100000L),
                isPublic = true,
                priority = Appointment.Priority.PRIMARY,
                type = Appointment.Type.DRIFT_SCAN
        )

        driftScanAppointmentUpdateForm = DriftScanAppointmentUpdateForm(
                startTime = appointment.startTime,
                endTime = appointment.endTime,
                telescopeId = appointment.telescopeId,
                isPublic = appointment.isPublic,
                priority = appointment.priority,
                elevation = 90.0,
                azimuth =  180.0
        )
    }

    @Test
    fun testSuccessResponse() {
        // Simulate a login
        getContext().login(user.id)
        getContext().currentRoles.addAll(listOf(UserRole.Role.USER, UserRole.Role.RESEARCHER))

        val result = driftScanAppointmentUpdateController.execute(
                appointmentId = appointment.id,
                form = driftScanAppointmentUpdateForm
        )

        assertNotNull(result)
        assertTrue(result.data is Long)
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
        // Simulate a login
        getContext().login(user.id)
        getContext().currentRoles.addAll(listOf(UserRole.Role.USER, UserRole.Role.RESEARCHER))

        // Create a copy of the form
        val formCopy = driftScanAppointmentUpdateForm.copy(
                startTime = null
        )

        val result = driftScanAppointmentUpdateController.execute(
                appointmentId = appointment.id,
                form = formCopy
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
    fun testValidForm_FailedValidationResponse() {
        // Simulate a login
        getContext().login(user.id)
        getContext().currentRoles.addAll(listOf(UserRole.Role.USER, UserRole.Role.RESEARCHER))

        // Create a copy of the form
        val formCopy = driftScanAppointmentUpdateForm.copy(
                telescopeId = 420L
        )

        val result = driftScanAppointmentUpdateController.execute(
                appointmentId = appointment.id,
                form = formCopy
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
        val result = driftScanAppointmentUpdateController.execute(
                appointmentId = appointment.id,
                form = driftScanAppointmentUpdateForm
        )

        assertNotNull(result)
        assertNull(result.data)
        assertNotNull(result.errors)
        assertEquals(HttpStatus.FORBIDDEN, result.status)
        assertEquals(1, result.errors!!.size)

        // Ensure a log record was created
        assertEquals(1, logRepo.count())

        logRepo.findAll().forEach {
            assertEquals(HttpStatus.FORBIDDEN.value(), it.status)
        }
    }

    @Test
    fun testInvalidResourceIdResponse() {
        // Simulate a login
        getContext().login(user.id)
        getContext().currentRoles.addAll(listOf(UserRole.Role.USER, UserRole.Role.RESEARCHER))

        val result = driftScanAppointmentUpdateController.execute(
                appointmentId = 311L,
                form = driftScanAppointmentUpdateForm
        )

        assertNotNull(result)
        assertNull(result.data)
        assertNotNull(result.errors)
        assertEquals(HttpStatus.NOT_FOUND, result.status)
        assertEquals(1, result.errors!!.size)

        // Ensure a log record was created
        assertEquals(1, logRepo.count())

        logRepo.findAll().forEach {
            assertEquals(HttpStatus.NOT_FOUND.value(), it.status)
        }
    }


}