# Nozzle

Nozzle is a lightweight nostr client for Android.

## State

This project is in alpha stage.

## Installation

Go to the [release page](https://github.com/dluvian/Nozzle/releases) and download the latest apk
file or clone this repository and build it yourself.

## Screenshots

<p float="left">
<img src="screenshots/feed.png" width="24%" height="24%" />
<img src="screenshots/friend_profile.png" width="24%" height="24%" />
<img src="screenshots/feed_dark.png" width="24%" height="24%" />
<img src="screenshots/friend_profile_dark.png" width="24%" height="24%" />
</p>

## Features

- [x] Import private key
- [x] Show threads, profiles, profile feed and home feed
- [x] Repost, like, reply, post, follow, unfollow, edit profile
- [x] Search bar
- [x] Relay selector for feed, replying and posting
- [x] Autopilot (gossip model for selecting relays)
- [ ] Mentions
- [ ] Notifications tab
- [ ] Relay management
- [ ] Quote repost
- [ ] nip05 verification
- [ ] Hashtags
- [ ] Bookmarks
- [ ] Profile lists (likes, reposts, following, followers)
- [ ] Lightning tip button, invoice widget, zaps
- [ ] Multiple accounts

## Implemented [NIPs](https://github.com/nostr-protocol/nips)

- [NIP-01](https://github.com/nostr-protocol/nips/blob/master/01.md)
  - Profile metadata, text notes and basic protocol flow
- [NIP-02](https://github.com/nostr-protocol/nips/blob/master/02.md)
  - Writing and reading contact lists
- [NIP-10](https://github.com/nostr-protocol/nips/blob/master/10.md)
  - Marked e tags
  - Set p tags when replying
- [NIP-15](https://github.com/nostr-protocol/nips/blob/master/15.md)
  - Close connection/subscription on EOSE
- [NIP-19](https://github.com/nostr-protocol/nips/blob/master/19.md)
  - npub, nsec and note1
  - nprofile and nevent coming soon
- [NIP-21](https://github.com/nostr-protocol/nips/blob/master/21.md)
  - nostr:npub1... and nostr:note1...
  - nostr:nprofile1... and nostr:nevent1... coming soon
- [NIP-25](https://github.com/nostr-protocol/nips/blob/master/25.md)
  - Create and read "like" reactions
  - Ignores other reactions
- [NIP-65](https://github.com/nostr-protocol/nips/blob/master/65.md)
  - Read and use nip65 lists for autopilot (gossip model for selecting relays)
  - Creating a nip65 list is coming soon

## Contributing

Contributors are very welcome.

## License

[MIT licence](https://github.com/dluvian/Nozzle/blob/master/LICENSE)
