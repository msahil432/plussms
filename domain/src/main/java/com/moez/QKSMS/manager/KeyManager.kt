package com.moez.QKSMS.manager

/**
 * Helper class for generating incrementing ids for messages
 */
interface KeyManager {

    /**
     * Should be called when a new sync is being started
     */
    fun reset()

    /**
     * Returns a valid ID that can be used to store a new message
     */
    fun newId(): Long

}