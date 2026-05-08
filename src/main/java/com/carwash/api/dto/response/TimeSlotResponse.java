package com.carwash.api.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotResponse {
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime time;
    private boolean available;
    private String label; // "09:00", "09:30" gibi
}
