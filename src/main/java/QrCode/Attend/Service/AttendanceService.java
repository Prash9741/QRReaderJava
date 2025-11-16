package QrCode.Attend.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import QrCode.Attend.Dto.AttendanceRequestDto;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

@Service
public class AttendanceService {
	
	@Autowired
	    private GoogleSheetService sheetService;
	
	public String mark(String code,AttendanceRequestDto request) throws Exception {
		String verifyCode=ValidateCode.validate();
		if(!verifyCode.equals(code))
			throw new Exception("Varification code failed");
		int distance=(int)request.distanceInMeters();
		if(!request.isInCampus(distance))
			throw new Exception("You are "+ distance +"M away from the location");
		
		sheetService.markAttendance(request.rollNo, request.buildDayMonth(), request.buildCourseKey());

		return "Attendance marked \nDate: "+request.date+"\nRoll No: " + request.rollNo+" \nDistance: "+distance+"M";		
	}
}


//import java.time.ZoneOffset;
//import java.time.format.DateTimeFormatter;
//import java.util.Scanner;

class ValidateCode {
    private static final String SECRET = "shared-secret-123";
    private static final int WINDOW_SIZE = 5; // seconds

    // ✅ Isolated backend logic
    public static String validate() throws Exception {
    	long utcSeconds = Instant.now().getEpochSecond(); // UTC+0 seconds
//      String customTimeStr = "2025-10-25T07:10:33.196+00:00";
//      Instant customInstant = Instant.parse(customTimeStr);
//      long utcSeconds = customInstant.getEpochSecond() - 5;
        long window = utcSeconds / WINDOW_SIZE;

        String input = SECRET + window;
        String hash = sha256(input);
        String code = hexToCode(hash);

//      String humanTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC+0'")
//      	.withZone(ZoneOffset.UTC)
//          .format(Instant.ofEpochSecond(utcSeconds));

//		System.out.println("Backend UTC+0 Time: " + humanTime);
// 		System.out.println("Backend Window: " + window);
// 		System.out.println("Backend Code: " + code);
// 		System.out.println("-----------------------------------");
        return code;
    }

    private static String sha256(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static String hexToCode(String hex) {
        BigInteger num = new BigInteger(hex, 16);
        String base36 = num.toString(36).toUpperCase();
        return base36.substring(0, 6);
    }
}
