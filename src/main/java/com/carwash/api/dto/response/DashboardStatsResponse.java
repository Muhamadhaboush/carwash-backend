package com.carwash.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    // Ciro
    private BigDecimal todayRevenue;
    private BigDecimal monthRevenue;
    private BigDecimal lastMonthRevenue;

    // Randevu sayıları
    private int pendingAppointmentsToday;
    private int approvedAppointmentsToday;
    private int completedAppointmentsToday;
    private int cancelledAppointmentsToday;
    private int totalAppointmentsThisMonth;

    // Kullanıcı & Hizmet
    private int totalUsers;
    private int activeWashServices;

    // En çok tercih edilen hizmetler (bu ay)
    private List<ServiceStatItem> topServices;

    // Araç tipi dağılımı (bu ay)
    private Map<String, Long> vehicleTypeDistribution;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceStatItem {
        private String serviceName;
        private long count;
    }
}
