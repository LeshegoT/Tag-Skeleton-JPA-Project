package za.co.sbg.tag.skeleton.app.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.sbg.tag.skeleton.app.entity.jpa.SkeletonNameRepository;
import za.co.sbg.tag.skeleton.messages.commands.IncomingFixedLengthDelimitedMessageV1;
import za.co.sbg.tag.skeleton.messages.commands.IncomingJsonMessageV1;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SkeletonControllerTest {

    @Mock
    SkeletonNameRepository repository;

    @InjectMocks
    SkeletonController controller;

    @Test
    void givenUsaJsonCommand_whenProcessing_thenConvertDatePersistAndMap() {
        var originalDate = ZonedDateTime.parse("2026-09-22T13:50:00+02:00");
        var expectedDate = originalDate.withZoneSameInstant(ZoneId.of("America/New_York"));
        var incoming = IncomingJsonMessageV1.builder()
                .reference("reference")
                .name("Json name")
                .date(originalDate)
                .random(12)
                .build();

        var outgoing = controller.processJson(incoming, "USA");

        assertEquals(expectedDate, incoming.getDate());
        assertEquals(expectedDate, outgoing.getDate());
        assertEquals("Json name", outgoing.getName());
        assertTrue(outgoing.getRandomAlphaNumeric().startsWith("12-"));
        verify(repository).saveOrUpdate("Json name", expectedDate.toOffsetDateTime());
    }

    @Test
    void givenNonUsaJsonCommand_whenProcessing_thenPreserveDate() {
        var date = ZonedDateTime.parse("2026-09-22T13:50:00+02:00");
        var incoming = IncomingJsonMessageV1.builder()
                .name("Json name")
                .date(date)
                .random(0)
                .build();

        var outgoing = controller.processJson(incoming, "ZAF");

        assertEquals(date, outgoing.getDate());
        verify(repository).saveOrUpdate("Json name", date.toOffsetDateTime());
    }

    @Test
    void givenFixedLengthCommand_whenProcessing_thenMapToTextEvent() {
        var incoming = IncomingFixedLengthDelimitedMessageV1.builder()
                .reference("reference")
                .name("Text name")
                .date(ZonedDateTime.parse("2026-09-22T13:50:00+02:00"))
                .random(5)
                .build();

        var outgoing = controller.processFixedLength(incoming);

        assertEquals(incoming.getReference(), outgoing.getReference());
        assertEquals(incoming.getName(), outgoing.getName());
        assertTrue(outgoing.getRandomAlphaNumeric().startsWith("5-"));
    }

    @Test
    void givenInvalidRandom_whenProcessingJson_thenRejectBeforePersistence() {
        var incoming = IncomingJsonMessageV1.builder()
                .name("Invalid")
                .date(ZonedDateTime.parse("2026-09-22T13:50:00+02:00"))
                .random(-1)
                .build();

        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> controller.processJson(incoming, "ZAF")
        );

        assertEquals("Random value must be zero or greater", exception.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    void givenNullRandom_whenProcessingFixedLength_thenReject() {
        var incoming = IncomingFixedLengthDelimitedMessageV1.builder()
                .random(null)
                .build();

        assertThrows(
                IllegalArgumentException.class,
                () -> controller.processFixedLength(incoming)
        );
    }
}
