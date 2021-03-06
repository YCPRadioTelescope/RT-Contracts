package com.radiotelescope.contracts.celestialBody

import com.google.common.collect.Multimap
import com.radiotelescope.contracts.Command
import com.radiotelescope.repository.celestialBody.ICelestialBodyRepository
import com.radiotelescope.repository.coordinate.ICoordinateRepository
import com.radiotelescope.repository.model.celestialBody.SearchCriteria
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * Base concrete implementation of the [CelestialBodyFactory] interface
 *
 * @param celestialBodyRepo the [ICelestialBodyRepository] interface
 * @param coordinateRepo the [ICoordinateRepository] interface
 */
class BaseCelestialBodyFactory(
        private val celestialBodyRepo: ICelestialBodyRepository,
        private val coordinateRepo: ICoordinateRepository
) : CelestialBodyFactory {
    /**
     * Override of the [CelestialBodyFactory.create] method that will return a [Create] command object
     *
     * @param request the [Create.Request] object
     * @return a [Create] command object
     */
    override fun create(request: Create.Request): Command<Long, Multimap<ErrorTag, String>> {
        return Create(
                request = request,
                coordinateRepo = coordinateRepo,
                celestialBodyRepo = celestialBodyRepo
        )
    }

    /**
     * Override of the [CelestialBodyFactory.retrieve] method that will return a [Retrieve] command object
     *
     * @param id the Celestial Body id
     * @return a [Retrieve] command object
     */
    override fun retrieve(id: Long): Command<CelestialBodyInfo, Multimap<ErrorTag, String>> {
        return Retrieve(
                id = id,
                celestialBodyRepo = celestialBodyRepo
        )
    }

    /**
     * Override of the [CelestialBodyFactory.list] method that will return a [List] command object
     *
     * @param pageable the [Pageable] object
     * @return a [List] command object
     */
    override fun list(pageable: Pageable): Command<Page<CelestialBodyInfo>, Multimap<ErrorTag, String>> {
        return List(
                pageable = pageable,
                celestialBodyRepo = celestialBodyRepo
        )
    }

    /**
     * Override of the [CelestialBodyFactory.search] method that will return a [Search] command
     *
     * @param searchCriteria the [SearchCriteria]
     * @param pageable the [Pageable] object
     * @return a [Search] command object
     */
    override fun search(searchCriteria: kotlin.collections.List<SearchCriteria>, pageable: Pageable): Command<Page<CelestialBodyInfo>, Multimap<ErrorTag, String>> {
        return Search(
                searchCriteria = searchCriteria,
                pageable = pageable,
                celestialBodyRepo = celestialBodyRepo
        )
    }

    /**
     * Override of the [CelestialBodyFactory.update] method that will return an [Update] command
     *
     * @param request the [Update.Request] object
     * @return an [Update] command object
     */
    override fun update(request: Update.Request): Command<Long, Multimap<ErrorTag, String>> {
        return Update(
                request = request,
                coordinateRepo = coordinateRepo,
                celestialBodyRepo = celestialBodyRepo
        )
    }
}