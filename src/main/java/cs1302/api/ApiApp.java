package cs1302.api;

import java.net.http.HttpClient;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpRequest;
import java.io.IOException;
import java.lang.Math;
import java.nio.charset.StandardCharsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Api App that sends a request to the CheapShark API and gets an array of the best deals for
 * the specified game(s) or under a certain price. Using the {@code steamAppID} from the results,
 * the application then retrieves the player count and recent players of the listed game.
 */
public class ApiApp extends Application {

       /** Google {@code Gson} object for parsing JSON-formatted strings. */
    public static Gson GSON = new GsonBuilder()
        .setPrettyPrinting()                          // enable nice output when printing
        .create();                                    // builds and returns a Gson object

     /** HTTP client. */
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)           // uses HTTP protocol version 2 where possible
        .followRedirects(HttpClient.Redirect.NORMAL)  // always redirects, except from HTTPS to HTTP
        .build();                                     // builds and returns a HttpClient object

    Stage stage;
    Scene scene;
    VBox root;

    private HBox topPane;
    private ComboBox<String> types;
    private TextField searchField;
    private Button searchButton;

    private Label instructionLabel;

    private HBox results1;
    private ImageView image1;
    private Text resultsText1;
    private ImageView qrCode;

    private HBox results2;
    private ImageView image2;
    private Label title2;
    private Label salePrice2;
    private Label dealID2;
    private Label qrCode2;

    private HBox results3;
    private ImageView image3;
    private Label title3;
    private Label salePrice3;
    private Label dealID3;
    private Label qrCode3;

    private HBox bottomPane;
    private ProgressBar progressBar;
    private Button backButton;
    private Button nextButton;
    private Label apiLabel;

    private CheapSharkGameResult[] cheapSharkGameResults;
    private CheapSharkDealResult[] cheapSharkDealResults;
    private Image[] images;
    private BarterVgApiResult barterVgApiResult;
    private int[] playerCounts;
    private int[] recentPlayerCounts;
    private String gameSearchUri;
    private String bartervgSearchUri;
    private int resultsPageNumber;
    private int resultsLength;

    /**
     * Constructs an {@code ApiApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */
    public ApiApp() {
        this.stage = null;
        this.scene = null;

        this.root = new VBox(3);
        this.topPane = new HBox(4);
        this.types = new ComboBox<>();
        this.searchField = new TextField("Enter the upper price limit...");
        this.searchButton = new Button("Search");

        this.instructionLabel = new Label(
            "Select the type, type in the parameter, then click the search button.");
        this.results1 = new HBox();
        this.resultsText1 = new Text();
        this.image1 = new ImageView();

        this.bottomPane = new HBox(10);
        this.progressBar = new ProgressBar(0);
        this.apiLabel = new Label("Information provided by the CheapShark API and BarterVG API.");
        this.backButton = new Button("Back");
        this.nextButton = new Button("Next");
        this.backButton.setDisable(true);
        this.nextButton.setDisable(true);

        this.images = new Image[60];
        this.cheapSharkGameResults = new CheapSharkGameResult[60];
        this.cheapSharkDealResults = new CheapSharkDealResult[60];
        this.playerCounts = new int[60];
        this.recentPlayerCounts = new int[60];
        this.searchButton.setOnAction(event -> this.getResults());
        this.backButton.setOnAction(event -> {
            this.displayResults(resultsPageNumber - 1);
            this.nextButton.setDisable(false);
            resultsPageNumber--;
            if (resultsPageNumber == 0) {
                this.backButton.setDisable(true);
            }
        });
        this.types.setOnAction(event -> {
            if (types.getValue().equals("Deals")) {
                this.searchField.setText("Enter the Upper price limit...");
            } else if (types.getValue().equals("Games")) {
                this.searchField.setText("Enter the game title...");
            }
        });
        this.nextButton.setOnAction(event -> {
            this.displayResults(resultsPageNumber + 1);
            this.backButton.setDisable(false);
            resultsPageNumber++;
            if (resultsPageNumber == resultsLength - 1 || resultsPageNumber == 59) {
                this.nextButton.setDisable(true);
            }
        });
    } // ApiApp



    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {

        this.types.getItems().addAll(
            new String("Deals"),
            new String("Games"));
        this.types.getSelectionModel().select(0);
        this.searchField.setPrefWidth(400);
        this.results1.setPrefHeight(300);
        this.resultsText1.setWrappingWidth(300);
        this.progressBar.setPrefWidth(250);
        Insets inset = new Insets(4);
        root.setPadding(inset);
        this.topPane.getChildren().addAll(this.types, this.searchField, this.searchButton,
            this.backButton, this.nextButton);
        this.bottomPane.getChildren().addAll(this.progressBar, this.apiLabel);
        this.results1.getChildren().addAll(this.image1, this.resultsText1);

        this.root.getChildren().addAll(this.topPane, this.instructionLabel,
            this.results1, this.bottomPane);

        this.stage = stage;
        // setup scene

        scene = new Scene(root);

        // setup stage
        stage.setTitle("ApiApp!");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.sizeToScene();
        stage.show();
    } // start

    /**
     * The action taken by the application when the {@code searchButton} is pressed. The
     * program will create a new thread that requests the information from the API and
     * display the results page by page.
     */
    public void getResults() {
        this.searchButton.setDisable(true);
        this.backButton.setDisable(true);
        this.nextButton.setDisable(true);
        this.types.setDisable(true);
        this.instructionLabel.setText("Getting results...");
        this.progressBar.setProgress(0);
        Runnable resultsSearch = () -> {
            System.out.println("searching");
            runGameSharkApiSearch();
            runBarterVgApiSearch();
            createResults();
        };
        runNow(resultsSearch);
    } // getResults

    /**
     * Method call to retrieve the JSON response from the CheapShark APIs, parse the response, and
     * check if there are enough results to display.
     */
    public void runGameSharkApiSearch() {
        try {
            if (this.types.getValue().equals("Deals")) {
                if (!isNumeric(searchField.getText())) {
                    throw new IllegalArgumentException("parameter for deals should be a number.");
                }
                gameSearchUri = "https://www.cheapshark.com/api/1.0/deals?upperPrice=" +
                    URLEncoder.encode(searchField.getText());
            } else if (this.types.getValue().equals("Games")) {
                gameSearchUri = "https://www.cheapshark.com/api/1.0/games?title=" +
                    URLEncoder.encode(searchField.getText());
            }
            System.out.println(gameSearchUri);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(gameSearchUri))
                .build();
            HttpResponse<String> response = HTTP_CLIENT
                .send(request, BodyHandlers.ofString());
            System.out.println(response.statusCode());
            if (response.statusCode() != 200) {
                throw new IOException(response.toString());
            } // if
            String jsonString = response.body();
            System.out.println("jsonString: " + jsonString);
            if (this.types.getValue().equals("Deals")) {
                cheapSharkDealResults = GSON.fromJson(jsonString, CheapSharkDealResult[].class);
                resultsLength = cheapSharkDealResults.length;
                if (resultsLength == 0) {
                    throw new IllegalArgumentException("no search results found");
                }
            } else if (this.types.getValue().equals("Games")) {
                cheapSharkGameResults = GSON.fromJson(jsonString, CheapSharkGameResult[].class);
                resultsLength = cheapSharkGameResults.length;
                if (resultsLength == 0) {
                    throw new IllegalArgumentException("no search results found");
                }
            }
        } catch (Exception e) {
            this.alertError(e);
        }
    } // runSearch


    /**
     * Method call to retrieve a JSON response from the BarterVG API, parse the response, and
     * store the {@code playerCounts} and {@code recentPlayerCounts} to the program.
     */
    public void runBarterVgApiSearch () {
        try {
            if (this.types.getValue().equals("Deals")) {
                for (int i = 0; i < cheapSharkDealResults.length; i++) {
                    if (cheapSharkDealResults[i].steamAppID != null) {
                        bartervgSearchUri = "https://barter.vg/steam/app/" +
                            cheapSharkDealResults[i].steamAppID + "/json";
                        HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(bartervgSearchUri))
                            .build();
                        HttpResponse<String> response = HTTP_CLIENT
                            .send(request, BodyHandlers.ofString());
                        System.out.println(response.statusCode());
                        if (response.statusCode() != 200) {
                            throw new IOException(response.toString());
                        } // if
                        String jsonString = response.body();
                        barterVgApiResult = GSON.fromJson(
                            jsonString, BarterVgApiResult.class);
                        playerCounts[i] = barterVgApiResult.playerCount;
                        recentPlayerCounts[i] = barterVgApiResult.recentPlayers;
                        progressBar.setProgress(
                            1.0 * i / cheapSharkGameResults.length);
                    }
                }
            } else if (this.types.getValue().equals("Games")) {
                for (int i = 0; i < cheapSharkGameResults.length; i++) {
                    if (cheapSharkGameResults[i].steamAppID != null) {

                        bartervgSearchUri = "https://barter.vg/steam/app/" +
                            cheapSharkGameResults[i].steamAppID + "/json";
                        HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(bartervgSearchUri))
                            .build();
                        HttpResponse<String> response = HTTP_CLIENT
                            .send(request, BodyHandlers.ofString());
                        System.out.println(response.statusCode());
                        if (response.statusCode() != 200) {
                            throw new IOException(response.toString());
                        } // if
                        String jsonString = response.body();
                        barterVgApiResult = GSON.fromJson(
                            jsonString, BarterVgApiResult.class);
                        playerCounts[i] = barterVgApiResult.playerCount;
                        recentPlayerCounts[i] = barterVgApiResult.recentPlayers;
                        progressBar.setProgress(
                            1.0 * i / cheapSharkGameResults.length);
                    }
                }
            }
        } catch (Exception e) {
            this.alertError(e);
        }
    }

    /**
     * Creates the images from the parsed JSON response in {@code cheapSharkDealResults} and
     * {@code cheapSharkGameResults} to create images to be used in the {@code images1}
     * ImageView, and increases the progress bar for each image created.
     */
    public void createResults() {
        if (this.types.getValue().equals("Deals")) {
            for (int i = 0; i < cheapSharkGameResults.length; i++) {
                images[i] = new Image(cheapSharkDealResults[i].thumb);
                progressBar.setProgress(
                    0.5 * i / cheapSharkDealResults.length);
            }
            Platform.runLater(() -> {
                displayResults(0);
                progressBar.setProgress(1);
                searchButton.setDisable(false);
                nextButton.setDisable(false);
            });
        } else if (this.types.getValue().equals("Games")) {
            for (int i = 0; i < cheapSharkGameResults.length; i++) {
                images[i] = new Image(cheapSharkGameResults[i].thumb);
                progressBar.setProgress(
                    0.5 * i / cheapSharkGameResults.length + 0.5);
            }
            Platform.runLater(() -> {
                progressBar.setProgress(1);
                displayResults(0);
                instructionLabel.setText(gameSearchUri);
                searchButton.setDisable(false);
                nextButton.setDisable(false);
            });
        }

    }

    /**
     * Modifies the text in the {@code results1} HBox to display the important
     * characteristics of the specified game in the list, including an image of
     * the thumbnail, the sale price, the title, the player count, and the deal
     * link.
     *
     * @param start the specified game in the list to display.
     */
    public void displayResults(int start) {
        if (this.types.getValue().equals("Deals")) {
            CheapSharkDealResult result = cheapSharkDealResults[start];
            image1.setImage(images[start]);
            resultsText1.setText(
                "Title: " + result.title +
                "\nSale Price: " + result.salePrice +
                "\nNormal Price: " + result.normalPrice +
                "\nPlayer Count: " + playerCounts[start] +
                "\nRecent Players: " + recentPlayerCounts[start] +
                "\nMetacritic Score: " + result.metacriticScore +
                "\nDeal ID: " + "https://www.cheapshark.com/redirect?dealID=\n" +
                    result.dealID);
        } else if (this.types.getValue().equals("Games")) {
            CheapSharkGameResult result = cheapSharkGameResults[start];
            image1.setImage(images[start]);
            resultsText1.setText(
                "Title: " + result.external +
                "\nPlayer Count: " + playerCounts[start] +
                "\nRecent Players: " + recentPlayerCounts[start] +
                "\nSale Price: " + result.cheapest +
                "\nDeal ID: " + "https://www.cheapshark.com/redirect?dealID=\n" +
                result.cheapestDealID);
        }
    }

    /**
     * Pops up a new window displaying the thrown exception, such as a faulty status
     * code from the API response or a different exception during the search.
     *
     * @param cause the cause of the error.
     */
    public void alertError(Throwable cause) {
        Platform.runLater(() -> {
            this.instructionLabel.setText("Last attempt to get results failed...");
            this.progressBar.setProgress(1.0);
            searchButton.setDisable(false);
            Text text = new Text("CheapShark URI: " + gameSearchUri +
                "\nBarterVG URI: " + bartervgSearchUri +
                "\nException: " + cause.toString());
            Alert alert = new Alert(AlertType.ERROR);
            alert.getDialogPane().setContent(text);
            alert.setResizable(true);
            alert.showAndWait();
        });
    } // alertError

     /**
     * Creates a new thread that runs the code contained in the {@code target}.
     *
     * @param target the code to run on a new Daemon thread.
     */
    public static void runNow(Runnable target) {
        Thread t = new Thread(target);
        t.setDaemon(true);
        t.start();
    }

    /**
     * Checks whether a given string is numeric.
     *
     * @param str the string to check.
     *
     * @return whether the string is numeric or not.
     */
    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

} // ApiApp
