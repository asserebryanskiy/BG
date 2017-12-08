package badgegenerator.pdfeditor;

import badgegenerator.custompanes.FxField;
import badgegenerator.fileloader.PdfField;
import badgegenerator.fileloader.PdfToFxAdapter;
import com.sun.javafx.tk.Toolkit;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A realization of AbstractFieldsLayouter. Is used to position new FxFields
 */
class NewFieldsLayouter extends AbstractFieldsLayouter {
    private final Map<String, PdfToFxAdapter> adapters;

    NewFieldsLayouter(Pane fieldsParent,
                      Pane verticalScaleBar,
                      Pane horizontalScaleBar,
                      List<Line> gridLines,
                      String[] largestFields,
                      String[] longestWords,
                      String[] headings,
                      double imageToPdfRatio,
                      Map<String, PdfField> pdfFields) {
        super(fieldsParent, verticalScaleBar, horizontalScaleBar, gridLines,
                largestFields, longestWords, headings, imageToPdfRatio);
        adapters = new HashMap<>(pdfFields.size());
        pdfFields.forEach((name, pdfField) ->
                adapters.put(name, new PdfToFxAdapter(pdfField, imageToPdfRatio)));
    }

    @Override
    protected void setFieldFontAndSize(String columnId) {
        PdfToFxAdapter adapter = adapters.get(columnId);
        String fontPath = adapter.getFontPath();
        String fontName = adapter.getFontName();
        if (fontPath == null
                && !fontName.equals("Helvetica")
                && !fontName.equals("Helvetica Regular")) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        "Не удалось найти файл шрифта "
                                + fontName
                                + " для " + columnId
                                + ".\nИспользуется Helvetica.");
                alert.show();
            });
        }
        this.fontPath = fontPath;
        fontSize = adapter.getFontSize();
    }

    @Override
    protected void setFieldsParameters(FxField fxField) {
        PdfToFxAdapter adapter = adapters.get(fxField.getColumnId());
        y         = adapter.getY() - computeAscent(fxField);
        alignment = adapter.getAlignment();
        color     = adapter.getColor();
        switch (alignment) {
            case ("RIGHT"):
                x = adapter.getX()
                        + fxField.computeStringWidth(fxField.getColumnId())
                        - fxField.getPrefWidth();
                break;
            case ("CENTER"):
                x = adapter.getX()
                        + fxField.computeStringWidth(fxField.getColumnId()) / 2
                        - fxField.getPrefWidth() / 2;
                break;
            default: x = adapter.getX();
        }
    }

    private double computeAscent(FxField fxField) {
        return Toolkit.getToolkit().getFontLoader().getFontMetrics(fxField.getFont())
                .getMaxAscent();
    }

}
