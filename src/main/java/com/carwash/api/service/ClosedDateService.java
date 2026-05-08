package com.carwash.api.service;

import com.carwash.api.dto.request.closeddate.ClosedDateRequest;
import com.carwash.api.dto.response.ClosedDateResponse;

import java.time.LocalDate;
import java.util.List;

public interface ClosedDateService {
    ClosedDateResponse addClosedDate(ClosedDateRequest request);
    void removeClosedDate(LocalDate date);
    List<ClosedDateResponse> getUpcomingClosedDates();
    boolean isClosedDate(LocalDate date);
}
