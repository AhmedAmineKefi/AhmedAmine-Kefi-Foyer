package tn.esprit.spring.Config;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;
import java.time.Duration;

@Configuration
public class OpenTelemetryConfig {

    @Value("${otel.service.name:foyer-app}")
    private String serviceName;
    
    @Value("${otel.exporter.otlp.endpoint:http://localhost:4317}")
    private String otlpEndpoint;

    private OpenTelemetry openTelemetry;
    private SdkTracerProvider tracerProvider;

    @Bean
    public OpenTelemetry openTelemetry() {
        Resource resource = Resource.getDefault()
                .merge(Resource.builder()
                        .put(ResourceAttributes.SERVICE_NAME, serviceName)
                        .put(ResourceAttributes.SERVICE_VERSION, "1.0.0")
                        .build());

        // Configure OTLP exporter
        OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint(otlpEndpoint)
                .build();

        // Configure tracer provider with proper shutdown handling
        tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(spanExporter)
                        .setMaxExportBatchSize(512)
                        .setExporterTimeout(Duration.ofSeconds(2))
                        .setScheduleDelay(Duration.ofSeconds(5))
                        .build())
                .setResource(resource)
                .build();

        openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .build();

        // Set as global instance
        GlobalOpenTelemetry.set(openTelemetry);

        return openTelemetry;
    }

    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer(serviceName, "1.0.0");
    }

    @PreDestroy
    public void cleanup() {
        if (tracerProvider != null) {
            tracerProvider.close();
        }
    }
}
