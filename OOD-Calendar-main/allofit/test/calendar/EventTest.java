package calendar;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import calendar.model.Event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * Unit tests for the Event class.
 * Verifies that the Event constructor enforces non-null and non-empty subject,
 * non-null start time, and correct behavior when the end time is null or before the start.
 * Also tests setter methods for proper validation and adjustment logic, as well as
 * equality and isSame consistency.
 */

public class EventTest {
  private Event event;
  private LocalDateTime initialStart;
  private LocalDateTime initialEnd;

  @Before
  public void setUp() {
    initialStart = LocalDateTime.of(2025, 7, 10, 9, 0);
    initialEnd = LocalDateTime.of(2025, 7, 10, 10, 0);
    event = new Event("TestEvent", initialStart, initialEnd, "Office",
            "Description", "private");
  }

  @Test
  public void testSetEndBeforeStartAdjusts() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 10, 9, 0);
    Event e = new Event("Adjusted", start, LocalDateTime.of(2025, 6,
            10, 10, 0));
    e.setEnd(start.minusHours(1));
    assertEquals(start.plusHours(1), e.getEnd());
  }

  @Test
  public void testEqualsAndHashCode() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 10, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 10, 10, 0);
    Event e1 = new Event("Same", start, end);
    Event e2 = new Event("Same", start, end);
    assertEquals(e1, e2);
    assertEquals(e1.hashCode(), e2.hashCode());
  }

  @Test
  public void testIsSameMatchesEqualsLogic() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 11, 14, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 11, 15, 0);
    Event e1 = new Event("Compare", start, end);
    Event e2 = new Event("Compare", start, end);
    assertTrue(e1.isSame(e2));
  }

  @Test
  public void testToStringWithNoOptionalFields() {
    LocalDateTime start = LocalDateTime.of(2025, 7, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 7, 1, 10, 0);
    Event event = new Event("Meeting", start, end);
    String expected = "Meeting: " + start + " → " + end + " [" + event.getStatus() + "]";
    assertEquals(expected, event.toString());
  }

  @Test
  public void testToStringWithLocationOnly() {
    LocalDateTime start = LocalDateTime.of(2025, 7, 2, 14, 30);
    LocalDateTime end = LocalDateTime.of(2025, 7, 2, 15, 30);
    Event event = new Event("Review", start, end);
    event.setLocation("ConferenceRoom");
    String expected = "Review: " + start + " → " + end + " @ ConferenceRoom ["
            + event.getStatus() + "]";
    assertEquals(expected, event.toString());
  }

  @Test
  public void testToStringWithDescriptionOnly() {
    LocalDateTime start = LocalDateTime.of(2025, 7, 3, 11, 0);
    LocalDateTime end = LocalDateTime.of(2025, 7, 3, 12, 0);
    Event event = new Event("Standalone", start, end);
    event.setDescription("Lunch meeting");
    String expected = "Standalone: " + start + " → " + end + " (Lunch meeting) [public]";
    assertEquals(expected, event.toString());
  }

  @Test
  public void testToStringWithStatusOnly() {
    LocalDateTime start = LocalDateTime.of(2025, 7, 4, 8, 0);
    LocalDateTime end = LocalDateTime.of(2025, 7, 4, 9, 0);
    Event event = new Event("Workout", start, end);
    event.setPublic(false);
    String expected = "Workout: " + start + " → " + end + " [private]";
    assertEquals(expected, event.toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorNullSubjectThrows() {
    new Event(null,
            LocalDateTime.of(2025, 7, 1, 9, 0),
            LocalDateTime.of(2025, 7, 1, 10, 0),
            "Office",
            "Team meeting",
            "public");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorEmptySubjectThrows() {
    new Event("",
            LocalDateTime.of(2025, 7, 1, 9, 0),
            LocalDateTime.of(2025, 7, 1, 10, 0),
            "Office",
            "Team meeting",
            "public");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorNullStartThrows() {
    new Event("Subject",
            null,
            LocalDateTime.of(2025, 7, 1, 10, 0),
            "Office",
            "Description",
            "private");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorEndBeforeStartThrows() {
    LocalDateTime start = LocalDateTime.of(2025, 7, 1, 9, 0);
    LocalDateTime endBefore = start.minusHours(1);
    new Event("Backwards",
            start,
            endBefore,
            "Office",
            "Invalid",
            "public");
  }

  @Test
  public void testConstructorNullEndDefaultsToOneHourLater() {
    LocalDateTime start = LocalDateTime.of(2025, 7, 2, 14, 30);
    Event e = new Event("AutoEnd",
            start,
            null,
            "Lobby",
            "Quick chat",
            "public");
    assertEquals(start.plusHours(1), e.getEnd());
  }

  @Test
  public void testConstructorLocationAndDescriptionNullDefaultToEmpty() {
    LocalDateTime start = LocalDateTime.of(2025, 7, 3, 11, 0);
    LocalDateTime end = LocalDateTime.of(2025, 7, 3, 12, 0);
    Event e = new Event("NoExtras",
            start,
            end,
            null,
            null,
            "private");
    assertEquals("", e.getLocation());
    assertEquals("", e.getDescription());
  }

  @Test
  public void testConstructorStatusNullDefaultsToPrivate() {
    LocalDateTime start = LocalDateTime.of(2025, 7, 4, 8, 0);
    LocalDateTime end = LocalDateTime.of(2025, 7, 4, 9, 0);
    Event e = new Event("Workout",
            start,
            end,
            "Gym",
            "Morning session",
            null);
    assertEquals("private", e.getStatus());
  }

  @Test
  public void testConstructorStatusCaseInsensitivePublic() {
    LocalDateTime start = LocalDateTime.of(2025, 7, 5, 16, 0);
    LocalDateTime end = LocalDateTime.of(2025, 7, 5, 17, 0);
    Event e1 = new Event("PublicEvent",
            start,
            end,
            "Zoom",
            "Discuss milestones",
            "PUBLIC");
    Event e2 = new Event("PublicEvent",
            start,
            end,
            "Zoom",
            "Discuss milestones",
            "public");
    Event e3 = new Event("PublicEvent",
            start,
            end,
            "Zoom",
            "Discuss milestones",
            "PuBlIc");
    assertEquals("public", e1.getStatus());
    assertEquals("public", e2.getStatus());
    assertEquals("public", e3.getStatus());
  }

  @Test
  public void testConstructorStatusOtherDefaultsToPrivate() {
    LocalDateTime start = LocalDateTime.of(2025, 7, 6, 13, 0);
    LocalDateTime end = LocalDateTime.of(2025, 7, 6, 14, 0);
    Event e = new Event("MixedStatus",
            start,
            end,
            "Office",
            "Review notes",
            "unknown");
    assertEquals("private", e.getStatus());
  }

  @Test
  public void testConstructorAllFieldsSetCorrectly() {
    LocalDateTime start = LocalDateTime.of(2025, 7, 7, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 7, 7, 11, 30);
    Event e = new Event("FullEvent",
            start,
            end,
            "ConferenceRoom",
            "Planning session",
            "public");
    assertEquals("FullEvent", e.getSubject());
    assertEquals(start, e.getStart());
    assertEquals(end, e.getEnd());
    assertEquals("ConferenceRoom", e.getLocation());
    assertEquals("Planning session", e.getDescription());
    assertEquals("public", e.getStatus());
  }

  @Test
  public void testIsPublicWhenStatusPublic() {
    Event publicEvent = new Event("Pub", initialStart, initialEnd, "", "", "public");
    assertTrue(publicEvent.isPublic());

    Event mixedCase = new Event("Mixed", initialStart, initialEnd, "", "", "PuBlIc");
    assertTrue(mixedCase.isPublic());
  }

  @Test
  public void testIsPublicWhenStatusPrivateOrOther() {
    assertFalse(event.isPublic());

    Event otherStatus = new Event("Other", initialStart, initialEnd, "", "", "unknown");
    assertFalse(otherStatus.isPublic());
  }

  @Test
  public void testGetAndSetSeriesId() {
    assertNull(event.getSeriesId());

    UUID id = UUID.randomUUID();
    event.setSeriesId(id);
    assertEquals(id, event.getSeriesId());

    UUID newId = UUID.randomUUID();
    event.setSeriesId(newId);
    assertEquals(newId, event.getSeriesId());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetSubjectNullThrows() {
    event.setSubject(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetSubjectEmptyThrows() {
    event.setSubject("");
  }

  @Test
  public void testSetSubjectValid() {
    event.setSubject("NewSubject");
    assertEquals("NewSubject", event.getSubject());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetStartNullThrows() {
    event.setStart(null);
  }

  @Test
  public void testSetStartAdjustsEndIfBefore() {
    LocalDateTime newStart = LocalDateTime.of(2025, 7, 10, 11, 0);
    event.setStart(newStart);
    assertEquals(newStart, event.getStart());
    assertEquals(newStart.plusHours(1), event.getEnd());
  }

  @Test
  public void testSetStartKeepsEndIfAfter() {
    LocalDateTime newStart = LocalDateTime.of(2025, 7, 10, 8, 0);
    event.setStart(newStart);
    assertEquals(newStart, event.getStart());
    assertEquals(initialEnd, event.getEnd());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetEndNullThrows() {
    event.setEnd(null);
  }

  @Test
  public void testSetEndAdjustsIfBeforeStart() {
    LocalDateTime before = initialStart.minusHours(1);
    event.setEnd(before);
    assertEquals(initialStart.plusHours(1), event.getEnd());
  }

  @Test
  public void testSetEndKeepsIfAfterStart() {
    LocalDateTime newEnd = LocalDateTime.of(2025, 7, 10, 11, 30);
    event.setEnd(newEnd);
    assertEquals(newEnd, event.getEnd());
  }

}
