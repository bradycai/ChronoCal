package calendar.controller;

import calendar.model.CalendarLibrary;
import calendar.model.ICalendarLibrary;
import calendar.gui.CalendarGUIView;
import calendar.view.CalendarView;

/**
 * The entry point for the Calendar application.
 * This class sets up the controller and supports GUI, interactive, and headless modes.
 */
public class CalendarApp {
  private final ICalendarController controller;

  /**
   * Constructs a new CalendarApp without creating GUI immediately.
   */
  public CalendarApp() {
    ICalendarLibrary library = new CalendarLibrary();
    this.controller = new CalendarController(library, new CalendarView());

    // Create default calendar on startup
    this.controller.processCommand("create calendar \"Default\" America/New_York");
    this.controller.processCommand("switch calendar \"Default\"");
  }

  /**
   * Starts the calendar application.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    CalendarApp app = new CalendarApp();

    if (args.length == 0) {
      System.out.println("Error: Must specify mode: --mode interactive, --mode headless <file>,"
              + " or --mode gui");
    } else if (args.length >= 2 && args[0].equalsIgnoreCase("--mode")) {
      if (args[1].equalsIgnoreCase("interactive")) {
        app.controller.runInteractive();
      } else if (args[1].equalsIgnoreCase("headless") && args.length >= 3) {
        app.controller.runHeadless(args[2]);
      } else if (args[1].equalsIgnoreCase("gui")) {
        // Only create GUI when mode is GUI
        CalendarGUIView gui = new CalendarGUIView();
        app.controller.setGUIView(gui);
        app.controller.runGUI();
      } else {
        System.out.println("Error: Invalid mode or missing file path.");
      }
    } else {
      System.out.println("Error: Must specify mode: --mode interactive, --mode headless <file>,"
              + " or --mode gui");
    }
  }
}
