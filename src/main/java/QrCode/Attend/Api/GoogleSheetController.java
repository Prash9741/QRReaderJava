package QrCode.Attend.Api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import QrCode.Attend.Dto.AttendanceRequestDto;
import QrCode.Attend.Service.GoogleSheetService;

@RestController
@RequestMapping("/sheet")
public class GoogleSheetController {

    @Autowired
    private GoogleSheetService sheetService;

    @PostMapping("/mark")
    public String markAttendance(@RequestBody AttendanceRequestDto request) throws Exception {
        sheetService.markAttendance(request.rollNo, request.buildDayMonth(),request.buildCourseKey());
        return "Attendance marked for Roll No: " + request.rollNo;
    }
}

