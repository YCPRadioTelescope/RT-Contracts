package com.radiotelescope.contracts.appointment

import com.radiotelescope.TestUtil
import com.radiotelescope.repository.appointment.Appointment
import com.radiotelescope.repository.appointment.IAppointmentRepository
import com.radiotelescope.repository.user.IUserRepository
import com.radiotelescope.repository.user.User
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.util.*

@DataJpaTest
@RunWith(SpringRunner::class)
@ActiveProfiles(value = ["test"])
class ListFutureAppointmentByUserTest {
    @TestConfiguration
    internal class UtilTestContextConfiguration {
        @Bean
        fun utilService(): TestUtil { return TestUtil() }
    }

    @Autowired
    private lateinit var testUtil: TestUtil

    @Autowired
    private lateinit var userRepo: IUserRepository

    @Autowired
    private lateinit var appointmentRepo: IAppointmentRepository

    private val futureApptCreateRequest = Create.Request(
            startTime=Date(12012019120000),
            endTime = Date(12012019130011),
            isPublic = false,
            telescopeId = 1,
            userId = -1L
    )

    private lateinit var user1: User
    private lateinit var user2: User
    private var user1Id = -1L
    private var user2Id = -1L

    @Before
    fun init(){
        // Persist the users
        user1 = testUtil.createUser("rpim@ycp.edu")
        user2 = testUtil.createUser("rathanapim1@yahoo.com")

        // Set the userId
        user1Id = userRepo.findByEmail(user1.email)!!.id
        user2Id = userRepo.findByEmail(user2.email)!!.id

        // Persist the appointment
        testUtil.createAppointment(
                user = user1,
                telescopeId = futureApptCreateRequest.telescopeId,
                status = Appointment.Status.Requested,
                startTime = futureApptCreateRequest.startTime,
                endTime = futureApptCreateRequest.endTime,
                isPublic = futureApptCreateRequest.isPublic
        )



    }

    @Test
    fun testValid_UserHasOneFutureAppointment_Success(){
        val (infoPage, error) = ListFutureAppointmentByUser(
                userId = user1Id,
                pageable = PageRequest.of(0, 10),
                appointmentRepo = appointmentRepo,
                userRepo = userRepo
        ).execute()

        // Should not have failed
        assertNull(error)
        assertNotNull(infoPage)

        // should only have 1 AppointmentInfo
        assertEquals(1, infoPage!!.content.size)
    }

    // Should only grab the future appointment
    @Test
    fun testValid_UserHasOneFutureAndOnePastAppointment_Success(){
        val (infoPage, error) = ListFutureAppointmentByUser(
                userId = user1Id,
                pageable = PageRequest.of(0, 10),
                appointmentRepo = appointmentRepo,
                userRepo = userRepo
        ).execute()

        // Should not have failed
        assertNull(error)
        assertNotNull(infoPage)

        // should only have 1 AppointmentInfo
        assertEquals(infoPage!!.content.size, 1)

        // should be the future appointment
        assertTrue(Date().before(infoPage.content.get(0).endTime))
    }

    @Test
    fun testInvalid_NoApptWithSpecifiedUserId_Success(){
        val (infoPage, error) = ListFutureAppointmentByUser(
                userId = user2Id,
                pageable = PageRequest.of(1, 10),
                appointmentRepo = appointmentRepo,
                userRepo = userRepo
        ).execute()

        // Should not have failed and return empty infoPage
        assertNull(error)
        assertNotNull(infoPage)

        // Ensure it does not have any content
        assertFalse(infoPage!!.hasContent())
    }

    @Test
    fun testInvalid_NoUserWithSpecifiedUserId_Failure(){
        val (infoPage, error) = ListFutureAppointmentByUser(
                userId = 123456789,
                pageable = PageRequest.of(1, 10),
                appointmentRepo = appointmentRepo,
                userRepo = userRepo
        ).execute()

        // Should not have failed and return empty infoPage
        assertNotNull(error)
        assertNull(infoPage)

        // Ensure it failed because of the userId
        assertTrue(error!![ErrorTag.USER_ID].isNotEmpty())
    }


    @Test
    fun getInfo() {
        var page: Page<AppointmentInfo> = ListFutureAppointmentByUser(user1Id, PageRequest.of(1, 10), appointmentRepo, userRepo).execute().success!!

        if (page.hasContent())
            println("page has content")
        else println("page does not have content")


        for (a in page)
        {
         println("startTime is: " +a.startTime)
        }


    }

}