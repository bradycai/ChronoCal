package calendar.model;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The storing and managing of all calendar events.
 */
public class CalendarModel implements ICalendarModel {
  private final List<IEvent> events;
  private ZoneId timezone;
  private final ZoneId creationTimezone;


  /**
   * Makes a new Calendar with a specific timezone.
   *
   * @param timezone the current timezone we are in
   */
  public CalendarModel(ZoneId timezone) {
    this.timezone = timezone;
    this.creationTimezone = timezone;
    this.events = new ArrayList<>();
  }


  /**
   * Add events to the calendar.
   *
   * @param event represents another event to add to the list.
   */
  public void addEvent(IEvent event) {
    for (int i = 0; i < events.size(); i++) {
      IEvent currEvent = events.get(i);

      if (currEvent.isSame((Event) event)) {
        throw new IllegalArgumentException("Cannot add two events with the same subject");
      }
    }
    events.add(event);
  }

  /**
   * removes events from the calendar.
   *
   * @param event represents another event to remove from the list.
   */
  public void removeEvent(IEvent event) {
    for (int i = 0; i < events.size(); i++) {
      IEvent currEvent = events.get(i);
      if (currEvent.isSame((Event) event)) {
        events.remove(i);
        return;
      }
    }
    throw new IllegalArgumentException("Event not found in calendar");
  }

  /**
   * Finds and returns an event with the given subject and start time.
   * Returns null if no such event exists or if more than one match is found.
   *
   * @param subject the subject to match
   * @param start   the start date/time to match
   * @return the matching IEvent, or null if not found or ambiguous
   */
  public IEvent findEvent(String subject, LocalDateTime start) {
    IEvent found = null;
    for (IEvent e : events) {

      if (e.getSubject().equals(subject) && e.getStart().equals(start)) {
        if (found != null) {
          return null;
        }
        found = e;
      }
    }
    return found;
  }

  /**
   * Get all events on that date.
   *
   * @param date represents the date.
   * @return all events on that specific date.
   */
  public List<IEvent> getEventsOnDate(LocalDate date) {
    List<IEvent> result = new ArrayList<>();
    for (IEvent e : events) {
      if (e.getStart().toLocalDate().equals(date)) {
        result.add(e);
      }
    }
    return result;
  }

  /**
   * Gets all events within two dates.
   *
   * @param beginning the start date of the range.
   * @param ending    the end date of the range.
   * @return all events within the date range.
   */
  public List<IEvent> getEventsWithinDates(LocalDateTime beginning, LocalDateTime ending) {
    List<IEvent> result = new ArrayList<>();
    for (int i = 0; i < events.size(); i++) {
      IEvent currEvent = events.get(i);
      if (currEvent.getEnd().isAfter(beginning) && currEvent.getStart().isBefore(ending)) {
        result.add(currEvent);
      }
    }
    return result;
  }

  /**
   * Checks whether the event overlaps with a specific time.
   *
   * @param time is the time.
   * @return whether the event overlaps.
   */
  public boolean isBusy(LocalDateTime time) {
    for (IEvent currEvent : events) {
      if (currEvent.getEnd().isAfter(time) && currEvent.getStart().isBefore(time)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns true if adding the given Event `e` would conflict with an existing event.
   * We define a “conflict” to be: an existing calendar event that has the same
   * subject, the same start, and the same end.  In other words, duplicates are not allowed.
   *
   * @param e the Event to check
   * @return true if there is already an event equal to `e`; false otherwise
   */
  public boolean hasConflict(IEvent e) {
    for (IEvent existing : events) {
      if (existing.equals(e)) {
        return true;
      }
    }
    return false;
  }

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
  public IEvent createModifiedEvent(Event base, String property, String newValue,
                                    DateTimeFormatter formatter) {
    IEvent copy = new Event(
            base.getSubject(),
            base.getStart(),
            base.getEnd(),
            base.getLocation(),
            base.getDescription(),
            base.getStatus());

    if (base.getSeriesId() != null) {
      copy.setSeriesId(base.getSeriesId());
    }

    switch (property.toLowerCase()) {
      case "subject":
        copy.setSubject(newValue);
        break;
      case "start":
        LocalDateTime newStart = LocalDateTime.parse(newValue, formatter);
        copy.setStart(newStart);
        break;
      case "end":
        LocalDateTime newEnd = LocalDateTime.parse(newValue, formatter);
        copy.setEnd(newEnd);
        break;
      case "location":
        copy.setLocation(newValue);
        break;
      case "description":
        copy.setDescription(newValue);
        break;
      case "status":
        copy.setPublic(newValue.equalsIgnoreCase("public") || newValue.equalsIgnoreCase("true"));
        break;
      default:
        throw new IllegalArgumentException("Invalid property: " + property);
    }

    return copy;
  }

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
  public boolean editSingleEvent(IEvent event, String property, String newValue,
                                 DateTimeFormatter formatter) {
    IEvent modified = createModifiedEvent((Event) event, property, newValue, formatter);

    if (hasConflict(modified)) {
      return false;
    }

    removeEvent(event);
    addEvent(modified);
    return true;
  }

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
  public int editFutureEvents(IEvent event, String property, String newValue,
                              DateTimeFormatter formatter) {
    Event base = (Event) event;

    UUID seriesId = base.getSeriesId();
    if (seriesId == null) {
      boolean success = editSingleEvent(event, property, newValue, formatter);
      if (success) {
        return 1;
      } else {
        return 0;
      }
    }

    LocalDateTime baseStart = base.getStart();
    List<IEvent> allAfter = getEventsWithinDates(baseStart, LocalDateTime.MAX);
    int count = 0;

    for (IEvent e : allAfter) {
      Event ev = (Event) e;
      if (ev.getSeriesId() != null
              && ev.getSeriesId().equals(seriesId)
              && !ev.getStart().isBefore(baseStart)) {
        Event modified = (Event) createModifiedEvent(ev, property, newValue, formatter);
        if (!hasConflict(modified)) {
          removeEvent(ev);
          addEvent(modified);
          count++;
        }
      }
    }

    return count;
  }

  @Override
  public List<IEvent> getEventsFromDate(LocalDate date) {
    List<IEvent> matching = new ArrayList<>();
    for (IEvent event : events) {
      if (!event.getStart().toLocalDate().isBefore(date)) {
        matching.add(event);
      }
    }
    matching.sort(Comparator.comparing(IEvent::getStart));
    if (matching.size() > 10) {
      return matching.subList(0, 10);
    }
    return matching;
  }

  /**
   * Returns all events in this calendar.
   *
   * @return list of all events
   */
  public List<IEvent> getEvents() {
    return new ArrayList<>(events);
  }


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
  public int editWholeSeries(IEvent event, String property, String newValue,
                             DateTimeFormatter formatter) {

    UUID seriesId = event.getSeriesId();
    if (seriesId == null) {
      boolean success = editSingleEvent(event, property, newValue, formatter);
      if (success) {
        return 1;
      } else {
        return 0;
      }
    }

    List<IEvent> allEvents = getEventsWithinDates(LocalDateTime.MIN, LocalDateTime.MAX);
    int count = 0;

    for (IEvent e : allEvents) {
      Event ev = (Event) e;
      if (ev.getSeriesId() != null && ev.getSeriesId().equals(seriesId)) {
        Event modified = (Event) createModifiedEvent(ev, property, newValue, formatter);
        if (!hasConflict(modified)) {
          removeEvent(ev);
          addEvent(modified);
          count++;
        }
      }
    }

    return count;
  }

  @Override
  public ZoneId getTimezone() {
    return timezone;
  }

  @Override
  public void setTimezone(ZoneId timezone) {
    this.timezone = timezone;
  }

  @Override
  public ZoneId getCreationTimezone() {
    return creationTimezone;
  }


}
