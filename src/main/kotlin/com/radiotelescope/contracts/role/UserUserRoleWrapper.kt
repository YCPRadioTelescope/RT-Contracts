package com.radiotelescope.contracts.role

import com.google.common.collect.Multimap
import com.radiotelescope.contracts.SimpleResult
import com.radiotelescope.repository.role.IUserRoleRepository
import com.radiotelescope.repository.role.UserRole
import com.radiotelescope.repository.user.IUserRepository
import com.radiotelescope.security.AccessReport
import com.radiotelescope.security.UserContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * Wrapper responsible for adding user role validations for
 * endpoints for the UserRole Entity
 *
 * @property context the [UserContext] interface
 * @property factory the [UserRoleFactory] interface
 * @property userRepo the [IUserRepository] interface
 * @property userRoleRepo the [IUserRoleRepository] interface
 */
class UserUserRoleWrapper(
        private val context: UserContext,
        private val factory: UserRoleFactory,
        private val userRepo: IUserRepository,
        private val userRoleRepo: IUserRoleRepository
) {
    /**
     * Wrapper method for the [UserRoleFactory.unapprovedList] method that adds Spring
     * Security authentication to the [UnapprovedList] command object
     *
     * @param pageable the [Pageable] request
     * @return An [AccessReport] if authentication fails, null otherwise
     */
    fun unApprovedList(pageable: Pageable, withAccess: (result: SimpleResult<Page<UserRoleInfo>, Multimap<ErrorTag, String>>) -> Unit): AccessReport? {
        if (context.currentUserId() != null)
            return context.require(
                    requiredRoles = listOf(UserRole.Role.ADMIN),
                    successCommand = factory.unapprovedList(pageable)
            ).execute(withAccess)

        return AccessReport(missingRoles = listOf(UserRole.Role.USER, UserRole.Role.ADMIN))
    }
}