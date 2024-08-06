package panicathe.dividend.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import panicathe.dividend.model.Company;
import panicathe.dividend.model.Dividend;
import panicathe.dividend.model.ScrapedResult;
import panicathe.dividend.persist.CompanyRepository;
import panicathe.dividend.persist.DividendRepository;
import panicathe.dividend.persist.entity.CompanyEntity;
import panicathe.dividend.persist.entity.DividendEntity;

import java.util.List;

@Service
@AllArgsConstructor
public class FinanceService {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    public ScrapedResult getDividendByCompanyName(String companyName) {

        // 회사명으로 회사 정보 조회
        CompanyEntity companyEntity = this.companyRepository.findByName(companyName)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회사명"));

        // 조회된 회사 ID 로 배당금 정보 조회
        List<DividendEntity> dividendEntityList = this.dividendRepository.findAllByCompanyId(companyEntity.getId());

        // 결과 조합 후 반환
        List<Dividend> dividends = dividendEntityList.stream()
                .map(e -> Dividend.builder()
                        .date(e.getDate())
                        .dividend(e.getDividend())
                        .build())
                .toList();

        return new ScrapedResult((Company.builder()
                .ticker(companyEntity.getTicker())
                .name(companyEntity.getName())
                .build()),dividends);
    }
}
