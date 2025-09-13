Calendar Application (Assignment 5, Part 2)

1. How to Run

Interactive Mode:
Run in interactive mode:

```
--mode interactive
```

Headless Mode:
Run headless mode with a command script:

```
--mode headless res/valid_commands.txt
```

2. Features Implemented

### Multi-Calendar Support

✅	Create multiple calendars with unique names and timezones
✅  Updated ICalendarLibrary and ICalendarModel to include support for multi-calendar management.
✅	Switch between calendars (`switch calendar <name>`)
✅	Edit calendar name or timezone
✅	Delete calendars, list existing calendars

### Timezone Handling

✅	Each calendar has its own `ZoneId` timezone
✅	Times are interpreted in that calendar’s timezone
✅	Timezone conversion applied during event copying

### Event Creation

✅	All-day events (default 08:00–17:00)
✅	Timed events with explicit start/end
✅	Recurring events (count- and until-based), with day-of-week control
✅	Conflict detection prevents duplicates

### Editing Events

✅	Edit individual events (subject, start, end, location, description, status)
✅	Edit future events in a series (`edits`)
✅	Edit entire series (`edit series`)

### Querying

✅	`print events on <date>` → bullet list with details
✅	`print events from <start> to <end>` → time range query
✅	`show status on <dateTime>` → busy / available

### Copying Events Across Calendars

✅	`copy event "Subject" on <datetime> --target <calendar> to <datetime>`
✅	`copy events on <date> --target <calendar> to <date>` (timezone converted)
✅	`copy events between <start> and <end> --target <calendar> to <datetime>`
✅	Partial event series are copied with `seriesId` retained
✅	Conflict detection applied in destination calendar

### Command Syntax & Error Handling

✅	Unknown commands report errors
✅	Invalid formats (e.g., date/time, missing keywords) throw clear errors
✅	Missing `exit` in headless mode prints a message and quits
✅	Interactive and headless modes both supported

### Design Changes and Justifications
1. Added CalendarGUIView Class 
Change: Created a new CalendarGUIView class implementing a fully functional Java Swing graphical
interface.
Justification: This provides the required GUI mode for event creation, schedule viewing, and
interaction. Follows MVC separation by cleanly isolating the view from controller and model layers.

2. Introduced ICalendarGUIView Interface
Change: Added an interface ICalendarGUIView to decouple the controller from the concrete GUI view
class.
Justification: Enables proper MVC separation of concerns, allows easy unit testing of controllers
without reliance on actual Swing components, and improves code flexibility and maintainability.

3. Updated CalendarGUIController to depend on interfaces
Change: Modified CalendarGUIController to depend on the ICalendarGUIView interface rather
than the concrete CalendarGUIView class.
Justification: Follows SOLID principles, particularly Dependency Inversion Principle, allowing
unit tests to substitute dummy or stub views while testing controller logic. Fully enables
controller-level unit tests for GUI logic.

4. Added GUI Support for Editing Events
Change: Added a new "Edit Selected Event" button in the GUI, allowing users to select an existing
event, open an edit dialog, and modify the event’s subject, start, and end times.
Justification: Satisfies extra credit feature requirements. Improves GUI usability by enabling
full event lifecycle management directly through the interface.

5. Extended Controller Interface ICalendarGUIController with editEvent Method
Change: Added editEvent(IEvent event, String newSubject, String newStart, String newEnd) to 
ICalendarGUIController.
Justification: Supports event editing initiated by the GUI, preserves proper separation of
responsibilities between view and controller. 

6. Added getEvents() Method to Model
Change: Added a new getEvents() method to the model to allow test cases to verify model state
after controller actions.
Justification: Facilitates controller-level unit testing by providing direct access to model state
for test verification.

7. Introduced Full JUnit 4 Test Class for GUI Controller
Change: Added CalendarGUIControllerTest that tests createEvent, loadEventsFromDate, and editEvent
using the real model and real controller logic.
Justification: Ensures correctness of controller behavior in reaction to GUI actions, exactly as 
required by assignment testing guidelines. Fulfills testing requirements for the GUI controller
without directly testing GUI widgets.


3. Team Contributions

* **Suleman Sheikh (50%)**

    * Designed and implemented core model classes:
    * Wrote model logic for editing single/future/series events
    * Implemented `CalendarView` and `ICalendarView`, `Event`,
    * `EventSeries`, `IEvent`, `SingleEvent`
       * Created `CalendarLibrary` for managing multiple calendars and timezones
       * Implemented `copyEventToCalendar`, `copyEventsOnDateToCalendar`,
       * `copyEventsBetweenDatesToCalendar`
       * Developed full controller support for event creation, editing, and copy commands
       * Wrote unit tests:

        * `EventTest.java`, `EventSeriesTest.java`, `CalendarControllerTest.java`
       * Authored `res/` files:

        * `valid_commands.txt`, `invalid_commands.txt`, `no_exit_commands.txt`

* **Brady Cai (50%)**

    * Built `CalendarModel` (event storage, query, conflict detection)
    * Created `CalendarLibrary` for managing multiple calendars and timezones
    * Implemented `copyEventToCalendar`, `copyEventsOnDateToCalendar`,
    * `copyEventsBetweenDatesToCalendar`
    * Extended controller parsing for `edit`, `edits`, `edit series`
    * Created JUnit tests:

        * `CalendarModelTest.java`, `CalendarAppTest.java`, `CalendarViewTest.java`
      * Drafted and finalized `README.md`, documentation, and error examples

4. Notes for Graders

* `weekday` codes are parsed using `Set<DayOfWeek>` with single-letter format: M, T, W, R, F, S, U
* Timezone format uses strings like: `America/New_York`, `Europe/London`, etc.
* All-day events default to 08:00–17:00 in the calendar’s timezone
* Timezone conversion occurs during event copy (via `ZonedDateTime.withZoneSameInstant`)
* Edge cases (missing keywords, duplicate calendars, invalid dates) handled gracefully
* `res/valid_commands.txt` contains a full working script for grading
* `res/invalid_commands.txt` and `res/no_exit_commands.txt` demonstrate error handling and missing-exit behavior
