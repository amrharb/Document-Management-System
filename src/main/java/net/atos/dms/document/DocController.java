package net.atos.dms.document;

import jakarta.validation.constraints.NotBlank;
import net.atos.dms.search.TextAlgo;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocController {
    private final DocService svc;

    public DocController(DocService s) {
        this.svc = s;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam @NotBlank String workspaceId, @RequestParam @NotBlank String folderId, @RequestParam MultipartFile file, @RequestParam(required = false) List<String> tags, Authentication auth) throws IOException {
        String createdBy = auth.getName();
        String owner = createdBy;
        var d = svc.upload(workspaceId, owner, createdBy, folderId, file, tags == null ? List.of() : tags);
        return ResponseEntity.ok(d);
    }

    @GetMapping
    public Page<DocAsset> list(@RequestParam String workspaceId, @RequestParam String folderId, @RequestParam(defaultValue = "false") boolean includeDeleted, @RequestParam(required = false) String q, @RequestParam(defaultValue = "createdAt") String sortBy, @RequestParam(defaultValue = "DESC") org.springframework.data.domain.Sort.Direction dir, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return svc.list(workspaceId, folderId, includeDeleted, q, sortBy, dir, page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable String id) {
        return svc.get(id).<ResponseEntity<?>>map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> softDelete(@PathVariable String id) {
        svc.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable String id) throws IOException {
        var d = svc.get(id).orElseThrow();
        byte[] bytes = svc.read(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + d.getName() + "\"")
                .contentType(MediaType.parseMediaType(d.getContentType()))
                .body(bytes);
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<?> preview(@PathVariable String id) throws IOException {
        var d = svc.get(id).orElseThrow();
        byte[] bytes = svc.read(id);
        String b64 = Base64.getEncoder().encodeToString(bytes);
        return ResponseEntity.ok(Map.of("id", d.getId(), "name", d.getName(), "contentType", d.getContentType(), "base64", b64));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateMeta(@PathVariable String id, @RequestBody Map<String, Object> body) {
        return svc.get(id).map(d -> {
            if (body.containsKey("tags")) {
                Object tagsObj = body.get("tags");
                if (tagsObj instanceof java.util.List) {
                    // unchecked cast but acceptable here
                    d.setTags((List<String>) tagsObj);
                }
            }
            if (body.containsKey("version")) {
                Object v = body.get("version");
                if (v instanceof Number) {
                    d.setVersion(((Number) v).intValue());
                } else if (v instanceof String) {
                    try {
                        d.setVersion(Integer.parseInt((String) v));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            d.setNgrams(TextAlgo.ngrams(d.getName(), 3));
            return ResponseEntity.ok(d);
        }).orElse(ResponseEntity.notFound().build());
    }
}
