package calendar.gui;

import calendar.model.IEvent;

import javax.swing.JTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.border.TitledBorder;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * The main graphical user interface view for the Calendar application using Java Swing.
 * Provides input fields for creating events, viewing events for a specific date,
 * and editing existing events.
 */
public class CalendarGUIView extends JFrame implements ICalendarGUIView {

  private ICalendarGUIController controller;
  private final JTextField dateField = new JTextField(10);
  private final JTextField subjectField = new JTextField(20);
  private final JTextField startField = new JTextField(20);
  private final JTextField endField = new JTextField(20);
  private final DefaultListModel<String> eventListModel = new DefaultListModel<>();
  private final JList<String> eventList = new JList<>(eventListModel);
  private List<IEvent> lastShownEvents;

  /**
   * Constructs the Calendar GUI view.
   * Builds all GUI components and layouts.
   */
  public CalendarGUIView() {
    super("Calendar Application");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout());

    JPanel inputPanel = new JPanel(new GridLayout(5, 2, 5, 5));
    inputPanel.setBorder(new TitledBorder("Create Event"));

    inputPanel.add(new JLabel("Subject:"));
    inputPanel.add(subjectField);
    inputPanel.add(new JLabel("Start (yyyy-MM-dd'T'HH:mm):"));
    inputPanel.add(startField);
    inputPanel.add(new JLabel("End (yyyy-MM-dd'T'HH:mm):"));
    inputPanel.add(endField);

    JButton createButton = new JButton("Create Event");
    JButton clearButton = new JButton("Clear Fields");
    inputPanel.add(createButton);
    inputPanel.add(clearButton);

    JPanel topPanel = new JPanel();
    topPanel.add(new JLabel("Date (yyyy-MM-dd):"));
    topPanel.add(dateField);
    JButton viewButton = new JButton("View Schedule");
    topPanel.add(viewButton);

    JScrollPane scrollPane = new JScrollPane(eventList);
    scrollPane.setBorder(new TitledBorder("Schedule View"));

    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.add(inputPanel, BorderLayout.NORTH);
    centerPanel.add(scrollPane, BorderLayout.CENTER);
    JButton editButton = new JButton("Edit Selected Event");
    centerPanel.add(editButton, BorderLayout.SOUTH);

    add(topPanel, BorderLayout.NORTH);
    add(centerPanel, BorderLayout.CENTER);

    pack();
    setLocationRelativeTo(null);
    setVisible(true);

    // Button Listeners
    createButton.addActionListener(e -> handleCreateEvent());
    clearButton.addActionListener(e -> clearFields());
    viewButton.addActionListener(e -> handleViewSchedule());
    editButton.addActionListener(e -> handleEditEvent());
  }

  /**
   * Handles the create event button click.
   * Gathers user input and sends it to the controller to create the event.
   */
  private void handleCreateEvent() {
    String subject = subjectField.getText().trim();
    String start = startField.getText().trim();
    String end = endField.getText().trim();
    controller.createEvent(subject, start, end);
  }

  /**
   * Handles the view schedule button click.
   * Requests events for the entered date from the controller.
   */
  private void handleViewSchedule() {
    String date = dateField.getText().trim();
    controller.loadEventsFromDate(date);
  }

  /**
   * Clears all input fields in the create event form.
   */
  private void clearFields() {
    subjectField.setText("");
    startField.setText("");
    endField.setText("");
  }

  /**
   * Handles edit event button click.
   * Opens the edit dialog for the selected event in the schedule view.
   */
  private void handleEditEvent() {
    int index = eventList.getSelectedIndex();
    if (index == -1 || lastShownEvents == null || index >= lastShownEvents.size()) {
      showError("Please select an event to edit.");
      return;
    }
    IEvent selectedEvent = lastShownEvents.get(index);
    openEditDialog(selectedEvent);
  }

  /**
   * Opens a dialog window allowing the user to edit the selected event's details.
   * @param event the event to edit
   */
  private void openEditDialog(IEvent event) {
    JTextField subjectEdit = new JTextField(event.getSubject());
    JTextField startEdit = new JTextField(event.getStart().toString().replace('T',' '));
    JTextField endEdit = new JTextField(event.getEnd().toString().replace('T',' '));

    JPanel panel = new JPanel(new GridLayout(3, 2));
    panel.add(new JLabel("Subject:"));
    panel.add(subjectEdit);
    panel.add(new JLabel("Start (yyyy-MM-dd HH:mm):"));
    panel.add(startEdit);
    panel.add(new JLabel("End (yyyy-MM-dd HH:mm):"));
    panel.add(endEdit);

    int result = JOptionPane.showConfirmDialog(this, panel,
            "Edit Event", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

    if (result == JOptionPane.OK_OPTION) {
      String newSubject = subjectEdit.getText().trim();
      String newStartStr = startEdit.getText().trim().replace(' ', 'T');
      String newEndStr = endEdit.getText().trim().replace(' ', 'T');
      controller.editEvent(event, newSubject, newStartStr, newEndStr);
    }
  }

  /**
   * Sets the controller for this view.
   * @param controller the controller to use
   */
  @Override
  public void setController(ICalendarGUIController controller) {
    this.controller = controller;
  }

  /**
   * Displays the list of events retrieved from the controller.
   * @param events the list of events to display
   */
  @Override
  public void showEvents(List<IEvent> events) {
    eventListModel.clear();
    this.lastShownEvents = events;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    for (IEvent event : events) {
      eventListModel.addElement(
              String.format("%s: %s to %s",
                      event.getSubject(),
                      event.getStart().format(formatter),
                      event.getEnd().format(formatter))
      );
    }
  }

  /**
   * Displays an error message to the user.
   * @param message the error message
   */
  @Override
  public void showError(String message) {
    JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
  }
}
