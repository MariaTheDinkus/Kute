/**
 * The MIT License
 *
 * Copyright (c) 2020 vini2003
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package dev.vini2003.kute.external.youtube

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

/*
	As of now, actions which require more authentication
	than an API key are not supported.
 */

val ktorClient = HttpClient(CIO) {
	install(JsonFeature) {
		serializer = KotlinxSerializer()
	}
	
	expectSuccess = false
}

/*
	YouTube
 */
class YoutubeClient(var key: String) {
	fun key(key: String) {
		this.key = key
	}

	@Serializable
	data class Thumbnail(val url: String = "", val width: Int = 0, val height: Int = 0)

	@Serializable
	class Localizations : HashMap<String, Localizations.Localization>() {
		@Serializable
		data class Localization(
				val title: String
		)
	}

	@Serializable
	open class YouTubeResource

	abstract class YouTubeRequest<T : YouTubeResource> (
		private val endpoint: String,
		private val client: YoutubeClient,
		private val parts: MutableList<String> = mutableListOf(),
		private val options: MutableMap<String, Any> = mutableMapOf(),
	) {
		protected fun part(value: String) {
			parts.add(value)
		}

		protected fun option(key: String, value: Any) {
			options[key] = value
		}

		val url
			get() = Url("https://www.googleapis.com/youtube/v3/${endpoint}?part=${parts.joinToString("%2C")}${options.map { "&${it.key}=${it.value}" }.joinToString("")}&key=${client.key}")

		abstract suspend fun execute(): T
	}

	/**
	 * For details on Activities, see:
	 * https://developers.google.com/youtube/v3/docs/activities
	 */

	suspend fun getActivities(block: ActivityListRequest.() -> Unit): ActivityList = ActivityListRequest(this).apply(block).execute()
	

	class ActivityListRequest(client: YoutubeClient) : YouTubeRequest<ActivityList>("activities", client) {
		/**
		 * Part
		 */
		fun requestContentDetails() = part("contentDetails")

		fun requestId() = part("id")

		fun requestSnippet() = part("snippet")

		/**
		 * Required
		 */
		fun filterChannelId(channelId: String) = option("channelId", channelId)

		fun filterMine(mine: Boolean): Unit =  option("mine", mine)

		/**
		 * Optional
		 */
		fun maxResults(maxResults: Int) = option("maxResults", maxResults)

		fun pageToken(pageToken: String) = option("pageToken", pageToken)

		fun publishedAfter(publishedAfter: String) = option("publishedAfter", publishedAfter)

		fun publishedBefore(publishedBefore: String) = option("publishedBefore", publishedBefore)

		fun regionCode(regionCode: String) = option("regionCode", regionCode)

		/**
		 * Request
		 */
		override suspend fun execute(): ActivityList = ktorClient.get(url)
	}

	@Serializable
	data class ActivityList(
			val kind: String = "",
			val etag: String = "",
			val items: List<Activity> = emptyList(),
			val nextPageToken: String = "",
			val pageInfo: PageInfo = PageInfo()
	) : YouTubeResource() {
		@Serializable
		data class PageInfo(val totalResults: Int = 0, val resultsPerPage: Int = 0)
	}

	@Serializable
	data class Activity(
			private val kind: String = "",
			private val etag: String = "",
			private val id: String = "",
			private val snippet: Snippet = Snippet(),
			private val contentDetails: ContentDetails = ContentDetails(),
	) {
		@Serializable
		data class Snippet(
				val publishedAt: String = "",
				val channelId: String = "",
				val title: String = "",
				val description: String = "",
				val thumbnails: Map<String, Thumbnail> = emptyMap(),
				val channelTitle: String = "",
				val type: String = "",
				val groupId: String = "",
		)

		@Serializable
		data class ContentDetails(
				val upload: Upload = Upload(),
				val like: Like = Like(),
				val favorite: Favorite = Favorite(),
				val comment: Comment = Comment(),
				val subscription: Subscription = Subscription(),
				val playlistItem: PlaylistItem = PlaylistItem(),
				val recommendation: Recommendation = Recommendation(),
				val social: Social = Social(),
		) {
			@Serializable
			data class Upload(val videoId: String = "")

			@Serializable
			data class Like(val resourceId: ResourceId = ResourceId()) {
				@Serializable
				data class ResourceId(val kind: String = "", val videoId: String = "")
			}

			@Serializable
			data class Favorite(val resourceId: ResourceId = ResourceId()) {
				@Serializable
				data class ResourceId(val kind: String = "", val videoId: String = "")
			}

			@Serializable
			data class Comment(val resourceId: ResourceId = ResourceId()) {
				@Serializable
				data class ResourceId(val kind: String = "", val videoId: String = "", val channelId: String = "")
			}

			@Serializable
			data class Subscription(val resourceId: ResourceId = ResourceId()) {
				@Serializable
				data class ResourceId(val kind: String = "", val channelId: String = "")
			}

			@Serializable
			data class PlaylistItem(val resourceId: ResourceId = ResourceId(), val playlistId: String = "", val playlistItemId: String = "") {
				@Serializable
				data class ResourceId(val kind: String = "", val videoId: String = "")
			}

			@Serializable
			data class Recommendation(val resourceId: ResourceId = ResourceId(), val reason: String = "", val seedResourceId: SeedResourceId = SeedResourceId()) {
				@Serializable
				data class ResourceId(val kind: String = "", val videoId: String = "", val channelId: String = "")

				@Serializable
				data class SeedResourceId(val kind: String = "", val videoId: String = "", val channelId: String = "", val playlistId: String = "")
			}

			@Serializable
			data class Social(val type: String = "", val resourceId: ResourceId = ResourceId(), val author: String = "", val referenceUrl: String = "", val imageUrl: String = "") {
				@Serializable
				data class ResourceId(val kind: String = "", val videoId: String = "", val channelId: String = "", val playlistId: String = "")
			}
		}
	}

	/**
	 * For details on Captions, see:
	 * https://developers.google.com/youtube/v3/docs/captions
	 */

/*
	Due to lack of authentication, Captions miss the following endpoints:
	- insert
	- update
	- download
	- delete
 */
	
	suspend fun getCaptions(block: CaptionListRequest.() -> Unit): CaptionList = CaptionListRequest(this).apply(block).execute()

	class CaptionListRequest(client: YoutubeClient) : YouTubeRequest<CaptionList>("captions", client) {
		/**
		 * Part
		 */
		fun requestSnippet() = part("snippet")

		fun requestId() = part("id")

		/**
		 * Required
		 */
		fun filterVideoId(videoId: String) = option("videoId", videoId)

		/**
		 * Optional
		 */
		fun id(id: String) = option("id", id)

		fun onBehalfOfContentOwner(onBehalfOfContentOwner: String) = option("onBehalfOfContentOwner", onBehalfOfContentOwner)

		/**
		 * Request
		 */
		override suspend fun execute(): CaptionList = ktorClient.get(url)
	}


	@Serializable
	data class CaptionList(
			val kind: String = "",
			val etag: String = "",
			val items: List<Caption> = emptyList()
	) : YouTubeResource()

	@Serializable
	data class Caption(
			val kind: String = "",
			val etag: String = "",
			val id: String = "",
			val snippet: Snippet = Snippet(),
	) {
		@Serializable
		data class Snippet(
				val videoId: String = "",
				val lastUpdated: String = "",
				val trackKind: String = "",
				val language: String = "",
				val name: String = "",
				val audioTrackType: String = "",
				val isCC: Boolean = false,
				val isLarge: Boolean = false,
				val isEasyReader: Boolean = false,
				val isDraft: Boolean = false,
				val isAutoSynced: Boolean = false,
				val status: String = "",
				val failureReason: String = "",
		)
	}

	/**
	 * For details on Channels, see:
	 * https://developers.google.com/youtube/v3/docs/channels
	 */

/*
	Due to lack of authentication, Channels miss the following endpoints:
	- update
 */
	
	suspend fun getChannels(block: ChannelListRequest.() -> Unit): ChannelList = ChannelListRequest(this).apply(block).execute()

	class ChannelListRequest(client: YoutubeClient) : YouTubeRequest<ChannelList>("channels", client) {
		/**
		 * Part
		 */
		fun requestAuditDetails() = part("auditDetails")

		fun requestBrandingSettings() = part("brandingSettings")

		fun requestContentDetails() = part("contentDetails")

		fun requestContentOwnerDetails() = part("contentOwnerDetails")

		fun requestId() = part("id")

		fun requestLocalizations() = part("localizations")

		fun requestSnippet() = part("snippet")

		fun requestStatistics() = part("statistics")

		fun requestStatus() = part("status")

		fun requestTopicDetails() = part("topicDetails")

		/**
		 * Required
		 */
		fun filterCategoryId(categoryId: String) = option("categoryId", categoryId)

		fun filterForUsername(forUsername: String) = option("forUsername", forUsername)

		fun filterId(id: String) = option("id", id)

		fun filterManagedByMe(managedByMe: Boolean) = option("managedByMe", managedByMe)

		fun filterMine(mine: Boolean) = option("mine", mine)

		/**
		 * Optional
		 */
		fun hl(hl: String) = option("hl", hl)

		fun maxResults(maxResults: Int) = option("maxResults", maxResults)

		fun onBehalfOfContentOwner(onBehalfOfContentOwner: String) = option("onBehalfOfContentOwner", onBehalfOfContentOwner)

		fun pageToken(pageToken: String) = option("pageToken", pageToken)

		/**
		 * Request
		 */
		override suspend fun execute(): ChannelList = ktorClient.get(url)
	}


	@Serializable
	data class ChannelList(
			val kind: String = "",
			val etag: String = "",
			val nextPageToken: String = "",
			val prevPageToken: String = "",
			val pageInfo: PageInfo = PageInfo(),
			val items: List<Channel> = emptyList()
	) : YouTubeResource() {
		@Serializable
		data class PageInfo(val totalResults: Int = 0, val resultsPerPage: Int = 0)
	}

	@Serializable
	data class Channel(
			val kind: String = "",
			val etag: String = "",
			val id: String = "",
			val snippet: Snippet = Snippet(),
			val contentDetails: ContentDetails = ContentDetails(),
			val statistics: Statistics = Statistics(),
			val topicDetails: TopicDetails = TopicDetails(),
			val status: Status = Status(),
			val brandingSettings: BrandingSettings = BrandingSettings(),
			val auditDetails: AuditDetails = AuditDetails(),
			val contentOwnerDetails: ContentOwnerDetails = ContentOwnerDetails(),
			val localizations: Localizations = Localizations(),
	) {
		@Serializable
		data class Snippet(
				val title: String = "",
				val description: String = "",
				val customUrl: String = "",
				val publishedAt: String = "",
				val thumbnails: Map<String, Thumbnail> = emptyMap(),
				val defaultLanguage: String = "",
				val localized: Localized = Localized(),
				val country: String = "",
		) {
			@Serializable
			data class Localized(
					val title: String = "",
					val description: String = "",
			)
		}

		@Serializable
		data class ContentDetails(
				val relatedPlaylists: RelatedPlaylists = RelatedPlaylists()
		) {
			@Serializable
			data class RelatedPlaylists(
					val likes: String = "",
					val favorites: String = "",
					val uploads: String = "",
			)
		}

		@Serializable
		data class Statistics(
				val viewCount: Long = 0L,
				val subscriberCount: Long = 0L,
				val commentCount: Long = 0L,
				val hiddenSubscriberCount: Boolean = false,
				val videoCount: Long = 0L,
		)

		@Serializable
		data class TopicDetails(
				val topicIds: List<String> = emptyList(),
				val topicCategories: List<String> = emptyList(),
		)

		@Serializable
		data class Status(
				val privacyStatus: String = "",
				val isLinked: Boolean = false,
				val longUploadsStatus: String = "",
				val madeForKids: Boolean = false,
				val selfDeclaredMadeForKids: Boolean = false,
		)

		@Serializable
		data class BrandingSettings(
				val channel: Channel = Channel(),
				val watch: Watch = Watch()
		) {
			@Serializable
			data class Channel(
					val title: String = "",
					val description: String = "",
					val keywords: String = "",
					val defaultTab: String = "",
					val trackingAnalyticsAccountId: String = "",
					val moderateComments: Boolean = false,
					val showRelatedChannels: Boolean = false,
					val showBrowseView: Boolean = false,
					val featuredChannelsTitle: String = "",
					val featuredChannelsUrls: List<String> = emptyList(),
					val unsubscribedTrailer: String = "",
					val profileColor: String = "",
					val defaultLanguage: String = "",
					val country: String = "",
			)

			@Serializable
			data class Watch(
					val textColor: String = "",
					val backgroundColor: String = "",
					val featuredPlaylistId: String = "",
			)
		}

		@Serializable
		data class AuditDetails(
				val overallGoodStanding: Boolean = false,
				val communityGuidelinesGoodStanding: Boolean = false,
				val copyrightStrikesGoodStanding: Boolean = false,
				val contentIdClaimsGoodStanding: Boolean = false,
		)

		@Serializable
		data class ContentOwnerDetails(
				val contentOwner: String = "",
				val timeLinked: String = ""
		)
	}

	/**
	 * For details on ChannelSections, see:
	 * https://developers.google.com/youtube/v3/docs/channelSections
	 */

/*
	Due to lack of authentication, ChannelSections miss the following endpoints:
	- insert
	- update
	- delete
 */
	
	suspend fun getChannelSections(block: ChannelSectionListRequest.() -> Unit): ChannelSectionList = ChannelSectionListRequest(this).apply(block).execute()

	class ChannelSectionListRequest(client: YoutubeClient) : YouTubeRequest<ChannelSectionList>("channelSections", client) {
		/**
		 * Part
		 */
		fun requestContentDetails() = part("contentDetails")

		fun requestId() = part("id")

		fun requestLocalizations() = part("localizations")

		fun requestSnippet() = part("snippet")

		fun requestTargeting() = part("targeting")

		/**
		 * Required
		 */
		fun filterChannelId(channelId: String) = option("channelId", channelId)

		fun filterId(id: String) = option("id", id)

		fun filterMine(mine: Boolean) = option("mine", mine)

		/**
		 * Optional
		 */
		fun hl(hl: String) = option("hl", hl)

		fun onBehalfOfContentOwner(onBehalfOfContentOwner: String) = option("onBehalfOfContentOwner", onBehalfOfContentOwner)

		/**
		 * Request
		 */
		override suspend fun execute(): ChannelSectionList = ktorClient.get(url)
	}


	@Serializable
	data class ChannelSectionList(
			val kind: String = "",
			val etag: String = "",
			val items: List<ChannelSection> = emptyList()
	) : YouTubeResource()

	@Serializable
	data class ChannelSection(
			val kind: String = "",
			val etag: String = "",
			val id: String = "",
			val snippet: Snippet = Snippet(),
			val contentDetails: ContentDetails = ContentDetails(),
			val localizations: Localizations = Localizations(),

			) {
		@Serializable
		data class Snippet(
				val type: String = "",
				val style: String = "",
				val channelId: String = "",
				val title: String = "",
				val position: Long = 0L,
				val defaultLanguage: String = "",
				val localized: Localized = Localized(),
				val targeting: Targeting = Targeting(),
		) {
			@Serializable
			data class Localized(val title: String = "")
		}

		@Serializable
		data class ContentDetails(
				val playlists: List<String> = emptyList(),
				val channels: List<String> = emptyList(),
		)

		@Serializable
		data class Targeting(
				val languages: List<String> = emptyList(),
				val regions: List<String> = emptyList(),
				val countries: List<String> = emptyList(),
		)
	}

	/**
	 * For details on Comments, see:
	 * https://developers.google.com/youtube/v3/docs/comments
	 */

/*
	Due to lack of authentication, Comments miss the following endpoints:
	- insert
	- update
	- markAsSpam
	- setModerationStatus
	- delete
 */
	
	suspend fun getComments(block: CommentListRequest.() -> Unit): CommentList = CommentListRequest(this).apply(block).execute()

	class CommentListRequest(client: YoutubeClient) : YouTubeRequest<CommentList>("comments", client) {
		/**
		 * Part
		 */
		fun requestId() = part("id")

		fun requestSnippet() = part("snippet")

		/**
		 * Required
		 */
		fun filterId(id: String) = option("id", id)

		fun filterParentId(parentId: String) = option("parentId", parentId)

		/**
		 * Optional
		 */
		fun maxResults(maxResults: Int) = option("maxResults", maxResults)

		fun pageToken(pageToken: String) = option("pageToken", pageToken)

		fun textFormat(textFormat: String) = option("textFormat", textFormat)

		/**
		 * Request
		 */
		override suspend fun execute(): CommentList = ktorClient.get(url)
	}

	@Serializable
	data class CommentList(
			val kind: String = "",
			val etag: String = "",
			val nextPageToken: String = "",
			val pageInfo: PageInfo = PageInfo(),
			val items: List<Comment> = emptyList(),
	) : YouTubeResource() {
		@Serializable
		data class PageInfo(val totalResults: Int = 0, val resultsPerPage: Int = 0)
	}

	@Serializable
	data class Comment(
			val kind: String = "",
			val etag: String = "",
			val id: String = "",
			val snippet: Snippet = Snippet(),
	) {
		@Serializable
		data class Snippet(
				val authorDisplayName: String = "",
				val authorProfileImageUrl: String = "",
				val authorChannelUrl: String = "",
				val authorChannelId: AuthorChannelId = AuthorChannelId(),
				val channelId: String = "",
				val videoId: String = "",
				val textDisplay: String = "",
				val textOriginal: String = "",
				val parentId: String = "",
				val canRate: Boolean = false,
				val viewerRating: String = "",
				val likeCount: Int = 0,
				val moderationStatus: String = "",
				val publishedAt: String = "",
				val updatedAt: String = "",
		) {
			@Serializable
			data class AuthorChannelId(val value: String = "")
		}
	}

	/**
	 * For details on CommentThreads, see:
	 * https://developers.google.com/youtube/v3/docs/commentThreads
	 */

/*
	Due to lack of authentication, CommentThreads miss the following endpoints:
	- insert
	- update
 */
	
	suspend fun getCommentThreads(block: CommentThreadListRequest.() -> Unit): CommentThreadList = CommentThreadListRequest(this).apply(block).execute()

	class CommentThreadListRequest(client: YoutubeClient) : YouTubeRequest<CommentThreadList>("commentThreads", client) {
		/**
		 * Part
		 */
		fun requestId() = part("id")

		fun requestSnippet() = part("snippet")

		fun requestReplies() = part("replies")

		/**
		 * Required
		 */
		fun filterAllThreadsRelatedToChannelId(allThreadsRelatedToChannelId: String) = option("allThreadsRelatedToChannelId", allThreadsRelatedToChannelId)

		fun filterChannelId(channelId: String) = option("channelId", channelId)

		fun filterId(id: String) = option("id", id)

		fun filterVideoId(videoId: String) = option("videoId", videoId)

		/**
		 * Optional
		 */
		fun maxResults(maxResults: Int) = option("maxResults", maxResults)

		fun moderationStatus(moderationStatus: String) = option("moderationStatus", moderationStatus)

		fun order(order: String) = option("order", order)

		fun pageToken(pageToken: String) = option("pageToken", pageToken)

		fun searchTerms(searchTerms: String) = option("searchTerms", searchTerms)

		fun textFormat(textFormat: String) = option("textFormat", textFormat)

		/**
		 * Request
		 */
		override suspend fun execute(): CommentThreadList = ktorClient.get(url)
	}

	@Serializable
	data class CommentThreadList(
			val kind: String = "",
			val etag: String = "",
			val nextPageToken: String = "",
			val pageInfo: PageInfo = PageInfo(),
			val items: List<CommentThread> = emptyList()
	) : YouTubeResource() {
		@Serializable
		data class PageInfo(val totalResults: Int = 0, val resultsPerPage: Int = 0)
	}

	@Serializable
	data class CommentThread(
			val kind: String = "",
			val etag: String = "",
			val id: String = "",
			val snippet: Snippet = Snippet(),
			val replies: Replies = Replies(),
	) {
		@Serializable
		data class Snippet(
				val channelId: String = "",
				val videoId: String = "",
				val topLevelComment: Comment = Comment(),
				val canReply: Boolean = false,
				val totalReplyCount: Int = 0,
				val isPublic: Boolean = false,
		)

		@Serializable
		data class Replies(
				val comments: List<Comment> = emptyList()
		)
	}

	/**
	 * For details on i18nLanguages, see:
	 * https://developers.google.com/youtube/v3/docs/i18nLanguages
	 */
	
	suspend fun getI18nLanguages(block: I18nLanguageListRequest.() -> Unit): I18nLanguageList = I18nLanguageListRequest(this).apply(block).execute()

	class I18nLanguageListRequest(client: YoutubeClient) : YouTubeRequest<I18nLanguageList>("i18nLanguages", client) {
		/**
		 * Part
		 */
		fun requestSnippet() = part("snippet")

		/**
		 * Request
		 */
		override suspend fun execute(): I18nLanguageList = ktorClient.get(url)
	}

	@Serializable
	data class I18nLanguageList(
			val kind: String = "",
			val etag: String = "",
			val items: List<I18nLanguage> = emptyList()
	) : YouTubeResource()

	@Serializable
	data class I18nLanguage(
			val kind: String = "",
			val etag: String = "",
			val id: String = "",
			val snippet: Snippet = Snippet(),
	) {
		@Serializable
		data class Snippet(
				val hl: String = "",
				val name: String = "",
		)
	}

	/**
	 * For details on i18nRegions, see:
	 * https://developers.google.com/youtube/v3/docs/i18nRegions
	 */
	
	suspend fun getI18nRegions(block: I18nRegionListRequest.() -> Unit): I18nRegionList = I18nRegionListRequest(this).apply(block).execute()

	class I18nRegionListRequest(client: YoutubeClient) : YouTubeRequest<I18nRegionList>("i18nRegions", client) {
		/**
		 * Part
		 */
		fun requestSnippet() = part("snippet")

		/**
		 * Request
		 */
		override suspend fun execute(): I18nRegionList = ktorClient.get(url)
	}

	@Serializable
	data class I18nRegionList(
			val kind: String = "",
			val etag: String = "",
			val items: List<I18nRegion> = emptyList()
	) : YouTubeResource()

	@Serializable
	data class I18nRegion(
			val kind: String = "",
			val etag: String = "",
			val id: String = "",
			val snippet: Snippet = Snippet(),
	) {
		@Serializable
		data class Snippet(
				val gl: String = "",
				val name: String = "",
		)
	}

	/**
	 * For details on Members, see:
	 * https://developers.google.com/youtube/v3/docs/members
	 */

/*
	Due to lack of authentication, Members miss the following endpoints:
	- list
 */

	/**
	 * For details on MembershipLevels, see:
	 * https://developers.google.com/youtube/v3/docs/membershipsLevels
	 */

/*
	Due to lack of authentication, MembershipLevels miss the following endpoints:
	- insert
	- update
	- delete
 */

	/**
	 * For details on PlaylistItems, see:
	 * https://developers.google.com/youtube/v3/docs/playlistItems
	 */

/*
	Due to lack of authentication, PlaylistItems miss the following endpoints:
	- insert
	- update
	- delete
 */
	
	suspend fun getPlaylistItems(block: PlaylistItemListRequest.() -> Unit): PlaylistItemList = PlaylistItemListRequest(this).apply(block).execute()

	class PlaylistItemListRequest(client: YoutubeClient) : YouTubeRequest<PlaylistItemList>("playlistItems", client) {
		/**
		 * Part
		 */
		fun requestContentDetails() = part("contentDetails")

		fun requestId() = part("id")

		fun requestSnippet() = part("snippet")

		fun requestStatus() = part("status")

		/**
		 * Required
		 */
		fun filterId(id: String) = option("id", id)

		fun filterPlaylistId(playlistId: String) = option("playlistId", playlistId)

		/**
		 * Optional
		 */
		fun maxResults(maxResults: Int) = option("maxResults", maxResults)

		fun onBehalfOfContentOwner(onBehalfOfContentOwner: String) = option("onBehalfOfContentOwner", onBehalfOfContentOwner)

		fun pageToken(pageToken: String) = option("pageToken", pageToken)

		fun videoId(videoId: String) = option("videoId", videoId)

		/**
		 * Request
		 */
		override suspend fun execute(): PlaylistItemList = ktorClient.get(url)
	}

	@Serializable
	data class PlaylistItemList(
			val kind: String = "",
			val etag: String = "",
			val nextPageToken: String = "",
			val prevPageToken: String = "",
			val pageInfo: PageInfo = PageInfo(),
			val items: List<PlaylistItem> = emptyList()
	) : YouTubeResource() {
		@Serializable
		data class PageInfo(val totalResults: Int = 0, val resultsPerPage: Int = 0)
	}

	@Serializable
	data class PlaylistItem(
			val kind: String = "",
			val etag: String = "",
			val id: String = "",
			val snippet: Snippet = Snippet(),
			val contentDetails: ContentDetails = ContentDetails(),
			val status: Status = Status(),
	) {
		@Serializable
		data class Snippet(
				val publishedAt: String = "",
				val channelId: String = "",
				val title: String = "",
				val description: String = "",
				val thumbnails: Map<String, Thumbnail> = emptyMap(),
				val channelTitle: String = "",
				val playlistId: String = "",
				val position: Int = 0,
				val resourceId: ResourceId = ResourceId(),
		) {
			@Serializable
			data class ResourceId(
					val kind: String = "",
					val videoId: String = "",
			)
		}

		@Serializable
		data class ContentDetails(
				val videoId: String = "",
				val startAt: String = "",
				val endAt: String = "",
				val note: String = "",
				val videoPublishedAt: String = "",
		)

		@Serializable
		data class Status(
				val privacyStatus: String = ""
		)
	}

	/**
	 * For details on Playlists, see:
	 * https://developers.google.com/youtube/v3/docs/playlists
	 */

/*
	Due to lack of authentication, Playlists miss the following endpoints:
	- insert
	- update
	- delete
 */
	
	suspend fun getPlaylists(block: PlaylistListRequest.() -> Unit): PlaylistList = PlaylistListRequest(this).apply(block).execute()

	class PlaylistListRequest(client: YoutubeClient) : YouTubeRequest<PlaylistList>("playlist", client) {
		/**
		 * Part
		 */
		fun requestContentDetails() = part("contentDetails")

		fun requestId() = part("id")

		fun requestLocalizations() = part("localizations")

		fun requestPlayer() = part("player")

		fun requestSnippet() = part("snippet")

		fun requestStatus() = part("status")

		/**
		 * Required
		 */
		fun filterId(id: String) = option("id", id)

		fun filterChannelId(channelId: String) = option("channelId", channelId)

		fun filterMine(mine: Boolean): Unit =  option("mine", mine)

		/**
		 * Optional
		 */
		fun hl(hl: String) = option("hl", hl)

		fun maxResults(maxResults: Int) = option("maxResults", maxResults)

		fun onBehalfOfContentOwner(onBehalfOfContentOwner: String) = option("onBehalfOfContentOwner", onBehalfOfContentOwner)

		fun onBehalfOfContentOwnerChannel(onBehalfOfContentOwnerChannel: String) = option("onBehalfOfContentOwnerChannel", onBehalfOfContentOwnerChannel)

		fun pageToken(pageToken: String) = option("pageToken", pageToken)

		/**
		 * Request
		 */
		override suspend fun execute(): PlaylistList = ktorClient.get(url)
	}

	@Serializable
	data class PlaylistList(
			val kind: String = "",
			val etag: String = "",
			val nextPageToken: String = "",
			val prevPageToken: String = "",
			val pageInfo: PageInfo = PageInfo(),
			val items: List<Playlist> = emptyList()
	) : YouTubeResource() {
		@Serializable
		data class PageInfo(val totalResults: Int = 0, val resultsPerPage: Int = 0)
	}

	@Serializable
	data class Playlist(
			val kind: String = "",
			val etag: String = "",
			val id: String = "",
			val snippet: Snippet = Snippet(),
			val status: Status = Status(),
			val contentDetails: ContentDetails = ContentDetails(),
			val player: Player = Player(),
			val localizations: Localizations = Localizations(),
	) {
		@Serializable
		data class Snippet(
				val publishedAt: String = "",
				val channelId: String = "",
				val title: String = "",
				val description: String = "",
				val thumbnails: Map<String, Thumbnail> = emptyMap(),
				val channelTitle: String = "",
				val tags: List<String> = emptyList(),
				val defaultLanguage: String = "",
				val localized: Localized = Localized(),
		) {
			@Serializable
			data class Localized(
					val kind: String = "",
					val videoId: String = "",
			)
		}

		@Serializable
		data class ContentDetails(
				val videoId: String = "",
				val startAt: String = "",
				val endAt: String = "",
				val note: String = "",
				val videoPublishedAt: String = "",
		)

		@Serializable
		data class Status(
				val privacyStatus: String = ""
		)

		@Serializable
		data class Player(
				val embedHtml: String = "",
		)
	}

	/**
	 * For details on Search, see:
	 * https://developers.google.com/youtube/v3/docs/search
	 */
	
	suspend fun getSearchItems(block: SearchItemsRequest.() -> Unit): SearchList = SearchItemsRequest(this).apply(block).execute()

	class SearchItemsRequest(client: YoutubeClient) : YouTubeRequest<SearchList>("search", client) {
		/**
		 * Part
		 */
		fun requestSnippet() = part("snippet")


		/**
		 * Required
		 */
		fun filterForContentOwner(forContentOwner: Boolean) = option("forContentOwner", forContentOwner)

		fun filterForDeveloper(forDeveloper: Boolean) = option("forDeveloper", forDeveloper)

		fun filterForMine(forMine: Boolean) = option("forMine", forMine)

		fun filterRelatedToVideoId(forDeveloper: String) = option("forDeveloper", forDeveloper)

		/**
		 * Optional
		 */
		fun channelId(channelId: String) = option("channelId", channelId)

		fun channelType(channelType: String) = option("channelType", channelType)

		fun eventType(eventType: String) = option("eventType", eventType)

		fun location(location: String) = option("location", location)

		fun locationRadius(locationRadius: String) = option("locationRadius", locationRadius)

		fun maxResults(maxResults: Int) = option("maxResults", maxResults)

		fun onBehalfOfContentOwner(onBehalfOfContentOwner: String) = option("onBehalfOfContentOwner", onBehalfOfContentOwner)

		fun order(order: String) = option("order", order)

		fun pageToken(pageToken: String) = option("pageToken", pageToken)

		fun publishedAfter(publishedAfter: String) = option("publishedAfter", publishedAfter)

		fun publishedBefore(publishedBefore: String) = option("publishedBefore", publishedBefore)

		fun q(q: String) = option("q", q)

		fun regionCode(regionCode: String) = option("regionCode", regionCode)

		fun relevanceLanguage(relevanceLanguage: String) = option("relevanceLanguage", relevanceLanguage)

		fun safeSearch(safeSearch: String) = option("safeSearch", safeSearch)

		fun topicId(topicId: String) = option("topicId", topicId)

		fun type(type: String) = option("type", type)

		fun videoCaption(videoCaption: String) = option("videoCaption", videoCaption)

		fun videoCategoryId(videoCategoryId: String) = option("videoCategoryId", videoCategoryId)

		fun videoDefinition(videoDefinition: String) = option("videoDefinition", videoDefinition)

		fun videoDimension(videoDimension: String) = option("videoDimension", videoDimension)

		fun videoDuration(videoDuration: String) = option("videoDuration", videoDuration)

		fun videoEmbeddable(videoEmbeddable: String) = option("videoEmbeddable", videoEmbeddable)

		fun videoLicense(videoLicense: String) = option("videoLicense", videoLicense)

		fun videoSyndicated(videoSyndicated: String) = option("videoSyndicated", videoSyndicated)

		fun videoType(videoType: String) = option("videoType", videoType)

		/**
		 * Request
		 */
		override suspend fun execute(): SearchList = ktorClient.get(url)
	}

	@Serializable
	data class SearchList(
			val kind: String = "",
			val etag: String = "",
			val nextPageToken: String = "",
			val prevPageToken: String = "",
			val regionCode: String = "",
			val pageInfo: PageInfo = PageInfo(),
			val items: List<Search> = emptyList()
	) : YouTubeResource() {
		@Serializable
		data class PageInfo(val totalResults: Int = 0, val resultsPerPage: Int = 0)
	}

	@Serializable
	data class Search(
			val kind: String = "",
			val etag: String = "",
			val id: Id = Id(),
			val snippet: Snippet = Snippet(),

			) {
		@Serializable
		data class Id(
				val kind: String = "",
				val videoId: String = "",
				val channelId: String = "",
				val playlistId : String = "",
		)

		@Serializable
		data class Snippet(
				val publishedAt: String = "",
				val channelId: String = "",
				val title: String = "",
				val description: String = "",
				val thumbnails: Map<String, Thumbnail> = emptyMap(),
				val channelTitle: String = "",
				val liveBroadcastContent: String = "",
				val publishTime: String = "",
		)
	}

	/**
	 * For details on Subscriptions, see:
	 * https://developers.google.com/youtube/v3/docs/subscriptions
	 */

/*
	Due to lack of authentication, Playlists miss the following endpoints:
	- insert
	- delete
 */
	
	suspend fun getSubscriptions(block: SubscriptionListRequest.() -> Unit): SubscriptionList = SubscriptionListRequest(this).apply(block).execute()

	class SubscriptionListRequest(client: YoutubeClient) : YouTubeRequest<SubscriptionList>("subscriptions", client) {
		/**
		 * Part
		 */
		fun requestContentDetails() = part("contentDetails")

		fun requestId() = part("id")

		fun requestSnippet() = part("snippet")

		fun requestSubscriberSnippet() = part("subscriberSnippet")

		/**
		 * Required
		 */
		fun filterChannelId(channelId: String) = option("channelId", channelId)

		fun filterId(id: String) = option("id", id)

		fun filterMine(mine: Boolean): Unit =  option("mine", mine)

		fun filterMyRecentSubscribers(myRecentSubscribers: Boolean) = option("myRecentSubscribers", myRecentSubscribers)

		fun filterMySubscribers(mySubscribers: Boolean) = option("mySubscribers", mySubscribers)

		/**
		 * Optional
		 */
		fun forChannelId(forChannelId: String) = option("forChannelId", forChannelId)

		fun maxResults(maxResults: Int) = option("maxResults", maxResults)

		fun onBehalfOfContentOwner(onBehalfOfContentOwner: String) = option("onBehalfOfContentOwner", onBehalfOfContentOwner)

		fun onBehalfOfContentOwnerChannel(onBehalfOfContentOwnerChannel: String) = option("onBehalfOfContentOwnerChannel", onBehalfOfContentOwnerChannel)

		fun order(order: String) = option("order", order)

		fun pageToken(pageToken: String) = option("pageToken", pageToken)

		/**
		 * Request
		 */
		override suspend fun execute(): SubscriptionList = ktorClient.get(url)
	}

	@Serializable
	data class SubscriptionList(
			val kind: String = "",
			val etag: String = "",
			val nextPageToken: String = "",
			val prevPageToken: String = "",
			val pageInfo: PageInfo = PageInfo(),
			val items: List<Subscription> = emptyList()
	) : YouTubeResource() {
		@Serializable
		data class PageInfo(val totalResults: Int = 0, val resultsPerPage: Int = 0)
	}

	@Serializable
	data class Subscription(
			val kind: String = "",
			val etag: String = "",
			val id: String = "",
			val snippet: Snippet = Snippet(),
			val contentDetails: ContentDetails = ContentDetails(),
			val subscriberSnippet: SubscriberSnippet = SubscriberSnippet(),
	) {
		@Serializable
		data class Snippet(
				val publishedAt: String = "",
				val channelTitle: String = "",
				val title: String = "",
				val description: String = "",
				val resourceId: ResourceId = ResourceId(),
				val channelId: String = "",
				val thumbnails: Map<String, Thumbnail> = emptyMap(),
		) {
			@Serializable
			data class ResourceId(
					val kind: String = "",
					val channelId: String = "",
			)
		}

		@Serializable
		data class ContentDetails(
				val totalItemCount: Int = 0,
				val newItemCount: Int = 0,
				val activityType: String = "",
		)

		@Serializable
		data class SubscriberSnippet(
				val title: String = "",
				val description: String = "",
				val channelId: String = "",
				val thumbnails: Map<String, Thumbnail> = emptyMap(),
		)
	}

	/**
	 * For details on Thumbnails, see:
	 * https://developers.google.com/youtube/v3/docs/thumbnails
	 */

/*
	Due to lack of authentication, Thumbnails miss the following endpoints:
	- set
 */

	/**
	 * For details on VideoAbuseReportReasons, see:
	 * https://developers.google.com/youtube/v3/docs/videoAbuseReportReasons
	 */

/*
	Due to lack of authentication, VideoAbuseReportReasons miss the following endpoints:
	- list
 */

	/**
	 * For details on VideoCategories, see:
	 * https://developers.google.com/youtube/v3/docs/videoCategories
	 */
	
	suspend fun getVideoCategories(block: VideoCategoryListRequest.() -> Unit): VideoCategoryList = VideoCategoryListRequest(this).apply(block).execute()

	class VideoCategoryListRequest(client: YoutubeClient) : YouTubeRequest<VideoCategoryList>("videoCategories", client) {
		/**
		 * Part
		 */
		fun requestSnippet() = part("snippet")

		/**
		 * Required
		 */
		fun filterId(id: String) = option("id", id)

		fun filterRegionCode(regionCode: String) = option("regionCode", regionCode)

		/**
		 * Optional
		 */
		fun hl(hl: String) = option("hl", hl)

		/**
		 * Request
		 */
		override suspend fun execute(): VideoCategoryList = ktorClient.get(url)
	}

	@Serializable
	data class VideoCategoryList(
			val kind: String = "",
			val etag: String = "",
			val nextPageToken: String = "",
			val prevPageToken: String = "",
			val pageInfo: PageInfo = PageInfo(),
			val items: List<VideoCategory> = emptyList()
	) : YouTubeResource() {
		@Serializable
		data class PageInfo(val totalResults: Int = 0, val resultsPerPage: Int = 0)
	}

	@Serializable
	data class VideoCategory(
			val kind: String = "",
			val etag: String = "",
			val id: String = "",
			val snippet: Snippet = Snippet(),
	) {
		@Serializable
		data class Snippet(
				val channelId: String = "",
				val title: String = "",
				val assignable: String = "",
		)
	}

	/**
	 * For details on Videos, see:
	 * https://developers.google.com/youtube/v3/docs/videos
	 */

/*
	Due to lack of authentication, Videos miss the following endpoints:
	- insert
	- update
	- rate
	- getRating
	- reportAbuse
	- delete
 */
	
	suspend fun getVideos(block: VideoListRequest.() -> Unit): VideoList = VideoListRequest(this).apply(block).execute()

	class VideoListRequest(client: YoutubeClient) : YouTubeRequest<VideoList>("videos", client) {
		/**
		 * Part
		 */
		fun requestContentDetails() = part("contentDetails")

		fun requestFileDetails() = part("fileDetails")

		fun requestId() = part("id")

		fun requestLiveStreamingDetails() = part("liveStreamingDetails")

		fun requestLocalizations() = part("localizations")

		fun requestPlayer() = part("player")

		fun requestProcessingDetails() = part("processingDetails")

		fun requestRecordingDetails() = part("recordingDetails")

		fun requestSnippet() = part("snippet")

		fun requestStatistics() = part("statistics")

		fun requestStatus() = part("status")

		fun requestSuggestions() = part("suggestions")

		fun requestTopicDetails() = part("topicDetails")

		/**
		 * Required
		 */
		fun filterChart(chart: String) = option("chart", chart)

		fun filterId(id: String) = option("id", id)

		fun filterMyRating(myRating: String) = option("myRating", myRating)

		/**
		 * Optional
		 */
		fun hl(hl: String) = option("hl", hl)

		fun maxHeight(maxHeight: Int) = option("maxHeight", maxHeight)

		fun maxResults(maxResults: Int) = option("maxResults", maxResults)

		fun maxWidth(maxWidth: Int) = option("maxWidth", maxWidth)

		fun onBehalfOfContentOwner(onBehalfOfContentOwner: String) = option("onBehalfOfContentOwner", onBehalfOfContentOwner)

		fun pageToken(pageToken: String) = option("pageToken", pageToken)

		fun regionCode(regionCode: String) = option("regionCode", regionCode)

		fun videoCategoryId(videoCategoryId: String) = option("videoCategoryId", videoCategoryId)

		/**
		 * Request
		 */
		override suspend fun execute(): VideoList = ktorClient.get(url)
	}

	@Serializable
	data class VideoList(
			val kind: String = "",
			val etag: String = "",
			val nextPageToken: String = "",
			val prevPageToken: String = "",
			val pageInfo: PageInfo = PageInfo(),
			val items: List<Video> = emptyList()
	) : YouTubeResource() {
		@Serializable
		data class PageInfo(val totalResults: Int = 0, val resultsPerPage: Int = 0)
	}

	@Serializable
	data class Video(
			val kind: String = "",
			val etag: String = "",
			val id: String = "",
			val snippet: Snippet = Snippet(),
			val contentDetails: ContentDetails = ContentDetails(),
			val status: Status = Status(),
			val statistics: Statistics = Statistics(),
			val player: Player = Player(),
			val topicDetails: TopicDetails = TopicDetails(),
			val recordingDetails: RecordingDetails = RecordingDetails(),
			val fileDetails: FileDetails = FileDetails(),
			val processingDetails: ProcessingDetails = ProcessingDetails(),
			val suggestions: Suggestions = Suggestions(),
			val liveStreamingDetails: LiveStreamingDetails = LiveStreamingDetails(),
			val localizations: Localizations = Localizations(),
	) {
		@Serializable
		data class Snippet(
				val publishedAt: String = "",
				val channelId: String = "",
				val title: String = "",
				val description: String = "",
				val thumbnails: Map<String, Thumbnail> = emptyMap(),
				val channelTitle: String = "",
				val tags: List<String> = emptyList(),
				val categoryId: String = "",
				val liveBroadcastContent: String = "",
				val defaultLanguage: String = "",
				val localized: Localized = Localized(),
				val defaultAudioLanguage: String = "",
		) {
			@Serializable
			data class Localized(
					val title: String ="",
					val description: String = "",
			)
		}

		@Serializable
		data class ContentDetails(
				val duration: String = "",
				val dimension: String = "",
				val definition: String = "",
				val caption: String = "",
				val licensedContent: Boolean = false,
				val regionRestriction: RegionRestriction = RegionRestriction(),
				val contentRating: ContentRating = ContentRating(),
				val projection: String = "",
				val hasCustomThumbnail: String = "",
		) {
			@Serializable
			data class RegionRestriction(
					val allowed: List<String> = emptyList(),
					val blocked: List<String> = emptyList(),
			)

			@Serializable
			data class ContentRating(
					val acbRating: String = "",
					val agcomRating: String = "",
					val anatelRating: String = "",
					val bbfcRating: String = "",
					val bfvcRating: String = "",
					val bmukkRating: String = "",
					val catvRating: String = "",
					val catvfrRating: String = "",
					val cbfcRating: String = "",
					val cccRating: String = "",
					val cceRating: String = "",
					val chfilmRating: String = "",
					val chvrsRating: String = "",
					val cicfRating: String = "",
					val cnaRating: String = "",
					val cncRating: String = "",
					val csaRating: String = "",
					val cscfRating: String = "",
					val czfilmRating: String = "",
					val djctqRating: String = "",
					val djctqRatingReasons: List<String> = emptyList(),
					val ecbmctRating: String = "",
					val eefilmRating: String = "",
					val egfilmRating: String = "",
					val eirinRating: String = "",
					val fcbmRating: String = "",
					val fcoRating: String = "",
					val fmocRating: String = "",
					val fpbRating: String = "",
					val fpbRatingReasons: List<String> = emptyList(),
					val fskRating: String = "",
					val grfilmRating: String = "",
					val icaaRating: String = "",
					val ifcoRating: String = "",
					val ilfilmRating: String = "",
					val incaaRating: String = "",
					val kfcbRating: String = "",
					val kijkwijzerRating: String = "",
					val kmrbRating: String = "",
					val lsfRating: String = "",
					val mccaaRating: String = "",
					val mccypRating: String = "",
					val mcstRating: String = "",
					val mdaRating: String = "",
					val medietilsynetRating: String = "",
					val mekuRating: String = "",
					val mibacRating: String = "",
					val mocRating: String = "",
					val moctwRating: String = "",
					val mpaaRating: String = "",
					val mpaatRating: String = "",
					val mtrcbRating: String = "",
					val nbcRating: String = "",
					val nbcplRating: String = "",
					val nfrcRating: String = "",
					val nfvcbRating: String = "",
					val nkclvRating: String = "",
					val oflcRating: String = "",
					val pefilmRating: String = "",
					val rcnofRating: String = "",
					val resorteviolenciaRating: String = "",
					val rtcRating: String = "",
					val rteRating: String = "",
					val russiaRating: String = "",
					val skfilmRating: String = "",
					val smaisRating: String = "",
					val smsaRating: String = "",
					val tvpgRating: String = "",
					val ytRating: String = "",
			)
		}

		@Serializable
		data class Status(
				val uploadStatus: String = "",
				val failureReason: String = "",
				val rejectionReason: String = "",
				val privacyStatus: String = "",
				val publishAt: String = "",
				val license: String = "",
				val embeddable: Boolean = false,
				val publicStatsViewable: Boolean = false,
				val madeForKids: Boolean = false,
				val selfDeclaredMadeForKids: Boolean = false,
		)

		@Serializable
		data class Statistics(
				val viewCount: Long = 0L,
				val likeCount: Long = 0L,
				val dislikeCount: Long = 0L,
				val favoriteCount: Long = 0L,
				val commentCount: Long = 0L,
		)

		@Serializable
		data class Player(
				val embedHtml: String = "",
				val embedHeight: Long = 0L,
				val embedWidth: Long = 0L,
		)

		@Serializable
		data class TopicDetails(
				val topicIds: List<String> = emptyList(),
				val relevantTopicIds: List<String> = emptyList(),
				val topicCategories: List<String> = emptyList(),
		)

		@Serializable
		data class RecordingDetails(
				val recordingDate: String = "",
		)

		@Serializable
		data class FileDetails(
				val fileName: String = "",
				val fileSize: String = "",
				val fileType: String = "",
				val container: String = "",
				val videoStreams: List<VideoStream> = emptyList(),
				val audioStreams: List<AudioStream> = emptyList(),
				val durationMs: Long = 0L,
				val bitrateBps: Long = 0L,
				val creationTime: String = "",
		) {
			@Serializable
			data class VideoStream(
					val widthPixels: Int = 0,
					val heightPixels: Int = 0,
					val frameRateFps: Double = 0.0,
					val aspectRatio: Double = 0.0,
					val codec: String = "",
					val bitrateBps: Long = 0L,
					val rotation: String = "",
					val vendor: String = "",
			)

			@Serializable
			data class AudioStream(
					val channelCount: Int = 0,
					val codec: String = "",
					val bitrateBps: Long = 0L,
					val vendor: String = "",
			)
		}

		@Serializable
		data class ProcessingDetails(
				val processingStatus: String = "",
				val processingProgress: ProcessingProgress = ProcessingProgress(),
				val processingFailureReason: String = "",
				val fileDetailsAvailability: String = "",
				val processingIssuesAvailability: String = "",
				val tagSuggestionsAvailability: String = "",
				val editorSuggestionsAvailability: String = "",
				val thumbnailsAvailability: String = "",
		) {
			@Serializable
			data class ProcessingProgress(
					val partsTotal: Long = 0L,
					val partsProcessed: Long = 0L,
					val timeLeftMs: Long = 0L,
			)
		}

		@Serializable
		data class Suggestions(
				val processingErrors: List<String> = emptyList(),
				val processingWarnings: List<String> = emptyList(),
				val processingHints: List<String> = emptyList(),
				val tagSuggestions: List<TagSuggestion> = emptyList(),
				val editorSuggests: List<String> = emptyList(),
		) {
			@Serializable
			data class TagSuggestion(
					val tag: String = "",
					val categoryRestrictions: List<String> = emptyList(),
			)
		}

		@Serializable
		data class LiveStreamingDetails(
				val actualStartTime: String = "",
				val actualEndTime: String = "",
				val scheduledStartTime: String = "",
				val scheduledEndTime: String = "",
				val concurrentViewers: String = "",
				val activeLiveChatId: String = "",
		)
	}

	/**
	 * For details on Watermarks, see:
	 * https://developers.google.com/youtube/v3/docs/watermarks
	 */

/*
	Due to lack of authentication, Watermarks miss the following endpoints:
	- set
	- unset
 */

}