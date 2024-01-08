# Nozzle

Nozzle is a lightweight nostr client for Android.

## Installation

Install it via [Obtainium](https://github.com/ImranR98/Obtainium) or go to
the [release page](https://github.com/dluvian/Nozzle/releases) and download the latest apk file.

I don't plan to publish this app in the Google Play Store.

## Some points of difference

- Outbox model: Nozzle discovers relays through nip-65 events, reply relay hints and encoded relays
  in nprofiles/nevents. It fetches data only from relays expected to contain the desired
  information.
- Relay transparency: Nozzle will show you in which relays each note has been seen on and which
  reply relay hint has been set for each reply. Additionally, users can see and limit the relays to
  which they wish to send a post or reply.
- Mobile data friendly: Profile pictures are not rendered, pictures are only loaded when the user
  decides to, reactions of other people will not be fetched and the number of relay connections to
  fetch the feed is minimized.
- Small apk size of less than 8MB

## Screenshots

<p>
<img src="screenshots/feed.png" width="24%" height="24%" />
<img src="screenshots/friend_profile.png" width="24%" height="24%" />
<img src="screenshots/feed_dark.png" width="24%" height="24%" />
<img src="screenshots/friend_profile_dark.png" width="24%" height="24%" />
</p>

## Contributing

Contributors are very welcome.

## License

[MIT licence](https://github.com/dluvian/Nozzle/blob/master/LICENSE)
