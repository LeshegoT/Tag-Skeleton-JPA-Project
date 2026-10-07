package za.co.sbg.tag.skeleton.app.boundary;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.sbg.tag.platform.messaging.lifecycle.bus.MessageBus;
import za.co.sbg.tag.skeleton.app.entity.jpa.SkeletonNameEntity;
import za.co.sbg.tag.skeleton.app.entity.jpa.SkeletonNameRepository;
import za.co.sbg.tag.skeleton.app.entity.jpa.SkeletonNameRequest;
import za.co.sbg.tag.skeleton.app.entity.jpa.SkeletonNameResponse;
import za.co.sbg.tag.skeleton.messages.events.OutgoingXmlMessageV1;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SkeletonRestResourceTest {

    @Mock
    SkeletonNameRepository repository;

    @Mock
    MessageBus messageBus;

    @InjectMocks
    SkeletonRestResource resource;

    @Test
    void givenUpdatedFilter_whenGettingNames_thenMapRepositoryEntities() {
        var date = OffsetDateTime.parse("2026-09-22T11:50:00Z");
        when(repository.findByUpdated(true)).thenReturn(List.of(
                new SkeletonNameEntity(1L, "Updated name", date, true)
        ));

        try (var response = resource.getNames(true)) {
            assertEquals(200, response.getStatus());
            var names = (List<?>) response.getEntity();
            assertEquals(1, names.size());
            var name = (SkeletonNameResponse) names.get(0);
            assertEquals("Updated name", name.getDisplayName());
            assertEquals(date, name.getLastUpdatedDate());
            assertTrue(name.isPreviouslyUpdated());
        }
    }

    @Test
    void givenNameAndDate_whenCreating_thenPersistPublishAndReturnCreated() {
        var date = OffsetDateTime.parse("2026-09-22T11:50:00Z");
        var saved = new SkeletonNameEntity(1L, "Created name", date, false);
        when(repository.saveOrUpdate("Created name", date)).thenReturn(saved);

        try (var response = resource.createName("Created name", date.toString())) {
            assertEquals(201, response.getStatus());
            assertSame(saved, response.getEntity());
        }

        verifyPublishedMessage("Created name", date);
    }

    @Test
    void givenMissingName_whenUpdating_thenReturnNotFoundWithoutPublishing() {
        var request = request("Missing", OffsetDateTime.parse("2026-09-22T11:50:00Z"));
        when(repository.findByName("Missing")).thenReturn(null);

        try (var response = resource.updateName(request)) {
            assertEquals(404, response.getStatus());
            assertEquals("Name not found", response.getEntity());
        }

        verify(repository, never()).updateByName(request.getName(), request.getDate());
        verifyNoInteractions(messageBus);
    }

    @Test
    void givenExistingName_whenUpdating_thenUpdatePublishAndReturnOk() {
        var date = OffsetDateTime.parse("2026-09-23T09:00:00Z");
        var request = request("Existing", date);
        when(repository.findByName("Existing"))
                .thenReturn(new SkeletonNameEntity(1L, "Existing", date.minusDays(1), false));

        try (var response = resource.updateName(request)) {
            assertEquals(200, response.getStatus());
        }

        verify(repository).updateByName("Existing", date);
        verifyPublishedMessage("Existing", date);
    }

    @Test
    void givenMissingName_whenDeleting_thenReturnNotFoundWithoutPublishing() {
        when(repository.findByName("Missing")).thenReturn(null);

        try (var response = resource.deleteName("Missing")) {
            assertEquals(404, response.getStatus());
            assertEquals("Name not found", response.getEntity());
        }

        verify(repository, never()).deleteByName("Missing");
        verifyNoInteractions(messageBus);
    }

    @Test
    void givenExistingName_whenDeleting_thenDeletePublishAndReturnNoContent() {
        var date = OffsetDateTime.parse("2026-09-22T11:50:00Z");
        when(repository.findByName("Existing"))
                .thenReturn(new SkeletonNameEntity(1L, "Existing", date, true));

        try (var response = resource.deleteName("Existing")) {
            assertEquals(204, response.getStatus());
        }

        verify(repository).deleteByName("Existing");
        verifyPublishedMessage("Existing", date);
    }

    private SkeletonNameRequest request(String name, OffsetDateTime date) {
        var request = new SkeletonNameRequest();
        request.setName(name);
        request.setDate(date);
        return request;
    }

    private void verifyPublishedMessage(String expectedName, OffsetDateTime expectedDate) {
        var captor = ArgumentCaptor.forClass(OutgoingXmlMessageV1.class);
        verify(messageBus).publish(captor.capture());
        var message = captor.getValue();
        assertEquals(expectedName, message.getName());
        assertEquals(expectedDate.toZonedDateTime(), message.getDate());
        assertEquals(36, message.getReference().length());
        assertTrue(message.getRandomAlphaNumeric().matches("[0-9a-f]{8}"));
    }
}
