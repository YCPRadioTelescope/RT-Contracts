package com.radiotelescope.contracts.appointment

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.radiotelescope.contracts.Command
import com.radiotelescope.contracts.SimpleResult
import com.radiotelescope.repository.appointment.Appointment
import com.radiotelescope.repository.appointment.IAppointmentRepository

/**
 * Override of the [Command] interface used to approve or deny appointment requests.
 *
 * @param request the [ApproveDenyRequest.Request] data class
 * @param appointmentRepo the [IAppointmentRepository] interface
 */
class ApproveDenyRequest(
        private val request: Request,
        private val appointmentRepo: IAppointmentRepository
) : Command<Long, Multimap<ErrorTag, String>> {
    /**
     * Override of the [Command.execute] method. Calls the [validateRequest] method
     * that will handle all constraint checking and validation.
     *
     * If validation passes, it will set [Appointment.status] to Scheduled
     * and return the id in the [SimpleResult] object.
     *
     * If validation fails, it will return a [SimpleResult] with the errors
     */
    override fun execute(): SimpleResult<Long, Multimap<ErrorTag, String>> {
        validateRequest()?.let { return SimpleResult(null, it) } ?: let {
            val theAppointment = appointmentRepo.findById(request.appointmentId).get()
            if (request.isApprove)
                theAppointment.status = Appointment.Status.SCHEDULED
            else
                theAppointment.status = Appointment.Status.CANCELED
            appointmentRepo.save(theAppointment)
            return SimpleResult(theAppointment.id, null)
        }
    }

    /**
     * Method responsible for constraint checking and validations for the
     * appointment approve/deny request. It will ensure that the appointment exist
     * and it's status is REQUESTED
     */
    private fun validateRequest(): Multimap<ErrorTag, String>? {
        val errors = HashMultimap.create<ErrorTag,String>()

        with(request) {
            if (!appointmentRepo.existsById(appointmentId)) {
                errors.put(ErrorTag.ID, "Appointment does not exist")
                return errors
            }
            if(appointmentRepo.findById(appointmentId).get().status != Appointment.Status.REQUESTED)
                errors.put(ErrorTag.STATUS, "Appointment $appointmentId status is not requested")
            if(isApprove)
                if (isOverlap())
                    errors.put(ErrorTag.OVERLAP, "Appointment time is conflicted with another appointment")
        }
        return if (errors.isEmpty) null else errors
    }

    /**
     * Method responsible for check if the requested appointment
     * conflict with the one that are already scheduled
     */
    private fun isOverlap(): Boolean {
        val appointment = appointmentRepo.findById(request.appointmentId).get()
        var isOverlap = false
        val appointmentList = appointmentRepo.findConflict(
                endTime = appointment.endTime,
                startTime = appointment.startTime,
                telescopeId = appointment.telescopeId,
                priority = appointment.priority.toString()
        )

        if (!appointmentList.isEmpty()) {
            isOverlap = true
        }

        return isOverlap
    }

    /**
     * Data class containing all fields necessary for approving and denying
     * an appointment
     */
    data class Request(
        val appointmentId: Long,
        val isApprove: Boolean
    )
}