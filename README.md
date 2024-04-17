# THICCDUSTRY

The automation tower defense RTS, written in Java, thiccdustrized.

## Contributing

Just contact me @leomathguy (discord)

## Getting

Just look in releases lmao

### Windows

_Running:_ `gradlew desktop:run`  
_Building:_ `gradlew desktop:dist`  
_Sprite Packing:_ `gradlew tools:pack`

### Linux/Mac OS

_Running:_ `./gradlew desktop:run`  
_Building:_ `./gradlew desktop:dist`  
_Sprite Packing:_ `./gradlew tools:pack`

### Android

Figure it out yourself, I add unsigned sdks in releases

### Troubleshooting

#### Permission Denied

If the terminal returns `Permission denied` or `Command not found` on Mac/Linux, run `chmod +x ./gradlew` before running `./gradlew`. *This is a one-time procedure.*

---

Gradle may take up to several minutes to download files. Be patient. <br>
After building, the output .JAR file should be in `/desktop/build/libs/Thiccdustry.jar` for desktop builds, and in `/server/build/libs/server-release.jar` for server builds.
