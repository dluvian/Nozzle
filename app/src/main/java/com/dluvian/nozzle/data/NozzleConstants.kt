package com.dluvian.nozzle.data

// Boost trust score by 10%.
// Full green ring will be achieved with a raw trust score percentage of 90.9%
const val TRUST_SCORE_BOOST = 1.1f

const val DB_BATCH_SIZE = 30
const val DB_APPEND_BATCH_SIZE = 20

// TODO: Remove this after implementing infinite scroll via pagination
const val MAX_FEED_LENGTH = 100

const val SCOPE_TIMEOUT = 7_000L
const val WAIT_TIME = 1300L

const val APPEND_RETRY_TIME = 3333L

const val MAX_RELAYS = 7

const val MAX_APPEND_ATTEMPTS = 3

const val SWEEP_THRESHOLD = 3000

const val RESUB_AFTER = 5000L

const val RECOMMENDED_RELAY_NUM = 5

const val EVENT_PROCESSING_DELAY = 1111L
