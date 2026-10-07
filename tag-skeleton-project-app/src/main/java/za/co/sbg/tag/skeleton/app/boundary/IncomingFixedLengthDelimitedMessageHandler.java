package za.co.sbg.tag.skeleton.app.boundary;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import za.co.sbg.tag.platform.messaging.lifecycle.bus.MessageBus;
import za.co.sbg.tag.platform.messaging.lifecycle.processing.Handle;
import za.co.sbg.tag.platform.messaging.lifecycle.processing.MessageHandler;
import za.co.sbg.tag.skeleton.app.controller.SkeletonController;
import za.co.sbg.tag.skeleton.messages.commands.IncomingFixedLengthDelimitedMessageV1;

@ApplicationScoped
@MessageHandler
public class IncomingFixedLengthDelimitedMessageHandler {

    @Inject
    SkeletonController skeletonController;


    @Inject
    MessageBus messageBus;

    @Handle
    public void handle(IncomingFixedLengthDelimitedMessageV1 incoming) {
    var outgoing = skeletonController.processFixedLength(incoming);
        messageBus.publish(outgoing);
    }
}