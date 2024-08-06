package panicathe.dividend.web;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import panicathe.dividend.model.Company;
import panicathe.dividend.persist.entity.CompanyEntity;
import panicathe.dividend.service.CompanyService;

import java.util.List;

@RestController
@RequestMapping("/company")
@AllArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/autocomplete")
    public ResponseEntity<?> autocomplete(@RequestParam String keyword)
    {
       var result = this.companyService.getCompanyNAMesByKeyword(keyword);
       return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<?> searchCompany(final Pageable pageable)
    {
        Page<CompanyEntity> companies = this.companyService.getAllCompany(pageable);
        return ResponseEntity.ok(companies);
    }

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

    @DeleteMapping
    public ResponseEntity<?> deleteCompany()
    {
        return null;
    }
}
