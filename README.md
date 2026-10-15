# Lost Phone Song

An Android app that helps you find a lost phone. When **your trusted number** (or **trusted contact**) texts the phone a message containing **"lost phone song"**, the phone plays a song on the alarm stream at full volume, looping, for up to 5 minutes, even if it's on silent or vibrate. It works with plain SMS and, through notification access, with RCS chat messages.

- Works offline. The app has no internet permission, no backend, and no account.
- Only one trusted number (plus, for RCS, one trusted contact name) can trigger it. Texts from anyone else are ignored.
- The phrase is case-insensitive and must be those three words in order ("hey LOST PHONE SONG please" works; "lost my phone song" does not).
- Ships with a built-in song. You can pick any audio file on the phone instead.
- While it plays, a full-screen **"Congratulations, you found your phone!"** screen with a big **Stop** button shows, even over the lock screen. The notification also has a **Stop** button.

See [CHANGELOG.md](CHANGELOG.md) for what changed in each version.

## Download

Go to this repo's **Releases** page, open **Latest build**, and download **`lost-phone-song.apk`**. Every push to `main` rebuilds it.

## Install (sideload) on a Samsung Galaxy

1. Download `lost-phone-song.apk` on the phone (in Chrome or Samsung Internet), or copy it over and find it in **My Files**.
2. Tap the APK. If Android blocks it, tap **Settings** in the prompt and turn on **Allow from this source** for the app you're installing from (Chrome, Samsung Internet, or My Files).
   - Or: **Settings > Apps > ⋮ (top right) > Special access > Install unknown apps >** *(your browser or My Files)* **> Allow from this source**.
3. Go back and tap **Install**. If Play Protect warns about an unknown app, choose **More details > Install anyway**.

Newer releases install over older ones and keep your settings, because every build is signed with the same key.

## Set up in the app

1. Open **Lost Phone Song**.
2. **Trusted number:** enter the number that will text this phone (for example, your partner's phone or your second phone), then tap **Save**. It needs at least 10 digits; `+1 555-123-4567`, `(555) 123-4567` and `5551234567` all count as the same number.
3. **Trusted contact name (for RCS):** type the sender's name **exactly as it appears in your Messages app** (for example `Jamie Smith`), then tap **Save**. Case and leading/trailing spaces don't matter, but a partial name (`Jamie`) won't match. Leave it blank if the trusted number shows as a number in Messages. Saving a blank name clears it.
4. **Song:** leave it on **Default (built-in)**, or tap **Choose a different song** to pick an audio file. **Reset to default song** switches back.
5. **Permissions:** tap **Request permissions** and allow **SMS** and **Notifications**. Both status lines should read **Granted**.
6. **Notification access (for RCS):** tap **Open notification access settings** and turn on **Lost Phone Song** (see below). The status line should read **Enabled**.
7. **Battery:** tap **Open battery optimization settings** and allow the app to run unrestricted.

## Exact Samsung settings

These keep One UI from blocking or sleeping the app. Menu names can vary a little between One UI versions.

**Permissions:**
- **Settings > Apps > Lost Phone Song > Permissions**
  - **SMS: Allow**
  - **Notifications: Allow**

**If SMS is greyed out or says "Restricted setting":** Android restricts SMS access for sideloaded apps until you unlock it.
1. **Settings > Apps > Lost Phone Song** (App info).
2. Tap the **⋮** menu (top right) **> Allow restricted settings**, and confirm with your PIN or fingerprint.
3. Go back to **Permissions > SMS** and choose **Allow**.

(You may need to try to grant SMS once, from the app or Permissions, before **Allow restricted settings** appears in the menu.)

**Notification access (needed for RCS / chat messages):**
- **Settings > Notifications > Advanced settings > Notification access**, then turn on **Lost Phone Song** and confirm. (Or open Settings and search for **notification access**.)
- If the switch is greyed out or says "Restricted setting", use **Allow restricted settings** from App info as described above, then try again.
- In **Messages**, keep notifications **on** for the trusted conversation: don't mute it, and keep message previews/content on for the Messages app. The app reads the sender and text from that notification.
- The trusted contact name in the app must match **exactly** how the sender is shown in Messages (the conversation title / notification title).

**Full-screen Stop screen:** on Android 14+, check **Settings > Apps > Lost Phone Song > Full screen notifications** (or **Special access > Full screen notifications** / **Full screen intents**) is **allowed**. Without it, you still get the high-priority notification; tap it for the Stop screen.

**Battery:**
- **Settings > Apps > Lost Phone Song > Battery > Unrestricted**
- **Settings > Battery > Background usage limits > Never sleeping apps > +**, then add **Lost Phone Song**
- On the same **Background usage limits** screen, turn **off** **Put unused apps to sleep**

## How to test

1. In the app, tap **Test**. The song should play at full alarm volume and the **"Congratulations, you found your phone!"** screen should open. Tap its big **Stop** (or **Stop** in the notification).
2. Lock the phone and leave it alone for a minute. From the **trusted number**, text it: `lost phone song`. It should start playing within a few seconds and the Stop screen should appear over the lock screen.
3. If the two phones chat over RCS (Google Messages / Samsung Messages "chat"), repeat step 2 with RCS on. This needs notification access and an unmuted conversation.
4. Optional: text the same phrase from a different number. Nothing should happen.

If the in-app test works but the text doesn't, check the SMS permission, notification access, and the battery settings above.

## Build from source

You need JDK 17 and the Android SDK (API 35).

```sh
./gradlew testDebugUnitTest assembleDebug
```

The APK lands at `app/build/outputs/apk/debug/app-debug.apk`. CI (`.github/workflows/build.yml`) runs the same command and publishes the APK to the `latest` release.

## Assumptions and limits

- **The phone needs to be on and able to receive texts** (cell signal, or Wi-Fi calling). It can't help if the phone is off, dead, or offline.
- **SMS** is read directly from the SMS broadcast. **RCS chat** messages never arrive as SMS, so the app reads them from the messaging app's notification instead (Samsung Messages, Google Messages, or any app that marks its notifications as messages). That means:
  - **Notification access must be on** for Lost Phone Song.
  - **The conversation must not be muted** and Messages notifications must be on. If no notification is posted, there's nothing to read.
  - Lock-screen "hide content" settings **don't matter**: they only change what's drawn on the lock screen, and the listener still gets the full text.
  - The sender is identified by the notification title or per-message sender name. It matches if it contains the trusted number, or equals the trusted contact name exactly (ignoring case and surrounding spaces). If Messages shows a contact name and you didn't set the trusted contact name, RCS texts won't match.
  - If one text arrives both as SMS and as a notification, the second trigger within 10 seconds is ignored while the song is playing.
- **iMessage** never reaches an Android phone. Send from an Android phone, or as SMS.
- **It doesn't work after a reboot until the phone is unlocked once.** Before the first unlock after a restart, Android doesn't deliver SMS to normal apps.
- **Matching uses the last 10 digits** of each number (built around North American numbers). Short codes and alphanumeric senders can never match.
- **Background start depends on battery settings.** Android restricts starting foreground services from the background; the Unrestricted battery setting is what reliably allows it on Samsung. If Android still refuses, the app fails silently.
- **Volume and Do Not Disturb:** the song plays on the alarm stream, and the app raises alarm volume to max, then restores it when playback stops. If you've set Do Not Disturb to block alarms, it will stay silent.
- **Auto-stop:** playback stops after 5 minutes (the Stop screen closes too). Texting the phrase again during playback resets the 5-minute timer.
- **Custom song:** if the chosen file is deleted or moved, it falls back to the built-in song, then to the system alarm sound.
- **Security:** anyone who can send SMS that *appears* to come from your trusted number (caller-ID spoofing) can trigger the song. With a trusted contact name set, any sender that shows in Messages under that exact name can too (for example, an unsaved RCS sender whose profile uses that name). The worst they can do is make the phone play music.
- **Signing:** builds use a debug key committed to this repo (`app/debug.keystore`) so updates install over each other. It's not a release key, so don't reuse it for anything else.
- Not for Google Play distribution (SMS permissions aren't allowed for this use case there). Sideload only.
