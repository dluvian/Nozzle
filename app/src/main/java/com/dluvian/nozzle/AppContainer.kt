package com.dluvian.nozzle

import android.content.Context
import androidx.room.Room
import com.dluvian.nozzle.data.eventProcessor.EventProcessor
import com.dluvian.nozzle.data.eventProcessor.IEventProcessor
import com.dluvian.nozzle.data.manager.IKeyManager
import com.dluvian.nozzle.data.manager.IPersonalProfileManager
import com.dluvian.nozzle.data.manager.impl.KeyManager
import com.dluvian.nozzle.data.manager.impl.PersonalProfileManager
import com.dluvian.nozzle.data.mapper.IPostMapper
import com.dluvian.nozzle.data.mapper.PostMapper
import com.dluvian.nozzle.data.nostr.INostrService
import com.dluvian.nozzle.data.nostr.INostrSubscriber
import com.dluvian.nozzle.data.nostr.NostrService
import com.dluvian.nozzle.data.nostr.NostrSubscriber
import com.dluvian.nozzle.data.postCardInteractor.IPostCardInteractor
import com.dluvian.nozzle.data.postCardInteractor.PostCardInteractor
import com.dluvian.nozzle.data.preferences.IFeedSettingsPreferences
import com.dluvian.nozzle.data.preferences.NozzlePreferences
import com.dluvian.nozzle.data.profileFollower.IProfileFollower
import com.dluvian.nozzle.data.profileFollower.ProfileFollower
import com.dluvian.nozzle.data.provider.IAutopilotProvider
import com.dluvian.nozzle.data.provider.IContactListProvider
import com.dluvian.nozzle.data.provider.IFeedProvider
import com.dluvian.nozzle.data.provider.IInteractionStatsProvider
import com.dluvian.nozzle.data.provider.IProfileWithAdditionalInfoProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.provider.IThreadProvider
import com.dluvian.nozzle.data.provider.impl.AutopilotProvider
import com.dluvian.nozzle.data.provider.impl.ContactListProvider
import com.dluvian.nozzle.data.provider.impl.FeedProvider
import com.dluvian.nozzle.data.provider.impl.InteractionStatsProvider
import com.dluvian.nozzle.data.provider.impl.ProfileWithAdditionalInfoProvider
import com.dluvian.nozzle.data.provider.impl.RelayProvider
import com.dluvian.nozzle.data.provider.impl.ThreadProvider
import com.dluvian.nozzle.data.room.AppDatabase

class AppContainer(context: Context) {
    val roomDb: AppDatabase by lazy {
        Room.databaseBuilder(
            context = context,
            klass = AppDatabase::class.java,
            name = "nozzle_database",
        ).fallbackToDestructiveMigration().build()
    }

    val keyManager: IKeyManager = KeyManager(context = context)

    val contactListProvider: IContactListProvider = ContactListProvider(
        pubkeyProvider = keyManager,
        contactDao = roomDb.contactDao()
    )

    private val autopilotProvider: IAutopilotProvider = AutopilotProvider(
        pubkeyProvider = keyManager,
        nip65Dao = roomDb.nip65Dao(),
        eventRelayDao = roomDb.eventRelayDao()
    )

    val relayProvider: IRelayProvider = RelayProvider(
        contactListProvider = contactListProvider,
        autopilotProvider = autopilotProvider,
        pubkeyProvider = keyManager,
        nip65Dao = roomDb.nip65Dao(),
    )

    private val nozzlePreferences = NozzlePreferences(context = context)

    val feedSettingsPreferences: IFeedSettingsPreferences = nozzlePreferences

    private val eventProcessor: IEventProcessor = EventProcessor(
        reactionDao = roomDb.reactionDao(),
        profileDao = roomDb.profileDao(),
        contactDao = roomDb.contactDao(),
        postDao = roomDb.postDao(),
        eventRelayDao = roomDb.eventRelayDao(),
        nip65Dao = roomDb.nip65Dao(),
    )

    val nostrService: INostrService = NostrService(
        keyManager = keyManager,
        relayProvider = relayProvider,
        eventProcessor = eventProcessor
    )

    val nostrSubscriber: INostrSubscriber = NostrSubscriber(
        nostrService = nostrService,
        pubkeyProvider = keyManager,
        postDao = roomDb.postDao()
    )

    val postCardInteractor: IPostCardInteractor = PostCardInteractor(
        nostrService = nostrService,
        reactionDao = roomDb.reactionDao(),
    )

    val profileFollower: IProfileFollower = ProfileFollower(
        nostrService = nostrService,
        pubkeyProvider = keyManager,
        contactDao = roomDb.contactDao()
    )

    private val interactionStatsProvider: IInteractionStatsProvider = InteractionStatsProvider(
        pubkeyProvider = keyManager,
        reactionDao = roomDb.reactionDao(),
        postDao = roomDb.postDao()
    )

    private val postMapper: IPostMapper = PostMapper(
        interactionStatsProvider = interactionStatsProvider,
        pubkeyProvider = keyManager,
        postDao = roomDb.postDao(),
        profileDao = roomDb.profileDao(),
        eventRelayDao = roomDb.eventRelayDao(),
        contactDao = roomDb.contactDao()
    )

    val feedProvider: IFeedProvider = FeedProvider(
        postMapper = postMapper,
        nostrSubscriber = nostrSubscriber,
        postDao = roomDb.postDao(),
        contactListProvider = contactListProvider,
    )

    val profileWithFollowerProvider: IProfileWithAdditionalInfoProvider =
        ProfileWithAdditionalInfoProvider(
            pubkeyProvider = keyManager,
            nostrSubscriber = nostrSubscriber,
            profileDao = roomDb.profileDao(),
            contactDao = roomDb.contactDao(),
            eventRelayDao = roomDb.eventRelayDao(),
            nip65Dao = roomDb.nip65Dao(),
        )

    val personalProfileManager: IPersonalProfileManager = PersonalProfileManager(
        pubkeyProvider = keyManager,
        profileDao = roomDb.profileDao()
    )

    val threadProvider: IThreadProvider = ThreadProvider(
        postMapper = postMapper,
        nostrSubscriber = nostrSubscriber,
        postDao = roomDb.postDao()
    )
}
