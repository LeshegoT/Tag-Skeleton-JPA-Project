package za.co.sbg.tag.skeleton.app.entity.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "SKELETON_NAME")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@NamedQueries({
        @NamedQuery(
                name = "loadNamesByIsUpdated",
                query = """
                        SELECT s
                        FROM SkeletonNameEntity s
                        WHERE s.isUpdated = :isUpdated
                        """
        ),
        @NamedQuery(
                name = "updateNameByName",
                query = """
                        UPDATE SkeletonNameEntity s
                        SET s.date = :date,
                            s.isUpdated = :isUpdated
                        WHERE s.name = :name
                        """
        )
})
public class SkeletonNameEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME", nullable = false, unique = true)
    private String name;

    @Column(name = "DATE_VALUE", nullable = false)
    private OffsetDateTime date;

    @Column(name = "IS_UPDATED", nullable = false)
    private boolean isUpdated;

}