package calendar;

import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import calendar.model.Event;
import calendar.model.EventSeries;

import static org.junit.Assert.assertEquals;

/**
 * Testing Calendar Application.
 */

public class CalendarAppTest {

  @Test
  public void testValid() {
    LocalDateTime start = LocalDateTime.parse("2025-06-02T09:00");
    LocalDateTime end = LocalDateTime.parse("2025-06-02T10:00");
    Set<DayOfWeek> days = Set.of(DayOfWeek.MONDAY);

    EventSeries series = new EventSeries("Start Day", start, end, days, 1);

    assertEquals("2025-06-02T09:00", series.getEvents().get(0).getStart().toString());
  }

  @Test
  public void testCountZeroEvents() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 2, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 2, 10, 0);
    Set<DayOfWeek> days = Set.of(DayOfWeek.MONDAY);

    EventSeries series = new EventSeries("Zero Events", start, end, days, 0);

    assertEquals(0, series.getEvents().size());
  }

  @Test
  public void testGenerateByCountMWF3Occ() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 2, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 2, 10, 0);
    Set<DayOfWeek> days = Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);

    EventSeries series = new EventSeries("Math Class", start, end, days, 3);
    List<Event> events = series.getEvents();

    assertEquals(3, events.size());
    assertEquals("2025-06-02T09:00", events.get(0).getStart().toString());
    assertEquals("2025-06-04T09:00", events.get(1).getStart().toString());
    assertEquals("2025-06-06T09:00", events.get(2).getStart().toString());
    assertEquals("2025-06-02T10:00", events.get(0).getEnd().toString());
    assertEquals("2025-06-04T10:00", events.get(1).getEnd().toString());
    assertEquals("2025-06-06T10:00", events.get(2).getEnd().toString());
    assertEquals("Math Class", events.get(0).getSubject());
    assertEquals("Math Class", events.get(1).getSubject());
    assertEquals("Math Class", events.get(2).getSubject());
  }

  @Test
  public void testGenerateByCountTR2Occ() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 3, 13, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 3, 14, 0);
    Set<DayOfWeek> days = Set.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY);

    EventSeries series = new EventSeries("Beach Time", start, end, days, 2);
    List<Event> events = series.getEvents();

    assertEquals(2, events.size());
    assertEquals("2025-06-03T13:00", events.get(0).getStart().toString());
    assertEquals("2025-06-05T13:00", events.get(1).getStart().toString());
    assertEquals("2025-06-03T14:00", events.get(0).getEnd().toString());
    assertEquals("2025-06-05T14:00", events.get(1).getEnd().toString());
    assertEquals("Beach Time", events.get(0).getSubject());
    assertEquals("Beach Time", events.get(1).getSubject());
  }

  @Test
  public void testGenerateByCountS1Occ() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 7, 3, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 7, 5, 0);
    Set<DayOfWeek> days = Set.of(DayOfWeek.SATURDAY);

    EventSeries series = new EventSeries("Dancing", start, end, days, 1);
    List<Event> events = series.getEvents();

    assertEquals(1, events.size());
    assertEquals("2025-06-07T03:00", events.get(0).getStart().toString());
    assertEquals("2025-06-07T05:00", events.get(0).getEnd().toString());
    assertEquals("Dancing", events.get(0).getSubject());
    assertEquals("Dancing", events.get(0).getSubject());
  }

  @Test
  public void testAllWeekdays() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 2, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 2, 11, 0);
    Set<DayOfWeek> days = Set.of(DayOfWeek.values());

    EventSeries series = new EventSeries("Party", start, end, days, 7);
    List<Event> events = series.getEvents();

    assertEquals(7, events.size());
    assertEquals("2025-06-02T10:00", events.get(0).getStart().toString());
    assertEquals("2025-06-08T10:00", events.get(6).getStart().toString());
    assertEquals("2025-06-02T11:00", events.get(0).getEnd().toString());
    assertEquals("2025-06-08T11:00", events.get(6).getEnd().toString());
  }

  @Test
  public void testGenerateByDateMWF3Occ() {
    LocalDateTime start = LocalDateTime.parse("2025-06-02T09:00");
    LocalDateTime end = LocalDateTime.parse("2025-06-02T10:00");
    Set<DayOfWeek> days = Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
    LocalDate until = LocalDate.parse("2025-06-08");

    EventSeries series = new EventSeries("Math Class", start, end, days, until);
    List<Event> events = series.getEvents();

    assertEquals(3, events.size());
    assertEquals("2025-06-02T09:00", events.get(0).getStart().toString());
    assertEquals("2025-06-02T10:00", events.get(0).getEnd().toString());
    assertEquals("2025-06-04T09:00", events.get(1).getStart().toString());
    assertEquals("2025-06-04T10:00", events.get(1).getEnd().toString());
    assertEquals("2025-06-06T09:00", events.get(2).getStart().toString());
    assertEquals("2025-06-06T10:00", events.get(2).getEnd().toString());
    assertEquals("Math Class", events.get(0).getSubject());
  }

  @Test
  public void testGenerateByDateTR2Occ() {
    LocalDateTime start = LocalDateTime.parse("2025-06-03T13:00");
    LocalDateTime end = LocalDateTime.parse("2025-06-03T14:00");
    Set<DayOfWeek> days = Set.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY);
    LocalDate until = LocalDate.parse("2025-06-05");

    EventSeries series = new EventSeries("Beach Time", start, end, days, until);
    List<Event> events = series.getEvents();

    assertEquals(2, events.size());
    assertEquals("2025-06-03T13:00", events.get(0).getStart().toString());
    assertEquals("2025-06-03T14:00", events.get(0).getEnd().toString());
    assertEquals("2025-06-05T13:00", events.get(1).getStart().toString());
    assertEquals("2025-06-05T14:00", events.get(1).getEnd().toString());
    assertEquals("Beach Time", events.get(0).getSubject());
  }

  @Test
  public void testGenerateByDateS1Occ() {
    LocalDateTime start = LocalDateTime.parse("2025-06-07T03:00");
    LocalDateTime end = LocalDateTime.parse("2025-06-07T05:00");
    Set<DayOfWeek> days = Set.of(DayOfWeek.SATURDAY);
    LocalDate until = LocalDate.parse("2025-06-07");

    EventSeries series = new EventSeries("Dancing", start, end, days, until);
    List<Event> events = series.getEvents();

    assertEquals(1, events.size());
    assertEquals("2025-06-07T03:00", events.get(0).getStart().toString());
    assertEquals("2025-06-07T05:00", events.get(0).getEnd().toString());
    assertEquals("Dancing", events.get(0).getSubject());
  }

  @Test
  public void testGenerateByDateAllWeekdays() {
    LocalDateTime start = LocalDateTime.parse("2025-06-02T10:00");
    LocalDateTime end = LocalDateTime.parse("2025-06-02T11:00");
    Set<DayOfWeek> days = Set.of(DayOfWeek.values());
    LocalDate until = LocalDate.parse("2025-06-08");

    EventSeries series = new EventSeries("Party", start, end, days, until);
    List<Event> events = series.getEvents();

    assertEquals(7, events.size());
    assertEquals("2025-06-02T10:00", events.get(0).getStart().toString());
    assertEquals("2025-06-08T10:00", events.get(6).getStart().toString());
    assertEquals("2025-06-02T11:00", events.get(0).getEnd().toString());
    assertEquals("2025-06-08T11:00", events.get(6).getEnd().toString());
  }

  @Test
  public void testSkipsToFirstValidDay() {
    LocalDateTime start = LocalDateTime.parse("2025-06-02T09:00");
    LocalDateTime end = LocalDateTime.parse("2025-06-02T10:00");
    Set<DayOfWeek> days = Set.of(DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
    LocalDate until = LocalDate.parse("2025-06-06");

    EventSeries series = new EventSeries("Skip Start", start, end, days, until);
    List<Event> events = series.getEvents();

    assertEquals(2, events.size());
    assertEquals("2025-06-04T09:00", events.get(0).getStart().toString());
    assertEquals("2025-06-06T09:00", events.get(1).getStart().toString());
  }

  @Test
  public void testNotMatchingDay() {
    LocalDateTime start = LocalDateTime.parse("2025-06-02T09:00");
    LocalDateTime end = LocalDateTime.parse("2025-06-02T10:00");
    Set<DayOfWeek> days = Set.of(DayOfWeek.TUESDAY);
    LocalDate until = LocalDate.parse("2025-06-05");

    EventSeries series = new EventSeries("Day off", start, end, days, until);
    List<Event> events = series.getEvents();

    assertEquals(1, events.size());
    assertEquals("2025-06-03T09:00", events.get(0).getStart().toString());
  }

  @Test
  public void testAdjacentWeekdaysShortRange() {
    LocalDateTime start = LocalDateTime.parse("2025-06-03T15:00");
    LocalDateTime end = LocalDateTime.parse("2025-06-03T16:00");
    Set<DayOfWeek> days = Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
    LocalDate until = LocalDate.parse("2025-06-06");

    EventSeries series = new EventSeries("Work Week", start, end, days, until);
    List<Event> events = series.getEvents();

    assertEquals(4, events.size());
    assertEquals("2025-06-03T15:00", events.get(0).getStart().toString());
    assertEquals("2025-06-04T15:00", events.get(1).getStart().toString());
    assertEquals("2025-06-05T15:00", events.get(2).getStart().toString());
    assertEquals("2025-06-06T15:00", events.get(3).getStart().toString());
    assertEquals("2025-06-03T16:00", events.get(0).getEnd().toString());
    assertEquals("2025-06-04T16:00", events.get(1).getEnd().toString());
    assertEquals("2025-06-05T16:00", events.get(2).getEnd().toString());
    assertEquals("2025-06-06T16:00", events.get(3).getEnd().toString());
    assertEquals("Work Week", events.get(0).getSubject());
    assertEquals("Work Week", events.get(2).getSubject());
    assertEquals("Work Week", events.get(3).getSubject());
    assertEquals("Work Week", events.get(3).getSubject());
  }

  @Test
  public void testEqualsCountDate() {
    LocalDateTime start = LocalDateTime.parse("2025-06-02T09:00");
    LocalDateTime end = LocalDateTime.parse("2025-06-02T10:00");
    Set<DayOfWeek> days = Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);

    EventSeries byCount = new EventSeries("Test", start, end, days, 3);
    EventSeries byDate = new EventSeries("Test", start, end, days, LocalDate.parse("2025-06-06"));

    assertEquals(byCount.getEvents().size(), byDate.getEvents().size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEventSeriesSpanningMultipleDaysShouldThrow() {
    LocalDateTime start = LocalDateTime.parse("2025-06-06T23:00");
    LocalDateTime end = LocalDateTime.parse("2025-06-07T01:00");
    Set<DayOfWeek> days = Set.of(DayOfWeek.FRIDAY);
    LocalDate until = LocalDate.parse("2025-06-06");

    new EventSeries("Late Night", start, end, days, until);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeCountThrows() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 2, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 2, 10, 0);
    Set<DayOfWeek> days = Set.of(DayOfWeek.MONDAY);

    new EventSeries("Invalid", start, end, days, -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUntilBeforeStartThrows() {
    LocalDateTime start = LocalDateTime.parse("2025-06-10T09:00");
    LocalDateTime end = LocalDateTime.parse("2025-06-10T10:00");
    Set<DayOfWeek> days = Set.of(DayOfWeek.MONDAY);
    LocalDate until = LocalDate.parse("2025-06-01");

    new EventSeries("Invalid", start, end, days, until);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyDaysThrows() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 2, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 2, 10, 0);
    Set<DayOfWeek> days = Set.of();

    new EventSeries("Invalid", start, end, days, 5);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCountEmptyDays() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 2, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 2, 10, 0);
    Set<DayOfWeek> days = Set.of();

    EventSeries series = new EventSeries("Empty", start, end, days, 3);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDateEmptyDays() {
    LocalDateTime start = LocalDateTime.parse("2025-06-02T09:00");
    LocalDateTime end = LocalDateTime.parse("2025-06-02T10:00");
    Set<DayOfWeek> days = Set.of();
    LocalDate until = LocalDate.parse("2025-06-30");

    EventSeries series = new EventSeries("Empty", start, end, days, until);
    List<Event> events = series.getEvents();
  }
}