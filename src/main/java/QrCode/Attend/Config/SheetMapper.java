package QrCode.Attend.Config;

import java.util.Map;

public class SheetMapper {

    private static final Map<String, String> map = Map.of(
        "2025MCA1","1S2E2Fg_uwsaWFZFHLF_FGZNs6ry5WVebty9OHos8J1Y"
    );

    public static String get(String key) {
        return map.get(key);
    }
}
