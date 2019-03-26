package com.radiotelescope.contracts.appointment.factory.manual

import com.google.common.collect.Multimap
import com.radiotelescope.contracts.Command
import com.radiotelescope.contracts.appointment.ErrorTag
import com.radiotelescope.contracts.appointment.factory.BaseAppointmentFactory
import com.radiotelescope.contracts.appointment.manual.AddFreeControlAppointmentCommand
import com.radiotelescope.contracts.appointment.manual.StartFreeControlAppointment
import com.radiotelescope.repository.allottedTimeCap.IAllottedTimeCapRepository
import com.radiotelescope.repository.appointment.IAppointmentRepository
import com.radiotelescope.repository.coordinate.ICoordinateRepository
import com.radiotelescope.repository.role.IUserRoleRepository
import com.radiotelescope.repository.telescope.ITelescopeRepository
import com.radiotelescope.repository.user.IUserRepository

/**
 * Concrete implementation of the [ManualAppointmentFactory] for Free Control Appointments
 */
class FreeControlAppointmentFactory(
        private val appointmentRepo: IAppointmentRepository,
        private val userRepo: IUserRepository,
        private val telescopeRepo: ITelescopeRepository,
        private val coordinateRepo: ICoordinateRepository,
        userRoleRepo: IUserRoleRepository,
        allottedTimeCapRepo: IAllottedTimeCapRepository
) : ManualAppointmentFactory, BaseAppointmentFactory(
        appointmentRepo = appointmentRepo,
        userRepo = userRepo,
        telescopeRepo = telescopeRepo,
        userRoleRepo = userRoleRepo,
        allottedTimeCapRepo = allottedTimeCapRepo
) {
    /**
     * Override of the [ManualAppointmentFactory.startAppointment] method that will return a [StartFreeControlAppointment]
     * command object
     *
     * @param request the [StartFreeControlAppointment.Request] object
     * @return a [StartFreeControlAppointment] command
     */
    override fun startAppointment(request: StartFreeControlAppointment.Request): Command<Long, Multimap<ErrorTag, String>> {
        return StartFreeControlAppointment(
                request = request,
                appointmentRepo = appointmentRepo,
                telescopeRepo = telescopeRepo,
                userRepo = userRepo,
                coordinateRepo = coordinateRepo
        )
    }

    /**
     * Override of the [ManualAppointmentFactory.addCommand] method that will return a [AddFreeControlAppointmentCommand]
     * command object
     *
     * @param request the [AddFreeControlAppointmentCommand.Request] object
     * @return an [AddFreeControlAppointmentCommand] command
     */
    override fun addCommand(request: AddFreeControlAppointmentCommand.Request): Command<Long, Multimap<ErrorTag, String>> {
        return AddFreeControlAppointmentCommand(
                request = request,
                appointmentRepo = appointmentRepo,
                coordinateRepo = coordinateRepo
        )
    }
}