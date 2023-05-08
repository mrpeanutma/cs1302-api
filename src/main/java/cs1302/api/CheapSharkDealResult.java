package cs1302.api;

/**
 * This class represents a response from the CheapShark API, after a request for deals,
 * which is then used by Gson to parse the JSON response.
 */
public class CheapSharkDealResult {
    String title;
    String dealID;
    String salePrice;
    String normalPrice;
    String savings;
    String metacriticScore;
    String thumb;
    String steamAppID;
}
