package za.co.sbg.tag.skeleton.app.common.io;

import org.junit.jupiter.api.Test;
import za.co.sbg.tag.skeleton.messages.beanio.IncomingFixedLengthRecord;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StreamFactoryProducerTest {

    private final StreamFactoryProducer producer = new StreamFactoryProducer();
    private final FixedLengthStreamBuilderProvider provider =
            new FixedLengthStreamBuilderProvider();

    @Test
    void givenConfiguredProvider_whenCreatingFactory_thenParseFixedLengthRecord() {
        var factory = producer.streamFactory(provider);
        var payload = String.format("%-20s", "Fixed name")
                + "20260922135000+02:00"
                + "000042";
        var beanReader = factory.createReader(
                FixedLengthStreamBuilderProvider.FIXED_LENGTH_STREAM,
                new StringReader(payload)
        );

        try {
            var record = (IncomingFixedLengthRecord) beanReader.read();

            assertEquals("Fixed name", record.getName().trim());
            assertEquals("20260922135000+02:00", record.getDate());
            assertEquals("000042", record.getRandom());
        } finally {
            beanReader.close();
        }
    }

    @Test
    void givenEmptyContent_whenReadingConfiguredStream_thenReturnNull() {
        var factory = producer.streamFactory(provider);
        var beanReader = factory.createReader(
                FixedLengthStreamBuilderProvider.FIXED_LENGTH_STREAM,
                new StringReader("")
        );

        try {
            assertNull(beanReader.read());
        } finally {
            beanReader.close();
        }
    }
}
