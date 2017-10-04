package badgegenerator.custompanes;

import com.sun.javafx.tk.Toolkit;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * A special class, created to realize text dragging and further convertation to pdf.
 * Could be realized through SingleLineField and FieldWithHyphenation.
 */
public abstract class FxField extends DraggablePane {
    private static List<Guide> guides = new ArrayList<>();
    private static Line verticalGuide;

    protected int numberOfColumn;
    protected double imageToPdfRatio;
    protected double maxAllowableWidth;
    protected Font font;
    protected DoubleProperty fontSize;
    private String fontPath;
    private String alignment = "CENTER";
    private Color color = Color.color(0,0,0);
    protected TextField fontSizeField;
    protected TextField fontNameField;
    protected ColorPicker fontColorPicker;

    public FxField(int numberOfColumn,
                   double imageToPdfRatio,
                   double maxAllowableWidth,
                   String fontPath,
                   double fontSize) throws FileNotFoundException {
        super();
        this.numberOfColumn = numberOfColumn;
        this.imageToPdfRatio = imageToPdfRatio;
        this.maxAllowableWidth = maxAllowableWidth;
        if(fontPath != null) {
            this.font = Font.loadFont(new FileInputStream(fontPath), fontSize);
            this.fontPath = fontPath;
        } else {
            this.font = Font.loadFont(getClass()
                    .getResourceAsStream("/fonts/Helvetica.otf"), fontSize);
        }
        this.fontSize = new SimpleDoubleProperty(fontSize);
        if(fontPath != null) this.fontPath = fontPath;
        setId(String.format("field%s", numberOfColumn));
    }

    public static List<Guide> getGuides() {
        return guides;
    }

    public static void setVerticalGuide(Line verticalGuide) {
        FxField.verticalGuide = verticalGuide;
    }

    @Override
    double checkIfIntersectGuides(double newX) {
        if (verticalGuide != null) {
            if(newX + getPrefWidth() / 2 > verticalGuide.getStartX() - 5
                    && newX + getPrefWidth() / 2 < verticalGuide.getStartX() + 5) {
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
                            && newX > guide.getStartX() - 5
                            && newX < guide.getStartX() + 5) {
                        guide.setVisible(true);
                        newX = guide.getStartX();
                        setAlignment("LEFT");
                        break;
                    } else guide.setVisible(false);
                    if(guide.getId().contains("End")
                            && newX + getPrefWidth() > guide.getStartX() - 5
                            && newX + getPrefWidth() < guide.getStartX() + 5) {
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

    abstract void setTextFlowAligned(String alignment);

    @Override
    void makeGuidesInvisible() {
        if(guides != null) {
            guides.forEach(guide -> guide.setVisible(false));
        }
        if (verticalGuide != null) verticalGuide.setVisible(false);
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font newFont) {
        double oldWidth = getPrefWidth();
        font = newFont;
        setFontImpl(newFont);
        setMaxHeight(computeMaxHeight());
        setPrefWidth(getBoundsInLocal().getWidth());
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

    abstract void setFontImpl(Font font);

    public double getFontSize() {
        return fontSize.get();
    }

    public void setFontSize(double newFontSize) {
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

    public void setFill(Color color) {
        setFillImpl(color);
        this.color = color;
    }

    abstract void setFillImpl(Color color);

    public void addGuide(Guide guide) {
        guides.add(guide);
    }

    public void setFontPath(String fontPath) {
        this.fontPath = fontPath;
    }

    public void setAlignment(String alignment) {
        this.alignment = alignment;
        setAlignmentImpl(alignment);
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
}
