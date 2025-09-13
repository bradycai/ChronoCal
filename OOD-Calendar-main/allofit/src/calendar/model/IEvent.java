package calendar.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a general event in the calendar.
 * This interface defines information and behaviors all events have.
 */
public interface IEvent {

  /**
   * Gets the subject of the event.
   *
   * @return the subject as a String
   */
  String getSubject();

  /**
   * Gets the start date and time of the event.
   *
   * @return the start as a LocalDateTime
   */
  LocalDateTime getStart();

  /**
   * Gets the end date and time of the event.
   *
   * @return the end as a LocalDateTime
   */
  LocalDateTime getEnd();

  /**
   * Gets the location of the event.
   *
   * @return the location as a String
   */
  String getLocation();

  /**
   * Gets the description of the event.
   *
   * @return the description as a String
   */
  String getDescription();

  /**
   * Returns true if the event is public, false if it is private.
   *
   * @return boolean indicating public/private status
   */
  boolean isPublic();

  /**
   * Converts the event to a string for display.
   *
   * @return the event information as a String
   */
  String toString();

  /**
   * Returns the status string of this event ("public" or "private").
   *
   * @return the status as a String
   */
  String getStatus();

  /**
   * If not null, this event is part of a recurring series.
   * All events in that recurring group share the same UUID.
   *
   * @return the seriesId, or null if this is not part of a series
   */
  UUID getSeriesId();

  /**
   * Sets the seriesId for this event. If non-null, this event belongs to
   * that recurring series. If null, this event is treated as a standalone.
   *
   * @param seriesId the UUID of the series
   */
  void setSeriesId(UUID seriesId);

  /**
   * Sets the subject of the event.
   *
   * @param subject the new subject
   */
  void setSubject(String subject);

  /**
   * Sets the start time of the event.
   *
   * @param start the new start date/time
   */
  void setStart(LocalDateTime start);

  /**
   * Sets the end time of the event.
   *
   * @param end the new end date/time.
   */
  void setEnd(LocalDateTime end);

  /**
   * Sets the location of the event.
   *
   * @param location the new location
   */
  void setLocation(String location);

  /**
   * Sets the description of the event.
   *
   * @param description the new description
   */
  void setDescription(String description);

  /**
   * Sets the visibility of the event.
   *
   * @param isPublic true if public, false if private
   */
  void setPublic(boolean isPublic);

  /**
   * Checks if another event has the same.
   *
   * @param event the event
   * @return true if both events have same subject, start, and end
   */
  boolean isSame(Event event);

  /**
   * Creates and returns a new copy of this event with the provided start time.
   * The duration of the event (original end minus original start) is preserved,
   * so the new end time is automatically adjusted relative to the new start.
   *
   * @param newStart the new start date and time for the copied event
   * @return a new IEvent instance with updated start and end times, preserving subject
   *      and other properties
   */
  IEvent copyWithNewTime(LocalDateTime newStart);
}
