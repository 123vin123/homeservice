import java.time.*;

public class TestAvailability {
    public static void main(String[] args) {
        ZoneId zone = ZoneId.systemDefault(); // Asia/Kolkata
        // desired time is 2:00 PM IST (which is 08:30:00Z)
        ZonedDateTime desiredTime = ZonedDateTime.parse("2026-10-05T08:30:00Z");
        ZonedDateTime desiredEnd = desiredTime.plusMinutes(60);
        
        ZonedDateTime localDesiredTime = desiredTime.withZoneSameInstant(zone);
        ZonedDateTime localDesiredEnd = desiredEnd.withZoneSameInstant(zone);
        
        System.out.println("localDesiredTime: " + localDesiredTime);
        System.out.println("localDesiredEnd: " + localDesiredEnd);
        
        LocalTime startTime = LocalTime.parse("08:00");
        LocalTime endTime = LocalTime.parse("18:00");
        
        boolean cond1 = !localDesiredTime.toLocalTime().isBefore(startTime);
        boolean cond2 = !localDesiredEnd.toLocalTime().isAfter(endTime);
        boolean cond3 = localDesiredEnd.toLocalDate().equals(localDesiredTime.toLocalDate());
        
        System.out.println("cond1 (!isBefore " + startTime + "): " + cond1);
        System.out.println("cond2 (!isAfter " + endTime + "): " + cond2);
        System.out.println("cond3 (same day): " + cond3);
    }
}
