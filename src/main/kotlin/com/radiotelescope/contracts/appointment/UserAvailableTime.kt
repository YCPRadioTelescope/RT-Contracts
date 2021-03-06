package com.radiotelescope.contracts.appointment

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.radiotelescope.contracts.Command
import com.radiotelescope.contracts.SimpleResult
import com.radiotelescope.repository.allottedTimeCap.IAllottedTimeCapRepository
import com.radiotelescope.repository.appointment.IAppointmentRepository
import com.radiotelescope.repository.role.IUserRoleRepository
import com.radiotelescope.repository.user.IUserRepository

/**
 * Override of the [Command] interface used to retrieve the amount
 * of available time for a user
 *
 * @param userId the User's Id
 * @param appointmentRepo the [IAppointmentRepository] interface
 * @param userRepo the [IUserRepository] interface
 * @param allottedTimeCapRepo the [IAllottedTimeCapRepository] interface
 */
class UserAvailableTime(
        private val userId: Long,
        private val appointmentRepo: IAppointmentRepository,
        private val userRepo: IUserRepository,
        private val userRoleRepo: IUserRoleRepository,
        private val allottedTimeCapRepo: IAllottedTimeCapRepository
) : Command<Long, Multimap<ErrorTag, String>> {
    /**
     * Override of the [Command] execute method. Calls the [validateRequest] method
     * that will handle all constraint checking and validations.
     *
     * If validation passes it will remaining time a user can use to schedule their appointment.
     *
     * If validation fails, it will will return the errors in a [SimpleResult.error]
     * value.
     */
    override fun execute(): SimpleResult<Long, Multimap<ErrorTag, String>> {
        val errors = validateRequest()

        if(!errors.isEmpty)
            return SimpleResult(null, errors)

        // If the user has no appointments, this will return null
        // so change it to 0 if null
        var totalTime = appointmentRepo.findTotalScheduledAppointmentTimeForUser(userId)
        totalTime = totalTime ?: 0

        // Get allotted time, if it's null, return it as null
        val allottedTime = allottedTimeCapRepo.findByUserId(userId).allottedTime ?: return SimpleResult(Long.MAX_VALUE, null)

        var availableTime = allottedTime - totalTime

        if(availableTime < 0)
            availableTime = 0

        return SimpleResult(availableTime, null)
    }

    /**
     * Method responsible for constraint checking and validations that
     * the user exist
     */
    private fun validateRequest(): Multimap<ErrorTag, String> {
        val errors = HashMultimap.create<ErrorTag, String>()

        if(!userRepo.existsById(userId))
            errors.put(ErrorTag.USER_ID, "User #$userId could not be found")
        if(userRoleRepo.findMembershipRoleByUserId(userId) == null)
            errors.put(ErrorTag.CATEGORY_OF_SERVICE, "User's Category of Service has not yet been approved")

        return errors
    }
}