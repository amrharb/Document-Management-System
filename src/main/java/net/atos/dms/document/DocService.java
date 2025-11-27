package net.atos.dms.document;

import net.atos.dms.folder.Folder;
import net.atos.dms.folder.FolderRepository;
import net.atos.dms.search.TextAlgo;
import net.atos.dms.storage.LocalFileStorageService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DocService {
    private final DocAssetRepository repo;
    private final FolderRepository folderRepo;
    private final LocalFileStorageService storage;

    public DocService(DocAssetRepository r, FolderRepository f, LocalFileStorageService s) {
        this.repo = r;
        this.folderRepo = f;
        this.storage = s;
    }

    public DocAsset upload(String workspaceId, String ownerEmail, String createdBy, String folderId, MultipartFile file, List<String> tags) throws IOException {
        Folder folder = folderRepo.findById(folderId).orElseThrow();
        String relative = String.join("/", folder.getAncestors()) + "/" + folderId;
        String key = storage.save(ownerEmail, workspaceId, relative, Objects.requireNonNull(file.getOriginalFilename()), file);
        var d = new DocAsset();
        d.setWorkspaceId(workspaceId);
        d.setOwnerEmail(ownerEmail);
        d.setFolderId(folderId);
        d.setAncestors(new ArrayList<>(folder.getAncestors()));
        d.setName(file.getOriginalFilename());
        d.setContentType(Objects.requireNonNullElse(file.getContentType(), "application/octet-stream"));
        d.setSize(file.getSize());
        d.setFileKey(key);
        d.setTags(tags == null ? new ArrayList<>() : tags);
        d.setNgrams(TextAlgo.ngrams(d.getName(), 3));
        d.setCreatedBy(createdBy);
        return repo.save(d);
    }

    public Page<DocAsset> list(String workspaceId, String folderId, boolean includeDeleted, String q, String sortBy, org.springframework.data.domain.Sort.Direction dir, int page, int size) {
        Pageable p = PageRequest.of(page, size, Sort.by(dir, sortBy));
        String regex = (q == null || q.isBlank()) ? null : ".*" + java.util.regex.Pattern.quote(q) + ".*";

        if (regex == null) {
            if (includeDeleted) {
                return repo.findByWorkspaceIdAndFolderId(workspaceId, folderId, p);
            } else {
                return repo.findByWorkspaceIdAndFolderIdAndDeleted(workspaceId, folderId, false, p);
            }
        } else {
            if (includeDeleted) {
                return repo.findByWorkspaceIdAndFolderIdAndNameRegex(workspaceId, folderId, regex, p);
            } else {
                return repo.findByWorkspaceIdAndFolderIdAndDeletedAndNameRegex(workspaceId, folderId, false, regex, p);
            }
        }
    }

    public Optional<DocAsset> get(String id) {
        return repo.findById(id);
    }

    public void softDelete(String id) {
        repo.findById(id).ifPresent(d -> {
            d.setDeleted(true);
            repo.save(d);
        });
    }

    public byte[] read(String id) throws IOException {
        var d = repo.findById(id).orElseThrow();
        return storage.read(d.getFileKey());
    }

    public List<DocAsset> searchSmart(String workspaceId, String query, int limit) {
        String q = query.trim().toLowerCase(Locale.ROOT);
        List<String> grams = TextAlgo.ngrams(q, 3);
        Set<String> ids = new LinkedHashSet<>();
        for (var d : repo.findAll()) {
            if (!workspaceId.equals(d.getWorkspaceId()) || d.isDeleted()) continue;
            boolean cand = false;
            for (String g : grams)
                if (d.getNgrams().contains(g)) {
                    cand = true;
                    break;
                }
            if (!cand && q.length() < 3) cand = d.getName().toLowerCase(Locale.ROOT).contains(q);
            if (cand) ids.add(d.getId());
        }
        List<DocAsset> items = ids.stream().map(id -> repo.findById(id).orElseThrow()).collect(Collectors.toList());
        items.sort((a, b) -> Integer.compare(TextAlgo.score(b.getName(), q), TextAlgo.score(a.getName(), q)));
        if (items.size() > limit) return items.subList(0, limit);
        return items;
    }
}
