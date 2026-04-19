package org.example.qposbackend.order.data;

import java.util.Date;

public record SalesStatisticsRequest(Date start, Date end, String productName) {}
