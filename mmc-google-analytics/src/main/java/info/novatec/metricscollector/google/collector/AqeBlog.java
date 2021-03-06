package info.novatec.metricscollector.google.collector;

import static info.novatec.metricscollector.google.DimensionFilterOperators.EXACT;
import static info.novatec.metricscollector.google.DimensionFilterOperators.NOT;
import static info.novatec.metricscollector.google.GoogleAnalyticsProperties.GA_HOSTNAME;
import static info.novatec.metricscollector.google.GoogleAnalyticsProperties.GA_PAGEPATH;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.google.api.services.analyticsreporting.v4.model.GetReportsResponse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import info.novatec.metricscollector.commons.MetricCollector;
import info.novatec.metricscollector.google.GoogleAnalyticsProperties;
import info.novatec.metricscollector.google.Metrics;
import info.novatec.metricscollector.google.RequestBuilder;
import info.novatec.metricscollector.google.ResponseParser;


@Component
@RequiredArgsConstructor
public class AqeBlog implements MetricCollector {

    private final GoogleAnalyticsProperties properties;

    private final RequestBuilder requestBuilder;

    private final ResponseParser responseParser;

    @Getter
    private final List<Metrics> metrics;

    @Override
    public void collect() {
        GetReportsResponse response = requestMetrics();
        responseParser.parse(response, metrics);
    }

    GetReportsResponse requestMetrics() {
        return requestBuilder.prepareRequest()
            .addDimensions(properties.getSharedDimensions())
            .addDimensions(properties.getAqeBlog().getSpecificDimensions())
            .addMetrics(properties.getSharedMetrics())
            .addMetrics(properties.getAqeBlog().getSpecificMetrics())
            .addDimensionFilter(GA_PAGEPATH, NOT, properties.getAqeBlog().getExcludedUrls())
            .addDimensionFilter(GA_HOSTNAME, EXACT, Collections.singletonList(properties.getAqeBlog().getHostName()))
            .buildRequest(properties.getAqeBlog().getViewId())
            .sendRequest();
    }
}
