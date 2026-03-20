package com.sanity.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudStorageService {

    private final Storage storage;

    @Qualifier("gcsBucketName")
    private final String bucketName;

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    /**
     * Sube un archivo a Google Cloud Storage.
     *
     * @param file   archivo a subir
     * @param folder carpeta dentro del bucket (ej: "documentos/tarjeta_profesional")
     * @return URL pública del archivo subido
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        // Validar tipo de archivo
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Tipo de archivo no permitido. Solo se aceptan PDF, JPG y PNG.");
        }

        // Validar tamaño
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "El archivo excede el tamaño máximo permitido de 10MB.");
        }

        // Generar nombre único
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = folder + "/" + UUID.randomUUID().toString() + extension;

        // Subir a GCS
        BlobId blobId = BlobId.of(bucketName, uniqueFilename);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .build();

        storage.create(blobInfo, file.getBytes());

        // Retornar URL pública
        return String.format("https://storage.googleapis.com/%s/%s", bucketName, uniqueFilename);
    }
}
