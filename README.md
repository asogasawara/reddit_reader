# Reddit Reader

A command-line Reddit browser built in Kotlin that lets you explore subreddits, read posts, and view comments — all from your terminal.

> Fundies Homework 11 — December 2024

## Features

- **Browse subreddits** — Select any subreddit and pull its hot posts
- **Read posts & comments** — Navigate through post content, view comment threads, and check authors
- **Interactive CLI menu** — Numbered option system for seamless navigation between posts and comments
- **Safe browsing** — Automatically filters out NSFW content and empty text posts

## How It Works

The app connects to the Reddit API via [Reddit4J](https://github.com/masecla22/Reddit4J) and presents an interactive loop:

```
Select a subreddit → View posts → Read content / Check comments → Navigate or quit
```

Users can move forward through posts, dive into comment sections, reveal authors, or jump to a new subreddit at any point.

## Setup

1. Create a Reddit app at [reddit.com/prefs/apps](https://www.reddit.com/prefs/apps) to get your credentials
2. Add your `USERNAME`, `PASSWORD`, `CLIENT_ID`, and `CLIENT_SECRET` to a credentials file
3. Build and run:
   ```bash
   ./gradlew run
   ```

## Tech Stack

- Kotlin
- Reddit4J (Reddit API wrapper)
- Reddit API (OAuth2)
