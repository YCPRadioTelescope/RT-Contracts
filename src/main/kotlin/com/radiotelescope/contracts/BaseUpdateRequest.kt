package com.radiotelescope.contracts

/**
 * Each SQL Update operation will need to use this. It will take a request
 * and adapt it into an updated version of the persisted entity
 */
internal interface BaseUpdateRequest<ENTITY> {
    fun updateEntity(entity: ENTITY): ENTITY
}