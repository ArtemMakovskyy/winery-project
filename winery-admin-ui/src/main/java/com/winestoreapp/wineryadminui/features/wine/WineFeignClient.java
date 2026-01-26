package com.winestoreapp.wineryadminui.features.wine;

import com.winestoreapp.wineryadminui.features.wine.dto.WineCreateRequestDto;
import com.winestoreapp.wineryadminui.features.wine.dto.WineDto;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "wineFeignClient",
        url = "${api.backend-url}/wines"
)
public interface WineFeignClient {

    @PostMapping
    WineDto createWine(@RequestBody WineCreateRequestDto createDto);

    @DeleteMapping("/{id}")
    void deleteWine(@PathVariable("id") Long id);

    @GetMapping
    List<WineDto> getAll();

}
