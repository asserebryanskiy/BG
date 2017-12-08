package badgegenerator.fileloader;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        // Missing cells will be replaced with ""
        int numberOfRows = sheet.getPhysicalNumberOfRows();
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
        for(int row = 1; row < numberOfRows; row++) {
            for(int column = 0; column < numberOfColumns; column++) {
                Cell cell = sheet.getRow(row).getCell(column, Row.CREATE_NULL_AS_BLANK);
                values[row][column] = cell.getStringCellValue();
                if(values[row][column].length() > largestFields[column].length()) {
                    largestFields[column] = values[row][column];
                }
                String[] words = values[row][column].split("\\s");
                for (String word : words) {
                    if(word.length() > longestWords[column].length()) {
                        longestWords[column] = word;
                    }
                }
            }
        }
        Arrays.fill(values[0], "");
    }

    private void getHeadingsFromTable(Sheet sheet) throws IOException {
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
}
