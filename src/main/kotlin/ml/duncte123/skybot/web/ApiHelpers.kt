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

package ml.duncte123.skybot.web

import me.duncte123.botCommons.web.WebUtils
import me.duncte123.botCommons.web.WebUtilsErrorUtils
import me.duncte123.weebJava.helpers.QueryBuilder
import org.json.JSONObject
import me.duncte123.botCommons.web.WebUtils.ins as web
import ml.duncte123.skybot.utils.Variables.CONFIG as conf

class ApiHelpers {

    fun verifyCapcha(response: String): JSONObject {
        val fields = HashMap<String, Any>()
        fields["secret"] = conf.getString("apis.chapta.secret", "-")
        fields["response"] = response
        val req = web.preparePost("https://www.google.com/recaptcha/api/siteverify", fields,
                WebUtils.EncodingType.APPLICATION_JSON)
                .build(WebUtilsErrorUtils::toJSONObject, WebUtilsErrorUtils::handleError)

        return req.execute()
    }

    fun addTrelloCard(name: String, desc: String): JSONObject {
        val query = QueryBuilder()
                .append("https://api.trello.com/1/cards")
                .append("name", name)
                .append("desc", desc)
                .append("pos", "bottom")
                .append("idList", "5ad2a228bef59be0aca289c9")
                .append("keepFromSource", "all")
                .append("key", conf.getString("apis.trello.key", "-"))
                .append("token", conf.getString("apis.trello.token", "-"))

        val t = web.preparePost(query.build()).execute()
        return JSONObject(t)
    }

}