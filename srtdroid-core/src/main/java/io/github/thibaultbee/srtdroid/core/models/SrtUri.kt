/*
 * Copyright (C) 2024 Thibault B.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.thibaultbee.srtdroid.core.models

import android.net.Uri
import io.github.thibaultbee.srtdroid.core.extensions.replaceBetween
import io.github.thibaultbee.srtdroid.core.extensions.substringBetween
import io.github.thibaultbee.srtdroid.core.models.SrtUrl.Companion.SRT_SCHEME
import io.github.thibaultbee.srtdroid.core.models.SrtUrl.Companion.SRT_STREAM_ID_QUERY_PARAMETER
import io.github.thibaultbee.srtdroid.core.models.SrtUrl.Companion.STREAM_ID_QUERY_PARAMETER
import kotlin.random.Random

/**
 * An [URI] like class with specific SRT parameter for [streamId].
 *
 * StreamId in query is not RFC 2396 compliant. Example of SRT StreamId syntax: #!::u=admin,r=bluesbrothers1_hi
 * Parameters after streamId might be lost because they will be interpreted as fragment.
 *
 * The purpose of this class is to provide a way to get streamId and to get all parameters from the [rawUri].
 */
class SrtUri
internal constructor(private val rawUri: Uri) {
    init {
        require(rawUri.scheme == SRT_SCHEME) { "Uri scheme must be $SRT_SCHEME" }
    }

    private val fakeStreamId = "FAKE_STREAM_ID" + Random.nextInt()

    private val rawUriString by lazy { rawUri.toString() }
    private val hasStreamIdFormat by lazy { rawUriString.contains("$STREAM_ID_QUERY_PARAMETER=#!") }
    private val hasSrtStreamIdFormat by lazy {
        rawUriString.contains("$SRT_STREAM_ID_QUERY_PARAMETER=#!")
    }

    private val streamIdParameterName by lazy {
        when {
            rawUri.getQueryParameter(STREAM_ID_QUERY_PARAMETER) != null -> STREAM_ID_QUERY_PARAMETER
            rawUri.getQueryParameter(SRT_STREAM_ID_QUERY_PARAMETER) != null -> SRT_STREAM_ID_QUERY_PARAMETER
            else -> null
        }
    }

    private val uri: Uri by lazy {
        if (hasStreamIdFormat || hasSrtStreamIdFormat) {
            // Replace stream Id format #! with fake to avoid Uri parsing error
            val correctedUriString =
                rawUriString.replaceBetween("$streamIdParameterName=", "&", fakeStreamId)
            Uri.parse(correctedUriString)
        } else {
            rawUri
        }
    }

    val streamId: String? by lazy {
        if (streamIdParameterName != null) {
            if (hasStreamIdFormat || hasSrtStreamIdFormat) {
                rawUriString.substringBetween("$streamIdParameterName=", "&")
            } else {
                rawUri.getQueryParameter(streamIdParameterName)
            }
        } else {
            null
        }
    }

    /**
     * Gets the scheme from the URI.
     */
    val scheme by lazy { uri.scheme }

    /**
     * Gets the encoded host from the authority for this URI.
     */
    val host by lazy { uri.host }

    /**
     * Gets the port from the authority for this URI.
     */
    val port by lazy { uri.port }

    /**
     * Returns a set of the unique names of all query parameters. Iterating over the set will return the names in order of their first occurrence.
     */
    val queryParameterNames: Set<String> by lazy {
        uri.queryParameterNames
    }

    /**
     * Searches the query string for the first value with the given key.
     * @param key the query parameter name
     * @return the decoded value or null if no parameter is found
     */
    fun getQueryParameter(key: String): String? {
        if (key == STREAM_ID_QUERY_PARAMETER || key == SRT_STREAM_ID_QUERY_PARAMETER) {
            throw IllegalArgumentException("streamId is a reserved key")
        }
        return uri.getQueryParameter(key)
    }

    /**
     * Returns [Uri] as [String].
     */
    override fun toString(): String {
        val streamId = streamId ?: return uri.toString()
        return uri.toString().replace(fakeStreamId, streamId)
    }

    companion object {
        /**
         * Parse [uriString] to [SrtUri]
         *
         * @param uriString Uri string to parse
         * @return [SrtUri]
         */
        fun parse(uriString: String): SrtUri {
            return parse(Uri.parse(uriString))
        }

        /**
         * Parse [uri] to [SrtUri]
         *
         * @param uri Uri to parse
         * @return [SrtUri]
         */
        fun parse(uri: Uri): SrtUri {
            return SrtUri(uri)
        }
    }

    /**
     * Builder for [SrtUri]
     */
    internal class Builder {
        private val builder = Uri.Builder().scheme(SRT_SCHEME)
        private var streamId: String? = null

        fun streamId(streamId: String): Builder {
            this.streamId = streamId
            return this
        }

        fun appendQueryParameter(key: String, value: String): Builder {
            if ((key == STREAM_ID_QUERY_PARAMETER) || (key == SRT_STREAM_ID_QUERY_PARAMETER)) {
                throw IllegalArgumentException("streamId is a reserved key")
            }
            builder.appendQueryParameter(key, value)
            return this
        }

        fun encodedAuthority(authority: String?): Builder {
            builder.encodedAuthority(authority)
            return this
        }

        fun build(): SrtUri {
            val streamId = streamId ?: return SrtUri(builder.build())
            if (streamId.contains("#!")) {
                val fakeStreamId = "FAKE_STREAM_ID" + Random.nextInt()
                builder.appendQueryParameter(STREAM_ID_QUERY_PARAMETER, fakeStreamId)
                val uri = builder.build()
                val correctedUriString = uri.toString().replace(
                    fakeStreamId, streamId
                )
                return SrtUri(Uri.parse(correctedUriString))
            } else {
                builder.appendQueryParameter(STREAM_ID_QUERY_PARAMETER, streamId)
                return SrtUri(builder.build())
            }
        }
    }
}
