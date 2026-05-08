package com.carwash.api.service.impl;

import com.carwash.api.dto.request.closeddate.ClosedDateRequest;
import com.carwash.api.dto.response.ClosedDateResponse;
import com.carwash.api.entity.ClosedDate;
import com.carwash.api.exception.BusinessException;
import com.carwash.api.exception.ResourceNotFoundException;
import com.carwash.api.repository.ClosedDateRepository;
import com.carwash.api.service.ClosedDateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClosedDateServiceImpl implements ClosedDateService {

    private final ClosedDateRepository closedDateRepository;

    @Override
    @Transactional
    public ClosedDateResponse addClosedDate(ClosedDateRequest request) {
        if (closedDateRepository.existsByClosedDate(request.getClosedDate())) {
            throw new BusinessException("Bu tarih zaten kapalı olarak işaretlenmiş: " + request.getClosedDate());
        }
        ClosedDate closedDate = ClosedDate.builder()
                .closedDate(request.getClosedDate())
                .reason(request.getReason())
                .build();
        ClosedDate saved = closedDateRepository.save(closedDate);
        log.info("Kapalı gün eklendi: {} - {}", saved.getClosedDate(), saved.getReason());
        return ClosedDateResponse.from(saved);
    }

    @Override
    @Transactional
    public void removeClosedDate(LocalDate date) {
        ClosedDate closedDate = closedDateRepository.findByClosedDate(date)
                .orElseThrow(() -> new ResourceNotFoundException("Kapalı gün bulunamadı: " + date));
        closedDateRepository.delete(closedDate);
        log.info("Kapalı gün kaldırıldı: {}", date);
    }

    @Override
    public List<ClosedDateResponse> getUpcomingClosedDates() {
        return closedDateRepository
                .findAllByClosedDateGreaterThanEqualOrderByClosedDateAsc(LocalDate.now())
                .stream().map(ClosedDateResponse::from).collect(Collectors.toList());
    }

    @Override
    public boolean isClosedDate(LocalDate date) {
        return closedDateRepository.existsByClosedDate(date);
    }
}
