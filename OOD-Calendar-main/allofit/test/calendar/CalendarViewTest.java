package calendar;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import calendar.view.CalendarView;

import static org.junit.Assert.assertTrue;


/**
 * Unit tests for the CalendarView class.
 * This test class verifies that output methods in CalendarView produce the expected text
 * on System.out.
 * It captures console output and checks that welcome messages and help menus contain key phrases.
 */

public class CalendarViewTest {

  private CalendarView view;
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private PrintStream originalOut;

  @Before
  public void setUp() {
    view = new CalendarView();
    originalOut = System.out;
    System.setOut(new PrintStream(outContent));
  }

  @After
  public void tearDown() {
    System.setOut(originalOut);
  }

  @Test
  public void testDisplayWelcomeMessage() {
    view.displayWelcomeMessage();
    String output = outContent.toString();
    assertTrue(output.contains("Welcome to Calendar Application"));
    assertTrue(output.contains("Type 'help' to see available commands"));
  }

  @Test
  public void testDisplayHelp() {
    view.displayHelp();
    String output = outContent.toString();
    assertTrue(output.contains("Available Commands:"));
    assertTrue(output.contains("create event <subject> from <start> to <end>"));
    assertTrue(output.contains("edit event <property> <subject> from <start> to <end>"
            + " with <newValue>"));
    assertTrue(output.contains("print events on <date>"));
    assertTrue(output.contains("help - Show this help message"));
    assertTrue(output.contains("exit - Exit the application"));
  }
}
