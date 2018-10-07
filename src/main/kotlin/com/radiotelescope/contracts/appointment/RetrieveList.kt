package com.radiotelescope.contracts.appointment

import com.radiotelescope.contracts.Command
import com.radiotelescope.repository.user.IUserRepository
import com.radiotelescope.repository.appointment.IAppointmentRepository
import com.radiotelescope.repository.appointment.Appointment
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.radiotelescope.contracts.SimpleResult

//Only to be for past appointments

class RetrieveList(
       private val apptRepo: IAppointmentRepository,
        private val userId:Long,
        private val userRepo: IUserRepository
):Command <Long, Multimap<ErrorTag, String>> {
    override fun execute(): SimpleResult<Long, Multimap<ErrorTag, String>> {
        val errors = HashMultimap.create<ErrorTag, String>()
        //check if the user exists
        if (!userRepo.existsById(userId)) {
            errors.put(ErrorTag.USER_ID, "User with id ${userId} does not exist")
            return SimpleResult(userId, errors)
            //Success case
        } else {
           val apptList = userRepo.findByUser()
            for (appt: Appointment in apptList) {
                val apptInfo = AppointmentInfo(appt)
                val apptSaved : Appointment = apptRepo.save(appt)
            }
            return SimpleResult(userId, null)
        }
    }
}