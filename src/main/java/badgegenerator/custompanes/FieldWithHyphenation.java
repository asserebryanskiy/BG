package badgegenerator.custompanes;

import com.sun.javafx.tk.Toolkit;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An implementation of FxField for multi line text.
 * Resizable borders make it possible to change hyphenation point.
 */
public class FieldWithHyphenation extends FxField {
    private final String originalValue;                 // value with which field was initialized
    private String[] words;                             // array of all words
    private final String LONGEST_WORD;                  // word that determines max width of the field
    private SimpleIntegerProperty numberOfLines;        // number of subsequent horizontal lines of text
    private TextFlow textFlow;                          // base fx structure to hold lines of text
    private List<Text> lines;                           // list of text lines
    private List<ResizeableBorder> resizeableBorders;   // list of right and left resizeable borders
    private double longestLineWidth;                    // width of longest word in current font
    private double[] wordsWidth;                        // array of double values of words width
    private double spaceWidth;                          // width of space character in current font

    public FieldWithHyphenation(String value,
                                String longestWord,
                                int numberOfColumn,
                                double imageToPdfRatio,
                                double maxAllowableWidth,
                                String fontPath,
                                double fontSize) {
        super(numberOfColumn, imageToPdfRatio, maxAllowableWidth, fontPath, fontSize);
        this.originalValue = value;
        words = value.split("\\s");
        computeWordsWidth();
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

    public FieldWithHyphenation(String value,
                                int id,
                                double maxAllowableWidth) {
        this(value, Arrays.stream(value.split("\\s"))
                        .max(Comparator.comparingInt(String::length))
                        .get(),
                id, 1, maxAllowableWidth, null, 13);
    }

    private void computeWordsWidth() {
        wordsWidth = Arrays.stream(words)
                .mapToDouble(this::computeStringWidth)
                .toArray();
        spaceWidth = computeStringWidth(" ");
    }

    void computeHyphenation() {
        longestLineWidth = 0;
        Paint color = lines.get(0).getFill();
        textFlow.getChildren().clear();
        lines.clear();
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            double lineWidth = computeStringWidth(line.toString());
            if (line.length() == 0 && wordsWidth[i] + spaceWidth > getPrefWidth()) {
                Text newLine =
                        new Text(String.format(word + "%n"));
                newLine.setFont(font);
                newLine.setFill(color);
                textFlow.getChildren().add(newLine);
                lines.add(newLine);
            } else if (lineWidth + spaceWidth + wordsWidth[i] > getPrefWidth()) {
                Text newLine =
                        new Text(String.format(line.toString() + "%n"));
                newLine.setFont(font);
                newLine.setFill(color);
                textFlow.getChildren().add(newLine);
                lines.add(newLine);
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
        }
        if (longestLineWidth > getMinWidth()) {
            setPrefWidth(longestLineWidth);
        } else setPrefWidth(getMinWidth());
        numberOfLines.set(lines.size());
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
    void setFontImpl() {
        lines.forEach(text -> text.setFont(font));
        computeWordsWidth();
        longestLineWidth = lines.stream()
                .mapToDouble(line -> computeStringWidth(line.getText()))
                .max()
                .orElse(longestLineWidth);
        setPrefWidth(longestLineWidth);
    }

    @Override
    String getLongestWord() {
        return LONGEST_WORD;
    }

    @Override
    public String getText() {
        return Arrays.stream(words)
                .collect(Collectors.joining(" "));
    }

    @Override
    void setFontSizeImpl(double newFontSize) {
        setMinWidth(computeStringWidth(LONGEST_WORD));
        lines.forEach(text -> text.setFont(font));
        computeWordsWidth();
        longestLineWidth = lines.stream()
                .mapToDouble(txt -> computeStringWidth(txt.getText()))
                .max()
                .getAsDouble();
//        computeHyphenation(); // is called to reduce prefWidth up to longest line
        if(longestLineWidth > maxAllowableWidth) {
            setPrefWidth(maxAllowableWidth);
            computeHyphenation();
        } else setPrefWidth(longestLineWidth);
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
            fontColorPicker.requestFocus();
        }
        if (fontColorPicker != null) {
            fontColorPicker.setValue((Color) lines.get(0).getFill());
        }
        if(alignmentButtons != null) {
            alignmentButtons.forEach(btn -> {
                if(btn.getId().contains(getAlignment().toLowerCase())) {
                    ((SVGPath)btn.getGraphic()).setFill(Color.BLACK);
                } else {
                    ((SVGPath)btn.getGraphic()).setFill(Color.GRAY);
                }
            });
        }
        if(capsLockCheckBox != null) {
            capsLockCheckBox.setSelected(isCapitalized());
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

    @Override
    void setCapitalizedImpl(boolean value) {
        words = value ? originalValue.toUpperCase().split("\\s") : originalValue.split("\\s");
        computeHyphenation();
    }

    @Override
    void setBoldImpl(boolean value) {

    }

    @Override
    void setItalicImpl(boolean value) {

    }

    public void addResizeableBorder(ResizeableBorder border) {
        resizeableBorders.add(border);
    }

    public List<ResizeableBorder> getResizeableBorders() {
        return resizeableBorders;
    }
}
