package com.carwash.api.enums;

public enum AppointmentStatus {
    PENDING,    // Onay Bekliyor
    APPROVED,   // Onaylandı
    REJECTED,   // Reddedildi
    COMPLETED,  // Tamamlandı - Araç hazır
    CANCELLED   // İptal Edildi (müşteri tarafından)
}
