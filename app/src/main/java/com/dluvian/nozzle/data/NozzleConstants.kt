package com.dluvian.nozzle.data

// Boost trust score by 10%.
// Full green ring will be achieved with a raw trust score percentage of 90.9%
const val TRUST_SCORE_BOOST = 1.1f

const val DB_BATCH_SIZE = 50
const val DB_APPEND_BATCH_SIZE = 25

// TODO: Remove this after implementing infinite scroll via pagination
const val MAX_FEED_LENGTH = 100

const val SCOPE_TIMEOUT = 7_000L
const val WAIT_TIME = 1300L

// Don't ask more than 5 relays
const val MAX_RELAY_REQUESTS = 5
