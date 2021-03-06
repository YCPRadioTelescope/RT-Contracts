package com.radiotelescope.contracts.appointment

import com.radiotelescope.AbstractSpringTest
import com.radiotelescope.repository.appointment.Appointment
import com.radiotelescope.repository.appointment.IAppointmentRepository
import com.radiotelescope.repository.telescope.IRadioTelescopeRepository
import com.radiotelescope.repository.user.IUserRepository
import com.radiotelescope.repository.user.User
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.junit4.SpringRunner
import java.util.*

@DataJpaTest
@RunWith(SpringRunner::class)
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = ["classpath:sql/seedTelescope.sql"])
internal class ListBetweenDatesTest : AbstractSpringTest() {
    @Autowired
    private lateinit var userRepo: IUserRepository

    @Autowired
    private lateinit var radioTelescopeRepo: IRadioTelescopeRepository

    @Autowired
    private lateinit var appointmentRepo: IAppointmentRepository

    private lateinit var user1: User
    private lateinit var user2: User
    private var user1Id = -1L
    private var user2Id = -1L

    private var startTime = System.currentTimeMillis() + 10000L
    private var endTime =   System.currentTimeMillis() + 50000L

    @Before
    fun init(){
        // Persist the users
        user1 = testUtil.createUser("rpim@ycp.edu")
        user2 = testUtil.createUser("rpim1@ycp.edu")

        // Set the userId
        user1Id = userRepo.findByEmail(user1.email)!!.id
        user2Id = userRepo.findByEmail(user2.email)!!.id

        // Persist the appointments
        testUtil.createAppointment(
                user = user1,
                telescopeId = 1,
                status = Appointment.Status.SCHEDULED,
                startTime = Date(startTime + 100L),
                endTime = Date(startTime + 200L),
                isPublic = true,
                priority = Appointment.Priority.PRIMARY,
                type = Appointment.Type.POINT
        )

        testUtil.createAppointment(
                user = user2,
                telescopeId = 1,
                status = Appointment.Status.SCHEDULED,
                startTime = Date(startTime + 300L),
                endTime = Date(startTime + 400L),
                isPublic = true,
                priority = Appointment.Priority.PRIMARY,
                type = Appointment.Type.POINT
        )
    }


    @Test
    fun testValid_ValidConstraints_Success(){
        val (infoList, error) = ListBetweenDates(
                request = ListBetweenDates.Request(
                    startTime = Date(startTime),
                    endTime = Date(endTime),
                    telescopeId = 1L
                ),
                appointmentRepo = appointmentRepo,
                radioTelescopeRepo = radioTelescopeRepo
        ).execute()

        // Should not have failed
        Assert.assertNull(error)
        Assert.assertNotNull(infoList)

        // should have 2 AppointmentInfo
        Assert.assertEquals(2, infoList!!.size)
    }

    @Test
    fun testInvalid_EndTimeIsLessThanStartTime_Failure() {
        val (infoList, error) = ListBetweenDates(
                request = ListBetweenDates.Request(
                        startTime = Date(endTime),
                        endTime = Date(startTime),
                        telescopeId = 1L
                ),
                appointmentRepo = appointmentRepo,
                radioTelescopeRepo = radioTelescopeRepo
        ).execute()

        // Should have failed
        Assert.assertNotNull(error)
        Assert.assertNull(infoList)

        // Ensure it failed because of the endTime
        Assert.assertTrue(error!![ErrorTag.END_TIME].isNotEmpty())
    }

    @Test
    fun testInvalid_InvalidTelescopeId_Failure() {
        val (infoList, error) = ListBetweenDates(
                request = ListBetweenDates.Request(
                        startTime = Date(startTime),
                        endTime = Date(endTime),
                        telescopeId = 420L
                ),
                appointmentRepo = appointmentRepo,
                radioTelescopeRepo = radioTelescopeRepo
        ).execute()

        // Should have failed
        Assert.assertNotNull(error)
        Assert.assertNull(infoList)

        // Ensure it failed because of the endTime
        Assert.assertTrue(error!![ErrorTag.TELESCOPE_ID].isNotEmpty())
    }
}