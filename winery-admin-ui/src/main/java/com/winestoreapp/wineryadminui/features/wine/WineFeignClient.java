package com.winestoreapp.wineryadminui.features.wine;

import com.winestoreapp.wineryadminui.core.config.FeignConfig;
import com.winestoreapp.wineryadminui.features.wine.dto.WineCreateRequestDto;
import com.winestoreapp.wineryadminui.features.wine.dto.WineDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@FeignClient(
        name = "wineFeignClient",
        url = "${api.backend-url}/wines",
        configuration = FeignConfig.class
)
public interface WineFeignClient {

    @PostMapping
    WineDto createWine(@RequestBody WineCreateRequestDto createDto);

    @DeleteMapping("/{id}")
    void deleteWine(@PathVariable("id") Long id);

    @GetMapping
    List<WineDto> getAll();

    @PatchMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    WineDto updateWineImages(
            @PathVariable("id") Long id,
            @RequestPart("imageA") MultipartFile imageA,
            @RequestPart("imageB") MultipartFile imageB
    );

}
