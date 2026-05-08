package com.carwash.api.repository;

import com.carwash.api.entity.Appointment;
import com.carwash.api.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Müşteri kendi randevuları
    List<Appointment> findAllByUserId(Long userId);
    Page<Appointment> findAllByUserId(Long userId, Pageable pageable);
    @Query("SELECT a FROM Appointment a " +
           "JOIN FETCH a.user " +
           "JOIN FETCH a.service " +
           "JOIN FETCH a.vehicle " +
           "LEFT JOIN FETCH a.pickupAddress " +
           "WHERE a.id = :id AND a.user.id = :userId")
    Optional<Appointment> findByIdAndUserId(
            @org.springframework.data.repository.query.Param("id") Long id,
            @org.springframework.data.repository.query.Param("userId") Long userId);

    // Admin: durum filtreli sayfalı liste
    Page<Appointment> findAllByStatus(AppointmentStatus status, Pageable pageable);
    List<Appointment> findAllByStatus(AppointmentStatus status);

    // Slot bazlı sorgular
    List<Appointment> findAllByAppointmentDate(LocalDate date);
    boolean existsByAppointmentDateAndAppointmentTime(LocalDate date, LocalTime time);

    // Kapasite kontrolü
    int countByAppointmentDateAndAppointmentTimeAndStatusNotIn(
            LocalDate date, LocalTime time, List<AppointmentStatus> statuses);

    int countByAppointmentDateAndStatusNotIn(LocalDate date, List<AppointmentStatus> statuses);
    int countByAppointmentDateAndStatus(LocalDate date, AppointmentStatus status);
    int countByStatus(AppointmentStatus status);

    // Bu ay toplam
    int countByAppointmentDateBetweenAndStatusNotIn(
            LocalDate startDate, LocalDate endDate, List<AppointmentStatus> statuses);

    // Araç çakışma kontrolü
    List<Appointment> findAllByVehicleIdAndAppointmentDateAndStatusNotIn(
            Long vehicleId, LocalDate date, List<AppointmentStatus> statuses);

    List<Appointment> findAllByUserIdAndStatus(Long userId, AppointmentStatus status);

    // Dashboard: ciro (isPaid=true olan randevular - bireysel ödeme + kurumsal tamamlananlar)
    @Query("SELECT COALESCE(SUM(a.totalPrice), 0) FROM Appointment a " +
           "WHERE a.isPaid = true AND a.appointmentDate = :date")
    BigDecimal sumRevenueByDate(LocalDate date);

    @Query("SELECT COALESCE(SUM(a.totalPrice), 0) FROM Appointment a " +
           "WHERE a.isPaid = true AND a.appointmentDate BETWEEN :startDate AND :endDate")
    BigDecimal sumRevenueBetweenDates(LocalDate startDate, LocalDate endDate);

    // Dashboard: en çok tercih edilen hizmetler
    @Query("SELECT a.service.name, COUNT(a) as cnt FROM Appointment a " +
           "WHERE a.status = 'COMPLETED' AND a.appointmentDate BETWEEN :startDate AND :endDate " +
           "GROUP BY a.service.name ORDER BY cnt DESC")
    List<Object[]> findTopServicesBetweenDates(LocalDate startDate, LocalDate endDate);

    // Dashboard: araç tipi dağılımı
    @Query("SELECT a.vehicle.vehicleType, COUNT(a) as cnt FROM Appointment a " +
           "WHERE a.appointmentDate BETWEEN :startDate AND :endDate " +
           "AND a.status NOT IN ('CANCELLED', 'REJECTED') " +
           "GROUP BY a.vehicle.vehicleType ORDER BY cnt DESC")
    List<Object[]> findVehicleTypeDistribution(LocalDate startDate, LocalDate endDate);
    // Admin: ay filtreli sayfalı liste - Performans için JOIN FETCH eklendi
    @Query(value = "SELECT a FROM Appointment a " +
           "JOIN FETCH a.user " +
           "JOIN FETCH a.service " +
           "JOIN FETCH a.vehicle " +
           "LEFT JOIN FETCH a.pickupAddress " +
           "WHERE (:status IS NULL OR a.status = :status) " +
           "AND (a.appointmentDate >= :yearStart) " +
           "AND (a.appointmentDate <= :yearEnd) " +
           "AND (:search = '' OR LOWER(a.user.firstName) LIKE LOWER(:search) " +
           "OR LOWER(a.user.lastName) LIKE LOWER(:search) " +
           "OR LOWER(a.user.companyName) LIKE LOWER(:search) " +
           "OR LOWER(a.vehicle.plateNumber) LIKE LOWER(:search))",
           countQuery = "SELECT COUNT(a) FROM Appointment a " +
           "WHERE (:status IS NULL OR a.status = :status) " +
           "AND (a.appointmentDate >= :yearStart) " +
           "AND (a.appointmentDate <= :yearEnd) " +
           "AND (:search = '' OR LOWER(a.user.firstName) LIKE LOWER(:search) " +
           "OR LOWER(a.user.lastName) LIKE LOWER(:search) " +
           "OR LOWER(a.user.companyName) LIKE LOWER(:search) " +
           "OR LOWER(a.vehicle.plateNumber) LIKE LOWER(:search))")
    Page<Appointment> findAllFiltered(
            @org.springframework.data.repository.query.Param("status") AppointmentStatus status,
            @org.springframework.data.repository.query.Param("yearStart") LocalDate yearStart,
            @org.springframework.data.repository.query.Param("yearEnd") LocalDate yearEnd,
            @org.springframework.data.repository.query.Param("search") String search,
            Pageable pageable);
}
