package calendar;

import calendar.gui.CalendarGUIController;
import calendar.gui.ICalendarGUIController;
import calendar.gui.ICalendarGUIView;
import calendar.model.CalendarLibrary;
import calendar.model.Event;
import calendar.model.IEvent;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * JUnit 4 tests for the CalendarGUIController (no mock view).
 * Directly tests model behavior through controller calls.
 */
public class CalendarGUIControllerTest {

  private ICalendarGUIController controller;
  private CalendarLibrary library;

  @Before
  public void setUp() {
    library = new CalendarLibrary();
    library.createCalendar("Default", "America/New_York");
    library.useCalendar("Default");

    ICalendarGUIView dummyView = new ICalendarGUIView() {

      @Override
      public void setController(ICalendarGUIController controller) {
        //dummy stuff
      }

      @Override
      public void showEvents(List<IEvent> events) {
        //dummy stuff
      }

      @Override
      public void showError(String message) {
        //dummy stuff
      }
    };

    controller = new CalendarGUIController(library, dummyView);
  }

  @Test
  public void testCreateEvent() {
    controller.createEvent("Test Event", "2025-07-01T10:00", "2025-07-01T11:00");
    List<IEvent> events = library.getActiveCalendar().getEvents();
    assertEquals(1, events.size());
    IEvent event = events.get(0);
    assertEquals("Test Event", event.getSubject());
    assertEquals(LocalDateTime.of(2025, 7, 1, 10, 0),
            event.getStart());
  }

  @Test
  public void testEditEvent() {
    Event original = new Event("Original",
            LocalDateTime.of(2025, 7, 1, 10, 0),
            LocalDateTime.of(2025, 7, 1, 11, 0));
    library.getActiveCalendar().addEvent(original);

    controller.editEvent(original, "Updated", "2025-07-01T12:00",
            "2025-07-01T13:00");

    List<IEvent> events = library.getActiveCalendar().getEvents();
    assertEquals(1, events.size());
    IEvent updated = events.get(0);
    assertEquals("Updated", updated.getSubject());
    assertEquals(LocalDateTime.of(2025, 7, 1, 12, 0),
            updated.getStart());
  }
}
