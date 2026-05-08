package com.carwash.api.dto.request.closeddate;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ClosedDateRequest {
    @NotNull(message = "Tarih boş olamaz")
    private LocalDate closedDate;

    @NotNull(message = "Sebep boş olamaz")
    private String reason;
}
