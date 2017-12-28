package badgegenerator.pdfeditor;

import badgegenerator.custompanes.FxField;
import badgegenerator.fileloader.ExcelReader;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Class collects static methods to create error messages for different cases e.g.:
 *  - font size is to big for current FxField
 *  - font file is not found
 *  - etc.
 */
public class ErrorMessages {
    // helper method to create error message if fontSize is to big
    // adds information about installed fontSize and strings in Excel
    // that exceed width with provided fontSize
    static String tooBigFontSizeInPdf(FxField fxField, double fontSize, ExcelReader excelReader) {
        StringBuilder builder = new StringBuilder();
        double imageToPdfRatio = fxField.getImageToPdfRatio();
        builder.append(String.format("Размер шрифта %.1f, установленный раннее, слишком большой для ",
                fontSize / imageToPdfRatio));
        appendValuesOutOfRange(fxField, fontSize, excelReader, builder);
        builder.append(System.lineSeparator())
                .append("Длина текста будет больше, чем ширина pdf.")
                .append(System.lineSeparator())
                .append("Установлен максимально возможный размер шрифта: ")
                .append(String.format("%.1f", fxField.getMaxFontSize() / imageToPdfRatio));
        return builder.toString();
    }

    private static void appendValuesOutOfRange(FxField fxField,
                                               double fontSize,
                                               ExcelReader excelReader,
                                               StringBuilder builder) {
        List<String> bigStrings = fxField.getValuesOutOfRange(fontSize,
                excelReader.getColumn(fxField.getColumnId()));
        if (bigStrings.size() == 1) {
            builder.append("значения \"")
                    .append(bigStrings.get(0))
                    .append("\".");
        } else if (bigStrings.size() <= 5) {
            builder.append("значений \"");
            builder.append(bigStrings.stream().collect(Collectors.joining(", ")));
            builder.append("\".");
        } else builder.append(String.format("%d строк (из файла Excel, колонки \"%s\").",
                bigStrings.size(), fxField.getColumnId()));
    }

    // helper method to create error message if fontSize is to big
    // adds information about installed fontSize and strings in Excel
    // that exceed width with provided fontSize
    static String tooBigFontSize(FxField fxField, double fontSize,
                                  double imageToPdfRatio, ExcelReader excelReader) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("Размер шрифта %.1f слишком большой для ",
                fontSize / imageToPdfRatio));
        appendValuesOutOfRange(fxField, fontSize, excelReader, builder);
        builder.append(System.lineSeparator())
                .append("Длина текста будет больше, чем ширина pdf.")
                .append(System.lineSeparator())
                .append("Установлен максимально возможный размер шрифта: ")
                .append(String.format("%.1f", fxField.getMaxFontSize() / imageToPdfRatio));
        return builder.toString();
    }

    // Helper method to create error message if new Font can't be used
    // with installed font size because it becomes too large.
    // Adds information about installed fontSize and strings in Excel
    // that exceed width with previous fontSize.
    static String fontStyleError(FxField fxField, double previousFontSize,
                                 String style, double imageToPdfRatio,
                                 ExcelReader excelReader) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("Для стиля %s размер %.1f, установленный ранее, - слишком большой.%n",
                style, previousFontSize / imageToPdfRatio));
        appendValuesOutOfRangeFromNewLine(fxField, previousFontSize, excelReader, builder);
        builder.append(System.lineSeparator())
                .append("Установлен максимально возможный размер шрифта для данного стиля: ")
                .append(String.format("%.1f", fxField.getMaxFontSize() / imageToPdfRatio));
        return builder.toString();
    }

    private static void appendValuesOutOfRangeFromNewLine(FxField fxField, double previousFontSize, ExcelReader excelReader, StringBuilder builder) {
        List<String> bigStrings = fxField.getValuesOutOfRange(previousFontSize,
                excelReader.getColumn(fxField.getColumnId()));
        if (bigStrings.size() == 1) {
            builder.append(bigStrings.get(0))
                    .append(" не поместится в pdf.");
        } else if (bigStrings.size() <= 5) {
            builder.append("Значения \"")
                    .append(bigStrings.stream().collect(Collectors.joining(", ")))
                    .append("\" не поместятся в pdf.");
        } else builder.append(String.format("%d строк (из файла Excel, колонки \"%s\") ",
                bigStrings.size(), fxField.getColumnId()))
                .append("не поместятся в pdf.");
    }

    // Helper method to create error message if new Font can't be used
    // with installed font size because it becomes too large.
    // Adds information about installed fontSize and strings in Excel
    // that exceed width with previous fontSize.
    static String tooBigOldFontSize(FxField fxField, double previousFontSize,
                                    double imageToPdfRatio, ExcelReader excelReader) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("Для шрифта %s размер %.1f, установленный ранее, - слишком большой.%n",
                fxField.getFont().getName(), previousFontSize / imageToPdfRatio));
        appendValuesOutOfRangeFromNewLine(fxField, previousFontSize, excelReader, builder);
        builder.append(System.lineSeparator())
                .append("Установлен максимально возможный размер для данного шрифта: ")
                .append(String.format("%.1f", fxField.getMaxFontSize() / imageToPdfRatio));
        return builder.toString();
    }

    public static String tooBigFontSizeInSave(FxField fxField, double fontSize, ExcelReader excelReader) {
        StringBuilder builder = new StringBuilder();
        double imageToPdfRatio = fxField.getImageToPdfRatio();
        builder.append(String.format("Размер шрифта %.1f в сохранении слишком большой для ",
                fontSize / imageToPdfRatio));
        appendValuesOutOfRange(fxField, fontSize, excelReader, builder);
        builder.append(System.lineSeparator())
                .append("Длина текста будет больше, чем ширина pdf.")
                .append(System.lineSeparator())
                .append("Установлен максимально возможный размер шрифта: ")
                .append(String.format("%.1f", fxField.getMaxFontSize() / imageToPdfRatio));
        return builder.toString();
    }

    // Чтобы обечпечить заданное выравнивание (по левому краю) и одновременно уместтить текст в границах бейджа
    // размер шрифта был уменьшен до 10.9.
    // При заданном выравнивании и размере шрифта значения 1, 2, 3 поля "Фамилия" не помещаются.
    // Поэтому размер шрифта для данного поля был уменьшен до 10.
    public static String xCoordinateErrorMessage(FxField field,
                                                 double originalFontSize,
                                                 ExcelReader excelReader,
                                                 double maxWidth) {
        StringBuilder builder = new StringBuilder();
        double imageToPdfRatio = field.getImageToPdfRatio();
        List<String> bigStrings = field.getValuesOutOfRange(originalFontSize,
                excelReader.getColumn(field.getColumnId()), maxWidth);
        builder.append("При заданном выравнивании и размере шрифта ");
        if (bigStrings.size() == 1) {
            builder.append("значение \"")
                    .append(bigStrings.get(0))
                    .append("\" поля \"" + field.getColumnId() + "\" не помещается.");
        } else if (bigStrings.size() <= 5) {
            builder.append("значения \"")
                    .append(bigStrings.stream().collect(Collectors.joining(", ")))
                    .append("\" поля \"" + field.getColumnId() + "\" не помещаются.");
        } else builder.append(String.format("%d значений (из файла Excel, колонки \"%s\") не помещаются.",
                bigStrings.size(), field.getColumnId()));
        builder.append("Поэтому размер шрифта был изменен до ")
                .append(String.format("%.1f.", field.getFontSize() / imageToPdfRatio));
        return builder.toString();
    }
}
