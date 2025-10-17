package org.cage.eaglemq.broker.event.model;

import lombok.Data;
import org.cage.eaglemq.broker.timewheel.TimeWheelSlotModel;
import org.cage.eaglemq.common.event.Event;

import java.util.List;

@Data
public class TimeWheelEvent extends Event {

    private List<TimeWheelSlotModel> timeWheelSlotModelList;
}
