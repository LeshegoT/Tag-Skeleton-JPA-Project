package za.co.sbg.tag.skeleton.app.common.io;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import za.co.sbg.tag.platform.messaging.message.Message;
import za.co.sbg.tag.platform.serialization.ContentType;
import za.co.sbg.tag.platform.serialization.DeserializationException;
import za.co.sbg.tag.platform.serialization.SerializationException;
import za.co.sbg.tag.platform.serialization.Serializer;
import za.co.sbg.tag.skeleton.messages.commands.IncomingFixedLengthDelimitedMessageV1;
import za.co.sbg.tag.skeleton.messages.events.OutgoingTextMessageV1;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
@ContentType("application/text")
public class TextMessageSerializer implements Serializer {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssXXX");

    @Inject
    private FixedLengthReader fixedLengthReader;

    @Override
    public <T extends Message> byte[] serialize(T message)
            throws SerializationException {

        try {

            if (message instanceof OutgoingTextMessageV1 outgoing) {

                String output =
                        "name=" + outgoing.getName()
                                + "|date=" + outgoing.getDate()
                                + "|randomAlphaNumeric="
                                + outgoing.getRandomAlphaNumeric();

                return output.getBytes(StandardCharsets.UTF_8);
            }

            throw new SerializationException(
                    "%s is not a supported class type",
                    message.getClass().getCanonicalName()
            );

        } catch (SerializationException e) {
            throw e;

        } catch (Exception e) {
            throw new SerializationException(
                    "Text serialization failed",
                    e
            );
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Message> T deserialize(
            byte[] messageBody,
            Class<T> messageType)
            throws DeserializationException {

        try {

            if (!messageType.equals(
                    IncomingFixedLengthDelimitedMessageV1.class)) {

                throw new DeserializationException(
                        "%s is not a supported fixed-length message type",
                        messageType.getCanonicalName()
                );
            }

            var record =
                    fixedLengthReader.read(
                            new ByteArrayInputStream(messageBody)
                    );

            if (record == null) {
                throw new DeserializationException(
                        "Fixed-length message was empty"
                );
            }

            String name =
                    record.getName() == null
                            ? ""
                            : record.getName().trim();

            String dateText =
                    record.getDate() == null
                            ? ""
                            : record.getDate().trim();

            String randomText =
                    record.getRandom() == null
                            ? ""
                            : record.getRandom().trim();

            if (name.isBlank()) {
                throw new DeserializationException(
                        "Name cannot be blank"
                );
            }

            if (dateText.isBlank()) {
                throw new DeserializationException(
                        "Date cannot be blank"
                );
            }

            if (randomText.isBlank()) {
                throw new DeserializationException(
                        "Random cannot be blank"
                );
            }

            ZonedDateTime date =
                    ZonedDateTime.parse(
                            dateText,
                            DATE_FORMATTER
                    );

            Integer random =
                    Integer.valueOf(randomText);

            IncomingFixedLengthDelimitedMessageV1 message =
                    IncomingFixedLengthDelimitedMessageV1.builder()
                            .name(name)
                            .date(date)
                            .random(random)
                            .build();

            return (T) message;

        } catch (DeserializationException e) {
            throw e;

        } catch (Exception e) {
            throw new DeserializationException(
                    e,
                    "Failed to deserialize fixed-length message"
            );
        }
    }

    @Override
    public <T extends Message> T deserialize(
            byte[] messageBody,
            Class<T> messageType,
            String schemaPath)
            throws DeserializationException {

        return deserialize(
                messageBody,
                messageType
        );
    }
}