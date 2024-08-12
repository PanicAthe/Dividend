package panicathe.dividend.web;

import lombok.AllArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import panicathe.dividend.model.Company;
import panicathe.dividend.model.constants.CacheKey;
import panicathe.dividend.persist.entity.CompanyEntity;
import panicathe.dividend.service.CompanyService;

@RestController
@RequestMapping("/company")
@AllArgsConstructor
public class CompanyController {

    private final CompanyService companyService;
    private final CacheManager redisCacheManager;

    @GetMapping("/autocomplete")
    public ResponseEntity<?> autocomplete(@RequestParam String keyword)
    {
       var result = this.companyService.getCompanyNAMesByKeyword(keyword);
       return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasRole('READ')")
    @GetMapping
    public ResponseEntity<?> searchCompany(final Pageable pageable)
    {
        Page<CompanyEntity> companies = this.companyService.getAllCompany(pageable);
        return ResponseEntity.ok(companies);
    }

    @PreAuthorize("hasRole('WRITE')")
    @PostMapping
    public ResponseEntity<?> addCompany(@RequestBody Company request)
    {
        String ticker = request.getTicker().trim();
        if(ObjectUtils.isEmpty(ticker)){
            throw new RuntimeException("ticker is empty");
        }

        Company savedCompany = this.companyService.save(ticker);
        this.companyService.addAutocompleteKeyword(savedCompany.getName());

        return ResponseEntity.ok(savedCompany);
    }

    @PreAuthorize("hasRole('WRITE')")
    @DeleteMapping("/{ticker}")
    public ResponseEntity<?> deleteCompany(@PathVariable String ticker)
    {
        String companyName = this.companyService.deleteCompany(ticker);
        this.clearFinanceCache(companyName);
        return ResponseEntity.ok(companyName);
    }

    public void clearFinanceCache(String companyName){
        this.redisCacheManager.getCache(CacheKey.KEY_FINANCE).evict(companyName);
    }
}
