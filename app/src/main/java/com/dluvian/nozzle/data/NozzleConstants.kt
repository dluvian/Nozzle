package com.dluvian.nozzle.data

// Boost trust score by 10%.
// Full green ring will be achieved with a raw trust score percentage of 90.9%
const val TRUST_SCORE_BOOST = 1.1f

const val DB_BATCH_SIZE = 30

const val SCOPE_TIMEOUT = 7_000L
const val WAIT_TIME = 1800L
const val SHORT_WAIT_TIME = WAIT_TIME / 2

const val MAX_RELAYS = 7

const val SWEEP_THRESHOLD = 500

// Should be smaller than WAIT_TIME
const val EVENT_PROCESSING_DELAY = 500L

const val MAX_LIST_LENGTH = 100

const val MAX_SUGGESTION_LENGTH = 30

const val PULL_REFRESH_Z_INDEX = 3f
const val Z_INDEX_UNDER_PULL_REFRESH = PULL_REFRESH_Z_INDEX - 1f

// TODO: Figure out the real max value to not exceed SQL's max query length
const val MAX_SQL_PARAMS = 250
