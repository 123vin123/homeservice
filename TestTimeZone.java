import java.time.ZonedDateTime;
import java.time.ZoneId;

public class TestTimeZone {
    public static void main(String[] args) {
        System.out.println("JVM Default Timezone: " + ZoneId.systemDefault());
        
        String[] times = {"2026-10-05T02:30:00Z", "2026-10-05T05:30:00Z", "2026-10-05T08:30:00Z"};
        for(String t : times) {
            ZonedDateTime z = ZonedDateTime.parse(t);
            ZonedDateTime localZ = z.withZoneSameInstant(ZoneId.systemDefault());
            System.out.println("Input (UTC): " + t + " -> localDesiredTime: " + localZ + " -> .toLocalTime(): " + localZ.toLocalTime());
        }
    }
}
