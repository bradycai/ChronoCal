package calendar.model;

import java.util.List;

/**
 * Interface representing a series of recurring calendar events.
 * Provides access to the list of generated events.
 */
public interface IEventSeries {

  /**
   * Returns a list of all events in this recurring series.
   *
   * @return a list of Event objects that are part of the series
   */
  List<Event> getEvents();
}
