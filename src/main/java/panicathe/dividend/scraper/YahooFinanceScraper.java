package panicathe.dividend.scraper;


import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import panicathe.dividend.model.Company;
import panicathe.dividend.model.Dividend;
import panicathe.dividend.model.ScrapedResult;
import panicathe.dividend.model.constants.Month;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class YahooFinanceScraper implements Scraper{

    private static final String STATISTICS_URL = "https://finance.yahoo.com/quote/%s/history/?filter=div&frequency=1mo&period1=%d&period2=%d";
    private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s?p=%s";

    private static final long START_TIME = 86400; // 60 * 60 * 24

    @Override
    public ScrapedResult scrap(Company company){

        var scrapeResult = new ScrapedResult();
        scrapeResult.setCompany(company);

        try {
            long now = System.currentTimeMillis() / 1000;

            String url = String.format(STATISTICS_URL, company.getTicker(), START_TIME, now);

            // User-Agent 헤더를 추가하여 요청
            Connection connection = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            Document document = connection.get();

            // CSS 셀렉터를 사용하여 요소 선택
            Elements parsingDivs = document.select("#nimbus-app > section > section > section > article > div.container > div.table-container.yf-ewueuo > table");
            if (!parsingDivs.isEmpty()) {
                Element table = parsingDivs.first();
                Elements rows = table.select("tbody > tr");

                List<Dividend> dividends = new ArrayList<>();

                for (Element row : rows) {
                    Elements cells = row.select("td");
                    if (cells.size() > 1) {
                        String[] date = cells.get(0).text().split(" ");
                        int month = Month.strToNumber(date[0]);
                        int day = Integer.valueOf(date[1].replace(",", ""));
                        int year = Integer.valueOf(date[2]);

                        if( month < 0){
                            throw new RuntimeException("Unexpected month: " + date[0]);
                        }

                        String dividend = cells.get(1).text().split(" ")[0];

                        dividends.add(new Dividend(LocalDateTime.of(year, month, day, 0, 0), dividend));

                    }
                }

                scrapeResult.setDividends(dividends);

            } else {
                throw new RuntimeException("dividend not found");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return scrapeResult;
    }

    @Override
    public Company scrapCompanyByTicker(String ticker){
        String url = String.format(SUMMARY_URL, ticker, ticker);

        try{
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .get();

            Element titleEle = document.getElementsByTag("h1").get(1);
            String title = titleEle.text().split("\\(")[0].trim();

            return new Company(ticker, title);

        }catch (IOException e){
            e.printStackTrace();
        }


        return null;
    }
}
