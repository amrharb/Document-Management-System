package net.atos.dms.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.Objects;

@Service
public class LocalFileStorageService {
    private final Path root;

    public LocalFileStorageService(@Value("${dms.storage.local-root}") String localRoot) {
        this.root = Path.of(localRoot).toAbsolutePath().normalize();
    }

    public String save(String ownerNid, String workspaceId, String relativePath, String filename, MultipartFile file) throws IOException {
        Objects.requireNonNull(ownerNid, "ownerNid required");
        Objects.requireNonNull(workspaceId, "workspaceId required");
        Objects.requireNonNull(filename, "filename required");
        Objects.requireNonNull(file, "file required");

        String safeFilename = Paths.get(filename).getFileName().toString();

        String cleaned = (relativePath == null) ? "" : relativePath.replace("\\", "/");
        while (cleaned.startsWith("/")) cleaned = cleaned.substring(1);
        while (cleaned.startsWith("./")) cleaned = cleaned.substring(2);

        Path dir = root.resolve(ownerNid).resolve(workspaceId).resolve(LocalDate.now().toString());
        if (!cleaned.isBlank()) {
            String[] segments = cleaned.split("/");
            for (String seg : segments) {
                if (seg == null || seg.isBlank()) continue;
                if (seg.equals("..")) throw new IOException("Invalid relative path segment: ..");
                if (seg.equals(".")) continue;
                dir = dir.resolve(seg);
            }
        }

        Files.createDirectories(dir);

        Path target = dir.resolve(safeFilename).normalize();

        if (!target.toAbsolutePath().normalize().startsWith(root)) {
            throw new IOException("Invalid target path");
        }

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        return root.relativize(target.toAbsolutePath().normalize()).toString().replace("\\", "/");
    }

    public byte[] read(String fileKey) throws IOException {
        if (fileKey == null || fileKey.isBlank()) throw new IOException("fileKey required");
        String cleaned = fileKey.replace("\\", "/");
        Path p = root.resolve(cleaned).toAbsolutePath().normalize();
        if (!p.startsWith(root)) throw new IOException("Invalid file path");
        if (!Files.exists(p)) throw new IOException("File not found: " + p);
        return Files.readAllBytes(p);
    }
}
