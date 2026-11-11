package org.example.gameAuth;

/**
 * Наборы заголовков, имитирующие запросы браузера Firefox к farmrpg.com.
 * Формат: пары "имя", "значение" для HttpRequest.Builder#headers(String...).
 */
public final class BrowserHeaders {
    // Общие для любого запроса браузера
    public static final String[] _common = {
            "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:159.0) Gecko/20100101 Firefox/159.0",
            "Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7",
            "Sec-GPC", "1"
    };

    // Открытие страницы (переход по адресу в браузере)
    public static final String[] _page = {
            "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
            "Upgrade-Insecure-Requests", "1",
            "Sec-Fetch-Dest", "document",
            "Sec-Fetch-Mode", "navigate",
            "Sec-Fetch-Site", "none",
            "Sec-Fetch-User", "?1",
            "Priority", "u=0, i"
    };

    // AJAX-запрос со страницы farmrpg.com (worker.php)
    public static final String[] _ajax = {
            "Accept", "*/*",
            "Content-Type", "application/x-www-form-urlencoded; charset=UTF-8",
            "Origin", "https://farmrpg.com",
            "Referer", "https://farmrpg.com/",
            "X-Requested-With", "XMLHttpRequest",
            "Sec-Fetch-Dest", "empty",
            "Sec-Fetch-Mode", "cors",
            "Sec-Fetch-Site", "same-origin",
            "Priority", "u=0"
    };

    private BrowserHeaders() {
    }
}

