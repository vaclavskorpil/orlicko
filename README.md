# Radio Orlicko

Unofficial clients for [Radio Orlicko](https://radioorlicko.cz) radio station.

This monorepo contains two independent apps that stream Radio Orlicko audio and display current song metadata. Both apps use the public API from the [Radio Orlicko web player](https://radioorlicko.cz) to fetch the currently playing song.

## Apps

### orlicko-tui

Terminal UI player built with Go and [Bubble Tea](https://github.com/charmbracelet/bubbletea). Uses [mpv](https://mpv.io) as the audio backend.

**Features:**
- Play/pause with spacebar
- Current song display (fetched from the station API)
- MPRIS D-Bus integration (media keys, desktop player widgets on Linux)
- Styled terminal interface via [Lip Gloss](https://github.com/charmbracelet/lipgloss)

**Requirements:** Go 1.24+, mpv

```sh
cd orlicko-tui
go build -o orlicko-tui .
./orlicko-tui
```

### orlicko-mobile

Kotlin Multiplatform (KMP) + Compose Multiplatform app targeting **Android** and **iOS** (iOS is untested).

**Features:**
- Play/pause streaming audio
- Stream quality selection (32/64/128/192 kbps)
- Volume control
- Now playing info display
- Lock screen media controls (notification media widget)
- Native audio engines (ExoPlayer on Android, AVPlayer on iOS)

**Requirements:** JDK 17+, Android SDK (for Android), Xcode (for iOS)

```sh
cd orlicko-mobile
./gradlew :composeApp:assembleDebug    # Android
```

## License

MIT License

Copyright (c) 2026 Václav Škorpil

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
