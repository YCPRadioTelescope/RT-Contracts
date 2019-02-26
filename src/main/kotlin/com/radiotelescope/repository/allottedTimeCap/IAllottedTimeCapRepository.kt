package com.radiotelescope.repository.allottedTimeCap

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

/**
 * Spring Repository for the [AllottedTimeCap] Entity
 */
@Repository
interface IAllottedTimeCapRepository: CrudRepository<AllottedTimeCap, Long>{

    /**
     * Spring Repository method that will find the [AllottedTimeCap] record for
     * a user
     *
     * @param userId the User id
     * @return the [AllottedTimeCap] object
     */
    fun findByUserId(userId: Long): AllottedTimeCap
}