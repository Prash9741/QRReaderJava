package QrCode.Attend.Api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import QrCode.Attend.Dto.AttendanceRequestDto;
import QrCode.Attend.Service.AttendanceService;

@RestController
@RequestMapping("Atten/")
@CrossOrigin(origins = "http://127.0.0.1:5500")

public class Attendance {
	
	@Autowired
	AttendanceService attending;
	@GetMapping("Test/")
	public String test() {
		return "Testing";
	}
	@PostMapping("/MarkAttendance/{code}")
	public ResponseEntity<?> markAttendance(@PathVariable String code, @RequestBody AttendanceRequestDto req) {
	    try {
	        String result = attending.mark(code, req);
	        return ResponseEntity.ok(result); // HTTP 200 OK + result
	    } catch (Exception e) {
	        return ResponseEntity
	                .status(HttpStatus.BAD_REQUEST)
	                .body(e.getMessage());
	    }
	}

}

