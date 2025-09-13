package calendar.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a recurring series of events in the calendar.
 * The series is either a repetition count or an end date.
 */
public class EventSeries implements IEventSeries {
  private final String subject;
  private final LocalDateTime start;
  private final LocalDateTime end;
  private final Set<DayOfWeek> repeatingDays;
  private final Integer count;
  private final LocalDate lastDate;
  private final List<Event> series;

  /**
   * Constructs a series of events that continues for a specific count of times.
   *
   * @param subject  The name of the event.
   * @param start    When the event starts.
   * @param end      When the event ends.
   * @param weekdays Determines which weekdays the event will happen.
   * @param count    counts the number of occurrences.
   */
  public EventSeries(String subject, LocalDateTime start, LocalDateTime end,
                     Set<DayOfWeek> weekdays, int count) {
    if (subject == null || subject.isEmpty() || start == null || end == null || weekdays == null) {
      throw new IllegalArgumentException("Arguments cant be null/empty.");
    }
    if (weekdays.isEmpty()) {
      throw new IllegalArgumentException("Weekdays cant be empty.");
    }
    if (!end.isAfter(start)) {
      throw new IllegalArgumentException("End needs to be after start.");
    }
    if (count < 0) {
      throw new IllegalArgumentException("Count cant be negative.");
    }

    this.subject = subject;
    this.start = start;
    this.end = end;
    this.repeatingDays = new HashSet<>(weekdays);
    this.count = count;
    this.lastDate = null;
    this.series = new ArrayList<>();
    generateByCount();
  }

  /**
   * Constructs a series of events that continues until a certain date.
   *
   * @param subject  The name of the event.
   * @param start    When the event starts.
   * @param end      When the event ends.
   * @param weekdays Determines which weekdays the event will happen.
   * @param lastDate The last day of which the recurring events will happen before they stop.
   */
  public EventSeries(String subject, LocalDateTime start, LocalDateTime end,
                     Set<DayOfWeek> weekdays, LocalDate lastDate) {
    if (subject == null || subject.isEmpty() || start == null || end == null
            || weekdays == null || lastDate == null) {
      throw new IllegalArgumentException("Arguments cant be null/empty.");
    }
    if (weekdays.isEmpty()) {
      throw new IllegalArgumentException("Weekdays cant be empty.");
    }
    if (!end.isAfter(start)) {
      throw new IllegalArgumentException("End needs to be after start.");
    }
    if (lastDate.isBefore(start.toLocalDate())) {
      throw new IllegalArgumentException("Until date cant be before start date.");
    }

    this.subject = subject;
    this.start = start;
    this.end = end;
    this.repeatingDays = new HashSet<>(weekdays);
    this.count = null;
    this.lastDate = lastDate;
    this.series = new ArrayList<>();
    generateByDate();
  }

  /**
   * Helper for constructor, which generates a series of events,
   * that repeat on specific days of the week up to the specified count.
   */
  private void generateByCount() {
    if (repeatingDays == null || repeatingDays.isEmpty()) {
      return;
    }

    LocalDate currDate = start.toLocalDate();
    int counter = 0;

    while (counter < count) {
      if (repeatingDays.contains(currDate.getDayOfWeek())) {
        LocalDateTime starting = LocalDateTime.of(currDate, start.toLocalTime());
        LocalDateTime ending = LocalDateTime.of(currDate, end.toLocalTime());
        series.add(new Event(subject, starting, ending));
        counter++;
      }
      currDate = currDate.plusDays(1);
    }
  }

  /**
   * Helper for constructor, which generates a series of events,
   * that repeat on specific days of the week up to the specified date.
   */
  private void generateByDate() {
    LocalDate currDate = start.toLocalDate();

    while (!currDate.isAfter(lastDate)) {
      if (repeatingDays.contains(currDate.getDayOfWeek())) {
        LocalDateTime starting = LocalDateTime.of(currDate, start.toLocalTime());
        LocalDateTime ending = LocalDateTime.of(currDate, end.toLocalTime());
        series.add(new Event(subject, starting, ending));
      }
      currDate = currDate.plusDays(1);
    }
  }

  /**
   * Gets the events.
   *
   * @return the events in a list
   */
  public List<Event> getEvents() {
    return new ArrayList<>(series);
  }
}
