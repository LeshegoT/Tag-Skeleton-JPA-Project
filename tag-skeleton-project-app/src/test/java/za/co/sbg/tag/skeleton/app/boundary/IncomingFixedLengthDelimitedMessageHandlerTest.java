package za.co.sbg.tag.skeleton.app.boundary;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.sbg.tag.platform.messaging.lifecycle.bus.MessageBus;
import za.co.sbg.tag.skeleton.app.controller.SkeletonController;
import za.co.sbg.tag.skeleton.messages.commands.IncomingFixedLengthDelimitedMessageV1;
import za.co.sbg.tag.skeleton.messages.events.OutgoingTextMessageV1;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncomingFixedLengthDelimitedMessageHandlerTest {

    @Mock
    SkeletonController controller;

    @Mock
    MessageBus messageBus;

    @InjectMocks
    IncomingFixedLengthDelimitedMessageHandler handler;

    @Test
    void givenFixedLengthCommand_whenHandling_thenProcessAndPublish() {
        var incoming = IncomingFixedLengthDelimitedMessageV1.builder().name("Text name").build();
        var outgoing = OutgoingTextMessageV1.builder().name("Text name").build();
        when(controller.processFixedLength(incoming)).thenReturn(outgoing);

        handler.handle(incoming);

        verify(controller).processFixedLength(incoming);
        verify(messageBus).publish(outgoing);
    }
}
