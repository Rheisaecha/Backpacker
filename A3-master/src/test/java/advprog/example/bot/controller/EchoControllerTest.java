package advprog.example.bot.controller;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.LocationMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.UserSource;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@SpringBootTest(properties = "line.bot.handler.enabled=false")
@ExtendWith(SpringExtension.class)
public class EchoControllerTest {

    static {
        System.setProperty("line.bot.channelSecret", "SECRET");
        System.setProperty("line.bot.channelToken", "TOKEN");
    }

    @Autowired
    private EchoController echoController;

    @Test
    void testHandleRequestHospitalByUser() throws Exception {
        TextMessageContent textMessageContent = new TextMessageContent("123", "/hospital");
        MessageEvent<TextMessageContent> event = new MessageEvent<>(
                "123", new UserSource("1234"), textMessageContent, Instant.now()
        );
        echoController.handleTextMessageEvent(event);
        LocationMessageContent locationMessageContent =
                new LocationMessageContent("123",
                        "Faculty of Computer Science, University of Indonesia",
                        "Kampus UI Depok, Pd. Cina, Beji, Kota Depok, Jawa Barat 16424",
                        -6.3646009, 106.8264999);
        MessageEvent<LocationMessageContent> event2 = new MessageEvent<>(
                "123", new UserSource("1234"), locationMessageContent, Instant.now()
        );
        echoController.handleLocationMessageEvent(event2);
    }

    @Test
    void testHandleTextMessageRequestRandomHospitalByUser() {
        TextMessageContent textMessageContent = new TextMessageContent("123", "/random_hospital");
        MessageEvent<TextMessageContent> event = new MessageEvent<>(
                "123", new UserSource("1234"), textMessageContent, Instant.now()
        );
        echoController.handleTextMessageEvent(event);
    }

    @Test
    void testHandleDefaultMessage() {
        Event event = mock(Event.class);

        echoController.handleDefaultMessage(event);

        verify(event, atLeastOnce()).getSource();
        verify(event, atLeastOnce()).getTimestamp();
    }
}