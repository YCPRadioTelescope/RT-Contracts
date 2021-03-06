package com.radiotelescope.contracts.frontpagePicture

import com.google.common.collect.Multimap
import com.radiotelescope.contracts.Command
import com.radiotelescope.contracts.SimpleResult
import com.radiotelescope.repository.frontpagePicture.FrontpagePicture
import com.radiotelescope.repository.frontpagePicture.IFrontpagePictureRepository

/**
 * Override of the [Command] interface method used to retrieve all
 * approved [FrontpagePicture] information
 *
 * @param frontpagePictureRepo the [IFrontpagePictureRepository] interface
 */
class RetrieveApproved(
        private val frontpagePictureRepo: IFrontpagePictureRepository
) : Command<List<FrontpagePicture>, Multimap<ErrorTag, String>> {

    /**
     * Override of the [Command] execute method. It checks the database for
     * each approved [frontpagePictureRepo] and returns them as a [List]
     */
    override fun execute(): SimpleResult<List<FrontpagePicture>, Multimap<ErrorTag, String>> {
        val frontpagePictureApprovedList = frontpagePictureRepo.findAllApproved().toList()

        return SimpleResult(frontpagePictureApprovedList, null)
    }
}