package za.co.sbg.tag.skeleton.app.entity.jpa;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class SkeletonNameRequest {
    private String name;
    private OffsetDateTime date;
}