package info.novatec.metricscollector.google;

import java.util.ArrayList;
import java.util.List;

import org.influxdb.dto.Point;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import info.novatec.metricscollector.commons.MetricsValidator;
import info.novatec.metricscollector.commons.database.InfluxService;


@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsRepository {

    private static final String GA_PREFIX = "ga:";

    private final InfluxService influx;

    private final MetricsValidator metricsValidator;

    public void saveMetrics(List<Metrics> metricsForAllPages, String measurementName) {
        influx.savePoint(createPoints(metricsForAllPages, measurementName));
        influx.close();
    }

    List<Point> createPoints(List<Metrics> metricsForAllPages, String measurementName) {
        List<Point> points = new ArrayList<>();
        metricsForAllPages.forEach(metrics -> {
            Point.Builder pointBuilder = createPointBuilder(metrics, measurementName);
            if (pointBuilder != null){
                points.add(pointBuilder.build());
            }
        });
        log.info("Created {} points for measurement '{}'.", points.size(), measurementName);
        return points;
    }

    Point.Builder createPointBuilder(Metrics metrics, String measurementName) {
        log.info("Start creating points for page '{}'.", metrics.getPagePath());
        if (metricsValidator.hasNullValues(metrics)) {
            log.error(
                "Since there are null values in metrics, creating points for page '{}' isn't possible! Metrics content:\n{}",
                metrics.getPagePath(), metrics.toString());
            return null;
        }

        Point.Builder point = Point.measurement(measurementName).tag("pagePath", metrics.getPagePath());
        metrics.getMetrics().forEach((key, value) -> {
            key = key.startsWith(GA_PREFIX) ? key.substring(3) : key;
            point.addField(key, value);
        });

        return point;
    }

}
