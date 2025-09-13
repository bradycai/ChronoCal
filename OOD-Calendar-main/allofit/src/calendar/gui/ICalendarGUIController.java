package calendar.gui;

import calendar.model.IEvent;

/**
 * Interface for the Calendar GUI Controller.
 * Defines the operations that the GUI view can invoke on the controller.
 */
public interface ICalendarGUIController {

  /**
   * Creates a new event with the given subject, start time, and end time.
   * Displays an error message if parsing fails or the event cannot be added.
   * @param subject the event subject
   * @param start the start time in format yyyy-MM-dd'T'HH:mm
   * @param end the end time in format yyyy-MM-dd'T'HH:mm
   */
  void createEvent(String subject, String start, String end);

  /**
   * Loads and displays events from a given date forward.
   * Displays an error message if parsing fails.
   * @param date the date string in format yyyy-MM-dd
   */
  void loadEventsFromDate(String date);

  /**
   * Edits an existing event by removing the old event and adding a new event with updated values.
   * Displays error messages for parsing errors or model failures.
   * @param event the original event to edit
   * @param newSubject the new subject
   * @param newStart the new start time in format yyyy-MM-dd'T'HH:mm
   * @param newEnd the new end time in format yyyy-MM-dd'T'HH:mm
   */
  void editEvent(IEvent event, String newSubject, String newStart, String newEnd);
}
