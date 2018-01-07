package badgegenerator.custompanes;

import badgegenerator.appfilesmanager.AssessableFonts;
import com.itextpdf.kernel.color.DeviceCmyk;
import com.sun.javafx.tk.Toolkit;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A special class, created to realize text dragging and further convertation to pdf.
 * Could be realized through SingleLineField and FieldWithHyphenation.
 */
public abstract class FxField extends DraggablePane implements StyleableText {
    private static Logger logger = Logger.getLogger(FxField.class.getSimpleName());

    static final Color PRESSED_COLOR = Color.color(244.0 / 255, 144.0 / 255, 57.0 / 255);

    // min distance to grid line for field to be attracted to it
    private static final int GUIDE_OFFSET = 5;
    // min distance to guide for field to be attracted to it
    private static final int GRID_OFFSET = 3;

    private static List<Guide> guides = new ArrayList<>(); // list all guides connected with FxFields
    private static Line verticalGuide;                     // vertical line in the center of the screen
    private static Line horizontalGuide;                   // horizontal line in the center of the screen

    private String columnId;                        // name of the column in Excel from which this field was created
    private DoubleProperty fontSize;                // font size of the text of this field
    private String fontPath;                        // path to used font, to load font while pdf creation
    private String alignment = "CENTER";            // alignment of this FxField
    private Color color =
            Color.color(0,0,0);    // color of text
    private boolean capitalized;                    // indicates, whether text is capitalized
    private List<Line> horizontalGridLines =
            new ArrayList<>();                      // list of all horizontal gridLines
    private List<Line> verticalGridLines =
            new ArrayList<>();                      // list of all vertical gridLines
    private double hGridLineStep;                   // distance between two subsequent horizontal gridLines
    private double vGridLineStep;                   // distance between two subsequent vertical gridLines
    private boolean alignFieldWithGrid;             // indicates, whether this field should be aligned
                                                    // to grid while dragging

    // Is added because pdf may have CMYK color, that changes visually while
    // converting to rgb. Thus, original color also preserves.
    private com.itextpdf.kernel.color.Color pdfColor;   // original color extracted from pdf
    private boolean usePdfColor;                        // indicates, whether color extracted from pdf, should be used

    double imageToPdfRatio;         // ratio of pdf size to JavaFX editor pane size
    double maxAllowableWidth;       // width of editor window
    Font font;                      // font of the text of this field
    TextField fontSizeField;        // textField, that shows font size of this field multiplied by imageToPdfRatio
    TextField fontNameField;        // textField, that shows font name of this field
    ColorPicker fontColorPicker;    // colorPicker, that shows current color of this field
    List<Button> alignmentButtons;  // buttons, that shows current alignment of this field
    CheckBox capsLockCheckBox;      // checkBox, that is filled if field is capitalized
    CheckMenuItem usePdfColorMenuItem;

    public FxField(String columnId,
                   double imageToPdfRatio,
                   double maxAllowableWidth)  {
        super();
        this.columnId = columnId;
        this.imageToPdfRatio = imageToPdfRatio;
        this.maxAllowableWidth = maxAllowableWidth;
        this.font = new Font("Circe Light", 13);
        // if Circe Light is not already loaded, load it
        if (font.getName().equals("System Regular")) font = Font.loadFont(getClass()
                .getResourceAsStream("/fonts/CRC35.OTF"), 13);
        this.fontSize = new SimpleDoubleProperty(13);
        setId(String.format("field%d", columnId.hashCode()));
    }

    public FxField(String id, double maxAllowableWidth) {
        this(id, 1, maxAllowableWidth);
    }

    public static List<Guide> getGuides() {
        return guides;
    }

    public static void setVerticalGuide(Line verticalGuide) {
        FxField.verticalGuide = verticalGuide;
    }

    public static void setHorizontalGuide(Line horizontalGuide) {
        FxField.horizontalGuide = horizontalGuide;
    }

    @Override
    double checkIfIntersectVerticalGuides(double newX) {
        if (!verticalGridLines.isEmpty() && alignFieldWithGrid) {
            int lineIndex = (int) Math.round(newX / vGridLineStep);
            if(lineIndex < verticalGridLines.size()) {
                Line line = verticalGridLines.get(lineIndex);
                int endLineIndex = (int) Math.round((newX + getPrefWidth()) / vGridLineStep);
                if(endLineIndex < verticalGridLines.size()) {
                    Line endLine = verticalGridLines.get(endLineIndex);
                    double endX = newX + getPrefWidth();
                    if(endX > endLine.getStartX() - 3
                            && endX < endLine.getStartX() + 3) {
                        newX = endLine.getStartX() - getPrefWidth();
                        endLine.setStrokeWidth(0.5);
                    } else endLine.setStrokeWidth(0.1);
                }
                double lineX = line.getStartX();
                if(newX > lineX - 3 && newX < lineX + 3) {
                    newX = lineX;
                    line.setStrokeWidth(0.5);
                } else line.setStrokeWidth(0.1);
            }
        }
        if (verticalGuide != null) {
            if(newX + getPrefWidth() / 2 > verticalGuide.getStartX() - GUIDE_OFFSET
                    && newX + getPrefWidth() / 2 < verticalGuide.getStartX() + GUIDE_OFFSET) {
                verticalGuide.setVisible(true);
                newX = verticalGuide.getStartX() - getPrefWidth() / 2;
                setAlignment("CENTER");
            } else verticalGuide.setVisible(false);
        }
        if(guides != null) {
            for(Guide guide : guides) {
                // to avoid permanent chasing of guide after a field
                if(guide.getGuideId() != columnId.hashCode()) {
                    if(guide.getPosition().equals(Position.LEFT)
                            && newX > guide.getStartX() - GUIDE_OFFSET
                            && newX < guide.getStartX() + GUIDE_OFFSET) {
                        guide.setVisible(true);
                        newX = guide.getStartX();
                        setAlignment("LEFT");
                        break;
                    } else guide.setVisible(false);
                    if(guide.getPosition().equals(Position.RIGHT)
                            && newX + getPrefWidth() > guide.getStartX() - GUIDE_OFFSET
                            && newX + getPrefWidth() < guide.getStartX() + GUIDE_OFFSET) {
                        guide.setVisible(true);
                        newX = guide.getStartX() - getPrefWidth();
                        setAlignment("RIGHT");
                        break;
                    } else guide.setVisible(false);
                }
            }
        }
        return newX;
    }
    
    @Override
    double checkIfIntersectHorizontalGuides(double newY) {
        if(horizontalGridLines.size() != 0
                && horizontalGridLines.get(0).isVisible()
                && alignFieldWithGrid) {
            int lineIndex = (int) Math.round(newY / hGridLineStep);
            if(lineIndex < horizontalGridLines.size()) {
                Line line = horizontalGridLines.get(lineIndex);
                int endLineIndex = (int) Math.round((newY + getMaxHeight()) / hGridLineStep);
                if(endLineIndex < horizontalGridLines.size()) {
                    Line endLine = horizontalGridLines.get(endLineIndex);
                    double endY = newY + getMaxHeight();
                    double endLineY = endLine.getStartY();
                    if(endY > endLineY - GRID_OFFSET
                            && endY < endLineY + GRID_OFFSET) {
                        newY = endLineY - getMaxHeight();
                        endLine.setStrokeWidth(0.5);
                    } else endLine.setStrokeWidth(0.1);
                }
                double lineY = line.getStartY();
                if(newY > lineY - GRID_OFFSET && newY < lineY + GRID_OFFSET) {
                    newY = lineY;
                    line.setStrokeWidth(0.5);
                } else line.setStrokeWidth(0.1);
            }
        }
        if (horizontalGuide != null) {
            if(newY + getMaxHeight() / 2 > horizontalGuide.getStartY() - 5
                    && newY + getMaxHeight() / 2 < horizontalGuide.getStartY() + 5) {
                horizontalGuide.setVisible(true);
                newY = horizontalGuide.getStartY() - getMaxHeight() / 2;
            } else horizontalGuide.setVisible(false);
        }
        return newY;
    }

    @Override
    void makeGuidesInvisible() {
        if(guides != null) {
            guides.forEach(guide -> guide.setVisible(false));
        }
        if (verticalGuide != null) verticalGuide.setVisible(false);
        if (horizontalGuide != null) horizontalGuide.setVisible(false);
        if (horizontalGridLines != null) horizontalGridLines.forEach(line ->
                line.setStrokeWidth(0.1));
        if (verticalGridLines != null) verticalGridLines.forEach(line ->
                line.setStrokeWidth(0.1));
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) throws IllegalFontSizeException {
        this.font = font;
        fontPath = AssessableFonts.getFontPath(font.getName());
        setFont();
        if (Double.compare(font.getSize(), getFontSize()) != 0) {
            setFontSize(font.getSize());
        }
    }

    private void setFont() throws IllegalFontSizeException {
        double oldWidth = getPrefWidth();
        setFontImpl();
        setMaxHeight(computeMaxHeight());
        switch (alignment) {
            case("RIGHT"): {
                setLayoutX(getLayoutX() + oldWidth - getPrefWidth());
                break;
            }
            case("CENTER"): {
                setLayoutX(getLayoutX() + (oldWidth - getPrefWidth()) / 2);
                break;
            }
        }

        // is needed to catch cases when new font with old fontSize exceeds maxAllowableWidth
        setFontSize(getFontSize());

        if(fontNameField != null) {
            fontNameField.setText(this.font.getName());
        }
    }

    public void setFont(String fontPath) throws IllegalFontSizeException {
        FileInputStream fontInputStream = null;
        try {
            fontInputStream = new FileInputStream(fontPath);
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "Не удалось загрузить файл шрифта", e);
            e.printStackTrace();
        }
        this.fontPath = fontPath;
        font = Font.loadFont(fontInputStream, getFontSize());
        setFont();
    }

    abstract void setFontImpl();

    public double getFontSize() {
        return fontSize.get();
    }

    public double getMaxFontSize() {
        return computeMaxFontSize();
    }

    public void setMaxFontSize() {
        try {
            setFontSize(computeMaxFontSize());
        } catch (IllegalFontSizeException e) {
            e.printStackTrace();
        }
    }

    private double computeMaxFontSize() {
        int minFontSize = 5;
        double maxFontSize = computeStringWidth(getLongestWord()) < maxAllowableWidth ?
                getFontSize() : minFontSize;
        while (computeStringWidth(getLongestWord(), new Font(font.getName(), maxFontSize))
                < maxAllowableWidth) maxFontSize++;
        return --maxFontSize;
    }

    public void setFontSize(double newFontSize) throws IllegalFontSizeException {
        if (computeStringWidth(getLongestWord(), new Font(font.getName(), newFontSize))
                > maxAllowableWidth) {
            throw new IllegalFontSizeException();
        }
        font = new Font(font.getName(), newFontSize);
        double oldWidth = getPrefWidth();
        fontSize.set(newFontSize);
        setFontSizeImpl(newFontSize);
        setMaxHeight(computeMaxHeight());
        switch (alignment) {
            case("RIGHT"): {
                setLayoutX(getLayoutX() + oldWidth - getPrefWidth());
                break;
            }
            case("CENTER"): {
                setLayoutX(getLayoutX() + (oldWidth - getPrefWidth()) / 2);
                break;
            }
        }

        if (fontSizeField != null) {
            fontSizeField.setText(String.valueOf((int) (getFontSize() / imageToPdfRatio)));
        }
    }

    abstract String getLongestWord();

    public abstract String getText();

    abstract void setFontSizeImpl(double newFontSize);

    public double computeStringWidth(String str) {
        return Toolkit.getToolkit().getFontLoader().computeStringWidth(str, font);
    }

    private double computeStringWidth(String str, Font font) {
        return Toolkit.getToolkit().getFontLoader().computeStringWidth(str, font);
    }

    abstract double computeMaxHeight();

    public void setFontSizeField(TextField fontSizeField) {
        this.fontSizeField = fontSizeField;
    }

    public void setFontNameField(TextField fontNameField) {
        this.fontNameField = fontNameField;
    }

    public void setFontColorPicker(ColorPicker fontColorPicker) {
        this.fontColorPicker = fontColorPicker;
    }

    public void setCapsLockCheckBox(CheckBox capsLockCheckBox) {
        this.capsLockCheckBox = capsLockCheckBox;
    }

    public void setUsePdfColorMenuItem(CheckMenuItem usePdfColorMenuItem) {
        this.usePdfColorMenuItem = usePdfColorMenuItem;
    }

    public void setFill(Color color) {
        usePdfColor = false;
        this.color = color;
        setFillImpl(color);
        if (fontColorPicker != null) fontColorPicker.setValue(color);
        if (usePdfColorMenuItem != null) usePdfColorMenuItem.setSelected(false);
    }

    public Color getFill() {
        return color;
    }

    // allow customer indicate BadgeCreator to use color from pdf
    public void setUsePdfColor(boolean usePdfColor) {
        boolean value = pdfColor != null && usePdfColor;
        this.usePdfColor = value;
        if (value) {
            float[] colorValue = pdfColor.getColorValue();
            if (pdfColor instanceof DeviceCmyk) {
                colorValue = com.itextpdf.kernel.color.Color.convertCmykToRgb((DeviceCmyk) pdfColor)
                        .getColorValue();
            }
            Color color = Color.color(colorValue[0], colorValue[1], colorValue[2]);
            setFillImpl(color);
            this.color = color;
            if (fontColorPicker != null) fontColorPicker.setValue(color);
        }
        if (usePdfColorMenuItem != null) usePdfColorMenuItem.setSelected(value);
    }

    // allow customer to set fill, not setting color was change to true
    public void setFill(Color color, boolean colorWasChanged) {
        setFill(color);
        this.usePdfColor = colorWasChanged;
    }

    abstract void setFillImpl(Color color);

    public void addGuide(Guide guide) {
        guides.add(guide);
    }

    public void setAlignment(String alignment) {
        this.alignment = alignment.toUpperCase();
        if(alignmentButtons != null) {
            alignmentButtons.forEach(btn -> {
                if(btn.getId().contains(alignment.toLowerCase())) {
                    ((SVGPath)btn.getGraphic()).setFill(
                            PRESSED_COLOR);
                } else {
                    ((SVGPath)btn.getGraphic()).setFill(Color.WHITE);
                }
            });
        }
        setAlignmentImpl(alignment.toUpperCase());
    }

    abstract void setAlignmentImpl(String alignment);

    public String getAlignment() {
        return alignment;
    }

    public String getColumnId() {
        return columnId;
    }

    public String getFontPath() {
        return fontPath;
    }

    public double getImageToPdfRatio() {
        return imageToPdfRatio;
    }

    public void setCapitalized(boolean value) {
        if (capitalized == value) return;
        double oldWidth = getPrefWidth();
        this.capitalized = value;
        setCapitalizedImpl(value);
        setMaxHeight(computeMaxHeight());
        switch (alignment) {
            case("RIGHT"): {
                setLayoutX(getLayoutX() + oldWidth - getPrefWidth());
                break;
            }
            case("CENTER"): {
                setLayoutX(getLayoutX() + (oldWidth - getPrefWidth()) / 2);
                break;
            }
        }
    }

    abstract void setCapitalizedImpl(boolean value);

    public void setAlignmentButtons(List<Button> alignmentButtons) {
        this.alignmentButtons = alignmentButtons;
    }

    public boolean isCapitalized() {
        return capitalized;
    }

    public void addHorizontalGridLine(Line gridLine) {
        horizontalGridLines.add(gridLine);
    }

    public void addVerticalGridLine(Line gridLine) {
        verticalGridLines.add(gridLine);
    }

    public void setHGridLineStep(double hGridLineStep) {
        this.hGridLineStep = hGridLineStep;
    }

    public void setVGridLineStep(double vGridLineStep) {
        this.vGridLineStep = vGridLineStep;
    }

    public void setAlignFieldWithGrid(boolean alignFieldWithGrid) {
        this.alignFieldWithGrid = alignFieldWithGrid;
    }

    /**
     * Computes string width for every value in a provided list
     * with fxField current font and provided fontSize.
     *
     * Collects and returns values whose values exceeds maxAllowableWidth.
     *
     * @param fontSize - size on which out of range values should be computed
     * @param values - list of strings to be compared
     * @return filtered list of values that are bigger than maxAllowableWidth
     */
    public List<String> getValuesOutOfRange(final double fontSize, List<String> values) {
        return getValuesOutOfRange(fontSize, values, maxAllowableWidth);
    }

    public List<String> getValuesOutOfRange(final double fontSize, List<String> values, double maxWidth) {
        return values.stream()
                .map(s -> {
                    if (capitalized) return s.toUpperCase();
                    else return s;
                })
                .filter(text -> {
                    Font newFont = new Font(this.font.getName(), fontSize);
                    String[] words = text.split("\\s");
                    if (words.length > 1) return Arrays.stream(words)
                        .anyMatch(word -> computeStringWidth(word, newFont) > maxWidth);
                    return computeStringWidth(text, newFont) > maxWidth;
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FxField fxField = (FxField) o;

        if (Double.compare(fxField.imageToPdfRatio, imageToPdfRatio) != 0) return false;
        if (Double.compare(fxField.maxAllowableWidth, maxAllowableWidth) != 0) return false;
        if (Double.compare(fxField.getFontSize(), fontSize.get()) != 0) return false;
        if (capitalized != fxField.capitalized) return false;
        if (!columnId.equals(fxField.columnId)) return false;
        if (!font.equals(fxField.font)) return false;
        if (!alignment.equals(fxField.alignment)) return false;
        return color.equals(fxField.color);
    }

    @Override
    public int hashCode() {
        return columnId.hashCode();
    }

    public void setPdfColor(com.itextpdf.kernel.color.Color pdfColor) {
        this.pdfColor = pdfColor;
        usePdfColor = true;
    }

    public com.itextpdf.kernel.color.Color getPdfColor() {
        return pdfColor;
    }

    public boolean usePdfColor() {
        return usePdfColor;
    }
}
