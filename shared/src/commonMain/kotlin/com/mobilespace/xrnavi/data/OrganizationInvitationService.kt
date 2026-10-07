package com.mobilespace.xrnavi.data

import com.mobilespace.xrnavi.domain.OrganizationInvitationRepository
import com.mobilespace.xrnavi.domain.OrganizationInvitationResult

class UnconfiguredOrganizationInvitationService : OrganizationInvitationRepository {
    override suspend fun acceptInvitation(code: String, email: String): OrganizationInvitationResult =
        OrganizationInvitationResult.NotConfigured
}
