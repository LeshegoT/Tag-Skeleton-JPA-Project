package za.co.sbg.tag.skeleton.app.controller;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import za.co.sbg.tag.skeleton.messages.commands.IncomingFixedLengthDelimitedMessageV1;
import za.co.sbg.tag.skeleton.messages.commands.IncomingJsonMessageV1;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkeletonMapperTest {

    private final SkeletonMapper mapper = Mappers.getMapper(SkeletonMapper.class);

    @Test
    void givenJsonCommand_whenMapping_thenCopyFieldsAndGenerateRandomAlphaNumeric() {
        var date = ZonedDateTime.parse("2026-09-22T13:50:00+02:00");
        var incoming = IncomingJsonMessageV1.builder()
                .reference("json-reference")
                .name("Json name")
                .date(date)
                .random(42)
                .build();

        var outgoing = mapper.toOutgoingXml(incoming);

        assertEquals(incoming.getReference(), outgoing.getReference());
        assertEquals(incoming.getName(), outgoing.getName());
        assertEquals(date, outgoing.getDate());
        assertTrue(outgoing.getRandomAlphaNumeric().matches("42-[0-9a-f]{8}"));
    }

    @Test
    void givenFixedLengthCommand_whenMapping_thenCopyFieldsAndGenerateRandomAlphaNumeric() {
        var date = ZonedDateTime.parse("2026-09-22T13:50:00+02:00");
        var incoming = IncomingFixedLengthDelimitedMessageV1.builder()
                .reference("text-reference")
                .name("Text name")
                .date(date)
                .random(7)
                .build();

        var outgoing = mapper.toOutgoingText(incoming);

        assertEquals(incoming.getReference(), outgoing.getReference());
        assertEquals(incoming.getName(), outgoing.getName());
        assertEquals(date, outgoing.getDate());
        assertTrue(outgoing.getRandomAlphaNumeric().matches("7-[0-9a-f]{8}"));
    }
}
