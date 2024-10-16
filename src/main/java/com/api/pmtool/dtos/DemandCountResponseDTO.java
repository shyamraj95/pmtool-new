package com.api.pmtool.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DemandCountResponseDTO {
    private long totalCount;
    private long notStartedCount;
    private long inProgressCount;
    private long completedCount;
    private long dueOnThisWeekCount;
    private long dueExceededCount;
    private long dueDateChangedCount;

public DemandCountResponseDTO(long totalCount, long notStartedCount, long inProgressCount, long completedCount,
    long dueOnThisWeekCount, long dueExceededCount, long dueDateChangedCount) {
this.totalCount = totalCount;
this.notStartedCount = notStartedCount;
this.inProgressCount = inProgressCount;
this.completedCount = completedCount;
this.dueOnThisWeekCount = dueOnThisWeekCount;
this.dueExceededCount = dueExceededCount;
this.dueDateChangedCount = dueDateChangedCount;
}
}
