package badgegenerator.pdfeditor;

import badgegenerator.custompanes.FxField;
import javafx.scene.layout.Pane;

/**
 * A realization of AbstractFieldsLayouter. Is used to position new FxFields
 */
class NewFieldsLayouter extends AbstractFieldsLayouter {
    NewFieldsLayouter(Pane fieldsParent,
                                Pane verticalScaleBar,
                                Pane horizontalScaleBar,
                                String[] largestFields,
                                String[] longestWords,
                                double imageToPdfRatio) {
        super(fieldsParent, verticalScaleBar, horizontalScaleBar,
                largestFields, longestWords, imageToPdfRatio);
    }

    @Override
    protected void setFieldFontAndSize(int i) {
        fontPath = null;
        fontSize = 13;
    }

    @Override
    protected void setFieldsParameters(FxField fxField, int i) {
        x = fieldsParent.getBoundsInLocal().getWidth() / 2
                - fxField.getBoundsInLocal().getWidth() / 2;
        y = fieldsParent.getBoundsInLocal().getHeight() / 2
                - (largestFields.length * 20 / 2) + i * 20;
        alignment = "CENTER";
    }

}