package calendar;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import calendar.controller.CalendarController;
import calendar.model.CalendarLibrary;
import calendar.model.Event;
import calendar.model.ICalendarModel;
import calendar.model.IEvent;
import calendar.view.CalendarView;

/**
 * Comprehensive JUnit 4 test suite for the calendar application.
 */

public class CalendarControllerTest {
  private CalendarController controller;
  private ByteArrayOutputStream outContent;
  private PrintStream originalOut;
  private CalendarLibrary library;

  @Before
  public void setUp() {
    originalOut = System.out;
    outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    library = new CalendarLibrary();  // <-- correct assignment to class field
    library.createCalendar("default", "America/New_York");
    library.useCalendar("default");

    controller = new CalendarController(library, new CalendarView());
  }

  @After
  public void tearDown() {
    System.setOut(originalOut);
  }

  @Test
  public void testUnknownCommand() {
    controller.processCommand("foobar");
    String output = outContent.toString();
    assertTrue(output.toLowerCase().contains("unknown command"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateDuplicateEventThrows() {
    controller.processCommand("create event \"Test Event\" on 2025-06-10");
    controller.processCommand("create event \"Test Event\" on 2025-06-10");
  }

  @Test
  public void testCreateAllDayEvent() {
    controller.processCommand("create event \"Meeting\" on 2025-06-10");
    String output = outContent.toString().trim();
    assertTrue(output.contains("Created all-day event: \"Meeting\""));
  }

  @Test
  public void testCreateTimedEvent() {
    controller.processCommand("create event \"Standup\" from 2025-06-10T09:00 to 2025-06-10T09:30");
    String output = outContent.toString().trim();
    assertTrue(output.contains("Created timed event: \"Standup\""));
  }


  @Test
  public void testShowStatusAvailableAndBusy() {
    controller.processCommand("show status on 2025-06-10T08:00");
    String output1 = outContent.toString();
    assertTrue(output1.contains("available"));

    outContent.reset();
    controller.processCommand("create event \"Focus\" from 2025-06-10T09:00 to 2025-06-10T10:00");
    outContent.reset();
    controller.processCommand("show status on 2025-06-10T09:30");
    String output2 = outContent.toString();
    assertTrue(output2.contains("busy"));

    outContent.reset();
    controller.processCommand("show status on 2025-06-10T09:00");
    String output3 = outContent.toString();
    assertTrue(output3.contains("available"));

    outContent.reset();
    controller.processCommand("show status on 2025-06-10T10:00");
    String output4 = outContent.toString();
    assertTrue(output4.contains("available"));
  }

  @Test
  public void testPrintEventsOnDate() {
    controller.processCommand("create event \"Alpha\" on 2025-06-11");
    controller.processCommand("create event \"Beta\" on 2025-06-11");
    outContent.reset();
    controller.processCommand("print events on 2025-06-11");
    String output = outContent.toString();
    assertTrue(output.contains("• \"Alpha\""));
    assertTrue(output.contains("• \"Beta\""));
  }

  @Test
  public void testPrintEventsWithinRange() {
    controller.processCommand("create event \"X\" from 2025-06-09T10:00 to 2025-06-09T11:00");
    controller.processCommand("create event \"Y\" from 2025-06-10T12:00 to 2025-06-10T13:00");
    outContent.reset();
    controller.processCommand("print events from 2025-06-09T00:00 to 2025-06-09T23:59");
    String output = outContent.toString();
    assertTrue(output.contains("• \"X\""));
    assertFalse(output.contains("• \"Y\""));
  }

  @Test
  public void testCopySingleEventAcrossTimezones() {
    CalendarLibrary library = new CalendarLibrary();
    library.createCalendar("NY", "America/New_York");
    library.createCalendar("CA", "America/Los_Angeles");

    library.useCalendar("NY");

    LocalDateTime nyStart = LocalDateTime.of(2025, 7, 1, 13, 0);
    LocalDateTime nyEnd = LocalDateTime.of(2025, 7, 1, 14, 0);
    Event event = new Event("Lunch", nyStart, nyEnd);
    library.getActiveCalendar().addEvent(event);

    ZonedDateTime sourceZoned = nyStart.atZone(ZoneId.of("America/New_York"));
    ZonedDateTime targetZoned = sourceZoned
            .withZoneSameInstant(ZoneId.of("America/Los_Angeles"));
    LocalDateTime caStart = targetZoned.toLocalDateTime();

    Event copied = (Event) event.copyWithNewTime(caStart);

    library.useCalendar("CA");
    library.getActiveCalendar().addEvent(copied);

    List<IEvent> events = library.getActiveCalendar().getEventsOnDate(caStart.toLocalDate());

    assertEquals(1, events.size());
    assertEquals("Lunch", events.get(0).getSubject());
    assertEquals(caStart, events.get(0).getStart());
  }


  @Test
  public void testRecurringTimedEventCountBased() {
    controller.processCommand("create event \"DailyMeeting\" "
            + "from 2025-06-10T09:00 to 2025-06-10T10:00 repeats MTWRFSU for 3");
    String output = outContent.toString().trim();
    assertTrue(output.contains("Created recurring timed event series: \"DailyMeeting\""));

    outContent.reset();
    controller.processCommand("print events from 2025-06-10T00:00 to 2025-06-12T23:59");
    String printed = outContent.toString();
    int countOccurrences = printed.split("DailyMeeting", -1).length - 1;
    assertEquals(3, countOccurrences);
  }


  @Test
  public void testRecurringTimedEventUntilBased() {
    controller.processCommand("create event \"SprintReview\" "
            + "from 2025-06-10T14:00 to 2025-06-10T15:00 " + "repeats MTWRFSU until 2025-06-12");
    String output = outContent.toString().trim();
    assertTrue(output.contains("Created recurring timed event series: \"SprintReview\""));
    outContent.reset();
    controller.processCommand("print events from 2025-06-10T00:00 to 2025-06-13T00:00");
    String printed = outContent.toString();
    int countOccurrences = printed.split("SprintReview", -1).length - 1;
    assertEquals(3, countOccurrences);
  }

  @Test
  public void testCopyEventsInRangeSameTimezone() {
    CalendarLibrary library = new CalendarLibrary();
    library.createCalendar("Source", "America/New_York");
    library.createCalendar("Target", "America/New_York");

    library.useCalendar("Source");
    LocalDateTime start1 = LocalDateTime.of(2025, 7, 1, 9, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 7, 1, 10, 0);
    Event e1 = new Event("Morning Sync", start1, end1);
    library.getActiveCalendar().addEvent(e1);

    LocalDateTime start2 = LocalDateTime.of(2025, 7, 2, 14, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 7, 2, 15, 0);
    Event e2 = new Event("Afternoon Review", start2, end2);
    library.getActiveCalendar().addEvent(e2);

    List<IEvent> toCopy = library.getActiveCalendar()
            .getEventsWithinDates(LocalDateTime.of(2025, 7,
                    1, 0, 0), LocalDateTime.of(2025, 7, 2, 23, 59));
    List<Event> copied = new ArrayList<>();
    for (IEvent e : toCopy) {
      LocalDateTime shifted = e.getStart().plusDays(9);
      copied.add((Event) e.copyWithNewTime(shifted));
    }

    library.useCalendar("Target");
    for (Event e : copied) {
      library.getActiveCalendar().addEvent(e);
    }

    List<IEvent> result1 = library.getActiveCalendar()
            .getEventsOnDate(LocalDate.of(2025, 7, 10));
    List<IEvent> result2 = library.getActiveCalendar()
            .getEventsOnDate(LocalDate.of(2025, 7, 11));

    assertEquals(1, result1.size());
    assertEquals("Morning Sync", result1.get(0).getSubject());
    assertEquals(1, result2.size());
    assertEquals("Afternoon Review", result2.get(0).getSubject());
  }


  @Test
  public void testRecurringAllDayEventCountBased() {
    controller.processCommand("create event \"Gym\" on 2025-06-09 repeats MW for 2");
    String output = outContent.toString().trim();
    assertTrue(output.contains("Created recurring all-day event series: \"Gym\""));
    outContent.reset();
    controller.processCommand("print events on 2025-06-09");
    String printed1 = outContent.toString();
    assertTrue(printed1.contains("• \"Gym\""));
    outContent.reset();
    controller.processCommand("print events on 2025-06-11");
    String printed2 = outContent.toString();
    assertTrue(printed2.contains("• \"Gym\""));
  }


  @Test
  public void testRecurringAllDayEventUntilBased() {
    controller.processCommand("create event \"Yoga\" on 2025-06-09 repeats MF until 2025-06-13");
    String output = outContent.toString().trim();
    assertTrue(output.contains("Created recurring all-day event series: \"Yoga\""));

    outContent.reset();
    controller.processCommand("print events on 2025-06-09");
    String printed1 = outContent.toString();
    assertTrue(printed1.contains("• \"Yoga\""));

    outContent.reset();
    controller.processCommand("print events on 2025-06-13");
    String printed2 = outContent.toString();
    assertTrue(printed2.contains("• \"Yoga\""));
  }


  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventInvalidFormatThrows() {
    controller.processCommand("create event \"BadEvent\" 2025-06-10");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateTimedEventInvalidFormatThrows() {
    controller.processCommand("create event \"BadTimed\" from 2025-06-10T09:00");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateRecurringEventInvalidWeekdayThrows() {
    controller.processCommand("create event \"Faulty\" on 2025-06-10 repeats FOO for 3");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateRecurringEventMissingForOrUntilThrows() {
    controller.processCommand("create event \"Faulty\" on 2025-06-10 repeats MONDAY,BADDAY");
  }

  @Test
  public void testEditNonexistentEventPrintsError() {
    outContent.reset();
    controller.processCommand("edit location \"NoSuchEvent\" from 2025-07-06T10:00"
            + " with \"Room101\"");
    String output = outContent.toString();
    assertTrue(output.contains("Error: Event not found."));
  }


  @Test(expected = IllegalArgumentException.class)
  public void testEditMissingFromThrows() {
    controller.processCommand("edit event subject \"X\" 2025-07-06T10:00 with \"NewVal\"");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditMissingWithThrows() {
    controller.processCommand("edit event subject \"X\" from 2025-07-06T10:00");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditInvalidPropertyThrows() {
    controller.processCommand("edit event foo \"EventA\" from 2025-07-02T09:00 with \"value\"");
  }

  @Test
  public void testPrintEventFormatMatchesBulletAndTimeRange() {
    controller.processCommand("create event \"Planning\" from "
            + "2025-07-01T13:00 to 2025-07-01T14:30");
    outContent.reset();
    controller.processCommand("print events on 2025-07-01");
    String output = outContent.toString();

    assertTrue(output.contains("• \"Planning\""));
    assertTrue(output.contains("(2025-07-01 13:00 - 2025-07-01 14:30)"));
    assertTrue(output.contains("Status: public"));
  }

  @Test
  public void testControllerInputIsSentToModel() {
    controller.processCommand("create event \"Design Review\" from "
            + "2025-07-02T14:00 to 2025-07-02T15:00");
    outContent.reset();
    controller.processCommand("print events on 2025-07-02");
    String output = outContent.toString();
    assertTrue(output.contains("Design Review"));
  }

  @Test
  public void testEventPrintedCorrectlyAfterTimezoneChange() {
    controller.processCommand("create calendar Work America/New_York");
    controller.processCommand("switch calendar Work");
    controller.processCommand("create event \"Meeting\" from 2025-07-01T09:00 to 2025-07-01T10:00");
    controller.processCommand("edit calendar Work timezone Asia/Tokyo");
    outContent.reset();
    controller.processCommand("print events on 2025-07-01");
    String output = outContent.toString();
    assertTrue(output.contains("Meeting"));
    assertTrue(output.contains("22:00"));
  }

  @Test
  public void testCopySingleEventSameTimezone() {
    CalendarLibrary library = new CalendarLibrary();
    library.createCalendar("Work", "America/New_York");
    library.createCalendar("Personal", "America/New_York");

    library.useCalendar("Work");

    LocalDateTime srcStart = LocalDateTime.of(2025, 6, 15, 10, 0);
    LocalDateTime srcEnd = LocalDateTime.of(2025, 6, 15, 11, 0);
    Event event = new Event("Meeting", srcStart, srcEnd);
    library.getActiveCalendar().addEvent(event);
    LocalDateTime destStart = LocalDateTime.of(2025, 6, 16, 14, 0);
    Event copied = (Event) event.copyWithNewTime(destStart);
    library.getActiveCalendar().removeEvent(event);
    library.useCalendar("Personal");
    library.getActiveCalendar().addEvent(copied);

    List<IEvent> events = library.getActiveCalendar().getEventsOnDate(destStart.toLocalDate());

    assertEquals(1, events.size());
    assertEquals("Meeting", events.get(0).getSubject());
    assertEquals(destStart, events.get(0).getStart());
  }

  @Test
  public void testEditCalendarTimezone() {
    outContent.reset();

    controller.processCommand("create calendar Work America/New_York");
    controller.processCommand("switch calendar Work");
    controller.processCommand("edit calendar Work timezone Asia/Tokyo");

    library.useCalendar("Work");  // Use the shared library instance
    ICalendarModel activeModel = library.getActiveCalendar();
    assertEquals(ZoneId.of("Asia/Tokyo"), activeModel.getTimezone());
  }








}
