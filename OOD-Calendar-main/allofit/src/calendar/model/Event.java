package calendar.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a calendar event. A single event may optionally be part of a recurring series,
 * in which case `seriesId` will be non-null and shared across all occurrences.
 */
public class Event implements IEvent {
  private String subject;
  private LocalDateTime start;
  private LocalDateTime end;
  private String location;
  private String description;
  private String status;
  private UUID seriesId;

  /**
   * Three-argument constructor (subject, start, end).
   * If no end is provided, defaults to start + 1 hour.
   */
  public Event(String subject, LocalDateTime start, LocalDateTime end) {
    if (subject == null || subject.isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be null or empty");
    }
    if (start == null) {
      throw new IllegalArgumentException("Start date/time cannot be null");
    }

    this.subject = subject;
    this.start = start;

    if (end != null) {
      if (end.isBefore(start)) {
        throw new IllegalArgumentException("End date/time cannot be before start date/time");
      }
      this.end = end;
    } else {
      this.end = start.plusHours(1);
    }

    this.location = "";
    this.description = "";
    this.status = "public";
    this.seriesId = null;
  }

  /**
   * Six-argument constructor.
   *
   * @param subject     The event subject (non-null, non-empty).
   * @param start       The event start date/time (non-null).
   * @param end         The event end date/time. If null, defaults to start.plusHours(1);
   *                    if end < start, throws IllegalArgumentException.
   * @param location    The event location (may be null → treated as "").
   * @param description The event description (may be null → treated as "").
   * @param status      Either "public" or "private" (case-insensitive). Any other value
   *                    → treated as "private".
   */
  public Event(
          String subject,
          LocalDateTime start,
          LocalDateTime end,
          String location,
          String description,
          String status
  ) {
    if (subject == null || subject.isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be null or empty");
    }
    if (start == null) {
      throw new IllegalArgumentException("Start date/time cannot be null");
    }

    this.subject = subject;
    this.start = start;

    if (end != null) {
      if (end.isBefore(start)) {
        throw new IllegalArgumentException("End date/time cannot be before start date/time");
      }
      this.end = end;
    } else {
      this.end = start.plusHours(1);
    }

    this.location = Objects.requireNonNullElse(location, "");

    this.description = Objects.requireNonNullElse(description, "");

    if (status != null && status.equalsIgnoreCase("public")) {
      this.status = "public";
    } else {
      this.status = "private";
    }

    this.seriesId = null;
  }

  @Override
  public String getSubject() {
    return subject;
  }

  @Override
  public LocalDateTime getStart() {
    return start;
  }

  @Override
  public LocalDateTime getEnd() {
    return end;
  }

  @Override
  public String getLocation() {
    return location;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public boolean isPublic() {
    return status.equalsIgnoreCase("public");
  }

  /**
   * Converts the event to a string for display.
   *
   * @return the event information as a String
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(subject)
            .append(": ")
            .append(start)
            .append(" → ")
            .append(end);

    if (!location.isEmpty()) {
      sb.append(" @ ").append(location);
    }
    if (!description.isEmpty()) {
      sb.append(" (").append(description).append(")");
    }
    sb.append(" [").append(status).append("]");
    return sb.toString();
  }

  /**
   * Returns the raw status string ("public" or "private").
   *
   * @return the status as a String
   */
  @Override
  public String getStatus() {
    return status;
  }

  /**
   * If non-null, this event is part of a recurring series; otherwise, it's a standalone.
   *
   * @return the UUID of the series, or null if none
   */
  @Override
  public UUID getSeriesId() {
    return seriesId;
  }

  /**
   * Sets the seriesId for this event. If non-null, this event belongs to that series.
   * If null, it becomes a standalone event.
   *
   * @param seriesId the UUID of the series (or null for no series)
   */
  @Override
  public void setSeriesId(UUID seriesId) {
    this.seriesId = seriesId;
  }

  /**
   * Sets the subject for this event.
   *
   * @param subject the new subject
   * @throws IllegalArgumentException if subject is null or empty
   */
  @Override
  public void setSubject(String subject) {
    if (subject == null || subject.isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be null or empty");
    }
    this.subject = subject;
  }

  /**
   * Sets the start time for this event,
   * Adjusted 1 hour if the new start is after the current end.
   *
   * @param start the new start date/time
   * @throws IllegalArgumentException if start is null
   */
  @Override
  public void setStart(LocalDateTime start) {
    if (start == null) {
      throw new IllegalArgumentException("Start date/time cannot be null");
    }
    this.start = start;
    if (this.end.isBefore(this.start)) {
      this.end = this.start.plusHours(1);
    }
  }

  /**
   * Sets the end time for this event.
   * Adjusted 1 hour if end is before the current start.
   *
   * @param end the new end date/time
   * @throws IllegalArgumentException if end is null
   */
  @Override
  public void setEnd(LocalDateTime end) {
    if (end == null) {
      throw new IllegalArgumentException("End date/time cannot be null");
    }
    if (end.isBefore(this.start)) {
      this.end = this.start.plusHours(1);
    } else {
      this.end = end;
    }
  }

  /**
   * Sets the event's location.
   * Sets it to an empty string if null.
   *
   * @param location the event location
   */
  @Override
  public void setLocation(String location) {
    this.location = Objects.requireNonNullElse(location, "");
  }

  /**
   * Sets the event's description.
   * Sets it to an empty string if null.
   *
   * @param description the event description
   */
  @Override
  public void setDescription(String description) {
    this.description = Objects.requireNonNullElse(description, "");
  }

  /**
   * Sets the visibility of the event.
   *
   * @param isPublic true if public, false if private
   */
  @Override
  public void setPublic(boolean isPublic) {
    if (isPublic) {
      this.status = "public";
    } else {
      this.status = "private";
    }
  }

  /**
   * Checks if this event is the same as another.
   *
   * @param that the other event
   * @return true if both events have same subject, start, and end
   */
  @Override
  public boolean isSame(Event that) {
    return subject.equals(that.subject)
            && start.equals(that.start)
            && end.equals(that.end);
  }

  /**
   * Compares this event to another object for equality.
   *
   * @param o the different object.
   * @return true if this and other are equal events
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Event)) {
      return false;
    }
    Event that = (Event) o;
    return subject.equals(that.subject)
            && start.equals(that.start)
            && end.equals(that.end);
  }

  @Override
  public IEvent copyWithNewTime(LocalDateTime newStart) {
    long minutes = java.time.Duration.between(this.start, this.end).toMinutes();
    LocalDateTime newEnd = newStart.plusMinutes(minutes);

    Event copy = new Event(this.subject, newStart, newEnd,
            this.location, this.description, this.status);

    if (this.seriesId != null) {
      copy.setSeriesId(this.seriesId);
    }

    return copy;
  }


  /**
   * Returns a hash code for this event.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(subject, start, end);
  }
}