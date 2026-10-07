package za.co.sbg.tag.skeleton.messages.beanio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.beanio.annotation.Field;
import org.beanio.annotation.Record;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Record
public class IncomingFixedLengthRecord {

    @Field(ordinal = 0, length = 20)
    private String name;

    @Field(ordinal = 1, length = 20)
    private String date;

    @Field(ordinal = 2, length = 6)
    private String random;
}