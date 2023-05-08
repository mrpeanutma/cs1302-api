package cs1302.api;

/**
 * The class represents a response from the CheapShark API after a call for games, and is
 * used by Gson to create an object from the JSON response.
 */
public class CheapSharkGameResult {
    String cheapest;
    String cheapestDealID;
    String external;
    String thumb;
    String steamAppID;
}
