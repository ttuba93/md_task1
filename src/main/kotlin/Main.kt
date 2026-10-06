import kotlinx.coroutines.*

enum class Genre { ADVENTURE, FAMILY, MUSICAL, COMEDY, FANTASY }

interface Describable {
    fun describe(): String
}

abstract class Movie : Describable {
    abstract val title: String
    abstract val year: Int
    abstract val genre: Genre
    abstract val minutes: Int
    abstract val type: String

    override fun describe() = "$title ($year), $genre, $minutes min"
}

data class AnimatedMovie(
    override val title: String,
    override val year: Int,
    override val genre: Genre,
    override val minutes: Int,
    val studio: String
) : Movie() {
    override val type = "Animated"
    override fun describe() = "Animated: ${super.describe()}, studio: $studio"
}

class LiveActionMovie(
    override val title: String,
    override val year: Int,
    override val genre: Genre,
    override val minutes: Int
) : Movie() {
    override val type = "Live-action"
    override fun describe() = "Live-action: ${super.describe()}"
}

class ChannelMovie(
    override val title: String,
    override val year: Int,
    override val genre: Genre,
    override val minutes: Int
) : Movie() {
    override val type = "Channel movie"
    override fun describe() = "TV movie: ${super.describe()}"
}

sealed class AddResult {
    data class Added(val movie: Movie) : AddResult()
    data class Duplicate(val movie: Movie) : AddResult()
    data class YearOutOfRange(val title: String, val year: Int) : AddResult()
}

fun message(result: AddResult): String = when (result) {
    is AddResult.Added -> "Added to watchlist: '${result.movie.title}'"
    is AddResult.Duplicate -> "'${result.movie.title}' is already in the watchlist"
    is AddResult.YearOutOfRange -> "'${result.title}' (${result.year}) is outside 2000-2015"
}

class WatchList {
    private val movies = mutableListOf<Movie>()
    private val watched = mutableSetOf<String>()
    private val ratings = mutableMapOf<String, Int>()
    private val yearRange = 2000..2015

    fun add(movie: Movie): AddResult {
        if (movie.year !in yearRange) return AddResult.YearOutOfRange(movie.title, movie.year)
        if (movies.any { it.title.equals(movie.title, ignoreCase = true) }) {
            return AddResult.Duplicate(movie)
        }
        movies.add(movie)
        return AddResult.Added(movie)
    }

    fun all(): List<Movie> = movies

    fun isWatched(movie: Movie): Boolean = movie.title in watched

    fun planned(): List<Movie> = movies.filter { !isWatched(it) }

    fun markWatched(title: String, rating: Int): Boolean {
        val movie = movies.find { it.title.equals(title, ignoreCase = true) } ?: return false
        watched.add(movie.title)
        ratings[movie.title] = rating
        return true
    }

    fun ratingsMap(): Map<String, Int> = ratings
}

fun filterMovies(movies: List<Movie>, predicate: (Movie) -> Boolean): List<Movie> =
    movies.filter(predicate)

fun <T> forEachItem(list: List<T>, action: (T) -> Unit) {
    for (element in list) action(element)
}

fun totalOf(movies: List<Movie>, selector: (Movie) -> Int): Int =
    movies.map(selector).reduce { sum, value -> sum + value }

suspend fun sendReminder(movie: Movie) {
    delay(300)
    println("  Tonight's idea: '${movie.title}' (${movie.minutes} min)")
}

suspend fun syncWatchlist(count: Int): String {
    delay(500)
    return "Synced $count movies to the cloud"
}

fun main() {
    val watchList = WatchList()

    val appName: String = "My Disney-style Watchlist (2000-2015)"
    var addedCount = 0
    val shortMovieLimit = 100

    val candidates = listOf(
        AnimatedMovie("Tangled", 2010, Genre.ADVENTURE, 100, "Disney"),
        AnimatedMovie("Frozen", 2013, Genre.MUSICAL, 102, "Disney"),
        AnimatedMovie("Lilo & Stitch", 2002, Genre.FAMILY, 85, "Disney"),
        AnimatedMovie("Big Hero 6", 2014, Genre.ADVENTURE, 102, "Disney"),
        AnimatedMovie("Up", 2009, Genre.ADVENTURE, 96, "Pixar"),
        AnimatedMovie("Finding Nemo", 2003, Genre.FAMILY, 100, "Pixar"),
        AnimatedMovie("Inside Out", 2015, Genre.FAMILY, 95, "Pixar"),
        LiveActionMovie("Enchanted", 2007, Genre.FANTASY, 107),
        LiveActionMovie("Alice in Wonderland", 2010, Genre.FANTASY, 108),
        ChannelMovie("High School Musical", 2006, Genre.MUSICAL, 98),
        AnimatedMovie("Moana", 2016, Genre.ADVENTURE, 107, "Disney"),
        AnimatedMovie("Frozen", 2013, Genre.MUSICAL, 102, "Disney")
    )

    println("=== $appName ===")

    for (movie in candidates) {
        val result = watchList.add(movie)
        println(message(result))
        if (result is AddResult.Added) addedCount++
    }
    println("Movies in the watchlist: $addedCount")

    println("\n--- Planned movies ---")
    for (movie in watchList.planned()) {
        println(movie.describe())
    }

    val tangled = AnimatedMovie("Tangled", 2010, Genre.ADVENTURE, 100, "Disney")
    val rewatch = tangled.copy(title = "Tangled (rewatch)")
    println("\nOriginal: $tangled")
    println("Copy:     $rewatch")
    println("Same data? ${tangled == tangled.copy()}")

    val musicalTitles = watchList.all()
        .filter { it.genre == Genre.MUSICAL }
        .map { it.title }
    val totalMinutes = totalOf(watchList.planned()) { it.minutes }
    val countByType = watchList.all()
        .groupBy { it.type }
        .mapValues { entry -> entry.value.size }
    val byYear = watchList.all().sortedBy { it.year }.map { "${it.year} ${it.title}" }

    println("\nMusicals: $musicalTitles")
    println("Total time to watch everything: $totalMinutes min (${totalMinutes / 60} h)")
    println("Movies by type: $countByType")
    println("Sorted by year: $byYear")

    val shortMovies = filterMovies(watchList.planned()) { it.minutes <= shortMovieLimit }
    println("Short movies (<= $shortMovieLimit min):")
    forEachItem(shortMovies) { println(" - ${it.title}") }

    println("\n--- Watched ---")
    println("Up watched: ${watchList.markWatched("Up", 5)}")
    println("Frozen watched: ${watchList.markWatched("Frozen", 4)}")
    println("Unknown watched: ${watchList.markWatched("Unknown", 3)}")
    val ratings = watchList.ratingsMap()
    println("My ratings: $ratings")
    println("Average rating: ${ratings.values.average()}")
    println("Still planned: ${watchList.planned().map { it.title }}")

    println("\n--- Coroutines ---")
    runBlocking {
        coroutineScope {
            for (movie in watchList.planned().filter { it.minutes <= shortMovieLimit }.take(2)) {
                launch { sendReminder(movie) }
            }
            val sync = async { syncWatchlist(watchList.all().size) }
            println(sync.await())
        }
        println("Done.")
    }
}
