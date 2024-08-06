package panicathe.dividend.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TestSecheduler {

    @Scheduled(cron = "")
    public void yahooFinanceScheduler() {

    }


}
