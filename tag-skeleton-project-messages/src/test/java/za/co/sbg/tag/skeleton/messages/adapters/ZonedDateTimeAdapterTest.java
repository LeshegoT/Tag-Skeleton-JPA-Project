package za.co.sbg.tag.skeleton.messages.adapters;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ZonedDateTimeAdapterTest {

    private final ZonedDateTimeAdapter adapter = new ZonedDateTimeAdapter();

    @Test
    void givenZonedDateTime_whenMarshalling_thenReturnIsoText() {
        var date = ZonedDateTime.parse("2026-09-22T13:50:00+02:00[Africa/Johannesburg]");

        assertEquals(date.toString(), adapter.marshal(date));
    }

    @Test
    void givenIsoText_whenUnmarshalling_thenReturnZonedDateTime() {
        var value = "2026-09-22T13:50:00+02:00[Africa/Johannesburg]";

        assertEquals(ZonedDateTime.parse(value), adapter.unmarshal(value));
    }

    @Test
    void givenNullValues_whenAdapting_thenReturnNull() {
        assertNull(adapter.marshal(null));
        assertNull(adapter.unmarshal(null));
    }
}
