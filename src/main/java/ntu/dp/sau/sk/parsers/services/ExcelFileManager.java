package ntu.dp.sau.sk.parsers.services;
import ntu.dp.sau.sk.parsers.model.Item;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;
@Service
public class ExcelFileManager {

    public static final String PARSING_HEADER = "HotLine";

    public void saveItemsToFile(String fileName, List<Item> listForSave, String name){
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet(PARSING_HEADER + " " + name);
        prepareHeaderOfSheet(name, sheet);

        for(int rowNumber=2;rowNumber < listForSave.size()+2;rowNumber++){
            saveItem(listForSave, rowNumber, sheet);

        }
        try {
            FileOutputStream outputStream = new FileOutputStream(fileName);
            wb.write(outputStream);
            wb.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveItem(List<Item> listForSale, int rowNumber, Sheet sheet) {
        Item item = listForSale.get(rowNumber - 2);
        Row row = sheet.createRow(rowNumber);
        row.createCell(0).setCellValue(item.getId());
        row.createCell(1).setCellValue(item.getName());
        row.createCell(2).setCellValue(item.getLowPrice());
        row.createCell(3).setCellValue(item.getHighPrice());
        row.createCell(4).setCellValue(item.getCurrency());
        row.createCell(5).setCellValue(item.getLink());
    }

    private void prepareHeaderOfSheet(String name, Sheet sheet) {
        Row rowTitle = sheet.createRow(0);
        Row rowItem = sheet.createRow(1);
        rowTitle.createCell(0).setCellValue(name);
        rowItem.createCell(0).setCellValue("ID");
        rowItem.createCell(1).setCellValue("NAME");
        rowItem.createCell(2).setCellValue("LOWPRICE");
        rowItem.createCell(3).setCellValue("HIGHPRICE");
        rowItem.createCell(4).setCellValue("CURRENCY");
        rowItem.createCell(4).setCellValue("LINK");
    }
}

