package ntu.dp.sau.sk.parsers.controllers;


import ntu.dp.sau.sk.parsers.model.Item;
import ntu.dp.sau.sk.parsers.services.ExcelFileManager;
import ntu.dp.sau.sk.parsers.services.HotLineParser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
public class HotlineController {
    private final HotLineParser hotLineParser;
    private final ExcelFileManager tableResult;

    @Autowired
    public HotlineController(HotLineParser hotLineParser, ExcelFileManager tableResult) {
        this.hotLineParser = hotLineParser;
        this.tableResult = tableResult;
    }

    @GetMapping("/application")
    public String getIndex() {
        return "homePage";
    }

    @PostMapping("/getResult")
    public String handleFormSubmission(@RequestParam("query") String query,
                                       @RequestParam("count") String countStr,
                                       Model model) throws IOException {

        model.addAttribute("processing", true);
        model.addAttribute("query", query);
        model.addAttribute("count", countStr);

        if (query.isEmpty() || countStr.isEmpty()) {
            // Обробляємо ситуацію, коли значення не введено
            return "notResults";
        }

        int count;
        try {
            count = Integer.parseInt(countStr);
            // Ваша логіка обробки, якщо значення 'count' успішно сконвертовано в тип int
        } catch (NumberFormatException e) {
            // Обробляємо випадок, коли 'count' не вдалося сконвертувати в тип int
            return "notResults";
        }

        List<Item> items = hotLineParser.parseByQuery(query, count);
        String fileName = String.format("%s_parse.xlsx", query.replace(" ", "_"));
        tableResult.saveItemsToFile(fileName, items, query);

        model.addAttribute("fileName", fileName);

        if (items.isEmpty()) {
            return "notResults";
        }
        return "haveResults";
    }


    @GetMapping("/download")
    public ResponseEntity<Object> downloadExcelFileParse(@RequestParam("fileName") String fileName,
                                                         Model model) throws IOException {

        model.addAttribute("fileName", fileName);
        // Замініть шлях і ім'я вашого файлу Excel
        String excelFilePath = fileName;

        Path path = Paths.get(excelFilePath);
        ByteArrayResource resource;
        resource = new ByteArrayResource(Files.readAllBytes(path));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=query.xlsx");

        return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

}

