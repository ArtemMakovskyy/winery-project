package com.winestoreapp.wine.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winestoreapp.common.observability.SpanTagger;
import com.winestoreapp.wine.api.WineService;
import com.winestoreapp.wine.api.dto.WineCreateRequestDto;
import com.winestoreapp.wine.api.dto.WineDto;
import com.winestoreapp.wine.mapper.WineMapper;
import com.winestoreapp.wine.model.Wine;
import com.winestoreapp.wine.repository.WineRepository;
import io.micrometer.core.instrument.MeterRegistry;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest(classes = {
        WineServiceImpl.class,
        WineServiceCacheTest.TestRedisConfig.class
})
@ImportAutoConfiguration(CacheAutoConfiguration.class)
@TestPropertySource(properties = {
        "image.link.path=/img/"
})
class WineServiceCacheTest {

    @Container
    static GenericContainer<?> redisContainer =
            new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
    }

    @EnableCaching
    @EnableAsync(proxyTargetClass = true)
    static class TestRedisConfig {

        @Bean
        public org.springframework.data.redis.connection.RedisConnectionFactory redisConnectionFactory(
                @Value("${spring.data.redis.host}") String host,
                @Value("${spring.data.redis.port}") int port) {
            return new org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory(host, port);
        }

        @Bean(name = "mediumTtlCacheManager")
        @Primary
        public CacheManager cacheManager(
                org.springframework.data.redis.connection.RedisConnectionFactory connectionFactory) {

            var config = org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(30))
                    .disableCachingNullValues();

            return org.springframework.data.redis.cache.RedisCacheManager.builder(connectionFactory)
                    .cacheDefaults(config)
                    .build();
        }

        @Bean
        public StringRedisTemplate redisTemplate(
                org.springframework.data.redis.connection.RedisConnectionFactory connectionFactory) {
            return new StringRedisTemplate(connectionFactory);
        }
    }

    @Autowired
    private WineService wineService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @MockBean
    private ObjectMapper objectMapper;

    @MockBean
    private WineRepository wineRepository;

    @MockBean
    private WineMapper wineMapper;

    @MockBean
    private ImageStorageService imageStorageService;

    @MockBean
    private SpanTagger spanTagger;

    @MockBean
    private MeterRegistry registry;

    private Wine wine;
    private WineDto wineDto;

    @BeforeEach
    void setUp() {
        cacheManager.getCache("wines").clear();
        cacheManager.getCache("winesList").clear();

        wine = new Wine();
        wine.setId(1L);
        wine.setName("Test");
        wine.setPrice(BigDecimal.valueOf(25));

        wineDto = new WineDto();
        wineDto.setId(1L);
        wineDto.setName("Test");
        wineDto.setPrice(BigDecimal.valueOf(25));
    }

    @Test
    void findById_should_use_cache() {
        when(wineRepository.findById(1L)).thenReturn(Optional.of(wine));
        when(wineMapper.toDto(wine)).thenReturn(wineDto);

        wineService.findById(1L);
        wineService.findById(1L);

        verify(wineRepository, times(1)).findById(1L);
    }

    @Test
    void findAll_should_use_cache() {
        var pageable = PageRequest.of(0, 10, Sort.by("id").descending());

        when(wineRepository.findAll(pageable))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(wine)));
        when(wineMapper.toDto(wine)).thenReturn(wineDto);

        wineService.findAll(pageable);
        wineService.findAll(pageable);

        verify(wineRepository, times(1)).findAll(pageable);
    }

    @Test
    void add_should_evict_cache() {
        when(wineRepository.findById(1L)).thenReturn(Optional.of(wine));
        when(wineMapper.toDto(wine)).thenReturn(wineDto);

        Wine saved = new Wine();
        saved.setId(2L);

        when(wineMapper.toEntity(any())).thenReturn(saved);
        when(wineRepository.save(any())).thenReturn(saved);

        wineService.findById(1L);
        assertThat(cacheManager.getCache("wines").get(1L)).isNotNull();

        wineService.add(new WineCreateRequestDto());

        assertThat(cacheManager.getCache("wines").get(1L)).isNull();
    }

    @Test
    void ttl_should_exist() {
        when(wineRepository.findById(1L)).thenReturn(Optional.of(wine));
        when(wineMapper.toDto(wine)).thenReturn(wineDto);

        wineService.findById(1L);

        var conn = redisTemplate.getConnectionFactory().getConnection();
        Long ttl = conn.keyCommands().ttl("wines::1".getBytes());
        conn.close();

        assertThat(ttl).isGreaterThan(0);
    }
}