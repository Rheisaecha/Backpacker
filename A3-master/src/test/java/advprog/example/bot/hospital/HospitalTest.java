package advprog.example.bot.hospital;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.FileReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HospitalTest {
    Hospital[] hospitals;

    @BeforeEach
    void setUp() throws Exception {
        String path = "./src/main/java/advprog/example/bot/hospital/destinasi-list.json";
        BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
        ObjectMapper objectMapper = new ObjectMapper();
        hospitals = objectMapper.readValue(bufferedReader, Hospital[].class);
    }

}