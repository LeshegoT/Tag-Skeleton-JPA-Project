package za.co.sbg.tag.skeleton.messages.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import za.co.sbg.tag.platform.messaging.message.stereotype.Command;

import java.time.ZonedDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class IncomingJsonMessageV1 implements Command {
    private String reference;
    private String name;
    private ZonedDateTime date;
    private Integer random;
}
