package panicathe.dividend.scheduler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import panicathe.dividend.model.Company;
import panicathe.dividend.model.ScrapedResult;
import panicathe.dividend.model.constants.CacheKey;
import panicathe.dividend.persist.CompanyRepository;
import panicathe.dividend.persist.DividendRepository;
import panicathe.dividend.persist.entity.CompanyEntity;
import panicathe.dividend.persist.entity.DividendEntity;
import panicathe.dividend.scraper.Scraper;

import java.util.List;

@Slf4j
@Component
@EnableCaching
@AllArgsConstructor
public class ScraperScheduler {

    private final CompanyRepository companyRepository;
    private final Scraper yahooFinanceScraper;
    private final DividendRepository dividendRepository;

    @CacheEvict(value = CacheKey.KEY_FINANCE, allEntries = true) // 캐시 데이터 비우기 or Config 파일에서 TTL 설정해도 됨
    @Scheduled(cron = "${spring.scheduler.scrap.yahoo}")
    public void yahooFinanceScheduler() {

        log.info("scraping scheduler is started");

        // 지정된 회사 목록을 조회
        List<CompanyEntity> companies = this.companyRepository.findAll();


        // 회사마다 배당금 정보를 새로 스크래핑
        for(var company: companies){
            log.info("scraping scheduler is started -> "+ company.getName());
            ScrapedResult scrapedResult = this.yahooFinanceScraper
                    .scrap(new Company(company.getTicker(), company.getName()));

            // 스크래핑한 배당금 정보 중 데이터베이스에 없는 값은 저장
            scrapedResult.getDividends().stream()
                    .map(e -> new DividendEntity(company.getId(), e))
                    .forEach(e ->{
                        boolean exists = this.dividendRepository.existsByCompanyIdAndDate(company.getId(), e.getDate());
                        if(!exists){
                            this.dividendRepository.save(e);
                            log.info("insert new dividend -> " + e);
                        }
                    });

            // 스크래핑 대상 사이트 서버로 연속적인 요청 방지
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

    }


}
