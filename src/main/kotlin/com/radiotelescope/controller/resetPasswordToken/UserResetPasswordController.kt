package com.radiotelescope.controller.resetPasswordToken

import com.radiotelescope.contracts.resetPasswordToken.UserResetPasswordTokenWrapper
import com.radiotelescope.contracts.resetPasswordToken.ResetPassword
import com.radiotelescope.controller.BaseRestController
import com.radiotelescope.controller.model.Result
import com.radiotelescope.controller.model.resetPasswordToken.UpdateForm
import com.radiotelescope.controller.spring.Logger
import com.radiotelescope.repository.log.Log
import com.radiotelescope.toStringMap
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

/**
 * REST Controller to handle User Request Reset Password Token
 *
 * @param resetPasswordTokenWrapper the [UserResetPasswordTokenWrapper]
 * @param logger the [Logger] service
 */
@RestController
class UserResetPasswordController (
        private val resetPasswordTokenWrapper: UserResetPasswordTokenWrapper,
        logger: Logger
) : BaseRestController(logger) {
    /**
     * Execute method that is in charge of taking the [UpdateForm]
     * and adapting it to the a [ResetPassword.Request] if possible.
     * If it is not able to, it will respond with errors.
     *
     * Otherwise, it will execute the [UserResetPasswordTokenWrapper.resetPassword] method.
     * Execute the [ResetPassword] command and check if the method
     * was a success or not
     *
     * @param token the reset password token
     * @param form the [UpdateForm] object
     */
    @CrossOrigin(value = ["http://localhost:8081"])
    @PostMapping(value = ["/api/resetPassword"])
    fun execute(@RequestParam("token") token: String,
                @RequestBody form: UpdateForm
    ): Result {
        // If the form validation fails, respond with errors
        form.validateRequest()?.let {
            // Create error logs

            logger.createErrorLogs(
                    info = Logger.createInfo(
                            affectedTable = Log.AffectedTable.USER,
                            action = "User Password Reset",
                            affectedRecordId = null,
                            status = HttpStatus.BAD_REQUEST.value()
                    ),
                    errors = it.toStringMap()
            )

            result = Result(errors = it.toStringMap())
        } ?: let {
            // Otherwise call the factory command
            val simpleResult = resetPasswordTokenWrapper.resetPassword(
                    request = form.toRequest(),
                    token = token
            ).execute()
            // If the command was a success
            simpleResult.success?.let { data ->
                result = Result(
                        data = data
                )

                // Create a success log
                logger.createSuccessLog(
                        info = Logger.createInfo(
                                affectedTable = Log.AffectedTable.USER,
                                action = "User Password Reset",
                                affectedRecordId = data,
                                status = HttpStatus.OK.value()
                        )
                )
            }
            // If the command was a failure
            simpleResult.error?.let { error ->
                // Create an error log
                logger.createErrorLogs(
                        info = Logger.createInfo(
                                affectedTable = Log.AffectedTable.USER,
                                action = "User Password Reset",
                                affectedRecordId = null,
                                status = HttpStatus.BAD_REQUEST.value()
                        ),
                        errors = error.toStringMap()
                )

                result = Result(
                        errors = error.toStringMap()
                )
            }
        }

        return result
    }
}

