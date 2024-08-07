package panicathe.dividend.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Cache;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import panicathe.dividend.model.Company;
import panicathe.dividend.model.Dividend;
import panicathe.dividend.model.ScrapedResult;
import panicathe.dividend.model.constants.CacheKey;
import panicathe.dividend.persist.CompanyRepository;
import panicathe.dividend.persist.DividendRepository;
import panicathe.dividend.persist.entity.CompanyEntity;
import panicathe.dividend.persist.entity.DividendEntity;

import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@Service
@AllArgsConstructor
public class FinanceService {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    @Cacheable(key = "#companyName", value = CacheKey.KEY_FINANCE)
    public ScrapedResult getDividendByCompanyName(String companyName) {

        log.info("search company -> "+ companyName);

        // 회사명으로 회사 정보 조회
        CompanyEntity company = this.companyRepository.findByName(companyName)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회사명"));

        // 조회된 회사 ID 로 배당금 정보 조회
        List<DividendEntity> dividendEntityList = this.dividendRepository.findAllByCompanyId(company.getId());

        // 결과 조합 후 반환
        List<Dividend> dividends = dividendEntityList.stream()
                .map(e -> new Dividend(e.getDate(), e.getDividend()))
                .collect(Collectors.toList());

        return new ScrapedResult(new Company(company.getTicker(),
                company.getName()),dividends);
    }
}
