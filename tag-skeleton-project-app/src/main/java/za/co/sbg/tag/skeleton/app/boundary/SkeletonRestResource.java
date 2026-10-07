package za.co.sbg.tag.skeleton.app.boundary;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import za.co.sbg.tag.platform.messaging.lifecycle.bus.MessageBus;
import za.co.sbg.tag.skeleton.app.entity.jpa.SkeletonNameRepository;
import za.co.sbg.tag.skeleton.app.entity.jpa.SkeletonNameRequest;
import za.co.sbg.tag.skeleton.app.entity.jpa.SkeletonNameResponse;
import za.co.sbg.tag.skeleton.messages.events.OutgoingXmlMessageV1;

import java.time.OffsetDateTime;
import java.util.UUID;

@Path("/tag/skeleton")
@Produces(MediaType.APPLICATION_JSON)
//@Consumes(MediaType.APPLICATION_JSON)
public class SkeletonRestResource {

    @Inject
    SkeletonNameRepository repository;

    @Inject
    MessageBus messageBus;

    private OutgoingXmlMessageV1 createOutgoingMessage(
            String name,
            OffsetDateTime date) {

        return OutgoingXmlMessageV1.builder()
                .reference(UUID.randomUUID().toString())
                .name(name)
                .date(date.toZonedDateTime())
                .randomAlphaNumeric(UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 8))
                .build();
    }

    @GET
    @Path("/name")
    @Operation(
            summary = "Get names",
            description = "Returns names filtered by whether they have been updated"
    )
    @APIResponse(
            responseCode = "200",
            description = "Names returned successfully"
    )
    public Response getNames(
            @Parameter(
                    description = "Filter by updated status",
                    example = "true"
            )
            @QueryParam("isUpdated") boolean isUpdated) {

        var names = repository.findByUpdated(isUpdated);

        var response = names.stream()
                .map(entity -> new SkeletonNameResponse(
                        entity.getName(),
                        entity.getDate(),
                        entity.isUpdated()
                ))
                .toList();

        return Response.ok(response).build();
    }
    @PUT
    @Path("/name")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Update name",
            description = "Updates an existing name record and publishes an outgoing message"
    )
    @APIResponse(responseCode = "200", description = "Name updated successfully")
    @APIResponse(responseCode = "404", description = "Name not found")
    public Response updateName(SkeletonNameRequest request) {

        var existing = repository.findByName(request.getName());

        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Name not found")
                    .build();
        }

        repository.updateByName(
                request.getName(),
                request.getDate()
        );

        var outgoing = createOutgoingMessage(
                request.getName(),
                request.getDate()
        );

        messageBus.publish(outgoing);

        return Response.ok().build();
    }

    @POST
    @Path("/name")
    @Operation(
            summary = "Create name",
            description = "Creates or updates a name record and publishes an outgoing message"
    )
    @APIResponse(responseCode = "201", description = "Name created successfully")
    public Response createName(
            @Parameter(example = "SwaggerTest")
            @QueryParam("name") String name,

            @Parameter(example = "2026-09-22T14:00:00+02:00")
            @QueryParam("date") String date) {

        OffsetDateTime parsedDate = OffsetDateTime.parse(date);

        var saved = repository.saveOrUpdate(
                name,
                parsedDate
        );

        var outgoing = createOutgoingMessage(
                name,
                parsedDate
        );

        messageBus.publish(outgoing);

        return Response.status(Response.Status.CREATED)
                .entity(saved)
                .build();
    }
    @DELETE
    @Path("/name")
    @Operation(
            summary = "Delete name",
            description = "Deletes a name record and publishes an outgoing message"
    )
    @APIResponse(responseCode = "204", description = "Name deleted successfully")
    @APIResponse(responseCode = "404", description = "Name not found")
    public Response deleteName(
            @Parameter(example = "SwaggerTest")
            @QueryParam("name") String name) {

        var existing = repository.findByName(name);

        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Name not found")
                    .build();
        }

        repository.deleteByName(name);

        var outgoing = createOutgoingMessage(
                existing.getName(),
                existing.getDate()
        );

        messageBus.publish(outgoing);

        return Response.noContent().build();
    }
}