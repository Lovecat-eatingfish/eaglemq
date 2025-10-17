package org.cage.eaglemq.broker.timewheel;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class TimeWheelSlotListModel {

    private List<TimeWheelSlotModel> timeWheelSlotModels = new LinkedList<>();
}
