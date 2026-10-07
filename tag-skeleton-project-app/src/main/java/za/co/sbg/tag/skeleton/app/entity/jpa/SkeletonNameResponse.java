package za.co.sbg.tag.skeleton.app.entity.jpa;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SkeletonNameResponse {

    private String displayName;
    private OffsetDateTime lastUpdatedDate;
    private boolean previouslyUpdated;
}