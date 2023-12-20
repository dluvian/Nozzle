package com.dluvian.nozzle

import android.content.Context
import androidx.room.Room
import com.dluvian.nozzle.data.SWEEP_THRESHOLD
import com.dluvian.nozzle.data.annotatedContent.AnnotatedContentHandler
import com.dluvian.nozzle.data.cache.ClickedMediaUrlCache
import com.dluvian.nozzle.data.cache.IClickedMediaUrlCache
import com.dluvian.nozzle.data.cache.IIdCache
import com.dluvian.nozzle.data.cache.IdCache
import com.dluvian.nozzle.data.deletor.DatabaseSweeper
import com.dluvian.nozzle.data.deletor.IDatabaseSweeper
import com.dluvian.nozzle.data.deletor.INoteDeletor
import com.dluvian.nozzle.data.deletor.NoteDeletor
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
import com.dluvian.nozzle.data.postPreparer.IPostPreparer
import com.dluvian.nozzle.data.postPreparer.PostPreparer
import com.dluvian.nozzle.data.preferences.IDarkModePreferences
import com.dluvian.nozzle.data.preferences.IFeedSettingsPreferences
import com.dluvian.nozzle.data.preferences.NozzlePreferences
import com.dluvian.nozzle.data.profileFollower.IProfileFollower
import com.dluvian.nozzle.data.profileFollower.ProfileFollower
import com.dluvian.nozzle.data.provider.IAccountProvider
import com.dluvian.nozzle.data.provider.IAutopilotProvider
import com.dluvian.nozzle.data.provider.IContactListProvider
import com.dluvian.nozzle.data.provider.IPostWithMetaProvider
import com.dluvian.nozzle.data.provider.IProfileWithMetaProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.provider.ISimpleProfileProvider
import com.dluvian.nozzle.data.provider.IThreadProvider
import com.dluvian.nozzle.data.provider.feed.IFeedProvider
import com.dluvian.nozzle.data.provider.feed.IInboxFeedProvider
import com.dluvian.nozzle.data.provider.feed.ILikeFeedProvider
import com.dluvian.nozzle.data.provider.feed.ISearchFeedProvider
import com.dluvian.nozzle.data.provider.feed.impl.FeedProvider
import com.dluvian.nozzle.data.provider.feed.impl.InboxFeedProvider
import com.dluvian.nozzle.data.provider.feed.impl.LikeFeedProvider
import com.dluvian.nozzle.data.provider.feed.impl.SearchFeedProvider
import com.dluvian.nozzle.data.provider.impl.AccountProvider
import com.dluvian.nozzle.data.provider.impl.AutopilotProvider
import com.dluvian.nozzle.data.provider.impl.ContactListProvider
import com.dluvian.nozzle.data.provider.impl.PostWithMetaProvider
import com.dluvian.nozzle.data.provider.impl.ProfileWithMetaProvider
import com.dluvian.nozzle.data.provider.impl.RelayProvider
import com.dluvian.nozzle.data.provider.impl.SimpleProfileProvider
import com.dluvian.nozzle.data.provider.impl.ThreadProvider
import com.dluvian.nozzle.data.room.AppDatabase
import com.dluvian.nozzle.data.room.FullPostInserter
import com.dluvian.nozzle.data.subscriber.INozzleSubscriber
import com.dluvian.nozzle.data.subscriber.NozzleSubscriber
import okhttp3.OkHttpClient

class AppContainer(context: Context) {
    val roomDb: AppDatabase = Room.databaseBuilder(
        context = context,
        klass = AppDatabase::class.java,
        name = "nozzle_database",
    ).build()

    val keyManager: IKeyManager = KeyManager(context = context, accountDao = roomDb.accountDao())

    val contactListProvider: IContactListProvider = ContactListProvider(
        contactDao = roomDb.contactDao()
    )

    private val nozzlePreferences = NozzlePreferences(context = context)

    val feedSettingsPreferences: IFeedSettingsPreferences = nozzlePreferences

    val darkModePreferences: IDarkModePreferences = nozzlePreferences

    val dbSweepExcludingCache: IIdCache = IdCache()

    val fullPostInserter = FullPostInserter(
        postDao = roomDb.postDao(),
        hashtagDao = roomDb.hashtagDao(),
        mentionDao = roomDb.mentionDao(),
        repostDao = roomDb.repostDao(),
    )

    private val eventProcessor: IEventProcessor = EventProcessor(
        dbSweepExcludingCache = dbSweepExcludingCache,
        fullPostInserter = fullPostInserter,
        database = roomDb,
    )

    private val httpClient = OkHttpClient()

    val nip05Resolver: INip05Resolver = Nip05Resolver(httpClient = httpClient)

    val nostrService: INostrService = NostrService(
        httpClient = httpClient,
        keyManager = keyManager,
        eventProcessor = eventProcessor
    )

    private val nostrSubscriber: INostrSubscriber = NostrSubscriber(nostrService = nostrService)

    val relayProvider: IRelayProvider = RelayProvider(
        contactListProvider = contactListProvider,
        nip65Dao = roomDb.nip65Dao(),
    )

    val accountProvider: IAccountProvider = AccountProvider(accountDao = roomDb.accountDao())

    val nozzleSubscriber: INozzleSubscriber = NozzleSubscriber(
        nostrSubscriber = nostrSubscriber,
        relayProvider = relayProvider,
        pubkeyProvider = keyManager,
        accountProvider = accountProvider,
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
        nostrService.initialize(initRelays = relayProvider.getReadRelays())
    }

    val postCardInteractor: IPostCardInteractor = PostCardInteractor(
        nostrService = nostrService,
        relayProvider = relayProvider,
        reactionDao = roomDb.reactionDao(),
        repostDao = roomDb.repostDao()
    )

    val noteDeletor: INoteDeletor = NoteDeletor(
        nostrService = nostrService,
        dbExcludingCache = dbSweepExcludingCache,
        postDao = roomDb.postDao(),
        eventRelayDao = roomDb.eventRelayDao(),
    )

    val profileFollower: IProfileFollower = ProfileFollower(
        nostrService = nostrService,
        pubkeyProvider = keyManager,
        relayProvider = relayProvider,
        contactDao = roomDb.contactDao()
    )

    val clickedMediaUrlCache: IClickedMediaUrlCache = ClickedMediaUrlCache()

    val annotatedContentHandler = AnnotatedContentHandler()

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
        pubkeyProvider = keyManager,
        postDao = roomDb.postDao(),
    )

    val profileWithMetaProvider: IProfileWithMetaProvider =
        ProfileWithMetaProvider(
            pubkeyProvider = keyManager,
            nozzleSubscriber = nozzleSubscriber,
            profileDao = roomDb.profileDao(),
            contactDao = roomDb.contactDao(),
            eventRelayDao = roomDb.eventRelayDao(),
            nip65Dao = roomDb.nip65Dao(),
        )

    val personalProfileManager: IPersonalProfileManager = PersonalProfileManager(
        pubkeyProvider = keyManager,
        relayProvider = relayProvider,
        nostrService = nostrService,
        profileDao = roomDb.profileDao()
    )

    val threadProvider: IThreadProvider = ThreadProvider(
        postWithMetaProvider = postWithMetaProvider,
        nozzleSubscriber = nozzleSubscriber,
        postDao = roomDb.postDao()
    )

    val databaseSweeper: IDatabaseSweeper = DatabaseSweeper(
        keepPosts = SWEEP_THRESHOLD,
        dbSweepExcludingCache = dbSweepExcludingCache,
        database = roomDb,
    )

    val simpleProfileProvider: ISimpleProfileProvider = SimpleProfileProvider(
        pubkeyProvider = keyManager,
        profileDao = roomDb.profileDao(),
        contactDao = roomDb.contactDao(),
    )
    val searchFeedProvider: ISearchFeedProvider = SearchFeedProvider(
        postWithMetaProvider = postWithMetaProvider,
        postDao = roomDb.postDao(),
    )

    val postPreparer: IPostPreparer = PostPreparer(
        simpleProfileProvider = simpleProfileProvider,
        relayProvider = relayProvider
    )

    val inboxFeedProvider: IInboxFeedProvider = InboxFeedProvider(
        nozzleSubscriber = nozzleSubscriber,
        postWithMetaProvider = postWithMetaProvider,
        postDao = roomDb.postDao()
    )

    val likeFeedProvider: ILikeFeedProvider = LikeFeedProvider(
        nozzleSubscriber = nozzleSubscriber,
        postWithMetaProvider = postWithMetaProvider,
        postDao = roomDb.postDao(),
        reactionDao = roomDb.reactionDao()
    )
}
