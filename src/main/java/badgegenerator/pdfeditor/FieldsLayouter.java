package badgegenerator.pdfeditor;

import badgegenerator.custompanes.*;
import badgegenerator.fileloader.ExcelReader;
import badgegenerator.fileloader.PdfField;
import badgegenerator.fileloader.PdfToFxAdapter;
import badgegenerator.fxfieldsloader.FxFieldsLoader;
import badgegenerator.fxfieldssaver.FxFieldSave;
import com.sun.javafx.tk.Toolkit;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Lays out FxFields, Guides, ResizeableBorders and DistanceViewers on the provided
 * fieldsParent Pane.
 *
 * Firstly it creates FxField either from save or from PdfField (that contains
 * parameters extracted from pdf) or from default template.
 *
 * Secondly it corrects it's coordinates by either changing its hyphenation (if possible)
 * or decreasing fontSize to fit all FxFields in fieldsParent bounds.
 *
 * Method alignFields() is called to correct minor differences in x-coordinates
 * that were produced by several recomputauins of fxFields' prefWidth and layoutX.
 *
 * If something needs to be changed (e.g font size, coordinates, font) and user should
 * be notified about FieldsLayouter sends messages to the provided AlertCenter.
 */
class FieldsLayouter {
    private final String[] largestFields;           // largest values from every column from excel
    private final String[] longestWords;            // longest words from every column from excel
    private final String[] headings;                // headings from every column from excel
    private final List<FxField> fxFields;
    private final double imageToPdfRatio;           // ratio of JavaFx editing area size to pdf size
    private final ExcelReader excelReader;          // excel reader
    private final AlertCenter alertCenter;          // alert center where all notifications will be put
    private final Pane fieldsParent;                // pane where fields should be laid out
    private final Map<String, FxFieldSave> saves;   // map of columnId and corresponding FxFieldSave
    private final Map<String, PdfField> pdfFields;  // map of columnId and corresponding PdfField
    private final List<FxField> notFoundFxFields;   // fxFields, than weren't found neither in saves nor in pdf

    FieldsLayouter(Pane fieldsParent,
                   AlertCenter alertCenter,
                   ExcelReader excelReader,
                   String savesPath,
                   Map<String, PdfField> pdfFields,
                   double imageToPdfRatio) {
        this.fieldsParent    = fieldsParent;
        this.alertCenter     = alertCenter;
        this.excelReader     = excelReader;
        this.largestFields   = excelReader.getLargestFields();
        this.longestWords    = excelReader.getLongestWords();
        this.headings        = excelReader.getHeadings();
        this.pdfFields       = pdfFields;
        this.imageToPdfRatio = imageToPdfRatio;
        this.fxFields        = new ArrayList<>(largestFields.length);
        notFoundFxFields     = new ArrayList<>();

        // load saves if they exist
        if (savesPath != null) saves = FxFieldsLoader.load(savesPath);
        else saves = null;

        positionFields();
    }

    List<FxField> getFxFields() {
        return fxFields;
    }

    /**********************
     PRIVATE HELPER METHODS
     *********************/

    // Positions field inside fieldsParent. Combines strategy of the layout together:
    // 1. Create fields
    // 2. Set its parameters (font, font size, color, position, alignment)
    // 3. add field to the fieldParent pane
    // 4. add Guides and ResizeableBorders to the field
    // 5. add all guides, including VerticalGuide and HorizontalGuide on the screen
    // 6. if some fields were not found neither in pdf nor in saves layout them with default parameters
    // 7. if fields exceed fieldsParent's bounds move them inside decreasing their width or font size
    // 8. if there are minor differences in x-coordinate corrects it.
    private void positionFields() {
        IntStream.range(0, largestFields.length)
                .forEach(i -> {
                    FxField fxField;
                    // create FxField
                    if(largestFields[i].length() > longestWords[i].length()) {
                        fxField = new FieldWithHyphenation(largestFields[i],
                                longestWords[i],
                                headings[i],
                                imageToPdfRatio,
                                fieldsParent.getMaxWidth());
                    } else {
                        fxField = new SingleLineField(largestFields[i],
                                headings[i],
                                imageToPdfRatio,
                                fieldsParent.getMaxWidth());
                    }
                    // set field's fontSize, font, color, alignment, position
                    setFieldsParameters(fxField);
                    // add tp the parent's pane
                    fieldsParent.getChildren().add(fxField);
                    fxFields.add(fxField);
                    // add ResizeableBorders and Guides
                    addSupportivePanes(fxField);
                });
        positionNotFoundFxFields();
        addGuidesToScreen();
        moveFieldsInBounds();
        alignFields();
    }

    // If there exist fxFields whose columnId could not be found neither in saves nor in pdf
    // than default parameters should be set to it.
    // Default parameters could be computed only after all other fxFields (that
    // could be found) are completely processed and thus we can compute lastY, alignment and
    // proper x-coordinate.
    private void positionNotFoundFxFields() {
        if (notFoundFxFields.isEmpty()) return;
        final int V_GAP = 10;  // default vertical distance between two subsequent fxFields.

        // if no fxFields were found in pdf or saves create default fields in the middle of the page
        if (notFoundFxFields.size() == fxFields.size()) {
            double heightOfAllFields = fxFields.stream()    // height of all fields + V_GAP between them
                    .mapToDouble(FxField::getMaxHeight)
                    .sum() + V_GAP * (fxFields.size() - 1);
            // starting y-coordinate
            double y = fieldsParent.getMaxHeight() / 2 - heightOfAllFields / 2;
            for (FxField fxField : fxFields) {
                double x = fieldsParent.getMaxWidth() / 2 - fxField.getPrefWidth() / 2;
                fxField.setLayoutX(x);
                fxField.setLayoutY(y);
                // increment y to make next fxField position lower
                y += fxField.getMaxHeight() + V_GAP;
            }
            return;
        }

        // firstly try to compute default parameters from saves
        List<FxField> foundFxFields = fxFields.stream()
                .filter(f -> {
                    if (saves != null) return saves.containsKey(f.getColumnId());
                    return pdfFields.containsKey(f.getColumnId());
                })
                .collect(Collectors.toList());

        FxField first = foundFxFields.get(0);
        String alignment = first.getAlignment();
        double lastY  = foundFxFields.stream()  // y-coord of the lowest field
                .mapToDouble(f -> f.getLayoutY() + f.getMaxHeight())
                .max().getAsDouble();
        double firstY = foundFxFields.stream()
                .mapToDouble(Node::getLayoutY)
                .min().getAsDouble();
        double startX = 0;                      // x-coord where all fxFields start if they are aligned left
        double endX = 0;                        // x-coord where all fxFields end if they are aligned right
        if      (alignment.equals("LEFT"))  startX = first.getLayoutX();
        else if (alignment.equals("RIGHT")) endX   = first.getLayoutX() + first.getPrefWidth();

        for (FxField field : notFoundFxFields) {
            double x;
            switch (alignment) {
                case "CENTER":
                    x = fieldsParent.getMaxWidth() / 2 - field.getPrefWidth() / 2;
                    break;
                case "RIGHT":
                    x = endX - field.getPrefWidth();
                    break;
                default:
                    x = startX;
            }
            field.setAlignment(alignment);
            field.setLayoutX(x);
            // if new positioned field will be out of the screen position it from top
            if (lastY + V_GAP > fieldsParent.getMaxHeight()) {
                double newY = firstY - V_GAP - field.getMaxHeight();
                if (newY > 0) field.setLayoutY(newY);
                    // if there is no space from top also than put this field on the top of previous one
                else field.setLayoutY(lastY - field.getMaxHeight());
            } else {
                field.setLayoutY(lastY + V_GAP);
                lastY = field.getLayoutY() + field.getMaxHeight();
            }
        }
    }

    // if there are some slight differences in x-coord, corrects it
    private void alignFields() {
        for (int i = 1; i < fxFields.size(); i++) {
            FxField current  = fxFields.get(i);
            FxField previous = fxFields.get(i - 1);
            switch (current.getAlignment()) {
                case "CENTER": {
                    double currentCenterX  = current.getLayoutX() + current.getPrefWidth() / 2;
                    double previousCenterX = previous.getLayoutX() + previous.getPrefWidth() / 2;
                    if (currentCenterX != previousCenterX)
                        current.setLayoutX(previousCenterX - current.getPrefWidth() / 2);
                    break;
                }
                case "RIGHT" : {
                    double currentRightX  = current.getLayoutX() + current.getPrefWidth();
                    double previousRightX = previous.getLayoutX() + previous.getPrefWidth();
                    if (currentRightX != previousRightX)
                        current.setLayoutX(previousRightX - current.getPrefWidth());
                    break;
                }
                default: {
                    if (current.getLayoutX() != previous.getLayoutX()) {
                        System.out.println("Entered");
                        current.setLayoutX(previous.getLayoutX());
                    }
                }
            }
        }
    }

    // Adds all guides from FxFields to the fieldsParent,
    // creates VerticalGuide and HorizontalGuide
    private void addGuidesToScreen() {
        fieldsParent.getChildren().addAll(FxField.getGuides());
        Line verticalGuide = new Line(fieldsParent.getMaxWidth() / 2,
                1,
                fieldsParent.getMaxWidth() / 2,
                fieldsParent.getBoundsInLocal().getHeight() - 1);
        verticalGuide.setVisible(false);
        Line horizontalGuide = new Line(1,
                fieldsParent.getBoundsInLocal().getHeight() / 2,
                fieldsParent.getMaxWidth() - 1,
                fieldsParent.getBoundsInLocal().getHeight() / 2);
        horizontalGuide.setManaged(false);
        horizontalGuide.setVisible(false);
        fieldsParent.getChildren().addAll(verticalGuide, horizontalGuide);
        FxField.setVerticalGuide(verticalGuide);
        FxField.setHorizontalGuide(horizontalGuide);
    }

    // Checks that all fields are inside editing area.
    // If not it is firstly attempted to hyphenate words (if possible)
    // and than fields fontSize is decreased to fit in the bounds.
    // all manipulations are done preserving fxField's x-coordinate and alignment
    private void moveFieldsInBounds() {
        fxFields.forEach(field -> {
            double originalFontSize = field.getFontSize();
            switch (field.getAlignment()) {
                case ("RIGHT"): {
                    if (field.getLayoutX() < 0) {
                        double rightX = field.getLayoutX() + field.getPrefWidth();
                        if (field instanceof FieldWithHyphenation
                                && field.getMinWidth() < rightX) {
                            FieldWithHyphenation fieldWH = (FieldWithHyphenation) field;
                            fieldWH.setPrefWidth(rightX);
                            fieldWH.computeHyphenation();
                            fieldWH.setLayoutX(rightX - fieldWH.getPrefWidth());
                        } else {
                            while (field.getLayoutX() < 0)
                                try {
                                    field.setFontSize(field.getFontSize() - 1);
                                } catch (IllegalFontSizeException e) {
                                    field.setMaxFontSize();
                                }
                            alertCenter.showNotification(ErrorMessages.
                                    xCoordinateErrorMessage(field, originalFontSize,
                                            excelReader, rightX));
                        }
                    }
                    break;
                }case ("CENTER"): {
                    if (field.getLayoutX() < 0) {
                        double centerX = fieldsParent.getMaxHeight() / 2;
                        if (field instanceof FieldWithHyphenation
                                && field.getMinWidth() < fieldsParent.getMaxWidth()) {
                            FieldWithHyphenation fieldWH = (FieldWithHyphenation) field;
                            fieldWH.setPrefWidth(fieldsParent.getMaxWidth());
                            fieldWH.computeHyphenation();
                            fieldWH.setLayoutX(centerX - fieldWH.getPrefWidth() / 2);
                        } else {
                            field.setMaxFontSize();
                            alertCenter.showNotification(ErrorMessages.
                                    xCoordinateErrorMessage(field, originalFontSize,
                                            excelReader, fieldsParent.getMaxWidth()));
                        }
                    }
                    break;
                } default: {
                    if (field.getLayoutX() + field.getPrefWidth() > fieldsParent.getMaxWidth()) {
                        if (field instanceof FieldWithHyphenation
                                && field.getLayoutX() + field.getMinWidth()
                                < fieldsParent.getMaxWidth() - field.getLayoutX()) {
                            FieldWithHyphenation fieldWH = (FieldWithHyphenation) field;
                            fieldWH.setPrefWidth(fieldsParent.getMaxWidth() - field.getLayoutX());
                            fieldWH.computeHyphenation();
                        } else {
                            while (field.getPrefWidth() >= fieldsParent.getMaxWidth() - field.getLayoutX())
                                try {
                                    field.setFontSize(field.getFontSize() - 1);
                                } catch (IllegalFontSizeException e) {
                                    field.setMaxFontSize();
                                }
                            alertCenter.showNotification(ErrorMessages.
                                    xCoordinateErrorMessage(field, originalFontSize, excelReader,
                                            fieldsParent.getMaxWidth() - field.getLayoutX()));
                        }
                    }
                }
            }
        });

        // moves down field if upper one runs into it
        fxFields.sort(Comparator.comparingDouble(Node::getLayoutY));
        int shift = 0;
        final int INTENT = 10;
        double lastY = fxFields.get(0).getLayoutY() + fxFields.get(0).getMaxHeight();
        for (int i = 1; i < fxFields.size(); i++) {
            FxField field = fxFields.get(i);
            if (lastY < field.getLayoutY())
                shift += lastY - field.getLayoutY() + INTENT;
            if (field.getLayoutY() + field.getMaxHeight() + shift < fieldsParent.getMaxHeight()) {
                field.setLayoutY(field.getLayoutY() + shift);
            } else break;
            lastY = field.getLayoutY() + field.getMaxHeight();
        }
    }


    // extracts fields parameters from either pdf or provided save
    // is implemented dependent on a source by NewFieldsLayouter or by SavedFieldsLayouter
    private void setFieldsParameters(FxField fxField) {
        // if saves exist first try to extract parameters from them
        if (saves != null) setParametersFromSave(fxField);
        // if saves do not exist try to extract parameters from pdfField
        else setParametersFromPdf(fxField);
    }

    private void setParametersFromPdf(FxField fxField) {
        PdfField pdfField = pdfFields.get(fxField.getColumnId());
        if (pdfField == null) {
            notFoundFxFields.add(fxField);
            alertCenter.showNotification(String.format("Не удалось найти в pdf поле \"%s\". " +
                            "Для него установлены стандартные параметры: черный цвет, %.1f размер шрифта, " +
                            "шрифт Circe Light.",
                    fxField.getColumnId(),
                    fxField.getFontSize() / fxField.getImageToPdfRatio()));
            return;
        }
        PdfToFxAdapter adapter = new PdfToFxAdapter(pdfField, imageToPdfRatio);
        fxField.setAlignment(adapter.getAlignment());
        String fontPath = adapter.getFontPath();
        String fontName = adapter.getFontName();
        if (fontPath == null && !fontName.equals("Circe Light")) {
            String message = "Не удалось найти файл шрифта "
                    + fontName
                    + " для \"" + fxField.getText()
                    + "\".\nИспользуется Circe Light.";
            alertCenter.showNotification(message);
        }
        if (fontPath != null) try {
            fxField.setFont(fontPath);
        } catch (IllegalFontSizeException e) {
            fxField.setMaxFontSize();
        }
        try {
            fxField.setFontSize(adapter.getFontSize());
        } catch (IllegalFontSizeException e) {
            alertCenter.showNotification(ErrorMessages
                    .tooBigFontSizeInPdf(fxField, adapter.getFontSize(), excelReader));
            fxField.setMaxFontSize();
        }
        fxField.setFill(adapter.getColor());
        fxField.setCapitalized(adapter.isCapitalized());
        double x;
        switch (adapter.getAlignment()) {
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
        fxField.setLayoutX(x);
        double endY = adapter.getY();
        fxField.setLayoutY(endY - computeAscent(fxField));
    }

    // Tries to extract parameters of FxField from save. If fails, set parameters from pdf.
    // If fails loading parameters from pdf, sets default parameters.
    private void setParametersFromSave(FxField fxField) {
        String columnId = fxField.getColumnId();
        FxFieldSave save = saves.get(columnId);
        if (save == null) {
            PdfField pdfField = pdfFields.get(columnId);
            if (pdfField == null) {
                notFoundFxFields.add(fxField);
                alertCenter.showNotification(String.format("Ни в сохранениях ни в pdf-документе " +
                        "не удалось найти параметры для поля \"%s\". Используются стандартные параметры: " +
                        "черный цвет, %.1f размер шрифта, шрифт Circe Light.",
                        columnId, fxField.getFontSize() / imageToPdfRatio));
            }
            else {
                setParametersFromPdf(fxField);
                alertCenter.showNotification("Не удалось найти сохранение для поля \"" +
                    columnId + "\". Используются параметры из pdf-документа.");
            }
            return;
        }

        fxField.setAlignment(save.getAlignment());
        fxField.setCapitalized(save.isCapitalized());
        if (save.getFontPath() != null) try {
            fxField.setFont(save.getFontPath());
        } catch (IllegalFontSizeException e) {
            fxField.setMaxFontSize();
        }
        try {
            fxField.setFontSize(save.getFontSize());
        } catch (IllegalFontSizeException e) {
            alertCenter.showNotification(ErrorMessages
                    .tooBigFontSizeInSave(fxField, save.getFontSize(), excelReader));
        }
        fxField.setFill(Color.color(save.getRed(), save.getGreen(), save.getBlue()));
        double x;
        switch (fxField.getAlignment()) {
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
        fxField.setLayoutX(x);
        fxField.setLayoutY(save.getY());
    }

    // helper method to add ResizeableBorders and Guides
    private void addSupportivePanes(FxField fxField) {
        if(fxField instanceof FieldWithHyphenation) {
            ((FieldWithHyphenation) fxField).addResizeableBorder(
                    new ResizeableBorder(((FieldWithHyphenation) fxField),
                            Position.LEFT));
            ((FieldWithHyphenation) fxField).addResizeableBorder(
                    new ResizeableBorder(((FieldWithHyphenation) fxField),
                            Position.RIGHT));
            fieldsParent.getChildren().addAll(
                    ((FieldWithHyphenation) fxField).getResizeableBorders());
        }
        try {
            fxField.addGuide(new Guide(fxField, Position.LEFT));
            fxField.addGuide(new Guide(fxField, Position.RIGHT));
        } catch (NoParentFoundException | NoIdFoundException e) {
            e.printStackTrace();
        }
    }

    private double computeAscent(FxField fxField) {
        return Toolkit.getToolkit().getFontLoader().getFontMetrics(fxField.getFont())
                .getMaxAscent();
    }
}
