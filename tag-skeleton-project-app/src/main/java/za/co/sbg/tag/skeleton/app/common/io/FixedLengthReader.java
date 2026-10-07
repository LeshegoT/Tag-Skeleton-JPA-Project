package za.co.sbg.tag.skeleton.app.common.io;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.beanio.BeanReader;
import org.beanio.StreamFactory;
import za.co.sbg.tag.skeleton.messages.beanio.IncomingFixedLengthRecord;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class FixedLengthReader {

    @Inject
    private StreamFactory streamFactory;

    public IncomingFixedLengthRecord read(
            InputStream content) {

        BeanReader beanReader =
                streamFactory.createReader(
                        FixedLengthStreamBuilderProvider.FIXED_LENGTH_STREAM,
                        new InputStreamReader(
                                content,
                                StandardCharsets.UTF_8
                        )
                );

        try {
            return (IncomingFixedLengthRecord)
                    beanReader.read();
        } finally {
            beanReader.close();
        }
    }
}