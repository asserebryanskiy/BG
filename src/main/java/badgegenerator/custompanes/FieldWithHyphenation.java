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
                                String columnId,
                                double imageToPdfRatio,
                                double maxAllowableWidth) {
        super(columnId, imageToPdfRatio, maxAllowableWidth);
        this.originalValue = value;
        words = value.split("\\s");
        computeWordsWidth();
        // is needed because it may be that
        String longestInInput = Arrays.stream(words)
                .max(Comparator.comparingDouble(this::computeStringWidth))
                .get();
        LONGEST_WORD = computeStringWidth(longestInInput) < computeStringWidth(longestWord) ?
                longestWord : longestInInput;
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
                                String id,
                                double maxAllowableWidth) {
        this(value, Arrays.stream(value.split("\\s"))
                        .max(Comparator.comparingInt(String::length))
                        .get(),
                id, 1, maxAllowableWidth);
    }

    private void computeWordsWidth() {
        wordsWidth = Arrays.stream(words)
                .mapToDouble(this::computeStringWidth)
                .toArray();
        spaceWidth = computeStringWidth(" ");
    }

    public void computeHyphenation() {
        longestLineWidth = 0;
        Paint color = lines.get(0).getFill();
        textFlow.getChildren().clear();
        lines.clear();
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            double lineWidth = computeStringWidth(line.toString());
            // add only one first word to to line and start next line
            if (line.length() == 0 && wordsWidth[i] + spaceWidth > getPrefWidth()) {
                Text newLine =
                        new Text(String.format(word + "%n"));
                newLine.setFont(font);
                newLine.setFill(color);
                textFlow.getChildren().add(newLine);
                lines.add(newLine);
                if (longestLineWidth < wordsWidth[i]) longestLineWidth = wordsWidth[i];
            }
            // new word doesn't fir in this line, add it to the next line
            else if (lineWidth + spaceWidth + wordsWidth[i] > getPrefWidth()) {
                Text newLine =
                        new Text(String.format(line.toString() + "%n"));
                newLine.setFont(font);
                newLine.setFill(color);
                textFlow.getChildren().add(newLine);
                lines.add(newLine);
                line.delete(0, line.length());
                line.append(word);
                if (longestLineWidth < lineWidth) longestLineWidth = lineWidth;
            } else {
                if (line.length() > 0) line.append(" ").append(word);
                else                   line.append(word);
            }
        }
        // append last line if necessary
        if(line.length() > 0) {
            String text = line.toString();
            Text newLine = new Text(text);
            newLine.setFont(font);
            newLine.setFill(color);
            textFlow.getChildren().add(newLine);
            lines.add(newLine);
            double lineWidth = computeStringWidth(text);
            if (longestLineWidth < lineWidth) longestLineWidth = lineWidth;
        }

        if (longestLineWidth > getMinWidth()) {
            setPrefWidth(longestLineWidth);
        } else setPrefWidth(getMinWidth());
        numberOfLines.set(lines.size());
        setMaxHeight(computeMaxHeight());
    }

    /**
     * Method computes hyphenation and returns true if new
     * longestLineWidth differs from the old one.
     *
     * @return true if there are possibilities to change longestLineWidth
     */
    boolean couldBeHyphenated() {
        if (getPrefWidth() < getMinWidth()) return false;

        double nllw = 0;    // new longest line width
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            double lineWidth = line.length() == 0 ? 0 : computeStringWidth(line.toString());
            // add only one first word to to line and start next line
            if (line.length() == 0 && wordsWidth[i] + spaceWidth > getPrefWidth()) {
                if (nllw < wordsWidth[i]) nllw = wordsWidth[i];
            }
            // new word doesn't fit in this line, add it to the next line
            else if (lineWidth + spaceWidth + wordsWidth[i] > getPrefWidth()) {
                line.delete(0, line.length());
                line.append(word);
                if (nllw < lineWidth) nllw = lineWidth;
            } else {
                if (line.length() > 0) line.append(" ").append(word);
                else                   line.append(word);
            }
            if (nllw > longestLineWidth) return true;
        }
        // append last line if necessary
        if(line.length() > 0) {
            double lineWidth = computeStringWidth(line.toString());
            if (nllw < lineWidth) nllw = lineWidth;
        }

        return Double.compare(nllw, longestLineWidth) != 0;
    }

    /**
     * Is used in case when lines are smaller then min width
     */
    void setTextFlowAligned() {
        if(longestLineWidth < getMinWidth()) {
            double x;
            switch(getAlignment()) {
                case("RIGHT"):
                    x = getPrefWidth() - longestLineWidth;
                    break;
                case("CENTER"):
                    x = (getMinWidth() - longestLineWidth) / 2;
                    break;
                default:
                    x = 0;
            }
            textFlow.setLayoutX(x);
        } else {
            textFlow.setLayoutX(0);
        }
        textFlow.setTextAlignment(TextAlignment.valueOf(getAlignment()));
    }

    /**
     * Is used only by Resizeable border in case when alignment is Center
     * and it is necessary to move either Left RB (resizeable border) or Right RB.
     *
     * @param newX double value in range from 0 to (getPrefWidth() - getMinWidth())
     */
    void setTextFlowPos(double newX) {
        textFlow.setLayoutX(newX);
    }

    /**
     * Is used only by Resizeable border in case when alignment is Center
     * and it is necessary to move either Left RB (resizeable border) or Right RB.
     *
     * @return current layoutX of TextFlow inside its wrapping pane.
     */
    double getTextFlowPos() {
        return textFlow.getLayoutX();
    }

    @Override
    void setFontImpl() {
        changeFont();
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
        changeFont();
    }

    private void changeFont() {
        setMinWidth(computeStringWidth(LONGEST_WORD));
        lines.forEach(text -> text.setFont(font));
        computeWordsWidth();
        longestLineWidth = lines.stream()
                .mapToDouble(txt -> computeStringWidth(txt.getText()))
                .max()
                .getAsDouble();
        if(longestLineWidth > maxAllowableWidth) {
            setPrefWidth(maxAllowableWidth);
            computeHyphenation();
        } else setPrefWidth(Math.max(longestLineWidth, getMinWidth()));
        setTextFlowAligned();
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
                ((SVGPath)btn.getGraphic()).setFill(PRESSED_COLOR);
                } else {
                    ((SVGPath)btn.getGraphic()).setFill(Color.WHITE);
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
        setTextFlowAligned();
    }

    @Override
    void setCapitalizedImpl(boolean value) {
        words = value ? originalValue.toUpperCase().split("\\s") : originalValue.split("\\s");
        setMinWidth(computeStringWidth(value ? LONGEST_WORD.toUpperCase() : LONGEST_WORD));
        computeWordsWidth();
        computeHyphenation();
        longestLineWidth = lines.stream()
                .mapToDouble(txt -> computeStringWidth(txt.getText()))
                .max()
                .getAsDouble();
        if(longestLineWidth > maxAllowableWidth) {
            setPrefWidth(maxAllowableWidth);
            computeHyphenation();
        }
        else setPrefWidth(longestLineWidth);
        setTextFlowAligned();
    }

    public void addResizeableBorder(ResizeableBorder border) {
        resizeableBorders.add(border);
    }

    public List<ResizeableBorder> getResizeableBorders() {
        return resizeableBorders;
    }

    double getLongestLineWidth() {
        return longestLineWidth;
    }
}
