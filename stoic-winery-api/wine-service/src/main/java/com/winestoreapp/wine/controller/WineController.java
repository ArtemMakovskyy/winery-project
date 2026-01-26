package com.winestoreapp.wine.controller;

import com.winestoreapp.common.dto.ResponseErrorDto;
import com.winestoreapp.wine.api.WineService;
import com.winestoreapp.wine.api.dto.WineCreateRequestDto;
import com.winestoreapp.wine.api.dto.WineDto;
import jakarta.validation.Valid;
import java.net.MalformedURLException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.Tracer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Wine management", description = "Endpoints to managing wines")
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST,
        RequestMethod.PATCH, RequestMethod.DELETE})
@RequestMapping("/wines")
@Slf4j
public class WineController {
    private final WineService wineService;
    private final Tracer tracer;

    @Operation(summary = "Find wine by id",
            description = "Find existing wine by id. Available for all users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wine found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = WineDto.class))),
            @ApiResponse(responseCode = "404", description = "Wine not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDto.class)))
    })
    @GetMapping("/{id}")
    @Observed(name = "wine.controller", contextualName = "get-wine-by-id")
    public ResponseEntity<WineDto> findWineById(@PathVariable("id") Long id) {
        log.info("REST request to get wine by id: {}", id);
        if (tracer.currentSpan() != null) {
            tracer.currentSpan().tag("wine.id", String.valueOf(id));
        }
        return ResponseEntity.ok(wineService.findById(id));
    }

    @Operation(summary = "Find all wines",
            description = """
                    Find all wines. You can set pagination by: page, size, and sort parameters. 
                    By default, size 50, page = 0, sort by 'averageRatingScore,DESC' 
                    and after sort by 'id,DESC'. 
                    Pagination example: /wines?size=5&page=0&sort=id
                    Available for all users.""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of wines returned",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = WineDto.class))))
    })
    @GetMapping
    @Observed(name = "wine.controller", contextualName = "find-all-wines")
    public ResponseEntity<List<WineDto>> findAllWines(
            @PageableDefault(
                    page = 0,
                    size = 50,
                    sort = {"averageRatingScore", "id"},
                    direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.info("REST request to get all wines: {}", pageable);
        return ResponseEntity.ok(wineService.findAll(pageable));
    }

    @Operation(summary = "Creating a new Wine.",
            description = "Creating a new Wine with valid data. Available for managers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Wine created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = WineDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDto.class)))
    })
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping
    @Observed(name = "wine.controller", contextualName = "create-wine")
    public ResponseEntity<WineDto> createWine(@RequestBody @Valid WineCreateRequestDto createDto) {
        log.info("REST request to create new wine: {}", createDto.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(wineService.add(createDto));
    }

    @Operation(summary = "Add an image into path.",
            description = """
                    Add an image into path. Available for managers.
                    After you add or change photos, you will have to restart the app to view the changes.""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Images updated successfully"),
            @ApiResponse(responseCode = "404", description = "Wine not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDto.class)))
    })
    @PreAuthorize("hasRole('MANAGER')")
    @PatchMapping("/{id}/image")
    @Observed(name = "wine.controller", contextualName = "update-wine-images")
    public ResponseEntity<WineDto> addImageByIdIntoPath(
            @PathVariable("id") Long id,
            @RequestParam("imageA") MultipartFile imageA,
            @RequestParam("imageB") MultipartFile imageB
    ) throws MalformedURLException {
        log.info("REST request to update images for wine ID: {}", id);

        if (tracer.currentSpan() != null) {
            tracer.currentSpan().tag("wine.id", String.valueOf(id));
            tracer.currentSpan().tag("file.a.size", String.valueOf(imageA.getSize()));
            tracer.currentSpan().tag("file.b.size", String.valueOf(imageB.getSize()));
        }
        return ResponseEntity.ok(wineService.updateImage(id, imageA, imageB));
    }

    @Operation(summary = "Delete wine by id",
            description = "Delete existing wine by id. Available for managers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Wine deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "Wine not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseErrorDto.class)))
    })
    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/{id}")
    @Observed(name = "wine.controller", contextualName = "delete-wine")
    public ResponseEntity<Void> deleteWineById(@PathVariable("id") Long id) {
        log.info("REST request to delete wine by id: {}", id);

        if (tracer.currentSpan() != null) {
            tracer.currentSpan().tag("wine.id", String.valueOf(id));
        }

        wineService.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}