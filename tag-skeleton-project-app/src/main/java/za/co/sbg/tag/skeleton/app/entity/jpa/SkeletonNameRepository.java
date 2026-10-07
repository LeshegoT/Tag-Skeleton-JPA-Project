package za.co.sbg.tag.skeleton.app.entity.jpa;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@ApplicationScoped
public class SkeletonNameRepository {

    @PersistenceContext(unitName = "skeletonPU")
    EntityManager entityManager;

    @Transactional
    public SkeletonNameEntity saveOrUpdate(
            String name,
            OffsetDateTime date) {

        var existing =
                entityManager
                        .createQuery(
                                """
                                SELECT s
                                FROM SkeletonNameEntity s
                                WHERE s.name = :name
                                """,
                                SkeletonNameEntity.class
                        )
                        .setParameter("name", name)
                        .getResultStream()
                        .findFirst();

        if (existing.isPresent()) {

            SkeletonNameEntity entity = existing.get();

            entity.setDate(date);
            entity.setUpdated(true);

            return entity;
        }

        SkeletonNameEntity entity =
                new SkeletonNameEntity(
                        null,
                        name,
                        date,
                        false
                );

        entityManager.persist(entity);

        return entity;
    }

    public List<SkeletonNameEntity> findByUpdated(
            boolean isUpdated) {

        return entityManager
                .createNamedQuery(
                        "loadNamesByIsUpdated",
                        SkeletonNameEntity.class
                )
                .setParameter("isUpdated", isUpdated)
                .getResultList();
    }

    @Transactional
    public int updateByName(
            String name,
            OffsetDateTime date) {

        return entityManager
                .createNamedQuery("updateNameByName")
                .setParameter("name", name)
                .setParameter("date", date)
                .setParameter("isUpdated", true)
                .executeUpdate();
    }
    @Transactional
    public SkeletonNameEntity findByName(
            String name) {

        return entityManager
                .createQuery(
                        """
                        SELECT s
                        FROM SkeletonNameEntity s
                        WHERE s.name = :name
                        """,
                        SkeletonNameEntity.class
                )
                .setParameter("name", name)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Transactional
    public void deleteByName(
            String name) {

        SkeletonNameEntity entity =
                findByName(name);

        if (entity != null) {
            entityManager.remove(entity);
        }
    }
}