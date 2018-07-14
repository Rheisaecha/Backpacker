package advprog.example.bot.controller;

import advprog.example.bot.hospital.Hospital;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.message.LocationMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.UserSource;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.LocationMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import java.util.logging.Logger;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONObject;

@LineMessageHandler
public class EchoController {
    private static String currentStage = "";
    private String path = "./src/main/java/advprog/example/bot/hospital/hospital-list.json";
    private BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
    private ObjectMapper objectMapper = new ObjectMapper();
    private Hospital[] hospitals = objectMapper.readValue(bufferedReader, Hospital[].class);
    private Hospital[] randomHospital = new Hospital[3];
    private Hospital chosenRandomHospital;

    private static final Logger LOGGER = Logger.getLogger(EchoController.class.getName());

    public EchoController() throws IOException {
    }

    @EventMapping
    public List<Message> handlePostbackEvent(PostbackEvent event) {
        LOGGER.fine(String.format("PostbackEvent(timestamp='%s',source='%s')",
                event.getTimestamp(), event.getSource()));
        int chosenNumber = Integer.parseInt(event.getPostbackContent().getData());
        chosenRandomHospital = randomHospital[chosenNumber];
        return requestLocationMessage();
    }

    @EventMapping
    public List<Message> handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
        LOGGER.fine(String.format("TextMessageContent(timestamp='%s',content='%s')",
                event.getTimestamp(), event.getMessage()));
        TextMessageContent content = event.getMessage();
        String contentText = content.getText();
        if (((contentText.equals("/kemana") && event.getSource() instanceof UserSource)
                || (contentText.contains("darurat") && event.getSource() instanceof GroupSource))
                && currentStage.isEmpty()) {
            currentStage = "nearest_hospital";
            return requestLocationMessage();
        } else if ((contentText.equals("/random_destination")
                && event.getSource() instanceof UserSource)) {
            currentStage = "random_hospital";
            List<Message> messageList = new ArrayList<>();
            for (int i = 0; i < randomHospital.length; i++) {
                Random random = new Random();
                int value = random.nextInt(9);
                randomHospital[i] = hospitals[value];
            }
            CarouselTemplate carouselTemplate = new CarouselTemplate(
                    Arrays.asList(
                            new CarouselColumn(randomHospital[0].getImageLink(),
                                    "Tempat Wisata 1", randomHospital[0].getName(),
                                    Collections.singletonList(new PostbackAction("Pilih", "0"))),
                            new CarouselColumn(randomHospital[1].getImageLink(),
                                    "Tempat Wisata 2", randomHospital[1].getName(),
                                    Collections.singletonList(new PostbackAction("Pilih", "1"))),
                            new CarouselColumn(randomHospital[2].getImageLink(),
                                    "Tempat Wisata 3", randomHospital[2].getName(),
                                    Collections.singletonList(new PostbackAction("Pilih", "2")))
                    )
            );
            TextMessage textMessage =
                    new TextMessage("Silakan pilih salah satu tempat tujuan wisata dengan menekan opsi 'Pilih'");
            TemplateMessage templateMessage =
                    new TemplateMessage("Pilih tujuan", carouselTemplate);
            messageList.add(textMessage);
            messageList.add(templateMessage);
            return messageList;
        } else {
            return Collections.singletonList(new TextMessage("Ketik /kemana untuk memulai"));
        }
    }

    @EventMapping
    public List<Message> handleLocationMessageEvent(MessageEvent<LocationMessageContent> event)
            throws Exception {
        LOGGER.fine(String.format("Event(timestamp='%s',source='%s')",
                event.getTimestamp(), event.getSource()));
        LocationMessageContent locationMessage = event.getMessage();
        double currentLatitude = locationMessage.getLatitude();
        double currentLongitude = locationMessage.getLongitude();
        countDistanceToHospital(currentLatitude, currentLongitude);

        if (currentStage.equals("nearest_hospital")) {
            Arrays.sort(hospitals);
            Hospital nearestHospital = hospitals[0];
            return sendHospitalInfo(nearestHospital);
        } else if (currentStage.equals("random_hospital")) {
            return sendHospitalInfo(chosenRandomHospital);
        } else {
            return Collections.singletonList(new TextMessage("Ketik /kemana untuk memulai"));
        }
    }

    @EventMapping
    public void handleDefaultMessage(Event event) {
        LOGGER.fine(String.format("Event(timestamp='%s',source='%s')",
                event.getTimestamp(), event.getSource()));
    }

    private void countDistanceToHospital(double currentLatitude, double currentLongitude)
            throws IOException {
        for (Hospital hospital : hospitals) {
            double hospitalLatitude = hospital.getLatitude();
            double hospitalLongitude = hospital.getLongitude();

            String apiUrl =
                    "https://maps.googleapis.com/maps/api/distancematrix/json?units=metrics";
            String origin =
                    String.format("&origins=%s,%s", currentLatitude, currentLongitude);
            String destination =
                    String.format("&destinations=%s,%s", hospitalLatitude, hospitalLongitude);
            String apiKey =
                    "&key=AIzaSyCtkDu8O6LnSH7s7SaUnC734Z6uRJwRPMc";
            String url = String.format("%s%s%s%s", apiUrl, origin, destination, apiKey);

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Cache-Control", "no-cache")
                    .build();

            Response response = client.newCall(request).execute();
            assert response.body() != null;
            JSONObject jsonObject = new JSONObject(response.body().string());
            int distanceFromOrigin = (int) jsonObject.getJSONArray("rows")
                    .getJSONObject(0)
                    .getJSONArray("elements")
                    .getJSONObject(0)
                    .getJSONObject("distance")
                    .get("value");

            hospital.setDistance(distanceFromOrigin);
        }
    }

    private List<Message> requestLocationMessage() {
        List<Message> messageList = new ArrayList<>();
        TextMessage textMessage = new TextMessage("Silakan kirim lokasi kamu menginap "
                + "dengan menekan opsi 'Kirim Lokasi'");
        CarouselTemplate carouselTemplate = new CarouselTemplate(
                Arrays.asList(
                        new CarouselColumn("https://www.google.com/maps/about/images/"
                                + "home/home-benefits-1-1.jpg?"
                                + "mmfb=670787c0b70b970d52c3101316182a15",
                                "Kirim Lokasi", "Kirim lokasimu sekarang!",
                                Collections.singletonList(new URIAction("Kirim Lokasi",
                                        "line://nv/location")))
                )
        );
        TemplateMessage templateMessage =
                new TemplateMessage("Kirim lokasi kamu", carouselTemplate);

        messageList.add(textMessage);
        messageList.add(templateMessage);
        return messageList;
    }

    private List<Message> sendHospitalInfo(Hospital hospital) {
        List<Message> messageList = new ArrayList<>();

        ImageMessage hospitalImage = new ImageMessage(hospital.getImageLink(),
                hospital.getImageLink());
        LocationMessage hospitalLocation = new LocationMessage(
                hospital.getName(), hospital.getAddress(),
                hospital.getLatitude(), hospital.getLongitude()
        );
        TextMessage hospitalDetail = new TextMessage(
                String.format("Rekomendasi tempat tujuan wisata untuk Anda adalah %s\n\n"
                                + "Alamat: %s\n\n%s\n\n"
                                + "%s berjarak %s meter dari posisi Anda",
                        hospital.getName(), hospital.getAddress(),
                        hospital.getDescription(), hospital.getName(),
                        hospital.getDistance())
        );
        messageList.add(hospitalImage);
        messageList.add(hospitalLocation);
        messageList.add(hospitalDetail);
        currentStage = "";
        return messageList;
    }
}