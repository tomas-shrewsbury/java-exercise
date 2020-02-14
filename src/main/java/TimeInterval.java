import java.time.temporal.ChronoUnit;

public class TimeInterval {

    private ChronoUnit timeUnit;
    private int amount;

    public ChronoUnit getTimeUnit() {
        return timeUnit;
    }

    public void setType(ChronoUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public TimeInterval(int amount, ChronoUnit timeUnit) {
        this.amount = amount;
        this.timeUnit = timeUnit;
    }
}
