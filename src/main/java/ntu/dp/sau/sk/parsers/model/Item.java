package ntu.dp.sau.sk.parsers.model;


import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class Item {
    String id;
    String name;
    float lowPrice;
    float highPrice;
    String currency;
    String link;

}
