package calendar.gui;

import calendar.model.Event;
import calendar.model.ICalendarLibrary;
import calendar.model.IEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Controller for handling GUI actions in the Calendar application.
 * This class processes user input from the GUI, modifies the model, and updates the view.
 */
public class CalendarGUIController implements ICalendarGUIController {
  private final ICalendarLibrary library;
  private final ICalendarGUIView view;

  /**
   * Constructs a CalendarGUIController.
   * @param library the calendar library model
   * @param view the GUI view
   */
  public CalendarGUIController(ICalendarLibrary library, ICalendarGUIView view) {
    this.library = library;
    this.view = view;
    this.view.setController(this);
  }

  /**
   * Creates a new event with the given subject, start time, and end time.
   * Displays an error message if parsing fails or the event cannot be added.
   * @param subject the event subject
   * @param start the start time in format yyyy-MM-dd'T'HH:mm
   * @param end the end time in format yyyy-MM-dd'T'HH:mm
   */
  @Override
  public void createEvent(String subject, String start, String end) {
    try {
      LocalDateTime startTime = LocalDateTime.parse(start);
      LocalDateTime endTime = LocalDateTime.parse(end);
      IEvent event = new Event(subject, startTime, endTime);
      library.getActiveCalendar().addEvent(event);
      view.showError("Event created.");
    } catch (Exception e) {
      view.showError("Error creating event: " + e.getMessage());
    }
  }

  /**
   * Loads and displays events from a given date forward.
   * Displays an error message if parsing fails.
   * @param date the date string in format yyyy-MM-dd
   */
  @Override
  public void loadEventsFromDate(String date) {
    try {
      LocalDate startDate = LocalDate.parse(date);
      List<IEvent> events = library.getActiveCalendar().getEventsFromDate(startDate);
      view.showEvents(events);
    } catch (Exception e) {
      view.showError("Error loading events: " + e.getMessage());
    }
  }

  /**
   * Edits an existing event by removing the old event and adding a new event with updated values.
   * Displays error messages for parsing errors or model failures.
   * @param event the original event to edit
   * @param newSubject the new subject
   * @param newStart the new start time in format yyyy-MM-dd'T'HH:mm
   * @param newEnd the new end time in format yyyy-MM-dd'T'HH:mm
   */
  @Override
  public void editEvent(IEvent event, String newSubject, String newStart, String newEnd) {
    try {
      LocalDateTime newStartTime = LocalDateTime.parse(newStart);
      LocalDateTime newEndTime = LocalDateTime.parse(newEnd);
      library.getActiveCalendar().removeEvent(event);
      Event updatedEvent = new Event(newSubject, newStartTime, newEndTime);
      library.getActiveCalendar().addEvent(updatedEvent);
      view.showError("Event updated.");
      loadEventsFromDate(newStartTime.toLocalDate().toString());
    } catch (DateTimeParseException e1) {
      view.showError("Invalid date format. Please use yyyy-MM-dd'T'HH:mm");
    } catch (Exception e2) {
      view.showError("Error editing event: " + e2.getMessage());
    }
  }
}
