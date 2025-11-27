package net.atos.dms.search;

import net.atos.dms.document.DocAsset;
import net.atos.dms.document.DocService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
public class SearchController {
    private final DocService svc;

    public SearchController(DocService s) {
        this.svc = s;
    }

    @GetMapping
    public Map<String, Object> search(@RequestParam String workspaceId, @RequestParam String q, @RequestParam(defaultValue = "50") int limit) {
        List<DocAsset> docs = svc.searchSmart(workspaceId, q, limit);
        return Map.of("docs", docs, "count", docs.size());
    }
}
