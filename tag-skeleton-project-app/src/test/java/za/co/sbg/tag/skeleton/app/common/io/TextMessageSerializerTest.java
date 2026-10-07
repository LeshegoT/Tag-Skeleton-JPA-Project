package za.co.sbg.tag.skeleton.app.common.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.sbg.tag.platform.serialization.DeserializationException;
import za.co.sbg.tag.platform.serialization.SerializationException;
import za.co.sbg.tag.skeleton.messages.beanio.IncomingFixedLengthRecord;
import za.co.sbg.tag.skeleton.messages.commands.IncomingFixedLengthDelimitedMessageV1;
import za.co.sbg.tag.skeleton.messages.commands.IncomingJsonMessageV1;
import za.co.sbg.tag.skeleton.messages.events.OutgoingTextMessageV1;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TextMessageSerializerTest {

    @Mock
    private FixedLengthReader fixedLengthReader;

    @InjectMocks
    private TextMessageSerializer serializer;

    @Test
    void givenOutgoingTextMessage_whenSerializing_thenReturnUtf8KeyValuePayload()
            throws SerializationException {
        var message = OutgoingTextMessageV1.builder()
                .name("Text name")
                .date(ZonedDateTime.parse("2026-09-22T13:50:00+02:00"))
                .randomAlphaNumeric("42-ab12cd34")
                .build();

        var result = serializer.serialize(message);

        assertEquals(
                "name=Text name|date=2026-09-22T13:50+02:00|randomAlphaNumeric=42-ab12cd34",
                new String(result, StandardCharsets.UTF_8)
        );
    }

    @Test
    void givenUnsupportedMessage_whenSerializing_thenReject() {
        var unsupported = IncomingJsonMessageV1.builder().build();

        assertThrows(SerializationException.class, () -> serializer.serialize(unsupported));
    }

    @Test
    void givenNullMessage_whenSerializing_thenWrapFailure() {
        assertThrows(SerializationException.class, () -> serializer.serialize(null));
    }

    @Test
    void givenValidFixedLengthPayload_whenDeserializing_thenMapFields()
            throws DeserializationException {
        var payload = String.format("%-20s", "Fixed name")
                + "20260922135000+02:00"
                + "000042";
        when(fixedLengthReader.read(any())).thenReturn(
                IncomingFixedLengthRecord.builder()
                        .name("Fixed name          ")
                        .date("20260922135000+02:00")
                        .random("000042")
                        .build()
        );

        var result = serializer.deserialize(
                payload.getBytes(StandardCharsets.UTF_8),
                IncomingFixedLengthDelimitedMessageV1.class
        );

        assertEquals("Fixed name", result.getName());
        assertEquals(ZonedDateTime.parse("2026-09-22T13:50:00+02:00"), result.getDate());
        assertEquals(42, result.getRandom());
    }

    @Test
    void givenSchemaPath_whenDeserializing_thenUseBeanIoRecord()
            throws DeserializationException {
        var payload = String.format("%-20s", "Fixed name")
                + "20260922135000+02:00"
                + "000042\r\n";
        when(fixedLengthReader.read(any())).thenReturn(
                IncomingFixedLengthRecord.builder()
                        .name("Fixed name          ")
                        .date("20260922135000+02:00")
                        .random("000042")
                        .build()
        );

        var result = serializer.deserialize(
                payload.getBytes(StandardCharsets.UTF_8),
                IncomingFixedLengthDelimitedMessageV1.class,
                "unused-schema"
        );

        assertEquals("Fixed name", result.getName());
    }

    @Test
    void givenBeanIoReaderFailure_whenDeserializing_thenReject() {
        when(fixedLengthReader.read(any()))
                .thenThrow(new IllegalArgumentException("Invalid record length"));

        assertThrows(
                DeserializationException.class,
                () -> serializer.deserialize(
                        "too short".getBytes(StandardCharsets.UTF_8),
                        IncomingFixedLengthDelimitedMessageV1.class
                )
        );
    }

    @Test
    void givenEmptyPayload_whenBeanIoReturnsNoRecord_thenReject() {
        when(fixedLengthReader.read(any())).thenReturn(null);

        assertThrows(
                DeserializationException.class,
                () -> serializer.deserialize(
                        new byte[0],
                        IncomingFixedLengthDelimitedMessageV1.class
                )
        );
    }

    @Test
    void givenBlankName_whenDeserializing_thenReject() {
        var payload = " ".repeat(20) + "20260922135000+02:00" + "000042";
        when(fixedLengthReader.read(any())).thenReturn(
                IncomingFixedLengthRecord.builder()
                        .name(" ".repeat(20))
                        .date("20260922135000+02:00")
                        .random("000042")
                        .build()
        );

        assertThrows(
                DeserializationException.class,
                () -> serializer.deserialize(
                        payload.getBytes(StandardCharsets.UTF_8),
                        IncomingFixedLengthDelimitedMessageV1.class
                )
        );
    }

    @Test
    void givenNullName_whenDeserializing_thenReject() {
        when(fixedLengthReader.read(any())).thenReturn(record(null, "20260922135000+02:00", "000042"));

        assertThrows(
                DeserializationException.class,
                () -> serializer.deserialize(new byte[0], IncomingFixedLengthDelimitedMessageV1.class)
        );
    }

    @Test
    void givenBlankDate_whenDeserializing_thenReject() {
        when(fixedLengthReader.read(any())).thenReturn(record("Fixed name", " ", "000042"));

        assertThrows(
                DeserializationException.class,
                () -> serializer.deserialize(new byte[0], IncomingFixedLengthDelimitedMessageV1.class)
        );
    }

    @Test
    void givenNullDate_whenDeserializing_thenReject() {
        when(fixedLengthReader.read(any())).thenReturn(record("Fixed name", null, "000042"));

        assertThrows(
                DeserializationException.class,
                () -> serializer.deserialize(new byte[0], IncomingFixedLengthDelimitedMessageV1.class)
        );
    }

    @Test
    void givenBlankRandom_whenDeserializing_thenReject() {
        when(fixedLengthReader.read(any())).thenReturn(record("Fixed name", "20260922135000+02:00", " "));

        assertThrows(
                DeserializationException.class,
                () -> serializer.deserialize(new byte[0], IncomingFixedLengthDelimitedMessageV1.class)
        );
    }

    @Test
    void givenNullRandom_whenDeserializing_thenReject() {
        when(fixedLengthReader.read(any())).thenReturn(record("Fixed name", "20260922135000+02:00", null));

        assertThrows(
                DeserializationException.class,
                () -> serializer.deserialize(new byte[0], IncomingFixedLengthDelimitedMessageV1.class)
        );
    }

    @Test
    void givenMalformedDate_whenDeserializing_thenWrapFailure() {
        when(fixedLengthReader.read(any())).thenReturn(record("Fixed name", "not-a-date", "000042"));

        assertThrows(
                DeserializationException.class,
                () -> serializer.deserialize(new byte[0], IncomingFixedLengthDelimitedMessageV1.class)
        );
    }

    @Test
    void givenNonNumericRandom_whenDeserializing_thenWrapFailure() {
        when(fixedLengthReader.read(any())).thenReturn(
                record("Fixed name", "20260922135000+02:00", "ABCDEF")
        );

        assertThrows(
                DeserializationException.class,
                () -> serializer.deserialize(new byte[0], IncomingFixedLengthDelimitedMessageV1.class)
        );
    }

    @Test
    void givenUnsupportedType_whenDeserializing_thenReject() {
        assertThrows(
                DeserializationException.class,
                () -> serializer.deserialize(new byte[0], IncomingJsonMessageV1.class)
        );
    }

    private static IncomingFixedLengthRecord record(
            String name,
            String date,
            String random) {
        return IncomingFixedLengthRecord.builder()
                .name(name)
                .date(date)
                .random(random)
                .build();
    }
}
