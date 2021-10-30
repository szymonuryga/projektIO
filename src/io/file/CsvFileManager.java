package io.file;

import model.DataTable;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class CsvFileManager  {
    private final String fileName;


    public CsvFileManager(String fileName){
        this.fileName = fileName;
    }

    public int[][] importData() {
        int [][] dateFromExcel = new int[0][0];
        try {
            File file = new File(fileName);   //creating a new file instance
            FileInputStream fis = new FileInputStream(file);   //obtaining bytes from the file
            //creating Workbook instance that refers to .xlsx file
            XSSFWorkbook wb = new XSSFWorkbook(fis);
            XSSFSheet sheet = wb.getSheetAt(0);//creating a Sheet object to retrieve object
            int rows = sheet.getPhysicalNumberOfRows();
            int col = sheet.getRow(0).getPhysicalNumberOfCells();
            dateFromExcel = new int[rows][col];
            Iterator<Row> itr = sheet.iterator();    //iterating over excel file
            for (int i=0; itr.hasNext(); i++){
                Row row = itr.next();
                Iterator<Cell> cellIterator = row.cellIterator();   //iterating over each column
                for (int j = 0; cellIterator.hasNext() ; j++) {
                    Cell cell = cellIterator.next();
                    dateFromExcel[i][j] = (int) cell.getNumericCellValue();
                }
            }
            return dateFromExcel;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateFromExcel;
    }
}
