package badgegenerator.fileloader;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class ExcelReader {
    private final String srcPath;
    private int numberOfColumns;
    private String[] headings;
    private String[] largestFields;
    private String[] longestWords;
    private String[][] values;

    public ExcelReader(String srcPath) {
        this.srcPath = srcPath;
    }

    public void processFile() throws IOException {
        Workbook excelFile;
        if(srcPath.endsWith(".xlsx")) {
            excelFile = new XSSFWorkbook(new FileInputStream(srcPath));
        } else excelFile = new HSSFWorkbook(new FileInputStream(srcPath));
        Sheet sheet = excelFile.getSheetAt(0);
        if(sheet.getFirstRowNum() > 0) {
            throw new IOException("Список участников должен начинаться с первого ряда");
        }

        int numberOfRows = sheet.getLastRowNum() + 1;
        if(numberOfRows > 2000) {
            throw new IOException("Больше 2 000 рядов в таблице");
        } else if(numberOfRows == 0) {
            throw new IOException("Загруженный файл пустой");
        }

        // retrieve headings
        getHeadingsFromTable(sheet);
        values = new String[numberOfRows][numberOfColumns];
        largestFields = new String[numberOfColumns];
        Arrays.fill(largestFields, "");
        longestWords = new String[numberOfColumns];
        Arrays.fill(longestWords, "");

        // Saves indices of empty rows for further processing.
        // Row could be considered empty (but not null) if it e.g.
        // is filled with color but has no value.
        Set<Integer> emptyRows = new HashSet<>();

        for(int row = 1; row < numberOfRows; row++) {
            Row currentRow = sheet.getRow(row);
            if (currentRow == null) {
                emptyRows.add(row);
                continue;
            }
            int counter = 0;
            for(int column = 0; column < numberOfColumns; column++) {
                // Missing cells will be replaced with ""
                Cell cell = currentRow.getCell(column, Row.CREATE_NULL_AS_BLANK);

                String value;
                try {
                    value = cell.getStringCellValue();   // string cell value
                } catch (IllegalStateException e) {
                    value = String.valueOf((int) cell.getNumericCellValue());
                }
                if (value.length() == 0) counter++;
                values[row][column] = value;
                if(value.length() > largestFields[column].length()) {
                    largestFields[column] = value;
                }
                String[] words = value.split("\\s");
                for (String word : words) {
                    if(word.length() > longestWords[column].length()) {
                        longestWords[column] = word;
                    }
                }
            }
            if (counter == numberOfColumns) emptyRows.add(row);
        }

        // -1 because first row of values are headings
        String[][] copy = new String[numberOfRows - emptyRows.size() - 1][numberOfColumns];

        // get rid of empty rows an excel to exclude further empty pdf creation
        int newRow = 0;
        for (int oldRow = 1; oldRow < values.length; oldRow++) {
            if (emptyRows.contains(oldRow)) continue;
            System.arraycopy(values[oldRow], 0, copy[newRow++], 0, numberOfColumns);
        }
        values = copy;
    }

    private void getHeadingsFromTable(Sheet sheet) throws IOException {
        if (sheet.getRow(0) == null) throw new IOException("Загруженный файл пустой");
        List<String> temp = new ArrayList<>();
        sheet.getRow(0)
                .cellIterator()
                .forEachRemaining(cell -> {
                    try {
                        temp.add(cell.getStringCellValue());
                    } catch (IllegalStateException e) {
                        temp.add(String.valueOf((int) cell.getNumericCellValue()));
                    }
                });
        if(temp.size() > 10) throw new IOException("Больше 10 столбцов в таблице");
        numberOfColumns = temp.size();
        headings = temp.toArray(new String[numberOfColumns]);
    }


    public String[] getLargestFields() {
        return largestFields;
    }

    public String[][] getValues() {
        return values;
    }

    public String[] getHeadings() {
        return headings;
    }

    public String[] getLongestWords() {
        return longestWords;
    }

    /**
     * @param columnId - a heading of the query column
     * @return list of strings in specified column
     */
    public List<String> getColumn(String columnId) {
        int index = 0;
        for (int i = 0; i < headings.length; i++) {
            if (headings[i].equals(columnId)) {
                index = i;
                break;
            }
        }

        List<String> result = new ArrayList<>(values.length);
        for (int i = 0; i < values.length; i++) {
            result.add(values[i][index]);
        }

        return result;
    }
}
