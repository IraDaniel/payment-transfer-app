package io.daniel.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.Serializable;


public class JsonUtility {

    public static <T extends Serializable> T convertFromJson(String jsonString, Class<T> serializableClass) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        try {
            return mapper.readValue(jsonString, serializableClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String convertToJson(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        String methodInString;
        try {
            methodInString = mapper.writeValueAsString(object);
        } catch (JsonProcessingException je) {
            throw new RuntimeException(je);
        }
        return methodInString;
    }

    private JsonUtility() {
    }
}
