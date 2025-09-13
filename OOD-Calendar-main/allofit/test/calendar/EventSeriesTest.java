package calendar;

import org.junit.Before;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import calendar.model.Event;
import calendar.model.EventSeries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Unit tests for the EventSeries class.
 * This test suite verifies that the constructor enforces preconditions (e.g., non-null subject,
 * valid start/end times, non-empty weekday set, non-negative count) and that both count-based
 * and until-based series generation produce the expected list of Event instances. Each test
 * method checks a specific behavior or exception scenario.
 */

public class EventSeriesTest {
  private LocalDateTime mondayStart;
  private LocalDateTime mondayEnd;
  private Set<DayOfWeek> weekdays;

  @Before
  public void setUp() {
    mondayStart = LocalDateTime.of(2025, 6, 9, 9, 0);
    mondayEnd = LocalDateTime.of(2025, 6, 9, 10, 0);
    weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.MONDAY);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorNullSubjectThrows() {
    new EventSeries(null, mondayStart, mondayEnd, weekdays, 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorEmptySubjectThrows() {
    new EventSeries("", mondayStart, mondayEnd, weekdays, 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorNullStartThrows() {
    new EventSeries("Series", null, mondayEnd, weekdays, 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorNullEndThrows() {
    new EventSeries("Series", mondayStart, null, weekdays, 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorNullWeekdaysThrows() {
    new EventSeries("Series", mondayStart, mondayEnd, null, 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorEmptyWeekdaysThrows() {
    new EventSeries("EmptyDays", mondayStart, mondayEnd, new HashSet<>(), 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorCountNegativeThrows() {
    new EventSeries("Negative", mondayStart, mondayEnd, weekdays, -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorEndNotAfterStartThrows() {
    LocalDateTime before = mondayStart.minusHours(1);
    new EventSeries("Backward", mondayStart, before, weekdays, 3);
  }

  @Test
  public void testGetEventsCountZeroReturnsEmpty() {
    EventSeries seriesZero = new EventSeries("Zero", mondayStart, mondayEnd, weekdays, 0);
    List<Event> list = seriesZero.getEvents();
    assertTrue(list.isEmpty());
  }

  @Test
  public void testGetEventsSingleOccurrence() {
    EventSeries seriesOne = new EventSeries("Once", mondayStart, mondayEnd, weekdays, 1);
    List<Event> list = seriesOne.getEvents();
    assertEquals(1, list.size());
    Event single = list.get(0);
    assertEquals("Once", single.getSubject());
    assertEquals(mondayStart, single.getStart());
    assertEquals(mondayEnd, single.getEnd());
  }

  @Test
  public void testGetEventsMultipleOccurrences() {
    EventSeries seriesThree = new EventSeries("Tri", mondayStart, mondayEnd, weekdays, 3);
    List<Event> list = seriesThree.getEvents();
    assertEquals(3, list.size());
    for (int i = 0; i < 3; i++) {
      Event e = list.get(i);
      LocalDateTime expectedStart = mondayStart.plusWeeks(i);
      LocalDateTime expectedEnd = mondayEnd.plusWeeks(i);
      assertEquals(expectedStart, e.getStart());
      assertEquals(expectedEnd, e.getEnd());
    }
  }

  @Test
  public void testUntilBasedConstructorGeneratesCorrect() {
    LocalDate untilDate = LocalDate.of(2025, 6, 23);
    EventSeries series = new EventSeries("UntilTest", mondayStart,
            mondayEnd, weekdays, untilDate);
    List<Event> list = series.getEvents();
    assertEquals(3, list.size());
    for (int i = 0; i < 3; i++) {
      Event e = list.get(i);
      LocalDateTime expectedStart = mondayStart.plusWeeks(i);
      LocalDateTime expectedEnd = mondayEnd.plusWeeks(i);
      assertEquals(expectedStart, e.getStart());
      assertEquals(expectedEnd, e.getEnd());
    }
  }
}

