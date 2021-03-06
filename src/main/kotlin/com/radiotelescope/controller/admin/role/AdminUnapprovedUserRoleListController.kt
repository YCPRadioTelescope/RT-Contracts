package com.radiotelescope.controller.admin.role

import com.google.common.collect.HashMultimap
import com.radiotelescope.contracts.role.ErrorTag
import com.radiotelescope.contracts.role.UserUserRoleWrapper
import com.radiotelescope.controller.BaseRestController
import com.radiotelescope.controller.model.Result
import com.radiotelescope.controller.spring.Logger
import com.radiotelescope.repository.log.Log
import com.radiotelescope.toStringMap
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller to handle retrieving a [Page] of unapproved user roles
 *
 * @param roleWrapper the [UserUserRoleWrapper] interface
 * @param logger the [Logger] service
 */
@RestController
class AdminUnapprovedUserRoleListController(
        private val roleWrapper: UserUserRoleWrapper,
        logger: Logger
) : BaseRestController(logger) {
    /**
     * Execute method that is in charge of returning a [Page]
     * of unapproved user roles. If the [PageRequest] does not
     * have valid parameters, it will respond with errors.
     * Otherwise, it will call the [UserUserRoleWrapper.unapprovedList]
     * method, and respond accordingly
     *
     * @param pageNumber the Page Number
     * @param pageSize the Page Size
     */
    @CrossOrigin(value = ["http://localhost:8081"])
    @GetMapping(value = ["/api/roles/unapproved"])
    fun execute(@RequestParam("page") pageNumber: Int?,
                @RequestParam("size") pageSize: Int?): Result {
        // If any of the request params are null, respond with errors
        if((pageNumber == null || pageNumber < 0) || (pageSize == null || pageSize <= 0)) {
            val errors = pageErrors()
            // Create error logs
            logger.createErrorLogs(
                    info = Logger.createInfo(
                            affectedTable = Log.AffectedTable.USER_ROLE,
                            action = "Retrieve Unapproved Role List",
                            affectedRecordId = null,
                            status = HttpStatus.BAD_REQUEST.value()
                    ),
                    errors = errors.toStringMap()
            )

            result = Result(errors = errors.toStringMap())
        }
        // Otherwise call the wrapper method
        else {
            roleWrapper.unapprovedList(PageRequest.of(pageNumber, pageSize)) {
                // NOTE: This command currently only has a success scenario
                // (given the user is authenticated)
                // If the command was a success
                it.success?.let { page ->
                    page.content.forEach { info ->
                        logger.createSuccessLog(
                                info = Logger.createInfo(
                                        affectedTable = Log.AffectedTable.USER_ROLE,
                                        action = "Retrieve Unapproved Role List",
                                        affectedRecordId = info.id,
                                        status = HttpStatus.OK.value()
                                )
                        )
                    }

                    result = Result(data = page)
                }
            }?.let {
                // If we get here, this means the User did not pass authentication
                // Create error logs
                logger.createErrorLogs(
                        info = Logger.createInfo(
                                affectedTable = Log.AffectedTable.USER_ROLE,
                                action = "Retrieve Unapproved Role List",
                                affectedRecordId = null,
                                status = HttpStatus.FORBIDDEN.value()
                        ),
                        errors = it.toStringMap()
                )

                result = Result(
                        errors = it.toStringMap(),
                        status = HttpStatus.FORBIDDEN
                )
            }
        }

        return result
    }

    /**
     * Private method that will return errors if any of the parameters
     * are not valid
     */
    private fun pageErrors(): HashMultimap<ErrorTag, String> {
        val errors = HashMultimap.create<ErrorTag, String>()
        errors.put(ErrorTag.PAGE_PARAMS, "Invalid Page parameters")
        return errors
    }
}