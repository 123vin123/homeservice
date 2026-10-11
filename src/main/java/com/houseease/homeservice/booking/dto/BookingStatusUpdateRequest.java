package com.houseease.homeservice.booking.dto;

import com.houseease.homeservice.booking.BookingStatus;
import lombok.Data;

@Data
public class BookingStatusUpdateRequest {
    private BookingStatus status;
}
