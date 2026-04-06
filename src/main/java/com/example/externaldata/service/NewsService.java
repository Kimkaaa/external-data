package com.example.externaldata.service;

import com.example.externaldata.dto.NewsItemDto;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class NewsService {

    private static final String NEWS_URL = "https://news.naver.com/section/101";
    private static final String NAVER_BASE_URL = "https://news.naver.com";

    private static final String HEADLINE_LIST_SELECTOR = "ul[id^=_SECTION_HEADLINE_LIST_]";
    private static final String ITEM_SELECTOR = "li.sa_item";
    private static final String LINK_SELECTOR = "a.sa_thumb_link, a.sa_text_title";
    private static final String TITLE_SELECTOR = "a.sa_text_title, strong.sa_text_strong, span.sa_text_title";

    public List<NewsItemDto> getTopNews() {
        List<NewsItemDto> result = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(NEWS_URL)
                    .userAgent("Mozilla/5.0")
                    .get();

            Element list = doc.selectFirst(HEADLINE_LIST_SELECTOR);
            if (list == null) {
                log.warn("뉴스 목록 요소를 찾지 못했습니다. url={}", NEWS_URL);
                return result;
            }

            Elements items = list.select(ITEM_SELECTOR);

            for (Element item : items) {
                if (result.size() >= 3) {
                    break;
                }

                Element linkElement = item.selectFirst(LINK_SELECTOR);
                if (linkElement == null) {
                    continue;
                }

                String link = linkElement.attr("href").trim();
                if (link.startsWith("/")) {
                    link = NAVER_BASE_URL + link;
                }

                Element titleElement = item.selectFirst(TITLE_SELECTOR);
                String title = titleElement != null ? titleElement.text().trim() : "";

                Element imgElement = item.selectFirst("img");
                String imageUrl = "";
                if (imgElement != null) {
                    imageUrl = imgElement.hasAttr("data-src")
                            ? imgElement.attr("data-src").trim()
                            : imgElement.attr("src").trim();
                }

                result.add(new NewsItemDto(title, link, imageUrl));
            }

        } catch (Exception e) {
            log.error("뉴스 크롤링 중 오류가 발생했습니다.", e);
        }

        return result;
    }
}