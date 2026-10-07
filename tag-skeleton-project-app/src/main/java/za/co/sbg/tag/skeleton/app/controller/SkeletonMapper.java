package za.co.sbg.tag.skeleton.app.controller;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import za.co.sbg.tag.skeleton.messages.commands.IncomingFixedLengthDelimitedMessageV1;
import za.co.sbg.tag.skeleton.messages.commands.IncomingJsonMessageV1;
import za.co.sbg.tag.skeleton.messages.events.OutgoingTextMessageV1;
import za.co.sbg.tag.skeleton.messages.events.OutgoingXmlMessageV1;

import java.util.UUID;

@Mapper
public interface SkeletonMapper {

    @Mapping(
            target = "randomAlphaNumeric",
            source = "random",
            qualifiedByName = "generateRandomAlphaNumeric"
    )
    OutgoingXmlMessageV1 toOutgoingXml(
            IncomingJsonMessageV1 incoming
    );

    @Mapping(
            target = "randomAlphaNumeric",
            source = "random",
            qualifiedByName = "generateRandomAlphaNumeric"
    )
    OutgoingTextMessageV1 toOutgoingText(
            IncomingFixedLengthDelimitedMessageV1 incoming
    );

    @Named("generateRandomAlphaNumeric")
    default String generateRandomAlphaNumeric(Integer random) {
        return random + "-" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 8);
    }
}