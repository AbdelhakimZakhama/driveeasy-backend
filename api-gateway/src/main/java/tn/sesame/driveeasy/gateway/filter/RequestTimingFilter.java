package tn.sesame.driveeasy.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RequestTimingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestTimingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start = System.nanoTime();

        // header must be added before the response is committed
        exchange.getResponse().beforeCommit(() -> {
            long ms = (System.nanoTime() - start) / 1_000_000;
            exchange.getResponse().getHeaders().add("X-Response-Time-Ms", String.valueOf(ms));
            return Mono.empty();
        });

        return chain.filter(exchange).doFinally(signal -> {
            long ms = (System.nanoTime() - start) / 1_000_000;
            log.info("{} {} -> {} in {} ms",
                    exchange.getRequest().getMethod(),
                    exchange.getRequest().getURI().getPath(),
                    exchange.getResponse().getStatusCode(),
                    ms);
        });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;   // wraps everything, so the time is the full gateway time
    }
}