package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a library of calendars, each associated with a unique name and timezone.
 * Provides methods for creating, selecting, editing, and deleting calendars, as well as copying
 * events across calendars.
 */
public class CalendarLibrary implements ICalendarLibrary {
  private final Map<String, ICalendarModel> calendars;
  private final Map<String, ZoneId> calendarTimezones;
  private String currentCalendar;

  /**
   * Constructs an empty CalendarLibrary with no calendars.
   */
  public CalendarLibrary() {
    this.calendars = new HashMap<>();
    this.calendarTimezones = new HashMap<>();
    this.currentCalendar = null;
  }

  /**
   * Creates a new calendar with the given name and timezone.
   *
   * @param name           the unique name for the calendar
   * @param timezoneString the string ID of the desired timezone
   * @throws IllegalArgumentException if the name already exists or the timezone is invalid
   */
  public void createCalendar(String name, String timezoneString) {
    if (calendars.containsKey(name)) {
      throw new IllegalArgumentException("Calendar name already exists.");
    }

    ZoneId zone;
    try {
      zone = ZoneId.of(timezoneString);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid timezone: " + timezoneString);
    }

    calendars.put(name, new CalendarModel(zone));
    calendarTimezones.put(name, zone);
  }

  /**
   * Sets the specified calendar as the active one for subsequent operations.
   *
   * @param name the name of the calendar to activate
   * @throws IllegalArgumentException if the calendar does not exist
   */
  public void useCalendar(String name) {
    if (!calendars.containsKey(name)) {
      throw new IllegalArgumentException("No such calendar exists.");
    }
    this.currentCalendar = name;
  }

  /**
   * Returns the currently active calendar model.
   *
   * @return the active CalendarModel
   * @throws IllegalStateException if no calendar is currently in use
   */
  public ICalendarModel getActiveCalendar() {
    if (currentCalendar == null) {
      throw new IllegalStateException("No calendar in use.");
    }
    return calendars.get(currentCalendar);
  }

  /**
   * Returns the timezone of the currently active calendar.
   *
   * @return the ZoneId for the active calendar
   * @throws IllegalStateException if no calendar is currently in use
   */
  public ZoneId getActiveTimezone() {
    if (currentCalendar == null) {
      throw new IllegalStateException("No calendar in use.");
    }
    return calendarTimezones.get(currentCalendar);
  }

  /**
   * Edits a calendar's name or timezone.
   *
   * @param name     the existing calendar name
   * @param property either "name" or "timezone"
   * @param newValue the new name or timezone string
   * @throws IllegalArgumentException if the calendar doesn't exist,
   *                                  if the new name already exists, or the timezone is invalid
   */
  public void editCalendar(String name, String property, String newValue) {
    if (!calendars.containsKey(name)) {
      throw new IllegalArgumentException("No such calendar: " + name);
    }
    ICalendarModel model = calendars.get(name);
    if (property.equals("name")) {
      if (calendars.containsKey(newValue)) {
        throw new IllegalArgumentException("Calendar with name already exists: " + newValue);
      }
      calendars.remove(name);
      calendars.put(newValue, model);
      if (currentCalendar != null && currentCalendar.equals(name)) {
        currentCalendar = newValue;
      }
    } else if (property.equals("timezone")) {
      ZoneId zone = ZoneId.of(newValue);
      model.setTimezone(zone);
    } else {
      throw new IllegalArgumentException("Unknown property: " + property);
    }
  }

  /**
   * Deletes a calendar from the library.
   * If the calendar is currently in use, it is unset.
   *
   * @param name the name of the calendar to delete
   * @throws IllegalArgumentException if the calendar does not exist
   */
  public void deleteCalendar(String name) {
    if (!calendars.containsKey(name)) {
      throw new IllegalArgumentException("Calendar not found: " + name);
    }
    calendars.remove(name);
    calendarTimezones.remove(name);
    if (name.equals(currentCalendar)) {
      currentCalendar = null;
    }
  }

  /**
   * Copies a single event to another calendar, adjusting for timezones.
   *
   * @param subject   the event subject to identify the source event
   * @param start     the start time of the source event
   * @param targetCal the name of the calendar to copy into
   * @param dest      the new start time in the target calendar's timezone
   * @return true if successfully copied; false otherwise
   */
  public boolean copyEventToCalendar(String subject, LocalDateTime start, String targetCal,
                                     LocalDateTime dest) {
    if (currentCalendar == null || !calendars.containsKey(targetCal)) {
      return false;
    }

    ICalendarModel source = getActiveCalendar();
    ICalendarModel target = calendars.get(targetCal);
    ZoneId sourceZone = getActiveTimezone();
    ZoneId targetZone = calendarTimezones.get(targetCal);

    IEvent original = source.findEvent(subject, start);
    if (original == null) {
      return false;
    }

    IEvent shifted = original.copyWithNewTime(dest.atZone(targetZone)
            .withZoneSameInstant(sourceZone).toLocalDateTime());
    if (target.hasConflict(shifted)) {
      return false;
    }

    target.addEvent(shifted);
    return true;
  }

  /**
   * Copies all events on a specific date to another calendar.
   *
   * @param srcDate   the date from which to copy events
   * @param targetCal the name of the target calendar
   * @param destDate  the date to which events will be copied
   * @return the number of events successfully copied
   */
  public int copyEventsOnDateToCalendar(LocalDate srcDate, String targetCal, LocalDate destDate) {
    if (currentCalendar == null || !calendars.containsKey(targetCal)) {
      return 0;
    }

    ICalendarModel source = getActiveCalendar();
    ICalendarModel target = calendars.get(targetCal);
    ZoneId sourceZone = getActiveTimezone();
    ZoneId targetZone = calendarTimezones.get(targetCal);

    List<IEvent> events = source.getEventsOnDate(srcDate);
    int copied = 0;

    for (IEvent e : events) {
      long hour = e.getStart().getHour();
      long minute = e.getStart().getMinute();
      LocalDateTime destStart = destDate.atTime((int) hour, (int) minute);

      IEvent shifted = e.copyWithNewTime(destStart.atZone(sourceZone)
              .withZoneSameInstant(targetZone).toLocalDateTime());
      if (!target.hasConflict(shifted)) {
        target.addEvent(shifted);
        copied++;
      }
    }

    return copied;
  }

  /**
   * Copies all events from the source calendar to the target calendar that occur within
   * the specified date range (inclusive of both start and end dates).
   * The copied events are shifted relative to the target date to preserve their relative
   * positions in the range. Timezone conversion is applied based on each calendar's timezone.
   *
   * @param sourceCalendarName name of the source calendar
   * @param targetCalendarName name of the target calendar
   * @param startDate          starting date of the source range (inclusive)
   * @param endDate            ending date of the source range (inclusive)
   * @param targetDate         starting date in the target calendar where events will be shifted to
   * @return the number of events successfully copied
   * @throws IllegalArgumentException if calendars are not found or arguments are invalid
   */
  @Override
  public int copyEventsBetweenDatesToCalendar(
          String sourceCalendarName,
          String targetCalendarName,
          LocalDate startDate,
          LocalDate endDate,
          LocalDate targetDate) {

    if (!calendars.containsKey(sourceCalendarName)) {
      throw new IllegalArgumentException("Source calendar not found: " + sourceCalendarName);
    }
    if (!calendars.containsKey(targetCalendarName)) {
      throw new IllegalArgumentException("Target calendar not found: " + targetCalendarName);
    }

    ICalendarModel sourceCalendar = calendars.get(sourceCalendarName);
    ICalendarModel targetCalendar = calendars.get(targetCalendarName);

    ZoneId sourceZone = calendarTimezones.get(sourceCalendarName);
    ZoneId targetZone = calendarTimezones.get(targetCalendarName);

    int copied = 0;

    for (IEvent event : sourceCalendar.getEvents()) {
      LocalDate eventDate = event.getStart().toLocalDate();

      if (!eventDate.isBefore(startDate) && !eventDate.isAfter(endDate)) {
        long dayOffset = startDate.until(eventDate).getDays();
        LocalDate mappedDate = targetDate.plusDays(dayOffset);

        ZonedDateTime sourceStartZoned = event.getStart().atZone(sourceZone);
        ZonedDateTime sourceEndZoned = event.getEnd().atZone(sourceZone);

        LocalTime startTime = sourceStartZoned.toLocalTime();
        LocalTime endTime = sourceEndZoned.toLocalTime();

        ZonedDateTime targetStartZoned = ZonedDateTime.of(mappedDate, startTime, targetZone);
        ZonedDateTime targetEndZoned = ZonedDateTime.of(mappedDate, endTime, targetZone);

        Event copiedEvent = new Event(event.getSubject(),
                targetStartZoned.toLocalDateTime(),
                targetEndZoned.toLocalDateTime());

        if (!targetCalendar.hasConflict(copiedEvent)) {
          targetCalendar.addEvent(copiedEvent);
          copied++;
        }
      }
    }

    return copied;
  }

  /**
   * Returns the set of all calendar names currently stored.
   *
   * @return a set of calendar names
   */
  public Set<String> listCalendars() {
    return calendars.keySet();
  }

  /**
   * Returns the name of the currently active calendar.
   *
   * @return the name of the active calendar, or null if no calendar is in use
   */
  public String getCurrentCalendarName() {
    return currentCalendar;
  }
}
