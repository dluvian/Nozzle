package com.dluvian.nozzle.data.manager

import com.dluvian.nozzle.data.provider.IPersonalProfileProvider

interface IPersonalProfileManager : IPersonalProfileProvider {
    suspend fun setMeta(name: String, about: String, picture: String, nip05: String, lud16: String)
}
