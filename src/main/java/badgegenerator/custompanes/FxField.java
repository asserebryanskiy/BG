package badgegenerator.custompanes;

import badgegenerator.appfilesmanager.AssessableFonts;
import badgegenerator.appfilesmanager.LoggerManager;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A special class, created to realize text dragging and further convertation to pdf.
 * Could be realized through SingleLineField and FieldWithHyphenation.
 */
public abstract class FxField extends DraggablePane implements StyleableText {
    private static Logger logger = Logger.getLogger(FxField.class.getSimpleName());

    // min distance to grid line for field to be attracted to it
    private static final int GUIDE_OFFSET = 5;
    // min distance to guide for field to be attracted to it
    private static final int GRID_OFFSET = 3;

    private static List<Guide> guides = new ArrayList<>(); // list all guides connected with FxFields
    private static Line verticalGuide;                     // vertical line in the center of the screen
    private static Line horizontalGuide;                   // horizontal line in the center of the screen

    private int numberOfColumn;
    protected double imageToPdfRatio;
    double maxAllowableWidth;
    protected Font font;
    private DoubleProperty fontSize;
    private String fontPath;
    private String alignment = "CENTER";
    private Color color = Color.color(0,0,0);
    TextField fontSizeField;
    TextField fontNameField;
    ColorPicker fontColorPicker;
    List<Button> alignmentButtons;
    CheckBox capsLockCheckBox;
    private boolean capitalized;
    private List<Line> horizontalGridLines = new ArrayList<>();
    private List<Line> verticalGridLines = new ArrayList<>();
    private double hGridLineStep;
    private double vGridLineStep;
    private boolean alignFieldWithGrid;
    private boolean isBold;
    private boolean isItalic;

    public FxField(int numberOfColumn,
                   double imageToPdfRatio,
                   double maxAllowableWidth,
                   String fontPath,
                   double fontSize) {
        super();
        this.numberOfColumn = numberOfColumn;
        this.imageToPdfRatio = imageToPdfRatio;
        this.maxAllowableWidth = maxAllowableWidth;
        if(fontPath != null) {
            try {
                this.font = Font.loadFont(new FileInputStream(fontPath), fontSize);
                this.fontPath = fontPath;
            } catch (FileNotFoundException e) {
                LoggerManager.initializeLogger(logger);
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        "Не удалось загрузить файл шрифта для бейджа.");
                alert.show();
                logger.log(Level.SEVERE, String.format("Ошибка при загрузке шрифта из%s",
                        fontPath), e);
                this.font = Font.loadFont(getClass()
                        .getResourceAsStream("/fonts/Helvetica.otf"), fontSize);
            }
        } else {
            this.font = Font.loadFont(getClass()
                    .getResourceAsStream("/fonts/Helvetica.otf"), fontSize);
        }
        this.fontSize = new SimpleDoubleProperty(fontSize);
        if(fontPath != null) this.fontPath = fontPath;
        setId(String.format("field%d", numberOfColumn));
    }

    public FxField(int id, double maxAllowableWidth) throws FileNotFoundException {
        this(id, 1, maxAllowableWidth, null, 13);
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
            for(Line guide : guides) {
                // to avoid permanent chasing of guide after a field
                if(!guide.getId().contains(getId())) {
                    if(guide.getId().contains("Start")
                            && newX > guide.getStartX() - GUIDE_OFFSET
                            && newX < guide.getStartX() + GUIDE_OFFSET) {
                        guide.setVisible(true);
                        newX = guide.getStartX();
                        setAlignment("LEFT");
                        break;
                    } else guide.setVisible(false);
                    if(guide.getId().contains("End")
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

    abstract void setTextFlowAligned(String alignment);

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

    public void setFont(Font font) {
        this.font = font;
        fontPath = AssessableFonts.getFontPath(font.getName());
        setFont();
    }

    private void setFont() {
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

        if(fontNameField != null) {
            fontNameField.setText(this.font.getName());
        }
    }

    public void setFont(String fontPath)  {
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

    public void setFontSize(double newFontSize) {
        font = new Font(font.getName(), newFontSize);
        if (newFontSize > 200
                || computeStringWidth(getLongestWord()) > maxAllowableWidth) {
            setFontSize(--newFontSize);
            return;
        }
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
    }

    abstract String getLongestWord();

    abstract String getText();

    abstract void setFontSizeImpl(double newFontSize);

    public double computeStringWidth(String str) {
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

    public void setFill(Color color) {
        setFillImpl(color);
        this.color = color;
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
                    ((SVGPath)btn.getGraphic()).setFill(Color.BLACK);
                } else {
                    ((SVGPath)btn.getGraphic()).setFill(Color.GRAY);
                }
            });
        }
        setAlignmentImpl(alignment.toUpperCase());
    }

    abstract void setAlignmentImpl(String alignment);

    public Color getFill() {
        return color;
    }

    public String getAlignment() {
        return alignment;
    }

    public int getNumberOfColumn() {
        return numberOfColumn;
    }

    public String getFontPath() {
        return fontPath;
    }

    public double getImageToPdfRatio() {
        return imageToPdfRatio;
    }

    public void setCapitalized(boolean value) {
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

    public void setAlignmentButtons(List<Button> alignmentButtons) {
        this.alignmentButtons = alignmentButtons;
    }

    abstract void setCapitalizedImpl(boolean value);

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

    public void setBold(boolean value) {
        this.isBold = value;
        setBoldImpl(value);
    }

    public boolean isBold() {
        return isBold;
    }

    abstract void setBoldImpl(boolean value);

    public void setItalic(boolean value) {
        this.isItalic = value;
        setItalicImpl(value);
    }

    public boolean isItalic() {
        return isItalic;
    }

    abstract void setItalicImpl(boolean value);
}
