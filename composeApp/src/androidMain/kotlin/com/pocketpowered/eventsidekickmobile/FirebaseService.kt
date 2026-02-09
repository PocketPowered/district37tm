package com.district37.toastmasters

import com.district37.toastmasters.infra.EventSidekickFirebaseMessagingService

/**
 * Wrapper service to keep manifest entrypoint stable.
 */
class FirebaseService : EventSidekickFirebaseMessagingService()
