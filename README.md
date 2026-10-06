# Disney-style Movie Watchlist (Kotlin Console)

A small Kotlin/JVM console application: a to-do list of movies I plan to watch, only films from
2000-2015 in the style of Disney/Pixar (animated, live-action and Disney Channel movies).
Movies can be added, marked as watched with a rating, filtered and grouped.

## How to run
1. Open the project in IntelliJ IDEA (Kotlin/JVM, JDK 17+).
2. Let Gradle sync, then run `main()` in `src/main/kotlin/Main.kt`.
3. Or from terminal: `gradle run` (or `./gradlew run` if the Gradle wrapper is present).

## Where the requirements are demonstrated (all in `Main.kt`)
- **Variables, types, conditions, loops:** `main()` (`val`/`var`, `for`, `if`), `WatchList.add`
- **List, Set, Map:** `WatchList` (`movies`, `watched`, `ratings`)
- **map, filter, reduce:** `main()` (`musicalTitles`, `countByType`), `totalOf`
- **Functions, higher-order functions, lambdas:** `filterMovies`, `forEachItem`, `totalOf`, lambdas in `main()`
- **Classes and objects:** `WatchList`, `LiveActionMovie`, `ChannelMovie`
- **Inheritance:** `AnimatedMovie`, `LiveActionMovie`, `ChannelMovie` extend abstract class `Movie`
- **Interfaces and polymorphism:** interface `Describable`, overridden `describe()`
- **Data class:** `AnimatedMovie` (with `copy()` demo)
- **Sealed class:** `AddResult`, handled with `when` in `message()`
- **Suspend function and coroutine:** `sendReminder`, `syncWatchlist`, `runBlocking`/`launch`/`async` in `main()`
