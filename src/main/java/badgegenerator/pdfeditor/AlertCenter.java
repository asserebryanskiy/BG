package badgegenerator.pdfeditor;

import javafx.animation.FadeTransition;
import javafx.animation.FillTransition;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * AlertCenter is installed on some pane provided in a constructor
 * and shows different notifications to user, provided by the method
 * {@code showNotification(String message)}.
 *
 * Notification could be flagged with color to attract user's attention.
 *
 * Click on the alertLine opens popup with detailed information about messages.
 */
public class AlertCenter {
    private static final int LEFT_TEXT_PADDING = 10;
    private static final int BUTTON_WIDTH = 80;
    private static final int V_GAP = 10;
    private static final int TOP_PADDING = 10;
    private static final Color WARNING = Color.color(244.0/255, 144.0/255, 57.0/255);
    private static final Color COMMON = Color.color(62.0/255,41.0/255,54.0/255,0.5);
    private static final int ANIMATION_DURATION = 400;
    private static final String COMMON_COUNTER = "-fx-background-color: rgb(62,41,54);" +
            "-fx-text-fill: white;";
    private static final String WARNING_COUNTER = "-fx-background-color: rgb(239,105,74);" +
            "-fx-text-fill: rgb(62,41,54);";
    private static final double POPUP_MAX_HEIGHT = 200;

    private Popup popup;          // popup with all notifications that are shown in time descending order
    private GridPane notificationsGrid;   // root where all notifications are added
    private final Label last;           // alert line with last notification
    private final Button counter;       // button that on click shows all popups
                                        // (text on button reveals number of notifications on stack)
    private final Pane base;            // base for alert line, where last notification is shown
    private final Rectangle alertBack;  // background of the alert line

    // helper field that is true if new notification was added
    // but has not been yet revealed in details in popup
    // If true, alert line has bright, attention attracting colors.
    private boolean hasNewNotifications = false;

    public AlertCenter(Pane base) {
        this.base = base;
        preparePopup();

        // set up alert line (with last notification and notifications' number counter)
        StackPane alertRoot = new StackPane();
        base.getChildren().add(alertRoot);

        GridPane alertLine = new GridPane();
        alertLine.getStylesheets().add(getClass().getResource("/css/popup.css").toExternalForm());
        alertLine.prefWidthProperty().bind(base.prefWidthProperty());
        alertLine.getStyleClass().add("alert_line_background");
        last = new Label();
        last.prefWidthProperty().bind(base.prefWidthProperty()
                .subtract(BUTTON_WIDTH));
        last.setWrapText(false);
        last.setId("lastNotification");
        last.setOnMouseClicked(e -> showPopup());
        last.setCursor(Cursor.HAND);
        last.setFont(new Font("Circe Light", 13));
        alertLine.add(last, 0, 0);
        alertBack = new Rectangle();
        alertBack.widthProperty().bind(base.prefWidthProperty());
        alertBack.setHeight(TOP_PADDING * 2 + 13);
        alertBack.setFill(COMMON);
        alertRoot.getChildren().addAll(alertBack, alertLine);

        // set up counter btn
        counter = new Button("0");
        counter.setOnMouseClicked(e -> showPopup());
        counter.setId("counterBtn");
        counter.setStyle(COMMON_COUNTER);
        counter.setFont(new Font("Circe Bold", 13));
        alertLine.add(counter, 1, 0);
        ColumnConstraints cc = new ColumnConstraints(BUTTON_WIDTH);
        cc.setHalignment(HPos.CENTER);
        alertLine.getColumnConstraints().add(0, new ColumnConstraints());
        alertLine.getColumnConstraints().add(1, cc);
    }

    private void showPopup() {
        if (!popup.isShowing() && hasNotifications()) {
            // show popup
            Point2D coord = base.localToScreen(
                    base.getLayoutX(),
                    last.getBoundsInParent().getHeight());
            popup.show(base, coord.getX(), coord.getY());
            // if hasNewNotifications change it to false, swap colors, set last text to ""
            FadeTransition fade = new FadeTransition(Duration.millis(ANIMATION_DURATION), last);
            fade.setToValue(0);
            fade.setOnFinished(event -> last.setText(""));
            fade.play();
            if (hasNewNotifications) {
                fillAnimation(alertBack, WARNING, COMMON);
                counter.setStyle(COMMON_COUNTER);
                hasNewNotifications = false;
            }
        } else {
            popup.hide();
        }
    }

    private void preparePopup() {
        // set up popup
        popup = new Popup();
        popup.setAutoHide(true);

        // set up popup content
        VBox popupRoot = new VBox(V_GAP);
        popupRoot.getStylesheets().add(getClass().getResource("/css/popup.css").toExternalForm());
        popupRoot.getStyleClass().add("popup_background");
        popupRoot.setAlignment(Pos.CENTER_RIGHT);

        notificationsGrid = new GridPane();
        notificationsGrid.getStyleClass().add("popup_background");
        notificationsGrid.setPadding(new Insets(0, 0, 0, LEFT_TEXT_PADDING));
        notificationsGrid.setVgap(V_GAP);
        Button clearBtn = new Button("Очистить");
        clearBtn.setOnMouseClicked(e -> IntStream.range(0, numberOfNotifications()).forEach(this::remove));
        clearBtn.setPrefWidth(BUTTON_WIDTH);
        notificationsGrid.addColumn(1, clearBtn);
        notificationsGrid.getColumnConstraints().add(new ColumnConstraints());
        ColumnConstraints column = new ColumnConstraints(BUTTON_WIDTH);
        column.setHalignment(HPos.CENTER);
        notificationsGrid.getColumnConstraints().add(column);
        ScrollPane scrollPane = new ScrollPane(notificationsGrid) {
            // it is added because when pane is focused it
            // moves right on several px and breaks the design
            @Override
            public void requestFocus() {}
        };
        scrollPane.prefWidthProperty().bind(base.prefWidthProperty());
        notificationsGrid.boundsInParentProperty().addListener(((observable, oldValue, newValue) -> {
            scrollPane.setPrefHeight(newValue.getHeight() + V_GAP);
        }));
        scrollPane.setMaxHeight(POPUP_MAX_HEIGHT - clearBtn.getBoundsInLocal().getHeight() - V_GAP);
        scrollPane.setId("scrollPane");
        popupRoot.getChildren().addAll(scrollPane, clearBtn);

        popup.getContent().add(popupRoot);
    }

    public void flagLast() {
        if (hasNewNotifications) {
            FillTransition transition =
                    new FillTransition(Duration.millis(ANIMATION_DURATION), alertBack, WARNING, COMMON);
            FillTransition reverse =
                    new FillTransition(Duration.millis(ANIMATION_DURATION), alertBack, COMMON, WARNING);
            transition.setOnFinished(e -> reverse.play());
            transition.play();
        } else {
            fillAnimation(alertBack, COMMON, WARNING);
            counter.setStyle(WARNING_COUNTER);
            hasNewNotifications = true;
        }
    }

    public int numberOfNotifications() {
        return Integer.parseInt(counter.getText());
    }

    private void remove(int row) {
        // fade animation
        Stack<FadeTransition> transitions = new Stack<>();
        notificationsGrid.getChildren().stream()
                .filter(n -> GridPane.getRowIndex(n) == row)
                .forEach(node -> {
                    FadeTransition transition = new FadeTransition(Duration.millis(ANIMATION_DURATION), node);
                    transition.setToValue(0);
                    transition.play();
                    transitions.push(transition);
                });

        transitions.pop().setOnFinished(e -> {
            // delete row
            ObservableList<Node> children = notificationsGrid.getChildren();
            for (int i = 0; i < children.size(); i++) {
                Node child = children.get(i);
                int rowIndex = GridPane.getRowIndex(child);
                if      (rowIndex > row) GridPane.setRowIndex(child, rowIndex - 1);
                else if (rowIndex == row) notificationsGrid.getChildren().remove(i);
            }
            notificationsGrid.getParent().layout();

            // decrement counter
            int current = Integer.parseInt(counter.getText());
            counter.setText(String.valueOf(--current));
            if (current == 0) popup.hide();
        });
    }

    private void fillAnimation(Rectangle alertBack, Color from, Color to) {
        FillTransition transition = 
                new FillTransition(Duration.millis(ANIMATION_DURATION), alertBack, from, to);
        transition.play();
    }

    public boolean hasNotifications() {
        return Integer.parseInt(counter.getText()) > 0;
    }

    private void fade(Node node, double toVal) {
        FadeTransition transition = new FadeTransition(Duration.millis(ANIMATION_DURATION), node);
        transition.setToValue(toVal);
        transition.play();
    }

    public void showNotification(String message) {
        // change last label
        int lineBreak = message.indexOf("\n");
        if (lineBreak != -1) last.setText(message.substring(0, lineBreak));
        else                 last.setText(message);
        fade(last, 1);

        // add notification vBox to popup root
        VBox nRoot = new VBox();    // notifications root
        Text text = new Text(message);
        text.wrappingWidthProperty().bind(base.prefWidthProperty()
                .subtract(BUTTON_WIDTH + LEFT_TEXT_PADDING));
        text.getStyleClass().add("notification");
        Label time = new Label(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        time.getStyleClass().add("time");
        nRoot.getChildren().addAll(time, text);

        // add cross pane to popup root
        Pane crossPane = new Pane();
        crossPane.getStyleClass().add("cross");
        SVGPath svg = new SVGPath();
        svg.setContent("M24 20.188l-8.315-8.209 8.2-8.282-3.697-3.697-8.212 8.318-8.31-8.203-3.666 3.666 8.321 8.24-8.206 8.313 3.666 3.666 8.237-8.318 8.285 8.203z");
        svg.setFill(Color.WHITE);
        crossPane.getChildren().add(svg);
        crossPane.setOnMouseClicked(e -> {
            remove(GridPane.getRowIndex((Node) e.getSource()));
            crossPane.setCursor(Cursor.DEFAULT);
        });
        crossPane.setCursor(Cursor.HAND);
        crossPane.setOnMouseEntered(e -> svg.setFill(Color.LIGHTGRAY));
        crossPane.setOnMouseExited(e -> svg.setFill(Color.WHITE));

        notificationsGrid.getChildren()
                .forEach(node -> GridPane.setRowIndex(node, GridPane.getRowIndex(node) + 1));
        notificationsGrid.addRow(0, nRoot, crossPane);

        // change counter
        int current = Integer.parseInt(counter.getText());
        counter.setText(String.valueOf(current + 1));
    }

    public void showNotification(String message, boolean flag) {
        showNotification(message);
        if (flag) flagLast();
    }

    public List<String> getNotifications() {
        if (!hasNotifications()) return new ArrayList<>(0);
        return notificationsGrid.getChildren().stream()
                .filter(node -> node instanceof VBox)
                .map(node -> ((Text) ((VBox) node).getChildren().get(1)).getText())
                .collect(Collectors.toList());
    }
}
