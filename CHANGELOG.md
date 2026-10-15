# Changelog

## 1.1

- **RCS support.** A notification listener reads incoming message notifications (Samsung Messages, Google Messages, or any notification in the "message" category), so RCS chat messages can trigger the song too. Requires **Notification access**.
- **Trusted contact name.** New setting for the sender name as shown in your Messages app. RCS notifications match if the sender is the trusted number or exactly this name (case-insensitive, surrounding spaces ignored).
- **Setup screen:** trusted contact name field, plus a Notification access status line and a button that opens the Notification access settings.
- **Stop screen.** While the song plays, a full-screen "Congratulations, you found your phone!" screen with a big **Stop** button opens, even over the lock screen. It closes itself when playback stops or times out, and doesn't appear in Recents. The notification's **Stop** button still works.
- **Custom trigger phrase.** New **Trigger phrase** setting (default "lost phone song") with **Save** and **Reset to default**. It must contain at least one word. Matching is case-insensitive, with the exact words in order separated by any whitespace; punctuation in the phrase is matched literally. Used for both SMS and RCS.
- **Test** now opens the Stop screen too.
- If one text arrives both as SMS and as a notification, the duplicate trigger is ignored.

## 1.0

- First release. When the trusted number texts "lost phone song", it plays a song on the alarm stream at full volume, looping, for up to 5 minutes, even on silent or vibrate.
- Built-in song or a custom audio file, with fallback to the system alarm sound.
- Notification with a Stop button, a 5-minute auto-stop, and alarm volume restored afterwards.
- Setup screen for the trusted number, song, permissions, and battery optimization.
