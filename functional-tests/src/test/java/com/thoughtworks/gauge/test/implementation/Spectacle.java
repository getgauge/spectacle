package com.thoughtworks.gauge.test.implementation;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.Table;
import com.thoughtworks.gauge.test.common.Util;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Node;
import se.fishtank.css.selectors.Selectors;
import se.fishtank.css.selectors.dom.W3CNode;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import static com.thoughtworks.gauge.test.common.GaugeProject.getCurrentProject;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Spectacle {

    @Step("Verify spectacle documentation statistics with <statistics>")
    public void verifyStatistics(Table statistics) throws IOException {
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
        LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
        java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);
        final WebClient webClient = getWebClient();
        String documentationPath = getSpectaclePath();
        assertTrue(Files.exists(Paths.get(getCurrentProject().getProjectDir().getAbsolutePath())));
        assertTrue(Files.exists(Paths.get(documentationPath)));
        final HtmlPage page = webClient.getPage("file://" + documentationPath);
        Selectors<Node, W3CNode>  selectors = new Selectors<>(new W3CNode(page.getDocumentElement()));
        String expectedTotalSpecificationsCount = statistics.getTableRows().get(0).getCell("totalSpecificationsCount");
        String actualTotalSpecificationsCount = selectors.querySelectorAll(".specs .stats .stat").get(0).getTextContent();
        assertEquals("Total specifications count:", expectedTotalSpecificationsCount, actualTotalSpecificationsCount);
        String expectedTotalScenariosCount = statistics.getTableRows().get(0).getCell("totalScenariosCount");
        String actualTotalScenariosCount = selectors.querySelectorAll(".specs .stats .stat").get(1).getTextContent();
        assertEquals("Total scenarios count:", expectedTotalScenariosCount, actualTotalScenariosCount);
        
    }

    private String getSpectaclePath() {
        return getSpectaclePath(null);
    }

    private String getSpectaclePath(String specName) {
        String baseSpectaclePath = Util.combinePath(getCurrentProject().getProjectDir().getAbsolutePath(), "docs", "html");
        if (specName == null)
            return Util.combinePath(baseSpectaclePath, "index.html");
        return Util.combinePath(baseSpectaclePath, "specs", specName + ".html");
    }

    private WebClient getWebClient() {
        final WebClient webClient = new WebClient();
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        return webClient;
    }
    
}
