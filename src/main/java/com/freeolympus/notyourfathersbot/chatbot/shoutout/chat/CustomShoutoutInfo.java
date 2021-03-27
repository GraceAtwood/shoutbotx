package com.freeolympus.notyourfathersbot.chatbot.shoutout.chat;

import com.github.twitch4j.helix.domain.ChannelInformation;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import static java.lang.String.format;

/**
 * Describes the shoutout information for a streamer
 *
 * <pre>
 * {@code
 * "owldamone": {
 *   "message": "Owl's here!! She's the sweetest bean, and best weeb ever.  Check her out for the epic cosplay and raid dances, and stay for the wholesome love.",
 *   "subjectPronoun": "she",
 *   "objectPronoun": "her",
 *   "addLastPlaying": true
 * },
 * }
 * </pre>
 */
@Data
public class CustomShoutoutInfo {
    private String message;
    private String subjectPronoun;
    private String objectPronoun;
    private Boolean addLastPlaying;
    private Integer shoutoutIntervalMins;

    public String produceShoutoutMessage(ChannelInformation channelInformation) {

        var result = message;

        if (addLastPlaying) {
            var gameName = channelInformation.getGameName();

            if (StringUtils.isBlank(gameName)) {
                gameName = "nothing";
            }

            result = result.concat(format(" %s was last seen streaming %s!", StringUtils.capitalize(subjectPronoun), gameName));
        }

        return result.concat(format(" https://twitch.tv/%s", channelInformation.getBroadcasterName()));
    }
}
