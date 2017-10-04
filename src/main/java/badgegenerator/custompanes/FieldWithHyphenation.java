package badgegenerator.custompanes;

import com.sun.javafx.tk.Toolkit;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by andreyserebryanskiy on 28/09/2017.
 */
public class FieldWithHyphenation extends FxField {
    private final String[] WORDS;
    private final String LONGEST_WORD;

    private SimpleIntegerProperty numberOfLines;
    private TextFlow textFlow;
    private List<Text> lines;
    private List<ResizeableBorder> resizeableBorders;
    private double longestLineWidth;

    public FieldWithHyphenation(String value,
                                String longestWord,
                                int numberOfColumn,
                                double imageToPdfRatio,
                                double maxAllowableWidth,
                                String fontPath,
                                double fontSize) throws FileNotFoundException {
        super(numberOfColumn, imageToPdfRatio, maxAllowableWidth, fontPath, fontSize);
        WORDS = value.split("\\s");
        LONGEST_WORD = longestWord;
        lines = new ArrayList<>();
        Text text = new Text(value);
        text.setFont(font);
        lines.add(text);
        textFlow = new TextFlow(text);
        getChildren().add(textFlow);
        resizeableBorders = new ArrayList<>(2);
        numberOfLines = new SimpleIntegerProperty(1);
        setMinWidth(computeStringWidth(LONGEST_WORD));
        double currentWidth = computeStringWidth(value);
        if(currentWidth > maxAllowableWidth) {
            setPrefWidth(maxAllowableWidth);
            computeHyphenation();
        } else {
            setPrefWidth(currentWidth);
            longestLineWidth = currentWidth;
            setMaxHeight(computeMaxHeight());
        }
    }

    public void computeHyphenation() {
        longestLineWidth = 0;
        double spaceWidth = computeStringWidth(" ");
        Paint color = lines.get(0).getFill();
        textFlow.getChildren().clear();
        lines.clear();
        StringBuilder line = new StringBuilder();
        for (String word : WORDS) {
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
            lines.add(newLine);
            numberOfLines.set(lines.size());
        }
        if (longestLineWidth > getMinWidth()) {
            setPrefWidth(longestLineWidth);
        } else setPrefWidth(getMinWidth());
        setMaxHeight(computeMaxHeight());
    }

    /**
     * Is used in case when lines are smaller then min width
     */
    @Override
    public void setTextFlowAligned(String alignment) {
        if(longestLineWidth < getMinWidth()) {
            double x;
            switch(alignment) {
                case("RIGHT"):
                    x = getPrefWidth() - longestLineWidth;
                    break;
                case("CENTER"):
                    x = (getPrefWidth() - longestLineWidth) / 2;
                    break;
                default:
                    x = 0;
            }
            textFlow.setLayoutX(x);
            textFlow.setTextAlignment(TextAlignment.valueOf(alignment));
        } else {
            textFlow.setLayoutX(0);
            textFlow.setTextAlignment(TextAlignment.valueOf(alignment));
        }
    }

    @Override
    void setFontImpl(Font font) {
        lines.forEach(text -> text.setFont(font));
    }

    @Override
    void setFontSizeImpl(double newFontSize) {
        if(newFontSize > 200) return;
        font = new Font(font.getName(), newFontSize);
        for (String word : WORDS) {
            if(computeStringWidth(word) > maxAllowableWidth) {
                setFontSizeImpl(--newFontSize);
                return;
            }
        }

        setMinWidth(computeStringWidth(LONGEST_WORD));
        lines.forEach(text -> text.setFont(font));
//        computeHyphenation(); // is called to reduce prefWidth up to longest line
        if(getBoundsInLocal().getWidth() > maxAllowableWidth) {
            setPrefWidth(maxAllowableWidth);
        } else if(getBoundsInLocal().getWidth() < getMinWidth()){
            setPrefWidth(getMinWidth());
        }
        computeHyphenation();
        setTextFlowAligned(getAlignment());

        if (fontSizeField != null) {
            fontSizeField.setText(String.valueOf((int) (getFontSize() / imageToPdfRatio)));
        }
    }

    @Override
    double computeMaxHeight() {
        return Toolkit.getToolkit().getFontLoader().getFontMetrics(font).getLineHeight()
                * getNumberOfLines();
    }

    @Override
    void changeFieldsValues() {
        // displays information about clicked fxField
        if (fontSizeField != null) {
            fontSizeField.setText(String.valueOf((int) (getFontSize() / imageToPdfRatio)));
        }
        if (fontNameField != null) {
            fontNameField.setText(font.getName());
        }
        if (fontColorPicker != null) {
            fontColorPicker.setValue((Color) lines.get(0).getFill());
        }
    }

    public int getNumberOfLines() {
        return numberOfLines.get();
    }

    @Override
    void setFillImpl(Color color) {
        lines.forEach(text -> text.setFill(color));
    }

    @Override
    void setAlignmentImpl(String alignment) {
        setTextFlowAligned(alignment);
    }

    public void addResizeableBorder(ResizeableBorder border) {
        resizeableBorders.add(border);
    }

    public List<ResizeableBorder> getResizeableBorders() {
        return resizeableBorders;
    }
}
