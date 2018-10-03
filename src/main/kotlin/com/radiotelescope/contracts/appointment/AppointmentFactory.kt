package com.radiotelescope.contracts.appointment;

import com.google.common.collect.Multimap
import com.radiotelescope.repository.appointment

import com.radiotelescope.contracts.Command
import com.radiotelescope.repository.user.User


/*
So for the Appointment entity we have the findByAppointmentId and findByUsernameId command objects

 */

interface AppointmentFactory
{

    fun create(request: Create.Request):Command<Long, Multimap<ErrorTag, String>>

    fun delete(id:Long):Command<Long, Multimap<ErrorTag,String>>

    fun retrieve(id:Long):Command<Long, Multimap<ErrorTag,String>>

    fun retrieveList(u: User):Command <Long, Multimap<ErrorTag,String>>

    //For later
    /*
    fun retrieve(request: Validate.Request):Command

    fun update(request: Update.Request):Command
    */
}