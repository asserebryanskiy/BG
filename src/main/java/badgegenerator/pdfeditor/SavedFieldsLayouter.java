package badgegenerator.pdfeditor;

import badgegenerator.custompanes.FxField;
import badgegenerator.fxfieldsloader.FxFieldsLoader;
import badgegenerator.fxfieldssaver.FxFieldSave;
import javafx.scene.layout.Pane;

import java.util.Comparator;

/**
 * A realization of AbstractFieldsLayouter. Is used to load and position saved FxFields
 */
public class SavedFieldsLayouter extends AbstractFieldsLayouter {
    SavedFieldsLayouter(Pane fieldsParent,
                        Pane verticalScaleBar,
                        Pane horizontalScaleBar,
                        String[] largestFields,
                        String[] longestWords,
                        double imageToPdfRatio,
                        String savePath) {
        super(fieldsParent, verticalScaleBar, horizontalScaleBar,
                largestFields, longestWords, imageToPdfRatio);
        saves = FxFieldsLoader.load(savePath);
        saves.sort(Comparator.comparingInt(FxFieldSave::getNumberOfColumn));
    }

    @Override
    protected void setFieldFontAndSize(int i) {
        fontPath = saves.get(i).getFontPath();
        fontSize = saves.get(i).getFontSize();
    }

    @Override
    protected void setFieldsParameters(FxField fxField) {
        FxFieldSave save = saves.get(fxField.getNumberOfColumn());
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
        red = save.getRed();
        green = save.getGreen();
        blue = save.getBlue();
    }
}
