package za.co.sbg.tag.skeleton.app.common.io;

import jakarta.enterprise.context.ApplicationScoped;
import org.beanio.builder.FixedLengthParserBuilder;
import org.beanio.builder.StreamBuilder;
import za.co.sbg.tag.skeleton.messages.beanio.IncomingFixedLengthRecord;

@ApplicationScoped
public class FixedLengthStreamBuilderProvider {

    public static final String FIXED_LENGTH_STREAM =
            "SKELETON_FIXED_LENGTH";

    public StreamBuilder createStreamBuilder() {
        return new StreamBuilder(FIXED_LENGTH_STREAM)
                .format("fixedlength")
                .parser(new FixedLengthParserBuilder())
                .addRecord(IncomingFixedLengthRecord.class);
    }
}