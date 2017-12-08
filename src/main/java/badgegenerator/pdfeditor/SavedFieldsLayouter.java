package badgegenerator.pdfeditor;

import badgegenerator.custompanes.FxField;
import badgegenerator.fxfieldsloader.FxFieldsLoader;
import badgegenerator.fxfieldssaver.FxFieldSave;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.util.List;
import java.util.Map;

/**
 * A realization of AbstractFieldsLayouter. Is used to load and position saved FxFields
 */
public class SavedFieldsLayouter extends AbstractFieldsLayouter {
    private Map<String, FxFieldSave> saves;

    SavedFieldsLayouter(Pane fieldsParent,
                        Pane verticalScaleBar,
                        Pane horizontalScaleBar,
                        List<Line> gridLines,
                        String[] largestFields,
                        String[] longestWords,
                        String[] headings,
                        double imageToPdfRatio,
                        String savePath) {
        super(fieldsParent, verticalScaleBar, horizontalScaleBar, gridLines,
                largestFields, longestWords, headings, imageToPdfRatio);
        saves = FxFieldsLoader.load(savePath);
    }

    @Override
    protected void setFieldFontAndSize(String columnId) {
        fontPath = saves.get(columnId).getFontPath();
        fontSize = saves.get(columnId).getFontSize();
    }

    @Override
    protected void setFieldsParameters(FxField fxField) {
        FxFieldSave save = saves.get(fxField.getColumnId());
        alignment = save.getAlignment();
        fxField.setCapitalized(save.isCapitalized());
        switch (alignment) {
            case("RIGHT"): {
                x = save.getX() + save.getWidth() - fxField.getPrefWidth();
                break;
            }
            case("CENTER"): {
                x = save.getX() - (fxField.getPrefWidth() - save.getWidth()) / 2;
                break;
            }
            default:
                x = save.getX();
        }
        y = save.getY();
        color = Color.color(save.getRed(), save.getGreen(), save.getBlue());
    }
}
