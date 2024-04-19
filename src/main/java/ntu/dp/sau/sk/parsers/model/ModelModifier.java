package ntu.dp.sau.sk.parsers.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ModelModifier {
    static public int storage = 1;

    @Autowired
    ModelCreator modelCreator;

    public void updateStorage(int storageValue) {
        storage = storageValue;
    }

    public int getModifiedStorage(){
        return storage;
    }

}
