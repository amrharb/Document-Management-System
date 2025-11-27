package net.atos.dms.workspace;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {
    private final WorkspaceRepository repo;

    public WorkspaceController(WorkspaceRepository r) {
        this.repo = r;
    }

    @GetMapping
    public List<Workspace> list(Authentication auth) {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Workspace> get(@PathVariable("id") String id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Workspace create(@RequestBody Workspace w, Authentication auth) {
        w.setOwnerEmail(auth.getName());
        return repo.save(w);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Workspace> update(@PathVariable("id") String id, @RequestBody Workspace dto) {
        return repo.findById(id).map(w -> {
            w.setName(dto.getName());
            return ResponseEntity.ok(repo.save(w));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") String id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
