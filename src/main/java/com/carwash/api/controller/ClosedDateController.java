package com.carwash.api.controller;

import com.carwash.api.dto.request.closeddate.ClosedDateRequest;
import com.carwash.api.dto.response.ClosedDateResponse;
import com.carwash.api.service.ClosedDateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/closed-dates")
@RequiredArgsConstructor
@Tag(name = "Kapalı Günler", description = "Bayram, bakım veya özel kapalı gün yönetimi")
public class ClosedDateController {

    private final ClosedDateService closedDateService;

    @Operation(summary = "[ADMIN] Kapalı gün ekle (bayram, bakım vs.)")
    @PostMapping
    public ResponseEntity<ClosedDateResponse> addClosedDate(@Valid @RequestBody ClosedDateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(closedDateService.addClosedDate(request));
    }

    @Operation(summary = "[ADMIN] Kapalı günü kaldır")
    @DeleteMapping("/{date}")
    public ResponseEntity<Void> removeClosedDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        closedDateService.removeClosedDate(date);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Gelecekteki tüm kapalı günleri listele")
    @GetMapping
    public ResponseEntity<List<ClosedDateResponse>> getUpcomingClosedDates() {
        return ResponseEntity.ok(closedDateService.getUpcomingClosedDates());
    }
}
