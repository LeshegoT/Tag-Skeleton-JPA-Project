package za.co.sbg.tag.skeleton.messages.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import za.co.sbg.tag.platform.messaging.message.stereotype.Event;

import java.time.ZonedDateTime;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OutgoingTextMessageV1 implements Event {
    private String reference;
    private String name;
    private ZonedDateTime date;
    private String randomAlphaNumeric;
}
