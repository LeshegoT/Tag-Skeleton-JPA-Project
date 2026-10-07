package za.co.sbg.tag.skeleton.messages.events;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import za.co.sbg.tag.platform.messaging.message.stereotype.Event;
import za.co.sbg.tag.skeleton.messages.adapters.ZonedDateTimeAdapter;

import java.time.ZonedDateTime;

@XmlRootElement(name = "OutgoingXmlMessageV1")
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OutgoingXmlMessageV1 implements Event {

    private String reference;

    private String name;

    @XmlJavaTypeAdapter(ZonedDateTimeAdapter.class)
    private ZonedDateTime date;

    private String randomAlphaNumeric;
}