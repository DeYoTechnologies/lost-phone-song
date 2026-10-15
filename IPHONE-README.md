# Lost Phone Song on iPhone (using Shortcuts)

The Lost Phone Song **app is Android only**. iOS doesn't let apps read incoming texts or other apps' notifications, so no iPhone app can do this. Apple's built-in **Shortcuts** app can do nearly the same thing, with no install needed.

Requires iOS 17 or later (for "Run Immediately" on message automations).

## 1. Put the song on the iPhone
Pick one:
- **Apple Music / Music library:** add the song to your library (synced or purchased). Easiest for looping.
- **Files app:** save the MP3 to iCloud Drive or On My iPhone (e.g. from an email or AirDrop).

## 2. Create the automation
1. Open **Shortcuts** > **Automation** tab > **+** (New Automation).
2. Choose **Message**.
3. **Sender:** tap Choose and pick the trusted contact.
4. **Message Contains:** type `lost phone song` (or your own phrase).
5. Select **Run Immediately** (not "Run After Confirmation"). Turn **off** "Notify When Run" if you like.
6. Tap **Next** > **New Blank Automation**.

## 3. Add the actions
Add these in order (search each name in the action list):

**Option A, song from your Music library**
1. **Set Volume**: 100%.
2. **Set Repeat** (if shown) or skip; then **Play Music**: tap Music and pick the song.
3. **Set Repeat**: Repeat One (search "Repeat" under Music actions), so it loops.

**Option B, song from Files**
1. **Set Volume**: 100%.
2. **Repeat**: 20 times. Inside the Repeat block:
   - **Get File**: pick the MP3 (turn off "Show Document Picker").
   - **Play Sound**: input = the file.
3. End Repeat.

Tap **Done**.

## 4. Test it
Have the trusted contact text you something like "hey lost phone song please". The phone should play the song at full volume.

## Stopping it
Use the lock screen music controls (pause), or open Control Center. For Option B, open Shortcuts and tap the stop button on the running automation.

## Limits (compared to the Android app)
- Plays through the media volume, not an alarm. Media generally plays with the silent switch on, but a Focus/Do Not Disturb mode or the volume limit may affect it. Test with your phone's settings.
- No "Congratulations, you found your phone" stop screen, and no 5-minute auto-stop.
- The phrase match is a simple "contains" check (not the exact-word rules the Android app uses) and ignores capitals.
- Matches by contact, so the sender must be saved in Contacts.
- Apple's free **Find My > Play Sound** (from icloud.com/find or another Apple device) is another way to make a lost iPhone ring.

## Share it
You can share the shortcut steps by sending people this file. Message automations themselves can't be shared as a link; each person sets them up on their own phone (about 2 minutes).
