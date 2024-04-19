package ntu.dp.sau.sk.parsers.services;

import ntu.dp.sau.sk.parsers.model.Item;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;


import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class HotLineParser {

    private static final String PROTOCOL_URL = "https://";
    private static final String HEADER_URL = "hotline.ua";
    private static final String HT_CATALOG_POSTFIX = "/sr/?q=";
    private static final String POSTFIX_PAGE = "&p=";
    private static final int MAX_TIMEOUT = 80000;
    private static final long REQUEST_DELAY_MS = 500; // Задержка в миллисекундах (здесь 1 секунда)

    public List<Item> parseByQuery(String query, int maxParseCount) {
        String validQuery = validateQuery(query);
        List<Item> items = new ArrayList<>();
        Document document;
        String fullUrl = "";
        Elements elements;
        int pageIndex = 1;
        int remainingItems = maxParseCount;

        while (remainingItems > 0) {
            try {
                fullUrl = PROTOCOL_URL + HEADER_URL + HT_CATALOG_POSTFIX + validQuery + POSTFIX_PAGE + pageIndex;
                document = Jsoup.connect(fullUrl).timeout(MAX_TIMEOUT).get();
            } catch (IOException e) {
                System.out.println("problems with connection to " + fullUrl);
                throw new RuntimeException(e);
            }
            elements = document.getElementsByAttributeValue("class", "list-item flex");
            System.out.println(fullUrl);

            if (elements.isEmpty()) {
                System.out.println("Нічого не знайдено за вашим запитом: " + query);
                // Возможно, вам захочется что-то сделать здесь в случае отсутствия результатов
                break;
            }

            int itemsOnPage = elements.size();
            for (int i = 0; i < itemsOnPage; i++) {
                if (remainingItems <= 0) {
                    break; // Достигнуто максимальное количество элементов для парсинга
                }
                Element element = elements.get(i);
                element.getElementsByAttributeValue("class", "item-title text-md link link--black");
                String link = element.getElementsByAttributeValue("class", "item-title text-md link link--black").get(0).attributes().get("href");
                items.add(createItemFromLink(PROTOCOL_URL + HEADER_URL + link, itemsOnPage)); // Передача itemsOnPage в метод createItemFromLink()
                remainingItems--;
                try {
                    TimeUnit.MILLISECONDS.sleep(REQUEST_DELAY_MS);
                } catch (InterruptedException e) {
                    System.out.println("Thread interrupted while waiting between requests.");
                    Thread.currentThread().interrupt();
                }
            }
            pageIndex++;
        }
        return items;
    }

    private String validateQuery(String inputQuery) {

        String validQuery;
        validQuery = inputQuery.trim();
        validQuery = URLEncoder.encode(validQuery, StandardCharsets.UTF_8);
        return validQuery;
    }

    public Item createItemFromLink(String link, int itemsOnPage) {
        System.out.println(link);

        Document document;
        try {
            document = Jsoup.connect(link).timeout(MAX_TIMEOUT).get();
        } catch (IOException e) {
            System.out.println("problems with connetion to " + link);
            throw new RuntimeException(e);
        }



        String id;
        String lowPrice;
        String highPrice;
        String currency = "";
        String scriptText = document.getElementsByAttributeValueContaining("data-hid", "nuxt-jsonld-").last().toString();
        String name = document.getElementsByClass("title__main").last().ownText();
        if (scriptText.contains("\"sku\"")) {
            id = scriptText.split("\"sku\"")[1].split(",")[0].replace("\"", "").replace(":", " ");
            lowPrice = scriptText.split("\"lowPrice\"")[1].split(",")[0].replace("\"", "").replace(":", " ");
            highPrice = scriptText.split("\"highPrice\"")[1].split(",")[0].replace("\"", "").replace(":", " ");
            currency = scriptText.split("\"priceCurrency\"")[1].split(",")[0].replace("\"", "").replace(":", " ");
            System.out.println(lowPrice + currency);
            System.out.println(id);
            System.out.println(name);


        } else {
            System.out.println("Немає на складі");
            id = " null";
            lowPrice = String.valueOf(0.0);
            highPrice = String.valueOf(0.0);
            System.out.println(lowPrice + currency);
            System.out.println(name);
            // Обработка случая, когда информация о товаре отсутствует
        }

        String productInfo = "";
        Elements tables = document.getElementsByTag("table"); // Получаем все таблицы на странице

        List<String> tableHtmlList = new ArrayList<>();

        // Если таблицы найдены
        if (!tables.isEmpty()) {
            int tableNumber = 1;
            for (Element table : tables) {
                // Добавляем уникальный идентификатор к таблице
                String tableId = "table-" + tableNumber;
                table.attr("id", tableId);
                // Добавляем класс к таблице
                table.addClass("new-table");
                Element newTable = table.clone();
                String newTableHtml = newTable.outerHtml();
                tableHtmlList.add(newTableHtml.replace("?", ""));
                tableNumber++;
            }
        }

        try {
            FileWriter writer = new FileWriter("target/classes/templates/results.html"); // Укажите путь к вашему файлу
            // Записываем в файл HTML-код каждой таблицы из списка tableHtmlList
            for (String tableHtml : tableHtmlList) {
                writer.write(tableHtml);
            }
            writer.close();
            System.out.println("HTML файл успешно обновлен.");
        } catch (IOException e) {
            System.out.println("Ошибка при записи HTML файла.");
            e.printStackTrace();
        }
        return new Item(id, name, Float.parseFloat(lowPrice), Float.parseFloat(highPrice), currency, link);
    }
}

