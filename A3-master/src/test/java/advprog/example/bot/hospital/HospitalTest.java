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
        String path = "./src/main/java/advprog/example/bot/hospital/hospital-list.json";
        BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
        ObjectMapper objectMapper = new ObjectMapper();
        hospitals = objectMapper.readValue(bufferedReader, Hospital[].class);
    }

    @Test
    void testHospitalFunctionality() {
        Hospital hospital = hospitals[0];
        assertEquals("RSUD Tarakan", hospital.getName());
        assertEquals("Jl. Kyai Caringin No. 7, Cideng, Gambir, Jakarta Pusat, 10150",
                hospital.getAddress());
        assertEquals(-6.17156, hospital.getLatitude());
        assertEquals(106.810219, hospital.getLongitude());
        assertEquals("(021) 3503003", hospital.getPhone());
        assertTrue(hospital.getImageLink().contains("tarakan"));
        assertTrue(hospital.getDescription().contains("Tarakan"));
    }
}