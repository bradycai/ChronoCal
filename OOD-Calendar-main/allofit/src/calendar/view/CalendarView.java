package calendar.view;

import java.util.List;

/**
 * This class handles all user outputs in the calendar.
 * It displays welcome messages, help menus, and formatted event details.
 */
public class CalendarView implements ICalendarView {

  /**
   * Displays the welcome message.
   */
  public void displayWelcomeMessage() {
    System.out.println("Welcome to Calendar Application");
    System.out.println("Type 'help' to see available commands");
  }

  /**
   * Displays a list of all available commands for creating, editing,
   * and managing events.
   */
  public void displayHelp() {
    System.out.println("\nAvailable Commands:");
    System.out.println("CREATE EVENTS:");
    System.out.println("  create event <subject> from <start> to <end>");
    System.out.println(
            "  create event <subject> from <start> to <end> repeats <weekdays> for <N> times");
    System.out.println(
            "  create event <subject> from <start> to <end> repeats <weekdays> until <date>");
    System.out.println("  create event <subject> on <date>");
    System.out.println("  create event <subject> on <date> repeats <weekdays> for <N> times");
    System.out.println("  create event <subject> on <date> repeats <weekdays> until <date>");

    System.out.println("\nEDIT EVENTS:");
    System.out.println("  edit event <property> <subject> from <start> to <end> with <newValue>");
    System.out.println("  edit events <property> <subject> from <start> with <newValue>");
    System.out.println("  edit series <property> <subject> from <start> with <newValue>");

    System.out.println("\nQUERIES:");
    System.out.println("  print events on <date>");
    System.out.println("  print events from <start> to <end>");
    System.out.println("  show status on <dateTime>");

    System.out.println("\nOTHER:");
    System.out.println("  help - Show this help message");
    System.out.println("  exit - Exit the application");
  }

  @Override
  public void displayMessage(String message) {
    System.out.println(message);
  }

  @Override
  public void displayError(String error) {
    System.out.println("Error: " + error);
  }

  @Override
  public void displayFormattedEvents(List<String> eventStrings) {
    for (String s : eventStrings) {
      System.out.println(s);
    }
  }
}
