package za.co.sbg.tag.skeleton.messages.adapters;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.time.ZonedDateTime;

public class ZonedDateTimeAdapter extends XmlAdapter<String, ZonedDateTime> {

    @Override
    public ZonedDateTime unmarshal(String value) {
        return value == null ? null : ZonedDateTime.parse(value);
    }

    @Override
    public String marshal(ZonedDateTime value) {
        return value == null ? null : value.toString();
    }
}
