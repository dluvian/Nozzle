package com.dluvian.nozzle.model.helper

data class PubkeysAndAuthorPubkeys(
    val pubkeys: Collection<String> = emptyList(),
    val authorPubkeys: Collection<String> = emptyList()
)
