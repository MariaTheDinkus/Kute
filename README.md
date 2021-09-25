<p align="center">
  <img src="https://i.imgur.com/8QG2t5u.png" alt="Kute's logo"/>
</p>

<h1 align="center">Kute</h1>
<h2 align="center">A music bot that just works.</h2>

## Commands
* `|` stands for `or`!

`!prefix <new prefix>` - change the command prefix.

`!play | !p <YouTube Search Query>` - play a YouTube video matching those terms.

`!play | !p <YouTube Video Link>` - play the link's YouTube video.

`!play | !p <YouTube Playlist Link>` - play the link's YouTube playlist.

`!play | !p <Spotify Track Link>` - play the link's Spotify track.

`!play | !p <Spotify Playlist Link>` - play the link's Spotify playlist.

`!play | !p <Spotify Album Link>` - play the link's Spotify album.

`!pause` - pauses the current song.

`!skip | !s` - skips the current song.

`!queue | !q` - lists the current song queue.

`!repeat | !r` - toggles song repetition.

## Building

Run `.\gradlew build` in the project's root folder - the build result will be under `build\libs\kute-x.x.x.jar`.

## Running

Run `kute-x.x.x.jar`, providing the following environment variables:

 - `YOUTUBE_API_KEY` - a YouTube Data API v3 key.

 - `DISCORD_TOKEN` - a Discord application's token.

 - `SPOTIFY_CLIENT_ID` - a Spotify application's client ID.

 - `SPOTIFY_CLIENT_SECRET` - a Spotify application's client secret.

 - `DATABASE_URL` - a JDBC URL to an H2 or PostgreSQL database (to use a local H2 database, use `jdbc:h2:file:./db.sql`).