package com.radiotelescope.security.service

import com.radiotelescope.repository.role.IUserRoleRepository
import com.radiotelescope.repository.user.IUserRepository
import com.radiotelescope.repository.user.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

/**
 * This service is a concrete implementation of the [UserDetailsService]
 *
 * @param userRepo the [IUserRepository] interface
 * @param userRoleRepo the [IUserRoleRepository] interface
 */
@Service(value = "UserDetailsService")
class UserDetailsServiceImpl(
        var userRepo: IUserRepository,
        var userRoleRepo: IUserRoleRepository
) : UserDetailsService {
    /**
     * Override of the [UserDetailsService.loadUserByUsername] used to load the user
     * by the username (email) so it can create a [UserDetailsImpl] object. It checks
     * to make sure the email refers to a valid [User] Entity. If it does not, (or
     * the email is null) it will throw an error. It will then grab all of the user's
     * roles and create the [UserDetailsImpl]
     *
     * @param username the [User] email
     * @return a [UserDetailsImpl] object
     */
    override fun loadUserByUsername(username: String?): UserDetails {
        if (username == null) {
            throw UsernameNotFoundException("Invalid email address")
        }

        val user = userRepo.findByEmail(email = username) ?: throw UsernameNotFoundException("Invalid email or password")
        val roles = userRoleRepo.findAllApprovedRolesByUserId(user.id)

        if (roles.isEmpty())
            throw UsernameNotFoundException("This User does not have any roles")

        var grantedAuthorities: Set<GrantedAuthority> = HashSet()

        roles.forEach {
            grantedAuthorities = grantedAuthorities.plus(SimpleGrantedAuthority("ROLE_${it.role.name.toUpperCase()}"))
        }

        return UserDetailsImpl(user, grantedAuthorities)
    }
}