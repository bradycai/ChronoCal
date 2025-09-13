package calendar.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import javax.swing.SwingUtilities;

import calendar.gui.ICalendarGUIView;
import calendar.gui.CalendarGUIView;
import calendar.model.Event;
import calendar.model.EventSeries;
import calendar.model.ICalendarLibrary;
import calendar.model.IEvent;
import calendar.view.ICalendarView;

/**
 * Class that represents the controller for the calendar.
 */
public class CalendarController implements ICalendarController {
  private final ICalendarLibrary library;
  private final ICalendarView view;
  private ICalendarGUIView guiView;
  private static final DateTimeFormatter DATE_TIME_FORMAT =
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  public CalendarController(ICalendarLibrary library, ICalendarView view) {
    this.library = library;
    this.view = view;
  }

  /**
   * Public entry‐point used by tests
   * It delegates to handleCommand but only catches exceptions
   * from truly unknown commands.  All other IllegalArgumentExceptions
   * are rethrown.
   *
   * @param command the raw command line from the user or test
   */
  public void processCommand(String command) {
    try {
      handleCommand(command);
    } catch (IllegalArgumentException e) {
      if (e.getMessage() != null && e.getMessage().startsWith("Unrecognized command:")) {
        System.out.println("Unknown command: " + command);
      } else {
        throw e;
      }
    }
  }


  /**
   * Runs the calendar in interactive mode, reading commands from stdin.
   */
  @Override
  public void runInteractive() {
    Scanner scanner = new Scanner(System.in);
    view.displayWelcomeMessage();
    while (true) {
      System.out.print("> ");
      String input = scanner.nextLine().trim();
      if (input.equalsIgnoreCase("exit")) {
        break;
      } else if (input.equalsIgnoreCase("help")) {
        view.displayHelp();
      } else {
        processCommand(input);
      }
    }
  }


  /**
   * Starts the interactive mode for user input in a loop.
   * Displays the welcome message and waits for user commands.
   */
  @Override
  public void runHeadless(String filePath) {
    try (Scanner scanner = new Scanner(new File(filePath))) {
      boolean exitFound = false;

      while (scanner.hasNextLine()) {
        String line = scanner.nextLine().trim();

        if (line.isEmpty()) {
          continue;
        }

        if (line.equalsIgnoreCase("exit")) {
          exitFound = true;
          break;
        }

        try {
          processCommand(line);
        } catch (IllegalArgumentException | IllegalStateException e) {
          System.out.println("Error: " + e.getMessage());
        }
      }

      if (!exitFound) {
        System.out.println("Error: missing exit command.");
      }
    } catch (FileNotFoundException e) {
      System.out.println("Error: File not found - " + filePath);
    }
  }


  /**
   * Parses a single line of input and dispatches to create/edit/print/show.
   *
   * @param command the raw command string from the user
   */
  private void handleCommand(String command) {
    if (command.isEmpty()) {
      return;
    }
    String lower = command.toLowerCase();
    if (lower.startsWith("create calendar ")) {
      if (lower.contains("--name") && lower.contains("--timezone")) {
        handleCreateCalendarWithFlags(command);
      } else {
        handleCreateCalendar(command);
      }
      return;
    }

    // Handle "use calendar" as alias for switch calendar with --name flag
    if (lower.startsWith("use calendar ")) {
      if (lower.contains("--name")) {
        handleUseCalendarWithFlags(command);
        return;
      } else {
        throw new IllegalArgumentException("Unrecognized command: " + command);
      }
    }

    if (lower.startsWith("edit calendar ")) {
      handleEditCalendar(command);
    } else if (lower.startsWith("create")) {
      handleCreateEvent(command);
    } else if (lower.startsWith("edit ") || lower.startsWith("edits")
            || lower.startsWith("edit series")) {
      handleEditEvent(command);
    } else if (lower.startsWith("print ")) {
      handlePrintEvents(command);
    } else if (lower.startsWith("show status")) {
      handleShowStatus(command);
    } else if (lower.startsWith("switch calendar ")) {
      handleSwitchCalendar(command);
    } else if (lower.startsWith("rename calendar ")) {
      handleRenameCalendar(command);
    } else if (lower.startsWith("delete calendar ")) {
      handleDeleteCalendar(command);
    } else if (lower.equals("list calendars")) {
      handleListCalendars();
    } else if (lower.startsWith("copy event ")) {
      handleCopySingleEvent(command);
    } else if (lower.startsWith("copy events on ")) {
      handleCopyEventsOnDate(command);
    } else if (lower.startsWith("copy events between ")) {
      handleCopyEventsBetweenDates(command);
    } else {
      throw new IllegalArgumentException("Unrecognized command: " + command);
    }
  }

  /**
   * Handles commands that start with "create event".
   * Figures out if it’s a timed event or all-day event.
   *
   * @param command the full "create event" command
   */
  private void handleCreateEvent(String command) {
    if (command.contains(" on ")) {
      handleCreateAllDayEvent(command);
    } else if (command.contains(" from ")) {
      handleCreateTimedEvent(command);
    } else {
      throw new IllegalArgumentException("Invalid create event command");
    }
  }

  /**
   * Creates an all-day event from 8:00 to 17:00.
   * Can also create recurring all-day events.
   *
   * @param command the input string for creating an all-day event
   */
  private void handlePrintEvents(String command) {
    ZoneId currentTimezone = library.getActiveCalendar().getTimezone();
    List<String> formattedOutput = new ArrayList<>();

    if (command.contains(" on ")) {
      String dateStr = command.substring(command.indexOf(" on ") + 4).trim();
      LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);
      List<IEvent> events = library.getActiveCalendar().getEventsOnDate(date);

      if (events.isEmpty()) {
        formattedOutput.add("No events on " + dateStr);
      } else {
        for (IEvent e : events) {
          ZoneId creationTimezone = library.getActiveCalendar().getCreationTimezone();

          ZonedDateTime originalStart = e.getStart().atZone(creationTimezone);
          ZonedDateTime zonedStart = originalStart.withZoneSameInstant(currentTimezone);

          ZonedDateTime originalEnd = e.getEnd().atZone(creationTimezone);
          ZonedDateTime zonedEnd = originalEnd.withZoneSameInstant(currentTimezone);

          String eventStr = "• \"" + e.getSubject() + "\" ("
                  + zonedStart.toLocalDate() + " "
                  + String.format("%02d:%02d", zonedStart.getHour(), zonedStart.getMinute())
                  + " - "
                  + String.format("%02d:%02d", zonedEnd.getHour(), zonedEnd.getMinute())
                  + ")";
          formattedOutput.add(eventStr);
        }
      }
    }

    view.displayFormattedEvents(formattedOutput);
  }


  /**
   * Creates a timed event with specific start and end times.
   * Can also handle recurring timed events.
   *
   * @param command the input string for creating a timed event
   */
  private void handleCreateTimedEvent(String command) {
    String remaining = command.substring("create event ".length());
    String quoted = extractQuotedSubjectRaw(remaining);
    String subject = quoted.startsWith("\"") ? quoted.substring(1, quoted.length() - 1) : quoted;
    remaining = remaining.substring(quoted.length()).trim();


    if (!remaining.startsWith("from ")) {
      throw new IllegalArgumentException("Missing 'from' in timed event creation");
    }

    String[] parts = remaining.substring(5).split(" to ");
    if (parts.length < 2) {
      throw new IllegalArgumentException("Missing 'to' in timed event creation");
    }

    LocalDateTime start = parseDateTime(parts[0].trim());
    String endPart = parts[1].trim();

    String[] endParts = endPart.split(" repeats ", 2);
    LocalDateTime end = parseDateTime(endParts[0].trim());

    if (endParts.length > 1) {
      handleRecurringTimedEvent(subject, start, end, endParts[1]);
    } else {
      Event event = new Event(subject, start, end);
      if (library.getActiveCalendar().hasConflict(event)) {
        throw new IllegalArgumentException("Cannot create duplicate event.");
      }
      library.getActiveCalendar().addEvent(event);
      if (guiView != null) {
        guiView.showEvents(List.of(event));
      } else {
        System.out.println("Created timed event: \"" + subject + "\"");
      }

    }
  }

  private String extractQuotedSubjectRaw(String input) {
    if (input.startsWith("\"")) {
      int endQuote = input.indexOf("\"", 1);
      if (endQuote == -1) {
        throw new IllegalArgumentException("Unclosed quote in subject");
      }
      return input.substring(0, endQuote + 1);
    } else {
      int space = input.indexOf(" ");
      return (space == -1) ? input : input.substring(0, space);
    }
  }


  /**
   * Handles the availability status.
   *
   * @param command the full "show status" command with datetime
   */
  private void handleShowStatus(String command) {
    try {
      String dateTimeStr = command.substring(command.indexOf(" on ") + 4).trim();
      LocalDateTime dateTime = parseDateTime(dateTimeStr);
      boolean isBusy = library.getActiveCalendar().isBusy(dateTime);
      if (isBusy) {
        System.out.println("busy");
      } else {
        System.out.println("available");
      }
    } catch (DateTimeParseException e) {
      System.out.println("Error: Invalid date/time format. Use YYYY-MM-DDThh:mm");
    }
  }

  /**
   * Handles recurring timed events.
   */
  private void handleRecurringTimedEvent(String subject, LocalDateTime start,
                                         LocalDateTime end, String commandPart) {
    String[] repeatParts = commandPart.trim().split(" ");

    if (repeatParts.length < 3) {
      throw new IllegalArgumentException("Invalid recurring event format. "
              + "Expected: <weekdays> for/until <value>");
    }

    String weekdayStr = repeatParts[0].toUpperCase();
    Set<DayOfWeek> days = parseWeekdays(weekdayStr);

    if (repeatParts[1].equals("for")) {
      handleRecurringEventHelper(subject, start, end, repeatParts, days);
      System.out.println("Created recurring timed event series: \"" + subject + "\"");
    } else if (repeatParts[1].equals("until")) {
      handleRecurringEventSeriesHelper(subject, start, end, repeatParts, days);
      System.out.println("Created recurring timed event series: \"" + subject + "\"");
    } else {
      throw new IllegalArgumentException("Invalid recurring event specification. "
              + "Must include 'for' or 'until'.");
    }
  }

  private static class EditCommand {
    String editType;
    String property;
    String subject;
    LocalDateTime fromDateTime;
    String newValue;
  }

  private EditCommand parseEditCommand(String command) {
    String[] parts = command.split(" ");
    if (parts.length < 4) {
      throw new IllegalArgumentException("Invalid edit command. "
              + "Expected: <edit|edits|edit series> <property> <subject> "
              + "from <start> with <newValue>");
    }

    EditCommand result = new EditCommand();

    // Determine edit type
    if (parts[0].equalsIgnoreCase("edits")) {
      result.editType = "edits";
    } else if (parts[0].equalsIgnoreCase("edit") && parts[1].equalsIgnoreCase("series")) {
      result.editType = "edit series";
    } else if (parts[0].equalsIgnoreCase("edit")) {
      result.editType = "edit";
    } else {
      throw new IllegalArgumentException("Invalid edit type: " + parts[0]);
    }

    int idx = (result.editType.equals("edit series")) ? 2 : 1;

    // Property
    result.property = parts[idx].toLowerCase();
    if (!result.property.matches("subject|start|end|location|description|status")) {
      throw new IllegalArgumentException("Invalid property: " + result.property);
    }
    idx++;

    // Subject
    if (parts[idx].startsWith("\"")) {
      StringBuilder sbSubject = new StringBuilder();
      String piece = parts[idx];
      if (piece.endsWith("\"") && piece.length() > 1) {
        sbSubject.append(piece, 1, piece.length() - 1);
      } else {
        sbSubject.append(piece.substring(1));
        idx++;
        while (idx < parts.length && !parts[idx].endsWith("\"")) {
          sbSubject.append(" ").append(parts[idx]);
          idx++;
        }
        if (idx >= parts.length) {
          throw new IllegalArgumentException("Missing closing quote for subject.");
        }
        piece = parts[idx];
        sbSubject.append(" ").append(piece, 0, piece.length() - 1);
      }
      result.subject = sbSubject.toString();
      idx++;
    } else {
      result.subject = parts[idx];
      idx++;
    }

    if (idx >= parts.length || !parts[idx].equalsIgnoreCase("from")) {
      throw new IllegalArgumentException("Missing 'from' keyword in edit command.");
    }
    idx++;

    if (idx >= parts.length) {
      throw new IllegalArgumentException("Missing start date/time in edit command.");
    }
    try {
      result.fromDateTime = LocalDateTime.parse(parts[idx], DATE_TIME_FORMAT);
    } catch (DateTimeParseException ex) {
      throw new IllegalArgumentException("Invalid date/time format; expected YYYY-MM-DDThh:mm");
    }
    idx++;

    if (idx >= parts.length || !parts[idx].equalsIgnoreCase("with")) {
      throw new IllegalArgumentException("Missing 'with' keyword in edit command.");
    }
    idx++;

    StringBuilder sbValue = new StringBuilder();
    for (int i = idx; i < parts.length; i++) {
      sbValue.append(parts[i]);
      if (i < parts.length - 1) {
        sbValue.append(" ");
      }
    }
    result.newValue = sbValue.toString();

    return result;
  }


  /**
   * Handles edit commands like editing a single event, future events, or an entire series.
   *
   * @param command the user's edit command
   */
  private void handleEditEvent(String command) {
    System.out.println("DEBUG: Timezone-aware handlePrintEvents() called.");
    EditCommand editCmd = parseEditCommand(command);

    IEvent matchingEvent = library.getActiveCalendar().findEvent(editCmd.subject,
            editCmd.fromDateTime);
    if (matchingEvent == null) {
      view.displayError("Error: Event not found.");
      return;
    }

    switch (editCmd.editType) {
      case "edit":
        handleEditSingleEvent(matchingEvent, editCmd.property, editCmd.newValue);
        break;
      case "edits":
        handleEditFutureEvents(matchingEvent, editCmd.property, editCmd.newValue);
        break;
      case "edit series":
        handleEditWholeSeries(matchingEvent, editCmd.property, editCmd.newValue);
        break;
      default:
        throw new IllegalStateException("Unexpected edit type.");
    }
  }


  /**
   * Edits exactly one occurrence (the matchingEvent). If this event is part of a series,
   * other occurrences in that series are unaffected.
   *
   * @param event    the IEvent to change (actually an Event)
   * @param property which property to modify (subject, start, etc.)
   * @param newValue new value for the property
   */
  private void handleEditSingleEvent(IEvent event, String property, String newValue) {
    boolean success = library.getActiveCalendar().editSingleEvent(event, property, newValue,
            DATE_TIME_FORMAT);
    if (success) {
      System.out.println("Edited single event.");
    } else {
      System.out.println("Error: Cannot edit event due to a scheduling conflict.");
    }
  }

  /**
   * Edits this occurrence and all future occurrences in the same series. If the base
   * event is not part of any series (seriesId == null), behaves exactly like editSingleEvent().
   *
   * @param event    the IEvent in the series to start from
   * @param property which property to modify
   * @param newValue new value for the property
   */
  private void handleEditFutureEvents(IEvent event, String property, String newValue) {
    int count = library.getActiveCalendar().editFutureEvents(event, property, newValue,
            DATE_TIME_FORMAT);
    System.out.println("Modified " + count + " future event(s) in the series.");
  }

  /**
   * Edits every occurrence in the same series.
   *
   * @param event    the IEvent in the series
   * @param property which property to modify
   * @param newValue new value for the property
   */
  private void handleEditWholeSeries(IEvent event, String property, String newValue) {
    int count = library.getActiveCalendar().editWholeSeries(event, property, newValue,
            DATE_TIME_FORMAT);
    System.out.println("Modified " + count + " event(s) in the entire series.");
  }

  /**
   * Helper to parse a date-time string..
   *
   * @param s the date-time text
   * @return the parsed LocalDateTime
   * @throws DateTimeParseException if the format is invalid
   */
  private LocalDateTime parseDateTime(String s) {
    return LocalDateTime.parse(s, DATE_TIME_FORMAT);
  }

  /**
   * Creates an all-day event from 8:00 to 17:00.
   * Can also create recurring all-day events.
   *
   * @param command the input string for creating an all-day event
   */
  private void handleCreateAllDayEvent(String command) {
    String remaining = command.substring("create event ".length());
    String quoted = extractQuotedSubjectRaw(remaining);
    String subject = quoted.substring(1, quoted.length() - 1);
    remaining = remaining.substring(quoted.length()).trim();

    if (!remaining.startsWith("on ")) {
      throw new IllegalArgumentException("Missing 'on' in all-day event creation");
    }

    remaining = remaining.substring(3).trim();

    String[] parts = remaining.split(" repeats ", 2);
    String dateStr = parts[0].trim();
    LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);

    LocalDateTime start = date.atTime(8, 0);
    LocalDateTime end = date.atTime(17, 0);

    if (parts.length > 1) {
      handleRecurringAllDayEvent(subject, start, end, parts[1]);
    } else {
      Event event = new Event(subject, start, end);
      if (library.getActiveCalendar().hasConflict(event)) {
        throw new IllegalArgumentException("Cannot create duplicate event.");
      }
      library.getActiveCalendar().addEvent(event);
      System.out.println("Created all-day event: \"" + subject + "\"");
    }
  }

  /**
   * Handles the recurring all-day events based on
   * the given subject, start/end time, and repetition.
   *
   * @param subject     the event subject
   * @param start       the start time of the event
   * @param end         the end time of the event
   * @param commandPart the repeat specification string
   */
  private void handleRecurringAllDayEvent(String subject, LocalDateTime start,
                                          LocalDateTime end, String commandPart) {
    String[] repeatParts = commandPart.trim().split(" ");

    if (repeatParts.length < 3) {
      throw new IllegalArgumentException("Invalid recurring event format. "
              + "Expected: <weekdays> for/until <value>");
    }

    Set<DayOfWeek> days = parseWeekdays(repeatParts[0].toUpperCase());

    if (repeatParts[1].equals("for")) {
      handleRecurringEventHelper(subject, start, end, repeatParts, days);
      System.out.println("Created recurring all-day event series: \"" + subject + "\"");
    } else if (repeatParts[1].equals("until")) {
      handleRecurringEventSeriesHelper(subject, start, end, repeatParts, days);
      System.out.println("Created recurring all-day event series: \"" + subject + "\"");
    } else {
      throw new IllegalArgumentException("Invalid recurring event specification. "
              + "Must include 'for' or 'until'.");
    }
  }

  /**
   * Helper that creates recurring events for a certain number of times.
   */
  private void handleRecurringEventSeriesHelper(String subject, LocalDateTime start,
                                                LocalDateTime end,
                                                String[] repeatParts, Set<DayOfWeek> days) {
    LocalDate untilDate = LocalDate.parse(repeatParts[2], DateTimeFormatter.ISO_DATE);
    EventSeries series = new EventSeries(subject, start, end, days, untilDate);
    List<Event> events = series.getEvents();
    for (Event event : events) {
      library.getActiveCalendar().addEvent(event);
    }
  }

  /**
   * Helper that creates recurring events that go until a specific date.
   */
  private void handleRecurringEventHelper(String subject, LocalDateTime start, LocalDateTime end,
                                          String[] repeatParts, Set<DayOfWeek> days) {
    int count = Integer.parseInt(repeatParts[2]);
    EventSeries series = new EventSeries(subject, start, end, days, count);
    List<Event> events = series.getEvents();
    for (Event event : events) {
      library.getActiveCalendar().addEvent(event);
    }
  }

  /**
   * Converts weekday letters M = Monday into actual DayOfWeek values.
   *
   * @param weekdayStr the string with weekday letters
   * @return a set of days of the week
   */
  private Set<DayOfWeek> parseWeekdays(String weekdayStr) {
    Set<DayOfWeek> days = new HashSet<>();
    for (char c : weekdayStr.toCharArray()) {
      switch (c) {
        case 'M':
          days.add(DayOfWeek.MONDAY);
          break;
        case 'T':
          days.add(DayOfWeek.TUESDAY);
          break;
        case 'W':
          days.add(DayOfWeek.WEDNESDAY);
          break;
        case 'R':
          days.add(DayOfWeek.THURSDAY);
          break;
        case 'F':
          days.add(DayOfWeek.FRIDAY);
          break;
        case 'S':
          days.add(DayOfWeek.SATURDAY);
          break;
        case 'U':
          days.add(DayOfWeek.SUNDAY);
          break;
        default:
          throw new IllegalArgumentException("Invalid weekday character: " + c);
      }
    }
    return days;
  }

  /**
   * Handles the "create calendar" command.
   * Extracts the calendar name and timezone from the command string,
   * creates a new calendar in the library, and sets its timezone.
   *
   * @param command the full user command string
   * @throws IllegalArgumentException if the command format is invalid
   */
  private void handleCreateCalendar(String command) {
    String rest = command.substring("create calendar ".length()).trim();
    int space = rest.lastIndexOf(" ");
    if (space == -1) {
      throw new IllegalArgumentException("Invalid format. Use: create calendar \"name\" <ZoneId>");
    }

    String name = rest.substring(0, space).trim();
    if (name.startsWith("\"") && name.endsWith("\"")) {
      name = name.substring(1, name.length() - 1);
    }

    String zoneId = rest.substring(space + 1).trim();
    library.createCalendar(name, zoneId);
    System.out.println("Created calendar \"" + name + "\" with timezone " + zoneId);
  }

  private void handleCreateCalendarWithFlags(String command) {
    String[] tokens = command.split("\\s+");
    String name = null;
    String timezone = null;
    for (int i = 0; i < tokens.length; i++) {
      if (tokens[i].equalsIgnoreCase("--name") && i + 1 < tokens.length) {
        name = tokens[i + 1];
      }
      if (tokens[i].equalsIgnoreCase("--timezone") && i + 1 < tokens.length) {
        timezone = tokens[i + 1];
      }
    }
    if (name != null && timezone != null) {
      library.createCalendar(name, timezone);
      view.displayMessage("Created calendar \"" + name + "\" with timezone " + timezone);
    } else {
      view.displayError("Missing --name or --timezone argument.");
    }
  }

  private void handleUseCalendarWithFlags(String command) {
    String[] tokens = command.split("\\s+");
    String name = null;
    for (int i = 0; i < tokens.length; i++) {
      if (tokens[i].equalsIgnoreCase("--name") && i + 1 < tokens.length) {
        name = tokens[i + 1];
      }
    }
    if (name != null) {
      library.useCalendar(name);
      view.displayMessage("Switched to calendar \"" + name + "\"");
    } else {
      view.displayError("Missing --name argument.");
    }
  }


  /**
   * Handles the "switch calendar" command.
   * Extracts the calendar name and sets it as the active calendar.
   *
   * @param command the full user command string
   */
  private void handleSwitchCalendar(String command) {
    String name = command.substring("switch calendar ".length()).trim();
    if (name.startsWith("\"") && name.endsWith("\"")) {
      name = name.substring(1, name.length() - 1);
    }

    library.useCalendar(name);
    System.out.println("Switched to calendar \"" + name + "\"");
  }

  /**
   * Handles the "rename calendar" command.
   * Parses the old and new calendar names and updates the calendar's name.
   *
   * @param command the full user command string
   * @throws IllegalArgumentException if the command does not contain "to"
   */
  private void handleRenameCalendar(String command) {
    String rest = command.substring("rename calendar ".length()).trim();
    if (!rest.contains(" to ")) {
      throw new IllegalArgumentException("Missing 'to' in rename command");
    }

    String[] parts = rest.split(" to ");
    String oldName = parts[0].trim();
    String newName = parts[1].trim();

    if (oldName.startsWith("\"") && oldName.endsWith("\"")) {
      oldName = oldName.substring(1, oldName.length() - 1);
    }
    if (newName.startsWith("\"") && newName.endsWith("\"")) {
      newName = newName.substring(1, newName.length() - 1);
    }

    library.editCalendar(oldName, "name", newName);
    System.out.println("Renamed calendar \"" + oldName + "\" to \"" + newName + "\"");
  }

  private void handleEditCalendar(String command) {
    String[] parts = command.trim().split(" ");
    if (parts.length < 5) {
      throw new IllegalArgumentException("Invalid calendar edit command."
              + " Use: edit calendar <name> <property> <newValue>");
    }

    String name = parts[2];
    String property = parts[3];
    String newValue = String.join(" ", java.util.Arrays.copyOfRange(parts, 4, parts.length));

    library.editCalendar(name, property, newValue);
    System.out.println("Calendar \"" + name + "\" updated: " + property + " = " + newValue);
  }


  /**
   * Handles the "delete calendar" command.
   * Extracts the calendar name and removes it from the library.
   *
   * @param command the full user command string
   */
  private void handleDeleteCalendar(String command) {
    String name = command.substring("delete calendar ".length()).trim();
    if (name.startsWith("\"") && name.endsWith("\"")) {
      name = name.substring(1, name.length() - 1);
    }

    library.deleteCalendar(name);
    System.out.println("Deleted calendar \"" + name + "\"");
  }

  /**
   * Displays all existing calendar names.
   * Marks the currently active calendar with an asterisk.
   * If no calendars exist, prints a message.
   */
  private void handleListCalendars() {
    Set<String> names = library.listCalendars();
    if (names.isEmpty()) {
      System.out.println("No calendars exist.");
      return;
    }

    System.out.println("Calendars:");
    for (String name : names) {
      if (name.equals(library.getCurrentCalendarName())) {
        System.out.println("* " + name + " (active)");
      } else {
        System.out.println("  " + name);
      }
    }
  }

  // COPY HANDLERS

  private void handleCopySingleEvent(String command) {
    try {
      int onIdx = command.indexOf(" on ");
      int targetIdx = command.indexOf(" --target ");
      int toIdx = command.indexOf(" to ");

      if (onIdx == -1 || targetIdx == -1 || toIdx == -1) {
        throw new IllegalArgumentException("Invalid copy event syntax.");
      }

      String subject = command.substring("copy event ".length(), onIdx).trim();
      if (subject.startsWith("\"") && subject.endsWith("\"")) {
        subject = subject.substring(1, subject.length() - 1);
      }

      String sourceStartStr = command.substring(onIdx + 4, targetIdx).trim();
      LocalDateTime sourceStart = parseDateTime(sourceStartStr);

      String targetCal = command.substring(targetIdx + 10, toIdx).trim();
      String destStr = command.substring(toIdx + 4).trim();
      LocalDateTime dest = parseDateTime(destStr);

      boolean success = library.copyEventToCalendar(subject, sourceStart, targetCal, dest);
      if (success) {
        System.out.println("Event copied successfully.");
      } else {
        System.out.println("Error: Event not found or conflict in target calendar.");
      }
    } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
    }
  }

  private void handleCopyEventsOnDate(String command) {
    try {

      String[] tokens = command.split("\\s+");
      LocalDate sourceDate = null;
      String targetCalendar = null;
      LocalDate destinationDate = null;

      for (int i = 0; i < tokens.length; i++) {
        if (tokens[i].equalsIgnoreCase("on") && i + 1 < tokens.length) {
          sourceDate = LocalDate.parse(tokens[i + 1]);
        }
        if (tokens[i].equalsIgnoreCase("--target") && i + 1 < tokens.length) {
          targetCalendar = tokens[i + 1];
        }
        if (tokens[i].equalsIgnoreCase("to") && i + 1 < tokens.length) {
          destinationDate = LocalDate.parse(tokens[i + 1]);
        }
      }

      if (sourceDate == null || targetCalendar == null || destinationDate == null) {
        throw new IllegalArgumentException("Missing required arguments for "
                + "copy events on command.");
      }

      int count = library.copyEventsOnDateToCalendar(sourceDate, targetCalendar, destinationDate);
      System.out.println("Copied " + count + " event(s).");
    } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
    }
  }

  private void handleCopyEventsBetweenDates(String command) {
    try {
      String[] tokens = command.split("\\s+");
      LocalDate startDate = null;
      LocalDate endDate = null;
      String sourceCalendar = null;
      String targetCalendar = null;
      LocalDate destinationStartDate = null;

      for (int i = 0; i < tokens.length; i++) {
        if (tokens[i].equalsIgnoreCase("between") && i + 1 < tokens.length) {
          startDate = LocalDate.parse(tokens[i + 1]);
        }
        if (tokens[i].equalsIgnoreCase("and") && i + 1 < tokens.length) {
          endDate = LocalDate.parse(tokens[i + 1]);
        }
        if (tokens[i].equalsIgnoreCase("--source") && i + 1 < tokens.length) {
          sourceCalendar = tokens[i + 1];
        }
        if (tokens[i].equalsIgnoreCase("--target") && i + 1 < tokens.length) {
          targetCalendar = tokens[i + 1];
        }
        if (tokens[i].equalsIgnoreCase("to") && i + 1 < tokens.length) {
          destinationStartDate = LocalDate.parse(tokens[i + 1]);
        }
      }

      if (startDate == null || endDate == null || sourceCalendar == null
              || targetCalendar == null || destinationStartDate == null) {
        throw new IllegalArgumentException("Missing required arguments for"
                + " copy events between command.");
      }

      int count = library.copyEventsBetweenDatesToCalendar(
              sourceCalendar, targetCalendar, startDate, endDate, destinationStartDate);
      System.out.println("Copied " + count + " event(s).");
    } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
    }
  }

  /**
   * Sets the GUI view for the calendar application.
   *
   * @param gui the GUI view to use
   */
  public void setGUIView(CalendarGUIView gui) {
    this.guiView = gui;
  }

  /**
   * Launches the user interface for the calendar application.
   * This is the creation of the GUI on the Swing event-dispatch thread.
   */
  @Override
  public void runGUI() {
    SwingUtilities.invokeLater(() -> {
      CalendarGUIView gui = new CalendarGUIView();
      setGUIView(gui);
      gui.setVisible(true);
    });
  }

  /**
   * Gets a list of events for the specified date from the active calendar.
   * Converts IEvent objects to Event objects before returning.
   *
   * @param date the date where the events are
   * @return a list of events on that date
   */
  @Override
  public List<Event> getEventsOnDate(LocalDate date) {
    List<IEvent> events = library.getActiveCalendar().getEventsOnDate(date);
    List<Event> result = new ArrayList<>();
    for (IEvent e : events) {
      result.add((Event) e);
    }
    return result;
  }
}