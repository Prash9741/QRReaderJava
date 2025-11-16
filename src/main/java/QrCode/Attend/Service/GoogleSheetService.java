package QrCode.Attend.Service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;

import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



@Service
public class GoogleSheetService {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME = "Attendance Tracker";
    
    // Spreadsheet ID (NOT FINAL so you can change dynamically if needed)
//    private String spreadsheetId = "1S2E2Fg_uwsaWFZFHLF_FGZNs6ry5WVebty9OHos8J1Y";


    /**
     * Create Google Sheets API client
     */
    private Sheets getSheetsService() throws Exception {
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new ClassPathResource("credentials.json").getInputStream())
                .createScoped(Collections.singletonList(SheetsScopes.SPREADSHEETS));

        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                new HttpCredentialsAdapter(credentials)
        )
        .setApplicationName(APPLICATION_NAME)
        .build();
    }

    /**
     * Check if sheet exists
     */
    public boolean sheetExists(String sheetName, String spreadsheetId) throws Exception {

        Sheets service = getSheetsService();

        Spreadsheet sheet = service.spreadsheets()
                .get(spreadsheetId)
                .setIncludeGridData(false)
                .execute();

        for (Sheet s : sheet.getSheets()) {
            if (s.getProperties().getTitle().equalsIgnoreCase(sheetName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Create sheet if missing
     */
    public void createSheet(String sheetName, String spreadsheetId) throws Exception {

        Sheets service = getSheetsService();

        // 1️⃣ Create the sheet
        AddSheetRequest addSheetRequest = new AddSheetRequest()
                .setProperties(new SheetProperties().setTitle(sheetName));

        Request createSheetReq = new Request().setAddSheet(addSheetRequest);

        BatchUpdateSpreadsheetRequest createSheetBody =
                new BatchUpdateSpreadsheetRequest()
                        .setRequests(Collections.singletonList(createSheetReq));

        BatchUpdateSpreadsheetResponse response =
                service.spreadsheets().batchUpdate(spreadsheetId, createSheetBody).execute();

        System.out.println("Created sheet: " + sheetName);

        // Get sheet ID
        Integer sheetId = response.getReplies().get(0).getAddSheet()
                .getProperties().getSheetId();


        // 2️⃣ Determine days in month
        int daysInMonth = getDaysInMonth(sheetName);

        // Total columns needed = daysInMonth (NO rollNo column now)
        int requiredColumns = daysInMonth;


        // 3️⃣ Expand columns
        Request expandColumns = new Request()
                .setUpdateSheetProperties(
                        new UpdateSheetPropertiesRequest()
                                .setProperties(
                                        new SheetProperties()
                                                .setSheetId(sheetId)
                                                .setGridProperties(
                                                        new GridProperties().setColumnCount(requiredColumns)
                                                )
                                )
                                .setFields("gridProperties.columnCount")
                );

        service.spreadsheets().batchUpdate(
                spreadsheetId,
                new BatchUpdateSpreadsheetRequest()
                        .setRequests(Collections.singletonList(expandColumns))
        ).execute();


        // 4️⃣ Create Header Row → only dates
        List<CellData> headerCells = new ArrayList<>();

        for (int day = 1; day <= daysInMonth; day++) {
            headerCells.add(
                    new CellData().setUserEnteredValue(new ExtendedValue().setNumberValue((double) day))
            );
        }

        List<RowData> rows = new ArrayList<>();
        rows.add(new RowData().setValues(headerCells));


        // 5️⃣ Write header only (NO roll numbers)
        Request writeRequest = new Request()
                .setUpdateCells(
                        new UpdateCellsRequest()
                                .setStart(new GridCoordinate()
                                        .setSheetId(sheetId)
                                        .setRowIndex(0)
                                        .setColumnIndex(0))
                                .setRows(rows)
                                .setFields("*")
                );

        service.spreadsheets().batchUpdate(
                spreadsheetId,
                new BatchUpdateSpreadsheetRequest()
                        .setRequests(Collections.singletonList(writeRequest))
        ).execute();

        System.out.println("✔ Sheet formatted with only header (dates).");
    }



    /**
     * Convert day number to column (1=A, 2=B, 26=Z, 27=AA)
     */
    public String getColumnName(int num) {
        StringBuilder sb = new StringBuilder();

        while (num > 0) {
            int rem = (num - 1) % 26;
            sb.append((char) ('A' + rem));
            num = (num - 1) / 26;
        }

        return sb.reverse().toString();
    }
    private int getDaysInMonth(String monthName) {
        String m = monthName.substring(0, 3).toLowerCase();
        // 30-day months
        if (m.equals("apr") || m.equals("jun") || m.equals("sep") || m.equals("nov"))
            return 30;

        // February (handle leap year)
        if (m.equals("feb"))
            return Year.isLeap(LocalDate.now().getYear()) ? 29 : 28;
        
        // Default fallback
        return 31;
    }

    /**
     * Final Attendance Logic
     */
    public void markAttendance(int rollNo, String dateString, String spreadsheetId) throws Exception {

        Sheets service = getSheetsService();
        // ❌ Validate roll number (must be 1–99)
        if (rollNo < 2 || rollNo > 99) {
            throw new IllegalArgumentException("Invalid roll number! Must be between 2 and 99.");
        }

        // dateString example: "3-Dec"
        String[] parts = dateString.split("-");
        int day = Integer.parseInt(parts[0]);  // 3
        String monthSheet = parts[1];          // Dec

        System.out.print(spreadsheetId);
        // Create sheet if missing
        if (!sheetExists(monthSheet,spreadsheetId))
            createSheet(monthSheet,spreadsheetId);

        // Convert day → column (day 1 = A, day2 = B...)
        String column = getColumnName(day);  // NOT day+1 now

        // Row = roll number
        int row = rollNo;

        String cell = column + row;  // Example: "C12"
        ValueRange body = new ValueRange()
                .setValues(Collections.singletonList(
                        Collections.singletonList("Present")
                ));
        
        service.spreadsheets().values()
                .update(spreadsheetId, monthSheet + "!" + cell, body)
                .setValueInputOption("RAW")
                .execute();

        System.out.println("✔ Attendance updated at: " + monthSheet + "!" + cell);
    }

}
