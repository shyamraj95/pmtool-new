package com.api.pmtool.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TaskStatisticsCountDTO {
    private Long developersWithoutTasks;
    private Long developersWithMultipleTasks;
    private Long tasksPendingOneWeek;
    private Long tasksPendingTwoWeeks;
    private Long tasksPendingThreeOrMoreWeeks;
    private Long overdueTasks;
    private Long developersNotUpdatingProgress;
}
