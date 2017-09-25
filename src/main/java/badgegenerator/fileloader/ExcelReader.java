package badgegenerator.fileloader;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExcelReader {
    private boolean hasHeadings;
    private int numberOfColumns;
    private List<String> headings;
    private String[] largestFields;
    private String[] longestWords;
    private String[][] values;

    public ExcelReader(String src, boolean hasHeadings) throws IOException{
        this.hasHeadings = hasHeadings;

        XSSFWorkbook excelFile = new XSSFWorkbook(new FileInputStream(src));
        XSSFSheet sheet = excelFile.getSheetAt(0);
        // Missing cells will be replaced with ""
        int numberOfRows = sheet.getPhysicalNumberOfRows();
        //retrieve headings
        if(hasHeadings) getHeadingsFromTable(sheet);
        numberOfColumns = getNumberOfColumns(sheet, numberOfRows);
        values = new String[numberOfRows][numberOfColumns];
        largestFields = new String[numberOfColumns];
        Arrays.fill(largestFields, "");
        longestWords = new String[numberOfColumns];
        Arrays.fill(longestWords, "");
        for(int row = hasHeadings ? 1 : 0; row < numberOfRows; row++) {
            for(int column = 0; column < numberOfColumns; column++) {
                values[row][column] = sheet.getRow(row)
                        .getCell(column, Row.CREATE_NULL_AS_BLANK)
                        .getStringCellValue();
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
        if(hasHeadings) {
            Arrays.fill(values[0], "");
        }

    }

    private void getHeadingsFromTable(XSSFSheet sheet) {
        headings = new ArrayList<>();
        sheet.getRow(0)
                .cellIterator()
                .forEachRemaining(cell -> headings.add(cell.getStringCellValue()));
        numberOfColumns = headings.size();
    }

    private int getNumberOfColumns(XSSFSheet sheet, int numberOfRows) {
        if(hasHeadings) {
            return headings.size();
        } else {
            int maxNumberOfColumns = 0;
            for(int i = 0; i<numberOfRows; i++) {
                XSSFRow row = sheet.getRow(i);
                int counter = row.getPhysicalNumberOfCells();
                if(counter>maxNumberOfColumns) maxNumberOfColumns = counter;
            }
            return maxNumberOfColumns;
        }
    }

    public String[] getLargestFields() {
        return largestFields;
    }

    public String[][] getValues() {
        return values;
    }

    public List<String> getHeadings() {
        return headings;
    }

    public boolean getHasHeadings() {
        return hasHeadings;
    }

    public String[] getLongestWords() {
        return longestWords;
    }
}
