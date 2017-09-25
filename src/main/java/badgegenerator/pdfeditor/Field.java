package badgegenerator.pdfeditor;

import com.sun.javafx.tk.Toolkit;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.List;

/**
 * Field is special class to work with fields of event participants (such as name, surname etc.)
 */
public class Field extends Pane{
    private double maxAllowableWidth;
    public boolean mayHasHyphenation;
    private TextField fontSizeField;
    private TextField fontNameField;
    private ColorPicker fontColorPicker;
    private double imageToPdfRatio;
    private int numberOfColumn;
    public boolean isSelected = false;

    private int red;
    private int green;
    private int blue;

    private TextFlow textFlow;
    private String[] words;
    private Font font;
    private DoubleProperty fontSize;
    private List<Text> lines;
    private double mouseX;
    private double mouseY;
    private double fieldX;
    private double fieldY;
    private double deltaX;
    private double deltaY;
    private double leftLayoutBorder;
    private double rightLayoutBorder;
    private double topLayoutBorder;
    private double bottomLayoutBorder;
    private List<ResizeableBorder> resizeableBorders;
    private String longestWord;

    private static Line verticalGuide;
    private static List<Guide> guides = new ArrayList<>();
    private IntegerProperty numberOfLines;
    private String fontPath;

    // minWidth - longestWord width
    // prefWidth - current size that could be changed dragging ResizeableBorder
    // maxWidth - is set by layout due to its bounds - 48 = (24*2) - 2 resizeableBorder width
    public Field(String value,
                 String longestWord,
                 int numberOfColumnInExcel,
                 double maxAllowableWidth,
                 double imageToPdfRatio) {
        super();
        // Common
        font = Font.loadFont(getClass().getResourceAsStream("/fonts/Helvetica.otf"), 13);
        fontSize = new SimpleDoubleProperty(font.getSize());
        makeDraggableAndSelectable();
        Text text = new Text(value);
        text.setFont(font);
        textFlow = new TextFlow(text);
        textFlow.setManaged(false);
        getChildren().add(textFlow);
        lines = new ArrayList<>();
        lines.add(text);
        setPrefWidth(text.getBoundsInLocal().getWidth());
        setMinWidth(computeStringWidth(longestWord));
        setId(String.format("field%d", numberOfColumnInExcel));
        this.longestWord = longestWord;
        this.maxAllowableWidth = maxAllowableWidth;
        words = value.split("\\s");
        numberOfLines = new SimpleIntegerProperty(1);

        // for several words
        if(words.length > 1) {
            mayHasHyphenation = true;
            prefHeightProperty().bind(numberOfLines.multiply(fontSize));
            if(getPrefWidth() > maxAllowableWidth) {
                setPrefWidth(maxAllowableWidth);
                computeHyphenation();
            }
            resizeableBorders = new ArrayList<>();
        } else { // for one word
            setMaxHeight(computeMaxHeight());
        }

        this.numberOfColumn = numberOfColumnInExcel;
        this.imageToPdfRatio = imageToPdfRatio;
    }

    void computeHyphenation() {
        double longestLineWidth = 0;
        double spaceWidth = computeStringWidth(" ");
        Paint color = lines.get(0).getFill();
        textFlow.getChildren().clear();
        lines.clear();
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            double wordWidth = computeStringWidth(word);
            double lineWidth = computeStringWidth(line.toString());
            if (line.length() == 0 && wordWidth + spaceWidth > getPrefWidth()) {
                Text newLine =
                        new Text(String.format(word + "%n"));
                newLine.setFont(font);
                newLine.setFill(color);
                textFlow.getChildren().add(newLine);
                lines.add(newLine);
                numberOfLines.set(lines.size());
            } else if (lineWidth + spaceWidth + wordWidth > getPrefWidth()) {
                Text newLine =
                        new Text(String.format(line.toString() + "%n"));
                newLine.setFont(font);
                newLine.setFill(color);
                textFlow.getChildren().add(newLine);
                lines.add(newLine);
                numberOfLines.set(lines.size());
                line.delete(0, line.length());
                line.append(word);
            } else {
                if (line.length() > 0) line.append(" ").append(word);
                else line.append(word);

                lineWidth = computeStringWidth(line.toString());
                if (lineWidth > longestLineWidth) {
                    longestLineWidth = lineWidth;
                }
            }
        }

        if(line.length() > 0) {
            Text newLine = new Text(line.toString());
            newLine.setFont(font);
            newLine.setFill(color);
            textFlow.getChildren().add(newLine);
            textFlow.getChildren().add(new Text(System.lineSeparator()));
            lines.add(newLine);
            numberOfLines.set(lines.size());
        }
        if (longestLineWidth > getMinWidth()) {
            setPrefWidth(longestLineWidth);
        } else setPrefWidth(getMinWidth());
        setMaxHeight(computeMaxHeight());
    }

    public void setFieldFont(Font newFont, double newFontSize) {
        if(newFontSize > 200) return;
        font = new Font(newFont.getName(), newFontSize);
        // checks if new font is not bigger than pdf
        for (String word : words) {
            if(computeStringWidth(word) > maxAllowableWidth) {
                setFieldFont(font, newFontSize - 1);
                return;
            }
        }

        setMinWidth(computeStringWidth(longestWord));
        setFontSize(newFontSize);

        lines.forEach(text -> text.setFont(font));
        setPrefWidth(getBoundsInLocal().getWidth());
        computeHyphenation(); // is called to reduce prefWidth up to longest line
        if(getPrefWidth() > maxAllowableWidth) {
            setPrefWidth(maxAllowableWidth);
            computeHyphenation();
        } else if(getPrefWidth() < getMinWidth()) {
            setPrefWidth(getMinWidth());
            computeHyphenation();
        }

        if (fontSizeField != null) {
            fontSizeField.setText(String.valueOf((int) (getFontSize() / imageToPdfRatio)));
        }
    }

    public double computeStringWidth(String str) {
        return Toolkit.getToolkit().getFontLoader().computeStringWidth(str, font);
    }

    private double computeMaxHeight() {
        return Toolkit.getToolkit().getFontLoader().getFontMetrics(font).getLineHeight()
                * getNumberOfLines();
    }

    public static List<Guide> getGuides() {
        return guides;
    }

    private void calculateLayoutBorders() {
        leftLayoutBorder = getParent().getBoundsInLocal().getMinX();
        rightLayoutBorder = getParent().getBoundsInLocal().getWidth();
        topLayoutBorder = 0;
        bottomLayoutBorder = getParent().getBoundsInLocal().getHeight();
    }

    private void makeDraggableAndSelectable() {
        setOnMousePressed(event -> {
            calculateLayoutBorders();
            // ads possibility to select multiple fields
            if(event.isControlDown()) setSelected(true);
            else {
                for (Node node : getParent().getChildrenUnmodifiable()) {
                    if(node instanceof Field) {
                        ((Field) node).setSelected(false);
                    }
                }
                setSelected(true);
            }

            // displays information about clicked field
            if (fontSizeField != null) {
                fontSizeField.setText(String.valueOf((int) (getFontSize() / imageToPdfRatio)));
            }
            if (fontNameField != null) {
                fontNameField.setText(font.getName());
            }
            if (fontColorPicker != null) {
                fontColorPicker.setValue((Color) lines.get(0).getFill());
            }

            // point, where mouse clicked
            mouseX = event.getSceneX();
            // fieldY - center of field
            fieldX = getLayoutX() + getPrefWidth() / 2;
            // deltaX - distance between click and center of field
            deltaX = fieldX - mouseX;
            mouseY = event.getSceneY();
            fieldY = getLayoutY() + getMaxHeight() / 2;
            deltaY = fieldY - mouseY;
        });
        setOnMouseDragged(event -> {
            double newX = getLayoutX() + event.getSceneX() + deltaX - fieldX;
            double newY = getLayoutY() + event.getSceneY() + deltaY - fieldY;
            if (notInsideParent(newX, newY)) return;
            if (verticalGuide != null) {
                if(newX + getPrefWidth() / 2 > verticalGuide.getStartX() - 5
                        && newX + getPrefWidth() / 2 < verticalGuide.getStartX() + 5) {
                    verticalGuide.setVisible(true);
                    newX = verticalGuide.getStartX() - getPrefWidth() / 2;
                    textFlow.setTextAlignment(TextAlignment.CENTER);
                    setTextFlowAligned();
                } else verticalGuide.setVisible(false);
            }
            for(Line guide : guides) {
                // to avoid permanent chasing of guide after a field
                if(!guide.getId().contains(getId())) {
                    if(guide.getId().contains("Start")
                            && newX > guide.getStartX() - 5
                            && newX < guide.getStartX() + 5) {
                        guide.setVisible(true);
                        newX = guide.getStartX();
                        textFlow.setTextAlignment(TextAlignment.LEFT);
                        setTextFlowAligned();
                        break;
                    } else guide.setVisible(false);
                    if(guide.getId().contains("End")
                            && newX + getPrefWidth() > guide.getStartX() - 5
                            && newX + getPrefWidth() < guide.getStartX() + 5) {
                        guide.setVisible(true);
                        newX = guide.getStartX() - getPrefWidth();
                        textFlow.setTextAlignment(TextAlignment.RIGHT);
                        setTextFlowAligned();
                        break;
                    } else guide.setVisible(false);
                }
            }

            setLayoutX(newX);
            setLayoutY(newY);
            mouseX = event.getSceneX();
            mouseY = event.getSceneY();
            fieldX = getLayoutX() + getPrefWidth() / 2;
            fieldY = getLayoutY() + getMaxHeight() / 2;
        });
        setOnMouseReleased(event -> {
            guides.forEach(guide -> guide.setVisible(false));
            if (verticalGuide != null) verticalGuide.setVisible(false);
        });
    }

     /**
     * Is used in case when lines are smaller then min width
     */
    public void setTextFlowAligned() {
        if(textFlow.getBoundsInLocal().getWidth() < getMinWidth()) {
            double x;
            switch(textFlow.getTextAlignment().name()) {
                case("RIGHT"):
                    x = getPrefWidth() - textFlow.getBoundsInLocal().getWidth();
                    break;
                case("CENTER"):
                    x = (getPrefWidth() - textFlow.getBoundsInLocal().getWidth()) / 2;
                    break;
                default:
                    x = 0;
            }
            textFlow.setLayoutX(x);
        }
    }


    private boolean notInsideParent(double newX, double newY) {
        if (newX < leftLayoutBorder) {
            setLayoutX(leftLayoutBorder);
            return true;
        } else if (newX + getPrefWidth() > rightLayoutBorder) {
            setLayoutX(rightLayoutBorder - getPrefWidth());
            return true;
        }
        // LayoutY is calculated from bottom and the whole Y axis from top!
        if (newY < topLayoutBorder) {
            setLayoutY(topLayoutBorder);
            return true;
        } else if (newY + getMaxHeight() > bottomLayoutBorder) {
            setLayoutY(bottomLayoutBorder - getMaxHeight()
                    - Toolkit.getToolkit().getFontLoader().getFontMetrics(font).getMaxDescent());
            return true;
        }
        return false;
    }

    void calculateRgbColor() {
        Color color = (Color) lines.get(0).getFill();
        red = (int) (color.getRed() * 255);
        green = (int) (color.getGreen() * 255);
        blue = (int) (color.getBlue() * 255);
    }

    void setSelected(boolean selected) {
        isSelected = selected;
        setEffect(isSelected ? new Glow(1) : null);
    }

    int getNumberOfColumn() {
        return numberOfColumn;
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    double getMouseX() {
        return mouseX;
    }

    double getMouseY() {
        return mouseY;
    }

    public static void setVerticalGuide(Line verticalGuide) {
        Field.verticalGuide = verticalGuide;
    }

    public DoubleProperty fontSizeProperty() {
        return fontSize;
    }

    public double getFontSize() {
        return fontSize.get();
    }

    public void setFontSize(double newFontSize) {
        fontSize.set(newFontSize);
    }

    public void addGuide(Guide guide) {
        guides.add(guide);
    }

    public String getLongestWord() {
        return longestWord;
    }

    public void addResizeableBorder(ResizeableBorder resizeableBorder) {
        resizeableBorders.add(resizeableBorder);
    }

    public List<ResizeableBorder> getResizeableBorders() {
        return resizeableBorders;
    }

    public int getNumberOfLines() {
        return numberOfLines.get();
    }

    public Font getFont() {
        return font;
    }

    public List<Text> getLines() {
        return lines;
    }

    public TextFlow getTextFlow() {
        return textFlow;
    }

    public void setFontPath(String fontPath) {
        this.fontPath = fontPath;
    }

    public String getFontPath() {
        return fontPath;
    }

    public void setFontNameField(TextField fontNameField) {
        this.fontNameField = fontNameField;
    }

    public void setFontSizeField(TextField fontSizeField) {
        this.fontSizeField = fontSizeField;
    }

    public void setFontColorPicker(ColorPicker fontColorPicker) {
        this.fontColorPicker = fontColorPicker;
    }
}
