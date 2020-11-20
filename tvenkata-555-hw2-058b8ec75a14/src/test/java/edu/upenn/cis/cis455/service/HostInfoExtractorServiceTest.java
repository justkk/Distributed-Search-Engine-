package edu.upenn.cis.cis455.service;

import edu.upenn.cis.cis455.crawler.service.HostInfoExtractorService;
import edu.upenn.cis.cis455.crawler.service.URLDataExtractorService;
import edu.upenn.cis.cis455.model.hostRobotInfo.HostRobotInfo;
import edu.upenn.cis.cis455.model.hostRobotInfo.HostRobotInfoKey;
import edu.upenn.cis.cis455.model.representationModels.URLResponse;
import edu.upenn.cis.cis455.storage.managers.HostRobotInfoManager;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.util.Date;
import java.util.HashMap;

public class HostInfoExtractorServiceTest extends TestCase {

    private HostRobotInfoManager hostRobotInfoManager;
    private URLDataExtractorService urlDataExtractorService;
    private HostInfoExtractorService hostInfoExtractorService;

    String data = "User-agent: *\n" +
            "Disallow: /search\n" +
            "Allow: /search/about\n" +
            "Allow: /search/static\n" +
            "Allow: /search/howsearchworks\n" +
            "Disallow: /sdch\n" +
            "Disallow: /groups\n" +
            "Disallow: /index.html?\n" +
            "Disallow: /?\n" +
            "Allow: /?hl=\n" +
            "Disallow: /?hl=*&\n" +
            "Allow: /?hl=*&gws_rd=ssl$\n" +
            "Disallow: /?hl=*&*&gws_rd=ssl\n" +
            "Allow: /?gws_rd=ssl$\n" +
            "Allow: /?pt1=true$\n" +
            "Disallow: /imgres\n" +
            "Disallow: /u/\n" +
            "Disallow: /preferences\n" +
            "Disallow: /setprefs\n" +
            "Disallow: /default\n" +
            "Disallow: /m?\n" +
            "Disallow: /m/\n" +
            "Allow:    /m/finance\n" +
            "Disallow: /wml?\n" +
            "Disallow: /wml/?\n" +
            "Disallow: /wml/search?\n" +
            "Disallow: /xhtml?\n" +
            "Disallow: /xhtml/?\n" +
            "Disallow: /xhtml/search?\n" +
            "Disallow: /xml?\n" +
            "Disallow: /imode?\n" +
            "Disallow: /imode/?\n" +
            "Disallow: /imode/search?\n" +
            "Disallow: /jsky?\n" +
            "Disallow: /jsky/?\n" +
            "Disallow: /jsky/search?\n" +
            "Disallow: /pda?\n" +
            "Disallow: /pda/?\n" +
            "Disallow: /pda/search?\n" +
            "Disallow: /sprint_xhtml\n" +
            "Disallow: /sprint_wml\n" +
            "Disallow: /pqa\n" +
            "Disallow: /palm\n" +
            "Disallow: /gwt/\n" +
            "Disallow: /purchases\n" +
            "Disallow: /local?\n" +
            "Disallow: /local_url\n" +
            "Disallow: /shihui?\n" +
            "Disallow: /shihui/\n" +
            "Disallow: /products?\n" +
            "Disallow: /product_\n" +
            "Disallow: /products_\n" +
            "Disallow: /products;\n" +
            "Disallow: /print\n" +
            "Disallow: /books/\n" +
            "Disallow: /bkshp?*q=*\n" +
            "Disallow: /books?*q=*\n" +
            "Disallow: /books?*output=*\n" +
            "Disallow: /books?*pg=*\n" +
            "Disallow: /books?*jtp=*\n" +
            "Disallow: /books?*jscmd=*\n" +
            "Disallow: /books?*buy=*\n" +
            "Disallow: /books?*zoom=*\n" +
            "Allow: /books?*q=related:*\n" +
            "Allow: /books?*q=editions:*\n" +
            "Allow: /books?*q=subject:*\n" +
            "Allow: /books/about\n" +
            "Allow: /booksrightsholders\n" +
            "Allow: /books?*zoom=1*\n" +
            "Allow: /books?*zoom=5*\n" +
            "Allow: /books/content?*zoom=1*\n" +
            "Allow: /books/content?*zoom=5*\n" +
            "Disallow: /ebooks/\n" +
            "Disallow: /ebooks?*q=*\n" +
            "Disallow: /ebooks?*output=*\n" +
            "Disallow: /ebooks?*pg=*\n" +
            "Disallow: /ebooks?*jscmd=*\n" +
            "Disallow: /ebooks?*buy=*\n" +
            "Disallow: /ebooks?*zoom=*\n" +
            "Allow: /ebooks?*q=related:*\n" +
            "Allow: /ebooks?*q=editions:*\n" +
            "Allow: /ebooks?*q=subject:*\n" +
            "Allow: /ebooks?*zoom=1*\n" +
            "Allow: /ebooks?*zoom=5*\n" +
            "Disallow: /patents?\n" +
            "Disallow: /patents/download/\n" +
            "Disallow: /patents/pdf/\n" +
            "Disallow: /patents/related/\n" +
            "Disallow: /scholar\n" +
            "Disallow: /citations?\n" +
            "Allow: /citations?user=\n" +
            "Disallow: /citations?*cstart=\n" +
            "Allow: /citations?view_op=new_profile\n" +
            "Allow: /citations?view_op=top_venues\n" +
            "Allow: /scholar_share\n" +
            "Disallow: /s?\n" +
            "Allow: /maps?*output=classic*\n" +
            "Allow: /maps?*file=\n" +
            "Allow: /maps/api/js?\n" +
            "Allow: /maps/d/\n" +
            "Disallow: /maps?\n" +
            "Disallow: /mapstt?\n" +
            "Disallow: /mapslt?\n" +
            "Disallow: /maps/stk/\n" +
            "Disallow: /maps/br?\n" +
            "Disallow: /mapabcpoi?\n" +
            "Disallow: /maphp?\n" +
            "Disallow: /mapprint?\n" +
            "Disallow: /maps/api/js/\n" +
            "Disallow: /maps/api/staticmap?\n" +
            "Disallow: /maps/api/streetview\n" +
            "Disallow: /mld?\n" +
            "Disallow: /staticmap?\n" +
            "Disallow: /maps/preview\n" +
            "Disallow: /maps/place\n" +
            "Disallow: /maps/timeline/\n" +
            "Disallow: /help/maps/streetview/partners/welcome/\n" +
            "Disallow: /help/maps/indoormaps/partners/\n" +
            "Disallow: /lochp?\n" +
            "Disallow: /center\n" +
            "Disallow: /ie?\n" +
            "Disallow: /blogsearch/\n" +
            "Disallow: /blogsearch_feeds\n" +
            "Disallow: /advanced_blog_search\n" +
            "Disallow: /uds/\n" +
            "Disallow: /chart?\n" +
            "Disallow: /transit?\n" +
            "Disallow: /calendar/feeds/\n" +
            "Disallow: /calendar/ical/\n" +
            "Disallow: /cl2/feeds/\n" +
            "Disallow: /cl2/ical/\n" +
            "Disallow: /coop/directory\n" +
            "Disallow: /coop/manage\n" +
            "Disallow: /trends?\n" +
            "Disallow: /trends/music?\n" +
            "Disallow: /trends/hottrends?\n" +
            "Disallow: /trends/viz?\n" +
            "Disallow: /trends/embed.js?\n" +
            "Disallow: /trends/fetchComponent?\n" +
            "Disallow: /trends/beta\n" +
            "Disallow: /trends/topics\n" +
            "Disallow: /musica\n" +
            "Disallow: /musicad\n" +
            "Disallow: /musicas\n" +
            "Disallow: /musicl\n" +
            "Disallow: /musics\n" +
            "Disallow: /musicsearch\n" +
            "Disallow: /musicsp\n" +
            "Disallow: /musiclp\n" +
            "Disallow: /urchin_test/\n" +
            "Disallow: /movies?\n" +
            "Disallow: /wapsearch?\n" +
            "Allow: /safebrowsing/diagnostic\n" +
            "Allow: /safebrowsing/report_badware/\n" +
            "Allow: /safebrowsing/report_error/\n" +
            "Allow: /safebrowsing/report_phish/\n" +
            "Disallow: /reviews/search?\n" +
            "Disallow: /orkut/albums\n" +
            "Disallow: /cbk\n" +
            "Allow: /cbk?output=tile&cb_client=maps_sv\n" +
            "Disallow: /maps/api/js/AuthenticationService.Authenticate\n" +
            "Disallow: /maps/api/js/QuotaService.RecordEvent\n" +
            "Disallow: /recharge/dashboard/car\n" +
            "Disallow: /recharge/dashboard/static/\n" +
            "Disallow: /profiles/me\n" +
            "Allow: /profiles\n" +
            "Disallow: /s2/profiles/me\n" +
            "Allow: /s2/profiles\n" +
            "Allow: /s2/oz\n" +
            "Allow: /s2/photos\n" +
            "Allow: /s2/search/social\n" +
            "Allow: /s2/static\n" +
            "Disallow: /s2\n" +
            "Disallow: /transconsole/portal/\n" +
            "Disallow: /gcc/\n" +
            "Disallow: /aclk\n" +
            "Disallow: /cse?\n" +
            "Disallow: /cse/home\n" +
            "Disallow: /cse/panel\n" +
            "Disallow: /cse/manage\n" +
            "Disallow: /tbproxy/\n" +
            "Disallow: /imesync/\n" +
            "Disallow: /shenghuo/search?\n" +
            "Disallow: /support/forum/search?\n" +
            "Disallow: /reviews/polls/\n" +
            "Disallow: /hosted/images/\n" +
            "Disallow: /ppob/?\n" +
            "Disallow: /ppob?\n" +
            "Disallow: /accounts/ClientLogin\n" +
            "Disallow: /accounts/ClientAuth\n" +
            "Disallow: /accounts/o8\n" +
            "Allow: /accounts/o8/id\n" +
            "Disallow: /topicsearch?q=\n" +
            "Disallow: /xfx7/\n" +
            "Disallow: /squared/api\n" +
            "Disallow: /squared/search\n" +
            "Disallow: /squared/table\n" +
            "Disallow: /qnasearch?\n" +
            "Disallow: /app/updates\n" +
            "Disallow: /sidewiki/entry/\n" +
            "Disallow: /quality_form?\n" +
            "Disallow: /labs/popgadget/search\n" +
            "Disallow: /buzz/post\n" +
            "Disallow: /compressiontest/\n" +
            "Disallow: /analytics/feeds/\n" +
            "Disallow: /analytics/partners/comments/\n" +
            "Disallow: /analytics/portal/\n" +
            "Disallow: /analytics/uploads/\n" +
            "Allow: /alerts/manage\n" +
            "Allow: /alerts/remove\n" +
            "Disallow: /alerts/\n" +
            "Allow: /alerts/$\n" +
            "Disallow: /ads/search?\n" +
            "Disallow: /ads/plan/action_plan?\n" +
            "Disallow: /ads/plan/api/\n" +
            "Disallow: /ads/hotels/partners\n" +
            "Disallow: /phone/compare/?\n" +
            "Disallow: /travel/clk\n" +
            "Disallow: /hotelfinder/rpc\n" +
            "Disallow: /hotels/rpc\n" +
            "Disallow: /commercesearch/services/\n" +
            "Disallow: /evaluation/\n" +
            "Disallow: /chrome/browser/mobile/tour\n" +
            "Disallow: /compare/*/apply*\n" +
            "Disallow: /forms/perks/\n" +
            "Disallow: /shopping/suppliers/search\n" +
            "Disallow: /ct/\n" +
            "Disallow: /edu/cs4hs/\n" +
            "Disallow: /trustedstores/s/\n" +
            "Disallow: /trustedstores/tm2\n" +
            "Disallow: /trustedstores/verify\n" +
            "Disallow: /adwords/proposal\n" +
            "Disallow: /shopping/product/\n" +
            "Disallow: /shopping/seller\n" +
            "Disallow: /shopping/reviewer\n" +
            "Disallow: /about/careers/applications/\n" +
            "Disallow: /landing/signout.html\n" +
            "Disallow: /webmasters/sitemaps/ping?\n" +
            "Disallow: /ping?\n" +
            "Disallow: /gallery/\n" +
            "Disallow: /landing/now/ontap/\n" +
            "Allow: /searchhistory/\n" +
            "Allow: /maps/reserve\n" +
            "Allow: /maps/reserve/partners\n" +
            "Disallow: /maps/reserve/api/\n" +
            "Disallow: /maps/reserve/search\n" +
            "Disallow: /maps/reserve/bookings\n" +
            "Disallow: /maps/reserve/settings\n" +
            "Disallow: /maps/reserve/manage\n" +
            "Disallow: /maps/reserve/payment\n" +
            "Disallow: /maps/reserve/receipt\n" +
            "Disallow: /maps/reserve/sellersignup\n" +
            "Disallow: /maps/reserve/payments\n" +
            "Disallow: /maps/reserve/feedback\n" +
            "Disallow: /maps/reserve/terms\n" +
            "Disallow: /maps/reserve/m/\n" +
            "Disallow: /maps/reserve/b/\n" +
            "Disallow: /maps/reserve/partner-dashboard\n" +
            "Disallow: /about/views/\n" +
            "Disallow: /intl/*/about/views/\n" +
            "Disallow: /local/dining/\n" +
            "Disallow: /local/place/products/\n" +
            "Disallow: /local/place/reviews/\n" +
            "Disallow: /local/place/rap/\n" +
            "Disallow: /local/tab/\n" +
            "Allow: /finance\n" +
            "Allow: /js/\n" +
            "Disallow: /finance?*q=*\n" +
            "\n" +
            "# Certain social media sites are whitelisted to allow crawlers to access page markup when links to google.com/imgres* are shared. To learn more, please contact images-robots-whitelist@google.com.\n" +
            "User-agent: Twitterbot\n" +
            "Allow: /imgres\n" +
            "\n" +
            "User-agent: facebookexternalhit\n" +
            "Allow: /imgres\n" +
            "\n" +
            "Sitemap: http://www.gstatic.com/s2/sitemaps/profiles-sitemap.xml\n" +
            "Sitemap: https://www.google.com/sitemap.xml\n" +

            "User-agent: facebookexternalhit\n" +
            "User-agent: nikhilTemp\n" +
            "Allow: /allalooya\n";

    @Before
    public void setUp() {

        hostRobotInfoManager = Mockito.mock(HostRobotInfoManager.class);
        urlDataExtractorService = Mockito.mock(URLDataExtractorService.class);
        hostInfoExtractorService = new HostInfoExtractorService(hostRobotInfoManager, urlDataExtractorService);
    }


    @Test
    public void test1() {

        HostRobotInfo hostRobotInfo = new HostRobotInfo();
        HostRobotInfoKey hostRobotInfoKey = new HostRobotInfoKey("http", "www.google.com", 80);
        hostRobotInfo.setHostRobotInfoKey(hostRobotInfoKey);
        hostRobotInfo.setLastUpdatedDate(new Date());
        Mockito.when(hostRobotInfoManager.getHostRobotInfo(Matchers.any(), Matchers.any()))
                .thenReturn(hostRobotInfo);
        Assert.assertEquals(hostRobotInfo, hostInfoExtractorService.getHostInfo("http", "www.google.com",
                80, null));

    }

    @Test
    public void test2() {

        HostRobotInfo hostRobotInfo = new HostRobotInfo();
        HostRobotInfoKey hostRobotInfoKey = new HostRobotInfoKey("http", "www.google.com", 80);
        hostRobotInfo.setHostRobotInfoKey(hostRobotInfoKey);
        hostRobotInfo.setLastUpdatedDate(new Date(new Date().getTime() - 1000000));
        Mockito.when(hostRobotInfoManager.getHostRobotInfo(Matchers.any(), Matchers.any()))
                .thenReturn(hostRobotInfo);

        Mockito.when(urlDataExtractorService.getUrlData(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(new
                URLResponse(200, data, "temp", 200, 10000, new HashMap<>()));

        Mockito.when(hostRobotInfoManager.insertHostRobotInfo(Matchers.any(), Matchers.any())).thenReturn(hostRobotInfo);

        HostRobotInfo response = hostInfoExtractorService.getHostInfo("http", "www.google.com",
                80, null);

        Assert.assertEquals(response.getRobotStructure(), data);
    }


}
