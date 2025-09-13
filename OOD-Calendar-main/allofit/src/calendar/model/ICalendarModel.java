package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Interface for the calendar.
 * For adding, removing.
 */
public interface ICalendarModel {

  /**
   * Adds an event to the calendar.
   *
   * @param event the event
   * @throws IllegalArgumentException if the event is a duplicate
   */
  void addEvent(IEvent event);

  /**
   * Removes an event from the calendar.
   *
   * @param event the event
   * @throws IllegalArgumentException if the event is not found
   */
  void removeEvent(IEvent event);

  /**
   * Finds event through subject and start time.
   *
   * @param subject the subject of the event
   * @param start   the start time
   * @return the matching event
   */
  IEvent findEvent(String subject, LocalDateTime start);

  /**
   * Gets all events on that certain date.
   *
   * @param date the date
   * @return a list of matching events
   */
  List<IEvent> getEventsOnDate(LocalDate date);

  /**
   * Gets all events within the range of dates.
   *
   * @param beginning the start of the range
   * @param ending    the end of the range
   * @return a list of matching events
   */
  List<IEvent> getEventsWithinDates(LocalDateTime beginning, LocalDateTime ending);

  /**
   * Checks if the calendar has an event at that time.
   *
   * @param time the date/time to check
   * @return true if an event overlaps with this time, false otherwise
   */
  boolean isBusy(LocalDateTime time);

  /**
   * Checks whether an event would have the same subject,
   * start, and end time, with existing events.
   *
   * @param e the Event to test
   * @return true if a conflict exists, false otherwise
   */
  boolean hasConflict(IEvent e);

  /**
   * Returns a list of up to 10 events that occur on or after the specified date.
   * The returned list is sorted by event start time in ascending order.
   * If there are fewer than 10 events starting on or after the given date, all such
   * events are returned.
   *
   * @param date the starting date from which to retrieve events (inclusive)
   * @return a list of events starting from the specified date, sorted by start time,
   *      limited to 10 events
   */
  List<IEvent> getEventsFromDate(LocalDate date);

  /**
   * Returns all events in this calendar.
   *
   * @return list of all events
   */
  List<IEvent> getEvents();

  /**
   * Edits a single event by creating a modified copy with one updated property.
   * If the modified event conflicts with existing events, the change is aborted.
   *
   * @param event     the original event to edit
   * @param property  the name of the property to modify
   * @param newValue  the new value to assign to the property
   * @param formatter the formatter to parse new date/time values if needed
   * @return true if the event was successfully edited; false if there was a conflict
   */
  boolean editSingleEvent(IEvent event, String property, String newValue,
                          DateTimeFormatter formatter);

  /**
   * Edits all future events in the same recurring series as the given event,
   * starting from the given event's start time.
   * If the event is not part of a series, edits only that one event.
   * Skips conflicting events and does not modify them.
   *
   * @param event     the base event to edit from
   * @param property  the property to change
   * @param newValue  the new value for the property
   * @param formatter the formatter to parse date/time values
   * @return the number of events successfully modified
   */
  int editFutureEvents(IEvent event, String property, String newValue,
                       DateTimeFormatter formatter);

  /**
   * Edits all events in the recurring series to which the given event belongs.
   * If the event is not part of a series, edits only that one event.
   * Skips conflicting events and does not modify them.
   *
   * @param event     the base event in the series
   * @param property  the property to change
   * @param newValue  the new value for that property
   * @param formatter the formatter to parse date/time values
   * @return the number of events successfully modified
   */
  int editWholeSeries(IEvent event, String property, String newValue,
                      DateTimeFormatter formatter);

  /**
   * Creates a new Event object by copying all fields of `base`, then changing exactly one property.
   * The returned Event preserves the original seriesId if it was non-null.
   *
   * @param base      the existing Event to copy
   * @param property  which property to change
   * @param newValue  the new value for that property
   * @param formatter the DateTimeFormatter used to parse date/time values
   * @return a brand‐new Event reflecting the single change
   * @throws IllegalArgumentException if property is unrecognized or newValue is badly formatted
   */
  IEvent createModifiedEvent(Event base, String property, String newValue,
                             DateTimeFormatter formatter);

  /**
   * Get the timezone of this calendar.
   *
   * @return the ZoneId representing this calendar's timezone
   */
  ZoneId getTimezone();

  /**
   * Set the timezone of this calendar.
   *
   * @param timezone the new timezone to set
   */
  void setTimezone(ZoneId timezone);

  /**
   * Get the timezone in the creation of this calendar.
   *
   * @return ZoneId representing the timezone in the creation of this calendar.
   */
  ZoneId getCreationTimezone();
}
