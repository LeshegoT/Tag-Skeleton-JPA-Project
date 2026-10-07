package za.co.sbg.tag.skeleton.app.entity.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SkeletonNameRepositoryTest {

    @Mock
    EntityManager entityManager;

    @Mock
    TypedQuery<SkeletonNameEntity> typedQuery;

    @Mock
    Query updateQuery;

    @InjectMocks
    SkeletonNameRepository repository;

    @Test
    void givenExistingName_whenSaving_thenUpdateExistingEntity() {
        var oldDate = OffsetDateTime.parse("2026-09-22T11:50:00Z");
        var newDate = OffsetDateTime.parse("2026-09-23T09:00:00Z");
        var existing = new SkeletonNameEntity(1L, "Existing", oldDate, false);
        stubFindByNameQuery(Stream.of(existing));

        var result = repository.saveOrUpdate("Existing", newDate);

        assertSame(existing, result);
        assertEquals(newDate, existing.getDate());
        assertTrue(existing.isUpdated());
        verify(entityManager, never()).persist(existing);
    }

    @Test
    void givenNewName_whenSaving_thenPersistNewEntity() {
        var date = OffsetDateTime.parse("2026-09-22T11:50:00Z");
        stubFindByNameQuery(Stream.empty());

        var result = repository.saveOrUpdate("New name", date);

        assertNull(result.getId());
        assertEquals("New name", result.getName());
        assertEquals(date, result.getDate());
        assertFalse(result.isUpdated());
        verify(entityManager).persist(result);
    }

    @Test
    void givenUpdatedFlag_whenFinding_thenUseNamedQueryAndReturnResults() {
        var expected = List.of(new SkeletonNameEntity());
        when(entityManager.createNamedQuery("loadNamesByIsUpdated", SkeletonNameEntity.class))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter("isUpdated", true)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expected);

        var result = repository.findByUpdated(true);

        assertSame(expected, result);
    }

    @Test
    void givenNameAndDate_whenUpdating_thenExecuteNamedUpdate() {
        var date = OffsetDateTime.parse("2026-09-23T09:00:00Z");
        when(entityManager.createNamedQuery("updateNameByName")).thenReturn(updateQuery);
        when(updateQuery.setParameter("name", "Existing")).thenReturn(updateQuery);
        when(updateQuery.setParameter("date", date)).thenReturn(updateQuery);
        when(updateQuery.setParameter("isUpdated", true)).thenReturn(updateQuery);
        when(updateQuery.executeUpdate()).thenReturn(1);

        var result = repository.updateByName("Existing", date);

        assertEquals(1, result);
    }

    @Test
    void givenExistingName_whenFinding_thenReturnEntity() {
        var expected = new SkeletonNameEntity();
        stubFindByNameQuery(Stream.of(expected));

        assertSame(expected, repository.findByName("Existing"));
    }

    @Test
    void givenMissingName_whenFinding_thenReturnNull() {
        stubFindByNameQuery(Stream.empty());

        assertNull(repository.findByName("Missing"));
    }

    @Test
    void givenExistingName_whenDeleting_thenRemoveEntity() {
        var existing = new SkeletonNameEntity();
        stubFindByNameQuery(Stream.of(existing));

        repository.deleteByName("Existing");

        verify(entityManager).remove(existing);
    }

    @Test
    void givenMissingName_whenDeleting_thenDoNotRemove() {
        stubFindByNameQuery(Stream.empty());

        repository.deleteByName("Missing");

        verify(entityManager, never()).remove(org.mockito.ArgumentMatchers.any());
    }

    private void stubFindByNameQuery(Stream<SkeletonNameEntity> results) {
        when(entityManager.createQuery(anyString(), eq(SkeletonNameEntity.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("name"), org.mockito.ArgumentMatchers.anyString())).thenReturn(typedQuery);
        when(typedQuery.getResultStream()).thenReturn(results);
    }
}
