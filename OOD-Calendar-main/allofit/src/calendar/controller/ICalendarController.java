package calendar.controller;

import java.time.LocalDate;
import java.util.List;

import calendar.gui.CalendarGUIView;
import calendar.model.Event;

/**
 * Defines the public API of a calendar controller.
 * A calendar controller can process single commands (for tests or headless mode)
 * and run an interactive loop (for interactive user input).
 */
public interface ICalendarController {
  /**
   * Processes exactly one user command string.
   * May print output or errors to stdout.
   *
   * @param command the raw command line
   */
  void processCommand(String command);

  /**
   * Starts the interactive read–eval–print loop.
   * Reads commands from stdin until "exit" is received.
   */
  void runInteractive();

  /**
   * Executes commands from a file in headless mode.
   * Stops when "exit" is encountered or file ends.
   *
   * @param filePath the path to the script file containing commands
   */
  void runHeadless(String filePath);

  /**
   * Launches the graphical user interface GUI for the calendar.
   * This mode allows the user to interact with the calendar using Swing components.
   */
  void runGUI();

  /**
   * Gets a list of events scheduled for a specified date.
   *
   * @param date the date where the events are
   * @return a list of events on that date
   */
  List<Event> getEventsOnDate(LocalDate date);

  /**
   * Sets the GUI view for the calendar application.
   *
   * @param gui the GUI view to use
   */
  public void setGUIView(CalendarGUIView gui);
}
