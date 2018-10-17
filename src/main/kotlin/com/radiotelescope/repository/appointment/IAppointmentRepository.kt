package com.radiotelescope.repository.appointment

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/*
Spring Repository Interface for the Appointment Entity
 */
@Repository
interface IAppointmentRepository : PagingAndSortingRepository<Appointment, Long> {
    @Query(value = "SELECT * " +
            "FROM appointment " +
            "WHERE user_id=?1 AND end_time > CURRENT_TIMESTAMP() " +
            "AND status <> 'Canceled'",
            countQuery = "SELECT COUNT(*) " +
                    "FROM appointment " +
                    "WHERE user_id=?1 AND end_time > CURRENT_TIMESTAMP() " +
                    "AND status <> 'Canceled'",
            nativeQuery = true)
    fun findFutureAppointmentsByUser(userId: Long, pageable: Pageable): Page<Appointment>

    @Query(value = "select * from appointment where user_id=?1 AND end_time < CURRENT_TIMESTAMP", countQuery = "select count(*) from appointment where user_id=?1 and end_time < current_timestamp", nativeQuery = true)
    fun findPreviousAppointmentsByUser(userId: Long, pageable:Pageable): Page<Appointment>

    @Query(value ="select * from appointment where telescope_id = ?1", nativeQuery = true)
    fun retrieveAppointmentsByTelescopeId(tele_id: Long, pageable:Pageable): Page<Appointment>
}