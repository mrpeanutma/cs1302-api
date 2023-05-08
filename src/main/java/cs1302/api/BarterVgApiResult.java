package cs1302.api;

import com.google.gson.annotations.SerializedName;

/**
 * The class respresents a response from the BarterVG API. This is then used by
 * Gson to parse the JSON response into an object to be used by the program.
 */
public class BarterVgApiResult {
    @SerializedName("recent_players") int recentPlayers;
    @SerializedName("player_count") int playerCount;
}
