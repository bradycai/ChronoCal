package calendar.view;

import java.util.List;

/**
 * This interface handles all user outputs in the calendar.
 * It displays welcome messages, help menus, and formatted event details.
 */
public interface ICalendarView {

  /**
   * Displays the welcome message.
   */
  void displayWelcomeMessage();

  /**
   * Displays a list of all available commands for creating, editing,
   * and managing events.
   */
  void displayHelp();

  /**
   * Displays a general success or info message to the user.
   *
   * @param message the message to display
   */
  void displayMessage(String message);

  /**
   * Displays an error message to the user.
   *
   * @param error the error message to display
   */
  void displayError(String error);

  /**
   * Displays a list of event strings.
   *
   * @param eventStrings formatted event descriptions
   */
  void displayFormattedEvents(List<String> eventStrings);


}
