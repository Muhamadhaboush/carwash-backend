package com.carwash.api.dto.response;

import com.carwash.api.entity.ClosedDate;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class ClosedDateResponse {
    private Long id;
    private LocalDate closedDate;
    private String reason;

    public static ClosedDateResponse from(ClosedDate c) {
        return ClosedDateResponse.builder()
                .id(c.getId())
                .closedDate(c.getClosedDate())
                .reason(c.getReason())
                .build();
    }
}
