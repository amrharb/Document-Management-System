package net.atos.dms.folder;

import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/folders")
public class FolderController {
    private final FolderRepository repo;
    private final FolderService svc;

    public FolderController(FolderRepository r, FolderService s) {
        this.repo = r;
        this.svc = s;
    }

    public record CreateDto(@NotBlank String workspaceId, String parentId, @NotBlank String name) {
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateDto dto, Authentication auth) {
        String owner = auth.getName();
        var ancestors = svc.findAncestors(dto.parentId());
        var f = svc.create(dto.workspaceId(), owner, dto.name(), dto.parentId(), ancestors);
        return ResponseEntity.ok(f);
    }

    @GetMapping("/tree")
    public List<FolderService.TreeNode> tree(@RequestParam String workspaceId) {
        return svc.tree(workspaceId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        var f = repo.findById(id).orElseThrow();
        svc.softDeleteCascade(f);
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }
}
