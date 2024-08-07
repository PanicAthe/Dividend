package panicathe.dividend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import panicathe.dividend.scraper.Scraper;
import panicathe.dividend.scraper.YahooFinanceScraper;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class DividendApplication {

    public static void main(String[] args) {

        SpringApplication.run(DividendApplication.class, args);

//        Scraper scraper = new YahooFinanceScraper();
//        //var result = scraper.scrap(Company.builder().ticker("KO").build());
//        var result = scraper.scrapCompanyByTicker("KO");
//        System.out.println(result);
    }
}
