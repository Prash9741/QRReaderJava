package QrCode.Attend.Dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import QrCode.Attend.Config.SheetMapper;

public class AttendanceRequestDto {
    public int rollNo;
    public String date;
    public String course;
    public String section;
    public double latitude;
    public double longitude;
    
    public double distanceInMeters() {
    	double lat2 = 29.0480000;
    	double lon2 = 77.7112000;
        final int R = 6371000; // Earth's radius in meters

        double dLat = Math.toRadians(lat2 - latitude);
        double dLon = Math.toRadians(lon2 - longitude);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(latitude)) *
                   Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;  // distance in meters
    }
    public boolean isInCampus(int dis) {
    	return dis<100000?true:false;
    }
    public void print() {
    	System.out.println(rollNo+" "+date+" "+course+" "+section);
    }
    public String buildCourseKey() {
        LocalDate d = LocalDate.parse(date); // 2025-12-01
        return SheetMapper.get(d.getYear()+course + section);// 2025MCA1
    }
    public String buildDayMonth() {
        LocalDate d = LocalDate.parse(date);
        DateTimeFormatter f = DateTimeFormatter.ofPattern("d-MMM");
        return d.format(f);                  // 1-Dec
    }
}