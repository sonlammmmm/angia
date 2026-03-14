package vn.dichvuangia.management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import vn.dichvuangia.management.common.ApiResponse;
import vn.dichvuangia.management.service.FileStorageService;

import java.util.Objects;

@Tag(name = "File Upload", description = "Upload ảnh cho sản phẩm, thương hiệu, dịch vụ")
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @Operation(summary = "Upload ảnh — Authenticated users")
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> upload(
            @RequestParam("file") MultipartFile file) {

        String filename = fileStorageService.store(file);

        // Build URL trả về client: /api/v1/files/{filename}
        String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/files/")
                .path(Objects.requireNonNull(filename))
                .toUriString();

        return ResponseEntity.ok(ApiResponse.success("Upload thành công", fileUrl));
    }
}
