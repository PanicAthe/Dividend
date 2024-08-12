package panicathe.dividend.service;

import lombok.AllArgsConstructor;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import panicathe.dividend.exception.impl.NoCompanyException;
import panicathe.dividend.model.Company;
import panicathe.dividend.model.ScrapedResult;
import panicathe.dividend.persist.CompanyRepository;
import panicathe.dividend.persist.DividendRepository;
import panicathe.dividend.persist.entity.CompanyEntity;
import panicathe.dividend.persist.entity.DividendEntity;
import panicathe.dividend.scraper.Scraper;

import java.util.List;

@Service
@AllArgsConstructor
public class CompanyService {

    private final Trie trie;
    private final Scraper yahooFinanceScraper;

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    public Company save(String ticker){
        boolean existsByTicker = this.companyRepository.existsByTicker(ticker);
        if(existsByTicker){
            throw new RuntimeException("already exists by this ticker -> " +ticker);
        }
        return this.storeCompanyAndDividend(ticker);
    }

    public Page<CompanyEntity> getAllCompany(Pageable pageable){
        return this.companyRepository.findAll(pageable);
    }

    private Company storeCompanyAndDividend(String ticker){
        
        //ticker 기분으로 회사 스크래핑
        Company company = this.yahooFinanceScraper.scrapCompanyByTicker(ticker);
        if(ObjectUtils.isEmpty(company)){
            throw new RuntimeException("failed to scrap by ticker -> "+ ticker);
        }
        
        // 회사 존재 시, 배당금 정보 스크래핑
        ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(company);
        
        // 스크패링 결과 저장
        CompanyEntity companyEntity = this.companyRepository.save(new CompanyEntity(company));

        List<DividendEntity> dividendEntities = scrapedResult.getDividends().stream()
                .map(e -> new DividendEntity(companyEntity.getId(), e))
                .toList();

        this.dividendRepository.saveAll(dividendEntities);

        // 저장한 회사 정보 반환
        return company;
    }

    public List<String> getCompanyNAMesByKeyword(String keyword){
        Pageable limit = PageRequest.of(0, 10);

        Page<CompanyEntity> companyEntities = this.companyRepository.findByNameStartingWithIgnoreCase(keyword, limit);

        return companyEntities.stream()
                .map(e -> e.getName()).toList();
    }

    public void addAutocompleteKeyword(String keyword){
        this.trie.put(keyword, null);
    }

    public List<String> autocompleteKeyword(String keyword){
        return this.trie.prefixMap(keyword).keySet().stream().toList();
    }

    public void deleteAutocompleteKeyword(String keyword){
        this.trie.remove(keyword);
    }

    public String deleteCompany(String ticker) {
        var company = this.companyRepository.findByTicker(ticker)
                .orElseThrow(()-> new NoCompanyException());

        this.dividendRepository.deleteAllByCompanyId(company.getId());
        this.companyRepository.delete(company);

        this.deleteAutocompleteKeyword(company.getName());

        return company.getName();
    }
}
