package com.winestoreapp.wine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winestoreapp.common.exception.CustomGlobalExceptionHandler;
import com.winestoreapp.common.observability.SpanTagger;
import com.winestoreapp.wine.api.WineService;
import com.winestoreapp.wine.api.dto.WineColor;
import com.winestoreapp.wine.api.dto.WineCreateRequestDto;
import com.winestoreapp.wine.api.dto.WineDto;
import com.winestoreapp.wine.api.dto.WineType;
import com.winestoreapp.wine.config.WineControllerTestConfig;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WineController.class)
@ContextConfiguration(classes = {
        WineControllerTestConfig.class,
        CustomGlobalExceptionHandler.class
})
class WineControllerTest {
    @MockBean
    private SpanTagger spanTagger;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WineService wineService;

    private WineDto wineDto;

    @BeforeEach
    void setUp() {
        wineDto = new WineDto();
        wineDto.setId(1L);
        wineDto.setName("Cabernet Sauvignon");
        wineDto.setPrice(BigDecimal.valueOf(25.99));
        wineDto.setYear(2017);
    }

    @Test
    @WithMockUser
    @DisplayName("GET /wines/{id} - Success")
    void findWineById_ShouldReturnWine() throws Exception {
        when(wineService.findById(1L)).thenReturn(wineDto);

        mockMvc.perform(get("/wines/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Cabernet Sauvignon"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /wines - Success")
    void findAllWines_ShouldReturnList() throws Exception {
        when(wineService.findAll(any())).thenReturn(List.of(wineDto));

        mockMvc.perform(get("/wines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Cabernet Sauvignon"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("POST /wines - Success (Manager)")
    void createWine_ValidData_ShouldReturnCreated() throws Exception {
        WineCreateRequestDto requestDto = new WineCreateRequestDto();
        requestDto.setVendorCode("V123");
        requestDto.setQualityLevel("Select");
        requestDto.setName("New Wine");
        requestDto.setYear(2020);
        requestDto.setPrice(BigDecimal.valueOf(15.0));
        requestDto.setGrape("Merlot");
        requestDto.setIsDecantation(true);
        requestDto.setWineType(WineType.DRY);
        requestDto.setStrengthFrom(BigDecimal.valueOf(12.0));
        requestDto.setStrengthTo(BigDecimal.valueOf(13.5));
        requestDto.setWineColor(WineColor.RED);

        when(wineService.add(any(WineCreateRequestDto.class))).thenReturn(wineDto);

        mockMvc.perform(post("/wines")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Cabernet Sauvignon"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("POST /wines - Forbidden (Customer)")
    void createWine_CustomerRole_ShouldReturnForbidden() throws Exception {
        WineCreateRequestDto validDto = new WineCreateRequestDto();
        validDto.setVendorCode("V123");
        validDto.setQualityLevel("Select");
        validDto.setName("Test Wine");
        validDto.setYear(2022);
        validDto.setPrice(BigDecimal.TEN);
        validDto.setGrape("Grape");
        validDto.setIsDecantation(false);
        validDto.setWineColor(WineColor.RED);
        validDto.setWineType(WineType.DRY);
        validDto.setStrengthFrom(BigDecimal.ONE);
        validDto.setStrengthTo(BigDecimal.TEN);

        mockMvc.perform(post("/wines")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("DELETE /wines/{id} - Success")
    void deleteWineById_ExistingId_ShouldReturnNoContent() throws Exception {

        doNothing().when(wineService).deleteById(1L);

        mockMvc.perform(delete("/wines/1").with(csrf()))
                .andExpect(status().isNoContent());

        verify(wineService).deleteById(1L);
    }


    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("POST /wines - Invalid Data (Blank Name)")
    void createWine_InvalidData_ShouldReturnBadRequest() throws Exception {
        WineCreateRequestDto invalidDto = new WineCreateRequestDto();
        invalidDto.setName("");

        mockMvc.perform(post("/wines")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }
}
