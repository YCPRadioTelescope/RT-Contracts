package com.radiotelescope.contracts.rfdata

import com.radiotelescope.TestUtil
import com.radiotelescope.repository.rfdata.IRFDataRepository
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.junit4.SpringRunner
import java.util.*

@DataJpaTest
@RunWith(SpringRunner::class)
@ActiveProfiles(value = ["test"])
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = ["classpath:sql/seedAppointmentData.sql"])
internal class RFDataInfoTest {
    @TestConfiguration
    class UtilTestContextConfiguration {
        @Bean
        fun utilService(): TestUtil { return TestUtil() }
    }

    @Autowired
    private lateinit var rfDataRepository: IRFDataRepository

    @Test
    fun testPrimaryConstructor() {
        val date = Date()

        val rfDataInfo = RFDataInfo(
                id = 1L,
                appointmentId = 1L,
                intensity = 45762L,
                timeCaptured = date
        )

        assertEquals(1L, rfDataInfo.id)
        assertEquals(1L, rfDataInfo.appointmentId)
        assertEquals(45762L, rfDataInfo.intensity)
        assertEquals(date, rfDataInfo.timeCaptured)
    }

    @Test
    fun testSecondaryConstructor() {
        val rfData = rfDataRepository.findById(1L)

        assertTrue(rfData.isPresent)

        val rfDataInfo = RFDataInfo(
                rfData = rfData.get()
        )

        assertEquals(rfDataInfo.id, rfData.get().id)
        assertEquals(rfDataInfo.appointmentId, rfData.get().appointment.id)
        assertEquals(rfDataInfo.intensity, rfData.get().intensity!!)
        assertEquals(rfDataInfo.timeCaptured, rfData.get().timeCaptured)
    }
}