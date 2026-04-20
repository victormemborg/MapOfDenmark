package mod.core;

import javafx.animation.KeyFrame;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import mod.io.MapDataReader;
import mod.io.MapDataWriter;
import mod.osm.OsmNode;
import mod.osm.OsmTag;
import mod.osm.OsmWay;
import mod.pathfinding.AddressTrie;
import mod.pathfinding.Dijkstra;
import mod.renderer.MapData;
import mod.utils.Address;
import mod.utils.Transport;
import mod.view.MapCanvas;
import mod.view.PointOfInterest;
import mod.view.StyleAttributes;
import mod.view.Theme;
import javafx.animation.Timeline;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static mod.osm.OsmTag.Key.PATHFINDING;

public class Controller {
    private long lastMBPress;
    @FXML
    public Scene scene;
    @FXML
    public MenuBar menuBar;
    @FXML
    public VBox suggestions;
    @FXML
    public MenuItem debugPanelMenuButton;
    private Model model;
    private double lastX;
    private double lastY;
    @FXML
    private MapCanvas canvas;
    @FXML
    private VBox debugViewBox;
    @FXML
    private VBox POIViewBox;
    @FXML
    private MenuItem openPOIMenu;
    @FXML
    private GridPane searchGridPane;
    @FXML
    private TextField searchBarLookup;
    @FXML
    private TextField searchBarFrom;
    @FXML
    private TextField searchBarTo;
    @FXML
    private Button searchLookupButton;
    @FXML
    private CheckBox kdViewCheckBox;
    @FXML
    private ListView<String> autocompleteListView;
    @FXML
    private Button searchFindRouteButton;
    @FXML
    private ComboBox<String> transportMenu;
    @FXML
    private ComboBox<String> themeMenu;
    @FXML
    private Label coordsLabel;
    private TextField currentSearchBar;
    private static ProgressBar progressBar = new ProgressBar();
    private static Label progressLabel = new Label();
    private Stage progressStage = new Stage();
    @FXML
    private VBox routeViewBox;
    @FXML
    private CheckBox heuristicViewCheckBox;
    @FXML
    private Menu fileMenu;

    /**
     * Makes sure the address input is formatted correctly.
     * It will capitalize the first letter of each word and add a comma after the house no. if it is missing.
     * It will lowercase all other letters.
     *
     * @param input The string address to format.
     * @return The formatted address.
     */
    private static String formatAddress(String input) {
        if (input.isEmpty()) {
            return "";
        }

        Character lastCharacter = null;
        StringBuilder formatted = new StringBuilder();

        for (char character : input.toCharArray()) {
            if (lastCharacter == null) {
                lastCharacter = character;
                formatted.append(Character.toUpperCase(character));
                continue;
            }

            if (Character.isWhitespace(lastCharacter) && Character.isWhitespace(character)) {
                continue;
            }

            if (Character.isWhitespace(lastCharacter) || Character.isDigit(lastCharacter)) {
                formatted.append(Character.toUpperCase(character));
                lastCharacter = character;
                continue;
            }

            formatted.append(Character.toLowerCase(character));
            lastCharacter = character;
        }

        return formatted.toString();
    }

    /**
     * Called from Model to initialize the controller with the model.
     * @param model The model to initialize the controller with.
     */
    public void init(Model model) {
        this.model = model;
        this.initComboBox();
        this.toggleMapButtons(true);
        createProgressBar();
    }

    /**
     * Initializes the ComboBoxes for theme and transport mode.
     * Also sets the listeners for the ComboBoxes.
     */
    private void initComboBox() {
        // Get all the themes and set the theme menu
        List<String> themes = Arrays.stream(Theme.values()).map(Theme::toString).toList();
        this.themeMenu.getItems().setAll(themes);
        // Listen for changes in the theme menu
        this.themeMenu.valueProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue) {
                case "Default mode":
                    StyleAttributes.setTheme(Theme.DEFAULT);
                    break;
                case "Dark mode":
                    StyleAttributes.setTheme(Theme.DARK);
                    break;
                case "Color blind mode":
                    StyleAttributes.setTheme(Theme.COLORBLIND);
                    break;
                default:
                    throw new IllegalStateException("Theme does not exist!");
            }

            this.canvas.getRenderer().update();
        });

        // Get all the transport modes and set the transport menu
        List<String> transport = Arrays.stream(Transport.values()).map(Transport::toString).toList();
        this.transportMenu.getItems().setAll(transport);
        // Listen for changes in the theme menu
        this.transportMenu.valueProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue) {
                case "Car":
                    Transport.setMode(Transport.CAR);
                    break;
                case "Walk":
                    Transport.setMode(Transport.WALK);
                    break;
                case "Bike":
                    Transport.setMode(Transport.BIKE);
                    break;
                default:
                    throw new IllegalStateException("Transport mode does not exist!");
            }

            this.canvas.getRenderer().update();
        });
    }

    /**
     * Called when the window is resized.
     * @param stage The stage to resize the canvas to.
     */
    public void onResize(Stage stage) {
        this.canvas.setWidth(stage.getWidth() - this.searchGridPane.getWidth());
        this.canvas.setHeight(stage.getHeight() - this.menuBar.getHeight());
        this.canvas.getRenderer().update();
    }

    /**
     * Toggles various options in the GUI.
     * @param value true to hide it, false to show it.
     */
    public void toggleMapButtons(boolean value) {
        this.searchBarLookup.setDisable(value);
        this.searchLookupButton.setDisable(value);
        this.searchBarFrom.setDisable(value);
        this.searchBarTo.setDisable(value);
        this.transportMenu.setDisable(value);
        this.searchFindRouteButton.setDisable(value);
        this.themeMenu.setDisable(value);
        this.debugPanelMenuButton.setDisable(value);
        this.openPOIMenu.setDisable(value);
        this.canvas.setDisable(value);
    }

    /**
     * Shows an alert dialog with the given title and content.
     * @param title The title of the alert dialog.
     * @param content the message to display in the alert dialog.
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("An error occurred!");
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Shows an information dialog with the given title and content.
     * @param title The title of the information dialog.
     * @param content the message to display in the information dialog.
     */
    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("Information");
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * This method is called when the user moves the mouse on the canvas.
     * It changes the coordinates label to show the current map coordinates of the mouse.
     * @param event The mouse event.
     */
    @FXML
    public void onMouseMoved(MouseEvent event) {
        if (debugViewBox.isVisible()) {
            double lat = event.getX();
            double lon = event.getY();
            Point2D translatedCoords = this.canvas.toMapCoords(lat, lon);
            this.coordsLabel.setText(String.format("Lat: %.5f, Lon: %.5f%n", translatedCoords.getY(), translatedCoords.getX()));
        }
    }

    /**
     * This method is called when the user presses the mouse on the canvas.
     * It will create a point of interest if the user double clicks.
     * @param event The mouse event.
     */
    @FXML
    public void onMousePressed(MouseEvent event) {
        lastX = event.getX();
        lastY = event.getY();
        long thisMBPress = System.currentTimeMillis();

        // If double click, create point of interest
        if (thisMBPress - lastMBPress < 200) {
            createPOI();
            canvas.getRenderer().update();
        }

        lastMBPress = thisMBPress;
    }

    /**
     * This method is called when the user drags the mouse on the canvas.
     * It will pan the view of the canvas.
     * @param event The mouse event.
     */
    @FXML
    public void onMouseDragged(MouseEvent event) {
        double dx = event.getX() - lastX;
        double dy = event.getY() - lastY;
        canvas.pan(dx, dy);

        this.lastX = event.getX();
        this.lastY = event.getY();
    }

    /**
     * This method is called when the user scrolls on the canvas.
     * It will zoom the view of the canvas.
     * @param event The scroll event.
     */
    @FXML
    public void onScroll(ScrollEvent event) {
        double factor = Math.pow(1.01, event.getDeltaY());
        canvas.zoom(factor, event.getX(), event.getY());
    }

    /**
     * This method is called when the user clicks the "Create binary file" button.
     * It will open a file chooser dialog and create a binary file from the selected OSM file.
     * Furthermore, it uses multithreading so the GUI does not freeze while creating the binary file.
     */
    @FXML
    public void createBinaryAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a file");
        fileChooser.getExtensionFilters().setAll(new FileChooser.ExtensionFilter("Supported OSM file formats", "*.osm", "*.gz", "*.bz2"));
        File initialDir = Path.of(System.getProperty("user.home"), "Downloads").toFile();
        fileChooser.setInitialDirectory(initialDir);

        File file = fileChooser.showOpenDialog(scene.getWindow());

        // Check if dialog is closed without selecting a file.
        if (file == null) return;

        // Show progress bar
        showProgressBar();
        progressLabel.setText("Creating binary file");

        Task<Void> createBinaryTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    File outFile = new File(file.getAbsolutePath() + ".obj");
                    MapData mapData = model.getOsmParser().parse(file, false);
                    MapDataWriter mapDataWriter = model.getBinaryWriter();
                    mapDataWriter.write(outFile, mapData);
                    model.getOsmParser().cleanup();
                } catch (IOException | ClassNotFoundException e) {
                    throw e;
                }
                return null; // has to return something for Void.
            }
        };

        createBinaryTask.setOnSucceeded(e -> {
            progressStage.close();
            showInfo("Success", "Binary file created successfully");
        });

        createBinaryTask.setOnFailed(e -> {
            progressStage.close();
            showAlert("Error", createBinaryTask.getException().getMessage());
            createBinaryTask.getException().printStackTrace();
        });

        Thread thread = new Thread(createBinaryTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void createProgressBar() {
        progressBar.setStyle("-fx-min-width: 200px; -fx-min-height: 20px; ");
        progressBar.setProgress(0);

        //Animation + styling for label
        progressLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; ");
        Timeline dots = new Timeline(new KeyFrame(Duration.millis(500), e -> {
            if(progressLabel.getText().contains("Creating binary file"))
                if (progressLabel.getText().endsWith("...")) {
                    progressLabel.setText("Creating binary file");
                } else {
                    progressLabel.setText(progressLabel.getText() + ".");
                }
        }));
        dots.setCycleCount(Timeline.INDEFINITE);
        dots.play();

        //box + stage for progress bar and label
        VBox progressBox = new VBox(progressLabel, progressBar);
        progressBox.setStyle("-fx-alignment: center; -fx-background-color: lightgray; -fx-padding: 10px;");
        Scene progressScene = new Scene(progressBox, 360, 100);
        progressStage.resizableProperty().setValue(false);
        progressStage.setScene(progressScene);
        progressStage.setTitle("Loading");
    }


    private void showProgressBar() {
        progressBar.setProgress(0); // Reset progress bar
        progressStage.show();
        progressStage.setAlwaysOnTop(true);
    }

    /**
     * Sets the progress of the progress bar.
     * @param progress The progress to set the progress bar to.
     */
    public static void setProgressBarProgress(double progress) {
        progressBar.setProgress(progress);
    }

    /**
     * Sets the label of the progress bar.
     * @param progress the text to display in the progress vbox.
     */
    public static void setLabelProgress(String progress) {
        progressLabel.setText(progress);
    }

    /**
     * This method is called when the user clicks the "Load binary" button.
     * It will open a file chooser dialog and load the selected binary file.
     * Furthermore, it uses multithreading so the GUI does not freeze while loading the binary file.
     */
    @FXML
    public void loadBinaryAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a file");
        fileChooser.getExtensionFilters().setAll(new FileChooser.ExtensionFilter("Select binary file", "*.obj"));
        File initialDir = Path.of(System.getProperty("user.home"), "Downloads").toFile();
        fileChooser.setInitialDirectory(initialDir);

        File file = fileChooser.showOpenDialog(this.scene.getWindow());
        if (file == null) return;

        showProgressBar();

        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws IOException, ClassNotFoundException {
                MapDataReader mapDataReader = model.getBinaryReader();
                MapData mapData = mapDataReader.read(file);
                fileMenu.setDisable(true);

                Platform.runLater(() -> {
                    try {
                        canvas.getRenderer().render(mapData);
                        model.setMapData(mapData);
                        model.setMapLoaded(true);
                        toggleMapButtons(false);
                    } catch (Exception e) {
                        showAlert("Error during load", e.getMessage());
                        e.printStackTrace();
                    }
                });
                return null;
            }
        };

        loadTask.setOnFailed(e -> {
            progressStage.close();
            showAlert("Error", loadTask.getException().getMessage());
            loadTask.getException().printStackTrace();
        });
        loadTask.setOnSucceeded(e -> {
            progressStage.close();
            showInfo("Success", "Binary file loaded successfully");
        });

        Thread thread = new Thread(loadTask);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Opens/closes debug panel.
     */
    @FXML
    public void openDebugWindow() {
        this.debugViewBox.setVisible(!this.debugViewBox.isVisible());
    }

    /**
     * This method is called when the user types in the search bar.
     * It will retrieve possible completions for the input and display them in the autocomplete list view.
     */
    @FXML
    public void getLookupCompletions(KeyEvent event) {
        adjustListView(event);
        String nonformatted = ((TextField) event.getSource()).getText();
        String input = formatAddress(nonformatted);

        if (input.isBlank()) {
            this.autocompleteListView.setVisible(false);
            this.autocompleteListView.setManaged(false);
            return;
        }

        String[] completions = retrieveCompletions(input);
        updateAutocompleteListView(completions);
    }

    /**
     * Adjusts the position of the autocomplete list view based on the search bar that is currently being typed in.
     */
    private void adjustListView(KeyEvent event) {
        Object textBox = event.getSource();
        if (textBox instanceof TextField) {
            TextField textField = (TextField) textBox;
            this.currentSearchBar = textField;
            switch(textField.getId()) {
                case "searchBarLookup":
                    GridPane.setRowIndex(autocompleteListView, 2);
                    break;
                case "searchBarFrom":
                    GridPane.setRowIndex(autocompleteListView, 6);
                    break;
                case "searchBarTo":
                    GridPane.setRowIndex(autocompleteListView, 7);
                    break;
                default:
                    return;
            }
            autocompleteListView.setVisible(true);
            autocompleteListView.setManaged(true);
            autocompleteListView.toFront();
        }
    }


    private String[] retrieveCompletions(String input) {
        MapData mapData = this.model.getMapData();
        AddressTrie addressTrie = mapData.getAddressTrie();
        return addressTrie.getCompletions(input, 5);
    }

    private void updateAutocompleteListView(String[] completions) {
        this.autocompleteListView.getItems().clear();

        for (String completion : completions) {
            if (completion != null) { //maybe this should be changed in our address trie instead
                this.autocompleteListView.getItems().add(completion);
            }
        }

        this.autocompleteListView.setVisible(completions.length > 0);
    }

    /**
     * This method is called when the user clicks on a completion in the autocomplete list view.
     * It will set the text of the search bar to the selected completion and hide the autocomplete list view.
     */
    @FXML
    public void onAutocompleteClicked() {
        currentSearchBar.setText(autocompleteListView.getSelectionModel().getSelectedItem());
        this.autocompleteListView.setVisible(false);
        this.autocompleteListView.setManaged(false);
        this.canvas.requestFocus();
    }

    /**
     * This method is called when the user clicks the search button.
     * It will look for the address in the search bar and center the view to the address.
     */
    @FXML
    public void searchLookup(MouseEvent event) {
        AddressTrie addressTrie = this.model.getMapData().getAddressTrie();
        AddressTrie.Node addressNode = addressTrie.find(this.searchBarLookup.getText());

        if (addressNode == null) return;

        showAddress(addressNode.getAddress());
        this.canvas.getRenderer().update();
    }

    /**
     * This method is called when the user clicks the search button.
     * It will look for the address in the search bar and center the view to the address.
     */
    public void showAddress(Address address) {
       OsmNode node = address.getNode();
       canvas.focusOn(node.getLon(), node.getLat(), 0.95);
       PointOfInterest poi = new PointOfInterest(address.getStreet() + " " + address.getHouseNumber(), node.getLat(), node.getLon(), Color.ORANGERED);
       addTempPOI(poi);
       poi.setTemporary(true);
    }

    @FXML
    public void toggleKDView() {
        canvas.getRenderer().toggleViewDebug();
    }

    /**
     * Opens dialog for creating a new POI
     * and adds the POI to the list of POIs
     */
    private void createPOI() {
        //name of POI
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Point of Interest");
        dialog.setHeaderText("Enter name of Point of Interest");
        dialog.setContentText("Name:");

        // Color box
        ChoiceBox<String> colorChoiceBox = new ChoiceBox<>();
        colorChoiceBox.getItems().addAll("Red", "Green", "Blue");
        colorChoiceBox.setValue("Red");
        dialog.getDialogPane().setContent(new VBox(10, dialog.getDialogPane().getContent(), colorChoiceBox));

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            Point2D coords = canvas.toMapCoords(lastX, lastY);
            Color selectedColor = switch (colorChoiceBox.getValue()) {
                case "Red" -> Color.RED;
                case "Green" -> Color.GREEN;
                case "Blue" -> Color.BLUE;
                default -> Color.RED;
            };

            PointOfInterest poi = new PointOfInterest(name, coords.getY(), coords.getX(), selectedColor);

            List<PointOfInterest> pois = model.getMapData().getPointsOfInterest();
            pois.add(poi);
            updatePOIListView();
        });
    }



    /**
     * Opens the POI window
     */
    public void openPOIWindow() {
        this.POIViewBox.setVisible(!this.POIViewBox.isVisible());
        updatePOIListView();
    }

    /**
     * Responsible for adding all POI to the POI list view.
     * and adding the necessary event listeners to the items
     * moreover it adds vbox and a list view inside the POIViewBox
     */
    private void updatePOIListView() {
        this.POIViewBox.getChildren().clear();
        Label header = new Label("POI List");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-alignment: center;");
        List<PointOfInterest> pois = model.getMapData().getPointsOfInterest();

        ObservableList<VBox> items = FXCollections.observableArrayList();

        for (PointOfInterest poi : pois) {
            VBox poiBox = new VBox();
            Label nameLabel = new Label(poi.getName());
            //format lat and lon to 5 decimal places
            Label coordsLabel = new Label("Lat: " + String.format("%.5f", poi.getLat()) + ", Lon: " + String.format("%.5f", poi.getLong()));
            ChoiceBox<String> colorChoiceBox = new ChoiceBox<>();
            colorChoiceBox.setValue(poi.getColor());
            colorChoiceBox.getItems().addAll("Red", "Green", "Blue");
            colorChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                Color selectedColor = switch (newValue) {
                    case "Red" -> Color.RED;
                    case "Green" -> Color.GREEN;
                    case "Blue" -> Color.BLUE;
                    default -> Color.RED;
                };
                poi.setColor(selectedColor);
                canvas.getRenderer().update();
            });

            Button goToButton = new Button("Go to");
            goToButton.setOnAction(event -> {
                canvas.focusOn(poi.getLong(), poi.getLat(), 0.95);
            });
            Button deleteButton = new Button("Delete POI");
            deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-weight: 900;"); // Ovverride default button style
            deleteButton.setOnAction(event -> {
                pois.remove(poi);
                updatePOIListView();
                canvas.getRenderer().update();
            });
            poiBox.getChildren().addAll(nameLabel, coordsLabel, colorChoiceBox, goToButton, deleteButton);
            poiBox.setSpacing(5); // Add some spacing between elements
            poiBox.setStyle("-fx-padding: 10px; -fx-alignment: center;"); // Add padding to the VBox

            items.add(poiBox);
        }

        ListView<VBox> poiListView = new ListView<>(items);
        poiListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(VBox item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setGraphic(item);
                }
            }
        });

        VBox container = new VBox(header, poiListView);
        container.setSpacing(10); // Add spacing between header and ListView
        this.POIViewBox.getChildren().add(container);
    }

    /**
     * Adds a temporary POI to the list of POIs
     * and removes the old temporary POI
     * @param poi The temporary POI to add
     */
    private void addTempPOI(PointOfInterest poi) {
        List<PointOfInterest> pois = model.getMapData().getPointsOfInterest();
        for(PointOfInterest p : pois) {
            if(p.isTemporary()) {
                pois.remove(p);
                break;
            }
        }
        pois.add(poi);
        updatePOIListView();
    }

    /**
     * This method is called when the user clicks the "Find route" button.
     * It will find a path between the two addresses in the search bars.
     */
    public void searchFindRoute() {
        String from = this.searchBarFrom.getText();
        String to = this.searchBarTo.getText();
        Transport mode = Transport.getMode();
        showRouteStatus();
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                findRoute(from, to, mode);
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            showRouteStatus();
            showInfo("Success", "Route found successfully");
        });
        task.setOnFailed(e -> {
            showRouteStatus();
            showAlert("Error", task.getException().getMessage());
            task.getException().printStackTrace();
        });
        new Thread(task).start();
    }

    public void showRouteStatus() {
        routeViewBox.setVisible(!routeViewBox.isVisible());
    }

    public void findRoute(String from, String to, Transport mode) {
        try {
            OsmNode[] nodes = getNodesFromTwoAddresses(from, to);
            //Get the nodes from the addresses
            OsmNode fromAddr = nodes[0];
            OsmNode fromHwy = nodes[1];
            OsmNode toAddr = nodes[2];
            OsmNode toHwy = nodes[3];

            Dijkstra dijkstra = this.model.getMapData().getDijkstra();
            List<OsmNode> path = dijkstra.getShortestPath(fromHwy, toHwy, mode);

            OsmNode[] nodesToDraw = new OsmNode[path.size()];
            nodesToDraw = path.toArray(nodesToDraw);

            OsmWay wayToDraw = new OsmWay(nodesToDraw, new OsmTag[]{new OsmTag(PATHFINDING, null)});
            this.model.getMapData().setPath(wayToDraw);

            // only run on the JavaFX thread
            Platform.runLater(() -> this.canvas.focusOn(fromAddr, toAddr));

        } catch (IllegalArgumentException iae) {
            showAlert("Address not found", "One or both of the addresses could not be found. Please try again.");
            iae.printStackTrace();
        }
    }

    public OsmNode[] getNodesFromTwoAddresses(String firstAddress, String secondAddress) throws IllegalArgumentException {
        AddressTrie addressTrie = this.model.getMapData().getAddressTrie();
        AddressTrie.Node firstNode = addressTrie.find(firstAddress);
        AddressTrie.Node secondNode = addressTrie.find(secondAddress);

        if (firstNode == null || secondNode == null) {
            throw new IllegalArgumentException();
        }

        OsmNode firstAddrNode = firstNode.getAddress().getNode();
        OsmNode firstHwyNode = firstNode.getAddress().getNearestHighway();

        OsmNode secondAddrNode = secondNode.getAddress().getNode();
        OsmNode secondHwyNode = secondNode.getAddress().getNearestHighway();

        return new OsmNode[]{firstAddrNode, firstHwyNode, secondAddrNode, secondHwyNode};
    }

    /**
     * This method is called when the user clicks the "Load default binary" button.
     * It will load the default binary file.
     */
    public void useDefaultBinary() {
        showProgressBar();
        progressLabel.setText("Loading default binary file...");
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);

        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws IOException, ClassNotFoundException {
                MapDataReader mapDataReader = model.getBinaryReader();
                MapData mapData = mapDataReader.read(Controller.class.getResourceAsStream("/mod/data/default.osm.obj"));

                Platform.runLater(() -> {
                    try {
                        canvas.getRenderer().render(mapData);
                        model.setMapData(mapData);
                        model.setMapLoaded(true);
                        toggleMapButtons(false);
                        fileMenu.setDisable(true);
                    } catch (Exception e) {
                        showAlert("Error during load", e.getMessage());
                        e.printStackTrace();
                    }
                });
                return null;
            }
        };

        loadTask.setOnFailed(e -> {
            progressStage.close();
            showAlert("Error", loadTask.getException().getMessage());
            loadTask.getException().printStackTrace();
        });
        loadTask.setOnSucceeded(e -> {
            progressStage.close();
            showInfo("Success", "Binary file loaded successfully");
        });

        Thread thread = new Thread(loadTask);
        thread.setDaemon(true);
        thread.start();
    };

    public void toggleHeuristicView() {
        if(!routeViewBox.isVisible()) {
            this.canvas.getRenderer().toggleViewHeuristics();
        } else {
            showAlert("Error", "Cannot toggle heuristic view while route is generating");
            this.heuristicViewCheckBox.setSelected(false);
        }
    }
}
