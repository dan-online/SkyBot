/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.utils

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.connection.netty.NettyStreamFactoryFactory
import io.netty.channel.nio.NioEventLoopGroup
import ml.duncte123.skybot.objects.api.*
import org.bson.codecs.configuration.CodecRegistries
import java.time.OffsetDateTime
import java.util.*
import java.util.regex.Pattern

object ApiUtils {


    @JvmStatic
    fun getRandomLlama(): LlamaObject {

        val eventLoopGroup = NioEventLoopGroup()
        val client: com.mongodb.client.MongoClient = com.mongodb.client.MongoClients.create(
                com.mongodb.MongoClientSettings.builder()
                        .streamFactoryFactory(NettyStreamFactoryFactory.builder()
                                .eventLoopGroup(eventLoopGroup).build())
                        .applyToSslSettings { builder -> builder.enabled(true) }
                        .applyConnectionString(AirUtils.CONNECTION_STRING)
                        .build()
        )

        val session = client.startSession()
        val collection = client.getDatabase(AirUtils.CONFIG.getString("mongo.database")).getCollection("llamas", LlamaObject::class.java)
                .withCodecRegistry(CodecRegistries.fromCodecs(LlamaObjectCodecImpl()))

        val llama = collection.aggregate(session, listOf(
                Aggregates.sample(1)
        )).first()

        return if (llama != null) {

            session.close(); client.close(); eventLoopGroup.shutdownGracefully()

            llama

        } else {
            getRandomLlama()
        }
    }

    @JvmStatic
    fun getRandomKpopMember(search: String = ""): KpopObject {

        val eventLoopGroup = NioEventLoopGroup()
        val client: com.mongodb.client.MongoClient = com.mongodb.client.MongoClients.create(
                com.mongodb.MongoClientSettings.builder()
                        .streamFactoryFactory(NettyStreamFactoryFactory.builder()
                                .eventLoopGroup(eventLoopGroup).build())
                        .applyToSslSettings { builder -> builder.enabled(true) }
                        .applyConnectionString(AirUtils.CONNECTION_STRING)
                        .build()
        )

        val session = client.startSession()
        val collection = client.getDatabase(AirUtils.CONFIG.getString("mongo.database")).getCollection("kpop", KpopObject::class.java)
                .withCodecRegistry(CodecRegistries.fromCodecs(KpopCodecImpl()))

        val kpopObject: KpopObject? = if (search.isNotEmpty()) {
            collection.find(session, Filters.regex("name", Pattern.compile(search))).limit(1).first()
        } else {
            collection.aggregate(session, listOf(
                    Aggregates.sample(1)
            )).first()
        }
        return if (kpopObject != null) {

            session.close(); client.close(); eventLoopGroup.shutdownGracefully()

            kpopObject

        } else {
            getRandomKpopMember(search)
        }
    }

    @JvmStatic
    fun getWarnsForUser(userId: String, guildId: String): WarnObject {

        val warnings = ArrayList<Warning>()

        val eventLoopGroup = NioEventLoopGroup()
        val client: com.mongodb.client.MongoClient = com.mongodb.client.MongoClients.create(
                com.mongodb.MongoClientSettings.builder()
                        .streamFactoryFactory(NettyStreamFactoryFactory.builder()
                                .eventLoopGroup(eventLoopGroup).build())
                        .applyToSslSettings { builder -> builder.enabled(true) }
                        .applyConnectionString(AirUtils.CONNECTION_STRING)
                        .build()
        )
        val session = client.startSession()

        client.getDatabase(AirUtils.CONFIG.getString("mongo.database")).getCollection("kpop", Warning::class.java)
                .withCodecRegistry(CodecRegistries.fromCodecs(WarningCodecImpl())).find(session,
                Filters.and(
                        Filters.eq("user_id", userId.toLong()),
                        Filters.eq("guild_id"),
                        Filters.gte("expire_date", OffsetDateTime.now().toEpochSecond())
            )).forEach { warnings.add(it) }


        session.close(); client.close(); eventLoopGroup.shutdownGracefully()

        return WarnObject(userId, warnings)
    }

}