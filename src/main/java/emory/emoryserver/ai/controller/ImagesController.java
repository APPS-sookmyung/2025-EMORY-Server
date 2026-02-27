package emory.emoryserver.ai.controller;

import emory.emoryserver.ai.model.ImageAsset;
import emory.emoryserver.ai.repository.ImageAssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@RestController
@RequiredArgsConstructor
@RequestMapping("/images")
public class ImagesController {

    private final ImageAssetRepository imageAssetRepository;

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable String id) {
        ImageAsset asset = imageAssetRepository.findById(id).orElse(null);
        if (asset == null || asset.getBase64() == null || asset.getBase64().isBlank()) {
            return ResponseEntity.notFound().build();
        }

        byte[] bytes = Base64.getDecoder().decode(asset.getBase64());
        MediaType mt = MediaType.IMAGE_PNG;
        if (asset.getMimeType() != null && asset.getMimeType().contains("jpeg")) mt = MediaType.IMAGE_JPEG;

        return ResponseEntity.ok()
                .contentType(mt)
                .cacheControl(CacheControl.noCache())
                .body(bytes);
    }
}
