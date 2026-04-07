package com.courseflow.dto;

public class CourseAggregateResponse {

    private long totalCourses;
    private long totalCapacity;
    private long totalApprovedRegistrations;
    private long totalAvailableSeats;

    public long getTotalCourses() {
        return totalCourses;
    }

    public void setTotalCourses(long totalCourses) {
        this.totalCourses = totalCourses;
    }

    public long getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(long totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public long getTotalApprovedRegistrations() {
        return totalApprovedRegistrations;
    }

    public void setTotalApprovedRegistrations(long totalApprovedRegistrations) {
        this.totalApprovedRegistrations = totalApprovedRegistrations;
    }

    public long getTotalAvailableSeats() {
        return totalAvailableSeats;
    }

    public void setTotalAvailableSeats(long totalAvailableSeats) {
        this.totalAvailableSeats = totalAvailableSeats;
    }
}
