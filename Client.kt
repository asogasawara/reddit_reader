import masecla.reddit4j.client.Reddit4J
import masecla.reddit4j.client.UserAgentBuilder
import masecla.reddit4j.objects.RedditComment
import masecla.reddit4j.objects.RedditPost
import masecla.reddit4j.objects.subreddit.RedditSubreddit
import kotlin.system.exitProcess


fun handleUnrecoverableError(message: String) {
    println("Error: $message")
    exitProcess(1)
}


// These are used for creating the user agent.
// You should change AUTHOR.
private const val APP_NAME = "fundies homework 11"
private const val AUTHOR = "AOVrishi"
private const val VERSION = "0.1"




/**
 * A connection to the Reddit API for a specific user.
 */
object Connection {
    // Students don't need to worry about how these two statements work.
    private val userAgent = UserAgentBuilder().appname(APP_NAME).author(AUTHOR).version(VERSION).build()
    private val redditClient: Reddit4J = Reddit4J.rateLimited()


    init {
        try {
            redditClient.apply {
                username = USERNAME
                password = PASSWORD
                clientId = CLIENT_ID
                clientSecret = CLIENT_SECRET
                setUserAgent(userAgent)
                connect()
            }
        } catch (e: masecla.reddit4j.exceptions.AuthenticationException) {
            handleUnrecoverableError("Invalid credentials. Cannot authenticate with Reddit. Please check your credentials.")
        } catch (e: java.io.IOException) {
            handleUnrecoverableError("Unable to connect to the internet. Please check your network connection and try again.")
        } catch (e: Exception) {
            handleUnrecoverableError("An unexpected error occurred: ${e.localizedMessage}")
        }
    }


    /**
     * This user's name (from their profile).
     */
    val userName: String
        get() = redditClient.selfProfile.name


    /**
     * Gets the subreddit named [subredditName].
     */
    fun getSubreddit(subredditName: String): RedditSubreddit {
        return redditClient.getSubreddit(subredditName)
    }


    /**
     * Gets all comments for this [post].
     */
    fun getComments(post: RedditPost): List<RedditComment> {
        return redditClient.getCommentsForPost(post.subreddit, post.id).submit()
    }


    /**
     * Gets posts from [subreddit].
     *
     * @return text posts that are marked as being acceptable
     * for people under 18
     */
    fun getPosts(subreddit: RedditSubreddit): List<RedditPost> =
        subreddit.hot.submit().filter { !it.isOver18 }.filter { it.selftext.isNotEmpty() }
}




/**
 * An option to present to the user.
 *
 * @property text a textual description
 * @property function the function to call if the option is selected
 */
class Option(val text: String, val function: () -> Unit) {
    companion object {
        /**
         * Offers the user [options] of what to do next. In addition to showing
         * the passed options, there is always an option numbered 0 to quit the
         * program and a final option to select a subreddit.
         */
        fun offerOptions(options: List<Option>) {
            val allOptions = listOf(
                Option("Quit", function = { exitProcess(0) })
            ) + options + listOf(
                Option("Select a subreddit", function = { selectSubreddit() })
            )
            println("Select an option: ")
            for (i in allOptions.indices) {
                println("\t$i. ${allOptions[i].text}")
            }
            try {
                val input = readln().toIntOrNull()
                if (input == null || input !in allOptions.indices) {
                    println("Invalid input. Please enter a number between 0 and ${allOptions.size - 1}.")
                    offerOptions(options)
                    return
                }
                allOptions[input].function()
            } catch (e: NumberFormatException) {
                println("Please enter a valid number.")
                offerOptions(options)
            }
        }




        private fun showPostAuthor(posts: List<RedditPost>, postNumber: Int) {
            if (postNumber >= posts.size) {
                println("No more posts to show.")
                return
            }








            displayPost(posts[postNumber])




            val options = mutableListOf<Option>()
            options.add(Option("Show post author") { showPostAuthor(posts, postNumber) })
            options.add(Option("Check for comments") { checkForComments(posts, postNumber) })








            if (postNumber + 1 < posts.size) {
                options.add(Option("Show next post") { showPost(posts, postNumber + 1) })
            }








            offerOptions(options)
        }




        private fun checkForComments(posts: List<RedditPost>, postNumber: Int) {
            val options = mutableListOf(
                Option("Show post author", function = { showPostAuthor(posts, postNumber) }),
                Option("Show next post", function = { showPost(posts, postNumber + 1) }),
            )
            val comments: List<RedditComment> = Connection.getComments(posts[postNumber])
            println(
                when (comments.size) {
                    0 -> "There are no comments for this post."
                    1 -> "There is one comment for this post."
                    else -> "There are ${comments.size} comments for this post."
                }
            )
            if (comments.isNotEmpty()) {
                options.add(0, Option("Show first comment", function = { showComment(posts, postNumber, comments, 0) }))
            }
            offerOptions(options)
        }




        private fun displayPost(post: RedditPost) {
            println(post.title.uppercase())
            println()
            println(post.selftext)
            println()
        }




        private fun showPost(posts: List<RedditPost>, postNumber: Int) {
            if (postNumber >= posts.size) {
                println("No more posts to show.")
                return
            }




            displayPost(posts[postNumber])




            val options = mutableListOf<Option>()
            options.add(Option("Show post author") { showPostAuthor(posts, postNumber) })
            options.add(Option("Check for comments") { checkForComments(posts, postNumber) })




            if (postNumber + 1 < posts.size) {
                options.add(Option("Show next post") { showPost(posts, postNumber + 1) })
            }




            offerOptions(options)
        }




        private fun showComment(
            posts: List<RedditPost>, postNumber: Int, comments: List<RedditComment>, commentNumber: Int
        ) {
            if (commentNumber >= comments.size) {
                println("No more comments to show.")
                return
            }




            val comment = comments[commentNumber]
            println(comment.body)




            val options = mutableListOf<Option>()
            options.add(Option("Show comment author") {
                println("Author: ${comment.author}")
                showComment(posts, postNumber, comments, commentNumber)
            })
            options.add(Option("Show post again") { showPost(posts, postNumber) })




            if (commentNumber + 1 < comments.size) {
                options.add(Option("Show next comment") { showComment(posts, postNumber, comments, commentNumber + 1) })
            }
            if (postNumber + 1 < posts.size) {
                options.add(Option("Show next post") { showPost(posts, postNumber + 1) })
            }




            offerOptions(options)
        }




        private fun quit() {
            println("Goodbye.")
            exitProcess(0)
        }




        private fun selectSubreddit() {
            println("What subreddit would you like to select? ")
            val subredditName = readlnOrNull()?.trim()
            if (subredditName.isNullOrEmpty()) {
                println("Subreddit name cannot be empty. Please try again.")
                selectSubreddit()
                return
            }
            try {
                val subreddit: RedditSubreddit = Connection.getSubreddit(subredditName)
                println("You are now in ${subreddit.displayName}.")
                val posts = Connection.getPosts(subreddit)
                println(
                    when (posts.size) {
                        0 -> "There are no posts."
                        1 -> "There is one post."
                        else -> "There are ${posts.size} posts."
                    }
                )




                val options = mutableListOf<Option>()
                if (posts.isNotEmpty()) {
                    options.add(Option("Show first post") { showPost(posts, 0) })
                }
                offerOptions(options)
            } catch (e: Exception) {
                println("Failed to retrieve subreddit '$subredditName'. Error: ${e.localizedMessage}")
                println("Would you like to retry? (y/n)")
                when (readlnOrNull()?.lowercase()) {
                    "y", "yes" -> selectSubreddit()
                    else -> offerOptions(emptyList())
                }
            }
        }
    }
}




fun main() {
    println("Hello, ${Connection.userName}.")
    Option.offerOptions(emptyList())
}

