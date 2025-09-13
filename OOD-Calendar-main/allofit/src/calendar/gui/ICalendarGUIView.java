package calendar.gui;

import calendar.model.IEvent;
import java.util.List;

/**
 * Interface for the Calendar GUI View.
 * Specifies the methods that the view must provide to allow communication with the GUI controller.
 */
public interface ICalendarGUIView {

  /**
   * Sets the controller for this view.
   * Allows the view to communicate user actions to the controller.
   *
   * @param controller the controller implementation
   */
  void setController(ICalendarGUIController controller);

  /**
   * Displays the provided list of events in the GUI schedule view.
   *
   * @param events the list of events to display
   */
  void showEvents(List<IEvent> events);

  /**
   * Displays an error message to the user.
   *
   * @param message the error message to display
   */
  void showError(String message);
}
