package com.dluvian.nozzle

import android.content.Context
import androidx.room.Room
import com.dluvian.nozzle.data.SWEEP_THRESHOLD
import com.dluvian.nozzle.data.SWEEP_THRESHOLD_FACTOR
import com.dluvian.nozzle.data.annotatedContent.AnnotatedContentHandler
import com.dluvian.nozzle.data.cache.ClickedMediaUrlCache
import com.dluvian.nozzle.data.cache.IClickedMediaUrlCache
import com.dluvian.nozzle.data.cache.IIdCache
import com.dluvian.nozzle.data.cache.IdCache
import com.dluvian.nozzle.data.databaseSweeper.DatabaseSweeper
import com.dluvian.nozzle.data.databaseSweeper.IDatabaseSweeper
import com.dluvian.nozzle.data.eventProcessor.EventProcessor
import com.dluvian.nozzle.data.eventProcessor.IEventProcessor
import com.dluvian.nozzle.data.manager.IKeyManager
import com.dluvian.nozzle.data.manager.IPersonalProfileManager
import com.dluvian.nozzle.data.manager.impl.KeyManager
import com.dluvian.nozzle.data.manager.impl.PersonalProfileManager
import com.dluvian.nozzle.data.nostr.INostrService
import com.dluvian.nozzle.data.nostr.INostrSubscriber
import com.dluvian.nozzle.data.nostr.NostrService
import com.dluvian.nozzle.data.nostr.NostrSubscriber
import com.dluvian.nozzle.data.nostr.nip05.INip05Resolver
import com.dluvian.nozzle.data.nostr.nip05.Nip05Resolver
import com.dluvian.nozzle.data.postCardInteractor.IPostCardInteractor
import com.dluvian.nozzle.data.postCardInteractor.PostCardInteractor
import com.dluvian.nozzle.data.preferences.IFeedSettingsPreferences
import com.dluvian.nozzle.data.preferences.NozzlePreferences
import com.dluvian.nozzle.data.profileFollower.IProfileFollower
import com.dluvian.nozzle.data.profileFollower.ProfileFollower
import com.dluvian.nozzle.data.provider.IAutopilotProvider
import com.dluvian.nozzle.data.provider.IContactListProvider
import com.dluvian.nozzle.data.provider.IFeedProvider
import com.dluvian.nozzle.data.provider.IPostWithMetaProvider
import com.dluvian.nozzle.data.provider.IProfileWithMetaProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.provider.IThreadProvider
import com.dluvian.nozzle.data.provider.impl.AutopilotProvider
import com.dluvian.nozzle.data.provider.impl.ContactListProvider
import com.dluvian.nozzle.data.provider.impl.FeedProvider
import com.dluvian.nozzle.data.provider.impl.PostWithMetaProvider
import com.dluvian.nozzle.data.provider.impl.ProfileWithMetaProvider
import com.dluvian.nozzle.data.provider.impl.RelayProvider
import com.dluvian.nozzle.data.provider.impl.ThreadProvider
import com.dluvian.nozzle.data.room.AppDatabase
import com.dluvian.nozzle.data.subscriber.INozzleSubscriber
import com.dluvian.nozzle.data.subscriber.NozzleSubscriber

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

    private val nozzlePreferences = NozzlePreferences(context = context)

    val feedSettingsPreferences: IFeedSettingsPreferences = nozzlePreferences

    private val dbSweepExcludingCache: IIdCache = IdCache()

    private val eventProcessor: IEventProcessor = EventProcessor(
        dbSweepExcludingCache = dbSweepExcludingCache,
        database = roomDb,
    )

    val nip05Resolver: INip05Resolver = Nip05Resolver()

    val nostrService: INostrService = NostrService(
        keyManager = keyManager,
        eventProcessor = eventProcessor
    )

    private val nostrSubscriber: INostrSubscriber = NostrSubscriber(nostrService = nostrService)

    val relayProvider: IRelayProvider = RelayProvider(
        pubkeyProvider = keyManager,
        nip65Dao = roomDb.nip65Dao(),
    )

    val nozzleSubscriber: INozzleSubscriber = NozzleSubscriber(
        nostrSubscriber = nostrSubscriber,
        relayProvider = relayProvider,
        pubkeyProvider = keyManager,
        idCache = dbSweepExcludingCache,
        database = roomDb,
    )

    val autopilotProvider: IAutopilotProvider = AutopilotProvider(
        relayProvider = relayProvider,
        contactListProvider = contactListProvider,
        nip65Dao = roomDb.nip65Dao(),
        eventRelayDao = roomDb.eventRelayDao(),
        nozzleSubscriber = nozzleSubscriber,
    )

    init {
        nostrService.initialize(
            initRelays = relayProvider.getWriteRelays().toSet() + relayProvider.getReadRelays()
        )
    }

    val postCardInteractor: IPostCardInteractor = PostCardInteractor(
        nostrService = nostrService,
        relayProvider = relayProvider,
        reactionDao = roomDb.reactionDao(),
    )

    val profileFollower: IProfileFollower = ProfileFollower(
        nostrService = nostrService,
        pubkeyProvider = keyManager,
        contactDao = roomDb.contactDao()
    )

    val clickedMediaUrlCache: IClickedMediaUrlCache = ClickedMediaUrlCache()

    private val annotatedContentHandler = AnnotatedContentHandler()

    private val postWithMetaProvider: IPostWithMetaProvider = PostWithMetaProvider(
        pubkeyProvider = keyManager,
        contactListProvider = contactListProvider,
        annotatedContentHandler = annotatedContentHandler,
        nozzleSubscriber = nozzleSubscriber,
        postDao = roomDb.postDao(),
        eventRelayDao = roomDb.eventRelayDao(),
        contactDao = roomDb.contactDao(),
        profileDao = roomDb.profileDao()
    )

    val feedProvider: IFeedProvider = FeedProvider(
        postWithMetaProvider = postWithMetaProvider,
        nozzleSubscriber = nozzleSubscriber,
        contactListProvider = contactListProvider,
        postDao = roomDb.postDao(),
    )

    val profileWithMetaProvider: IProfileWithMetaProvider =
        ProfileWithMetaProvider(
            pubkeyProvider = keyManager,
            nozzleSubscriber = nozzleSubscriber,
            profileDao = roomDb.profileDao(),
            contactDao = roomDb.contactDao(),
            eventRelayDao = roomDb.eventRelayDao(),
        )

    val personalProfileManager: IPersonalProfileManager = PersonalProfileManager(
        pubkeyProvider = keyManager,
        profileDao = roomDb.profileDao()
    )

    val threadProvider: IThreadProvider = ThreadProvider(
        postWithMetaProvider = postWithMetaProvider,
        nozzleSubscriber = nozzleSubscriber,
        postDao = roomDb.postDao()
    )

    val databaseSweeper: IDatabaseSweeper = DatabaseSweeper(
        keepPosts = SWEEP_THRESHOLD,
        thresholdFactor = SWEEP_THRESHOLD_FACTOR,
        pubkeyProvider = keyManager,
        contactListProvider = contactListProvider,
        dbSweepExcludingCache = dbSweepExcludingCache,
        database = roomDb,
    )
}
