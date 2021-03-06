package com.radiotelescope.controller.model.appointment.create

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.radiotelescope.contracts.appointment.ErrorTag
import com.radiotelescope.contracts.appointment.create.CelestialBodyAppointmentCreate
import com.radiotelescope.controller.model.BaseForm
import com.radiotelescope.repository.appointment.Appointment
import java.util.*

/**
 * Create form that takes nullable versions of the [CelestialBodyAppointmentCreate.Request]
 * object. It is in charge of making sure these values are not null before adapting it to
 * a [CelestialBodyAppointmentCreate.Request] object.
 *
 * @param userId the User id
 * @param startTime the Appointment start time
 * @param endTime the Appointment end time
 * @param telescopeId the Telescope id
 * @param isPublic whether the Appointment is public or not
 * @param priority the Appointment priority
 * @param celestialBodyId the Celestial Body id
 */
data class CelestialBodyAppointmentCreateForm(
        override val userId: Long?,
        override val startTime: Date?,
        override val endTime: Date?,
        override val telescopeId: Long?,
        override val isPublic: Boolean?,
        override val priority: Appointment.Priority?,
        val celestialBodyId: Long?
) : CreateForm<CelestialBodyAppointmentCreate.Request>() {
    /**
     * Override of the [BaseForm.toRequest] method that adapts the
     * form into a [CelestialBodyAppointmentCreate.Request] object
     *
     * @return the [CelestialBodyAppointmentCreate.Request] object
     */
    override fun toRequest(): CelestialBodyAppointmentCreate.Request {
        return CelestialBodyAppointmentCreate.Request(
                userId = userId!!,
                startTime = startTime!!,
                endTime = endTime!!,
                telescopeId = telescopeId!!,
                isPublic = isPublic!!,
                priority = priority!!,
                celestialBodyId = celestialBodyId!!
        )
    }

    /**
     * Makes sure the required fields are not null
     *
     * @return a [HashMultimap] of errors or null
     */
    fun validateRequest(): Multimap<ErrorTag, String>? {
        val errors = HashMultimap.create<ErrorTag, String>()

        if (userId == null)
            errors.put(ErrorTag.USER_ID, "Invalid user id")
        if (startTime == null)
            errors.put(ErrorTag.START_TIME, "Required field")
        if (endTime == null)
            errors.put(ErrorTag.END_TIME, "Required field")
        if (telescopeId == null)
            errors.put(ErrorTag.TELESCOPE_ID, "Required field")
        if (isPublic == null)
            errors.put(ErrorTag.PUBLIC, "Required field")
        if (celestialBodyId == null)
            errors.put(ErrorTag.CELESTIAL_BODY, "Required field")
        if(priority == null)
            errors.put(ErrorTag.PRIORITY, "Required field")

        return if (errors.isEmpty) null else errors
    }
}