package panicathe.dividend.scraper;

import panicathe.dividend.model.Company;
import panicathe.dividend.model.ScrapedResult;

public interface Scraper {
    Company scrapCompanyByTicker(String ticker);
    ScrapedResult scrap(Company company);
}
