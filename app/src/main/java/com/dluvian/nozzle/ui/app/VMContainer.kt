package com.dluvian.nozzle.ui.app

import com.dluvian.nozzle.ui.app.views.addAccount.AddAccountViewModel
import com.dluvian.nozzle.ui.app.views.drawer.NozzleDrawerViewModel
import com.dluvian.nozzle.ui.app.views.editProfile.EditProfileViewModel
import com.dluvian.nozzle.ui.app.views.feed.FeedViewModel
import com.dluvian.nozzle.ui.app.views.hashtag.HashtagViewModel
import com.dluvian.nozzle.ui.app.views.inbox.InboxViewModel
import com.dluvian.nozzle.ui.app.views.keys.KeysViewModel
import com.dluvian.nozzle.ui.app.views.post.PostViewModel
import com.dluvian.nozzle.ui.app.views.profile.ProfileViewModel
import com.dluvian.nozzle.ui.app.views.profileList.ProfileListViewModel
import com.dluvian.nozzle.ui.app.views.relayEditor.RelayEditorViewModel
import com.dluvian.nozzle.ui.app.views.reply.ReplyViewModel
import com.dluvian.nozzle.ui.app.views.search.SearchViewModel
import com.dluvian.nozzle.ui.app.views.thread.ThreadViewModel

data class VMContainer(
    val drawerViewModel: NozzleDrawerViewModel,
    val profileViewModel: ProfileViewModel,
    val profileListViewModel: ProfileListViewModel,
    val feedViewModel: FeedViewModel,
    val inboxViewModel: InboxViewModel,
    val keysViewModel: KeysViewModel,
    val editProfileViewModel: EditProfileViewModel,
    val threadViewModel: ThreadViewModel,
    val replyViewModel: ReplyViewModel,
    val postViewModel: PostViewModel,
    val searchViewModel: SearchViewModel,
    val hashtagViewModel: HashtagViewModel,
    val relayEditorViewModel: RelayEditorViewModel,
    val addAccountViewModel: AddAccountViewModel,
)
