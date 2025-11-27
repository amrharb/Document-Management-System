package net.atos.dms.folder;

import net.atos.dms.document.DocAsset;
import net.atos.dms.document.DocAssetRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FolderService {
    private final FolderRepository repo;
    private final DocAssetRepository docRepo;

    public FolderService(FolderRepository r, DocAssetRepository d) {
        this.repo = r;
        this.docRepo = d;
    }

    public Folder create(String workspaceId, String ownerEmail, String name, String parentId, List<String> ancestors) {
        var f = new Folder();
        f.setWorkspaceId(workspaceId);
        f.setOwnerEmail(ownerEmail);
        f.setName(name);
        f.setParentId(parentId);
        f.setAncestors(new ArrayList<>(ancestors));
        return repo.save(f);
    }

    public record TreeNode(Folder folder, long folderSize, long fileCount, List<TreeNode> children) {
    }

    public List<TreeNode> tree(String workspaceId) {
        List<Folder> folders = repo.findAll().stream().filter(f -> workspaceId.equals(f.getWorkspaceId()) && !f.isDeleted()).collect(Collectors.toList());
        List<DocAsset> docs = docRepo.findAll().stream().filter(d -> workspaceId.equals(d.getWorkspaceId()) && !d.isDeleted()).collect(Collectors.toList());
        Map<String, List<Folder>> children = new HashMap<>();
        for (var f : folders) {
            String p = f.getParentId();
            if (p == null) p = "__ROOT__";
            children.computeIfAbsent(p, k -> new ArrayList<>()).add(f);
        }
        Map<String, Long> size = new HashMap<>();
        Map<String, Long> count = new HashMap<>();
        for (var d : docs) {
            for (var anc : d.getAncestors()) {
                size.merge(anc, d.getSize(), Long::sum);
                count.merge(anc, 1L, Long::sum);
            }
            size.merge(d.getFolderId(), d.getSize(), Long::sum);
            count.merge(d.getFolderId(), 1L, Long::sum);
        }
        List<TreeNode> roots = new ArrayList<>();
        for (var f : children.getOrDefault("__ROOT__", List.of())) roots.add(build(f, children, size, count));
        return roots;
    }

    private TreeNode build(Folder f, Map<String, List<Folder>> byParent, Map<String, Long> size, Map<String, Long> count) {
        List<TreeNode> kids = new ArrayList<>();
        for (var c : byParent.getOrDefault(f.getId(), List.of())) kids.add(build(c, byParent, size, count));
        long s = size.getOrDefault(f.getId(), 0L);
        long c = count.getOrDefault(f.getId(), 0L);
        return new TreeNode(f, s, c, kids);
    }

    public void softDeleteCascade(Folder root) {
        var descendants = repo.findByAncestorsContainsAndDeletedFalse(root.getId());
        root.setDeleted(true);
        repo.save(root);
        descendants.forEach(f -> {
            f.setDeleted(true);
            repo.save(f);
        });
        docRepo.findAll().forEach(d -> {
            if (!d.isDeleted() && (root.getId().equals(d.getFolderId()) || d.getAncestors().contains(root.getId()))) {
                d.setDeleted(true);
                docRepo.save(d);
            }
        });
    }

    public List<String> findAncestors(String parentId) {
        if (parentId == null) return new ArrayList<>();
        return repo.findById(parentId).map(f -> {
            var a = new ArrayList<>(f.getAncestors());
            a.add(f.getId());
            return a;
        }).orElseGet(ArrayList::new);
    }
}
