package com.sanity.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.sanity.dto.FaceVerificationResultDto;
import com.sanity.model.DocumentoTerapeuta;
import com.sanity.model.Persona;
import com.sanity.model.Terapeuta;
import com.sanity.repository.DocumentoTerapeutaRepository;
import com.sanity.repository.PersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class FaceVerificationService {

    private final RekognitionClient rekognitionClient;
    private final CloudStorageService cloudStorageService;
    private final Storage storage;
    private final String bucketName;
    private final DocumentoTerapeutaRepository documentoRepository;
    private final PersonaRepository personaRepository;

    @Value("${aws.rekognition.similarity-threshold:80.0}")
    private float similarityThreshold;

    public FaceVerificationService(
            @Autowired(required = false) RekognitionClient rekognitionClient,
            CloudStorageService cloudStorageService,
            Storage storage,
            @Qualifier("gcsBucketName") String bucketName,
            DocumentoTerapeutaRepository documentoRepository,
            PersonaRepository personaRepository) {
        this.rekognitionClient = rekognitionClient;
        this.cloudStorageService = cloudStorageService;
        this.storage = storage;
        this.bucketName = bucketName;
        this.documentoRepository = documentoRepository;
        this.personaRepository = personaRepository;

        if (rekognitionClient == null) {
            System.err.println("⚠️  FaceVerificationService: RekognitionClient no disponible. "
                    + "Configura AWS_ACCESS_KEY_ID y AWS_SECRET_ACCESS_KEY para habilitar la verificación facial.");
        }
    }

    /**
     * Verifica el rostro del terapeuta comparando su selfie con la foto del
     * documento de identidad.
     */
    public FaceVerificationResultDto verifyFace(String correoTerapeuta, MultipartFile selfieFile) throws IOException {
        // Verificar que Rekognition esté configurado
        if (rekognitionClient == null) {
            return FaceVerificationResultDto.error(
                    "El servicio de verificación facial no está configurado. Contacta al administrador.");
        }

        // 1. Buscar terapeuta
        Persona persona = personaRepository.findByCorreo(correoTerapeuta)
                .orElseThrow(() -> new RuntimeException("No se encontró el usuario con correo: " + correoTerapeuta));

        if (!(persona instanceof Terapeuta)) {
            throw new RuntimeException("El usuario no es un terapeuta.");
        }

        Terapeuta terapeuta = (Terapeuta) persona;

        // 2. Buscar el documento de identificación del terapeuta
        Optional<DocumentoTerapeuta> docIdentificacion = documentoRepository
                .findByTerapeutaIdPersonaAndTipoDocumento(terapeuta.getIdPersona(), "identificacion");

        if (docIdentificacion.isEmpty()) {
            return FaceVerificationResultDto.error(
                    "No se encontró un documento de identificación. Debes subir tu documento de identidad primero.");
        }

        DocumentoTerapeuta documento = docIdentificacion.get();

        // 3. Subir selfie a GCS para auditoría
        String selfieFolder = "selfies/" + terapeuta.getIdPersona();
        String selfieUrl = cloudStorageService.uploadFile(selfieFile, selfieFolder);

        // 4. Descargar los bytes de ambas imágenes desde GCS
        byte[] documentBytes = downloadFromGcs(documento.getUrlStorage());
        byte[] selfieBytes = selfieFile.getBytes();

        if (documentBytes == null || documentBytes.length == 0) {
            return FaceVerificationResultDto.error(
                    "No se pudo descargar la imagen del documento de identidad.");
        }

        // 5. Llamar a AWS Rekognition CompareFaces
        try {
            FaceVerificationResultDto result = compareFaces(documentBytes, selfieBytes, selfieUrl);

            // 6. Actualizar el documento con el resultado de la verificación facial
            documento.setVerificacionFacial(result.getEstado());
            documento.setSelfieUrl(selfieUrl);
            documentoRepository.save(documento);

            return result;

        } catch (RekognitionException e) {
            System.err.println("❌ Error en AWS Rekognition: " + e.getMessage());
            return FaceVerificationResultDto.error(
                    "Error en el servicio de verificación facial: " + e.getMessage());
        }
    }

    /**
     * Compara dos rostros usando AWS Rekognition.
     */
    private FaceVerificationResultDto compareFaces(byte[] sourceImageBytes, byte[] targetImageBytes, String selfieUrl) {
        Image sourceImage = Image.builder()
                .bytes(SdkBytes.fromByteArray(sourceImageBytes))
                .build();

        Image targetImage = Image.builder()
                .bytes(SdkBytes.fromByteArray(targetImageBytes))
                .build();

        CompareFacesRequest request = CompareFacesRequest.builder()
                .sourceImage(sourceImage)
                .targetImage(targetImage)
                .similarityThreshold(similarityThreshold)
                .build();

        System.out.println("🔍 AWS Rekognition - Comparando rostros con umbral: " + similarityThreshold + "%");

        CompareFacesResponse response = rekognitionClient.compareFaces(request);

        List<CompareFacesMatch> matches = response.faceMatches();
        List<ComparedFace> unmatchedFaces = response.unmatchedFaces();

        if (matches != null && !matches.isEmpty()) {
            float similarity = matches.get(0).similarity();
            System.out.println("✅ AWS Rekognition - Rostro coincide. Similitud: " + similarity + "%");
            return FaceVerificationResultDto.match(similarity, selfieUrl);
        }

        if (unmatchedFaces != null && !unmatchedFaces.isEmpty()) {
            System.out.println("❌ AWS Rekognition - Rostros no coinciden.");
            return FaceVerificationResultDto.noMatch(0, selfieUrl);
        }

        System.out.println("⚠️ AWS Rekognition - No se detectaron rostros.");
        return FaceVerificationResultDto.noFaceDetected(
                "Asegúrate de que la selfie y el documento tengan rostros visibles y claros.");
    }

    /**
     * Descarga los bytes de un archivo desde Google Cloud Storage.
     */
    private byte[] downloadFromGcs(String fileUrl) {
        try {
            String objectName = fileUrl.replace(
                    "https://storage.googleapis.com/" + bucketName + "/", "");
            Blob blob = storage.get(bucketName, objectName);
            if (blob == null) {
                System.err.println("⚠️ No se encontró el archivo en GCS: " + objectName);
                return null;
            }
            return blob.getContent();
        } catch (Exception e) {
            System.err.println("❌ Error al descargar de GCS: " + e.getMessage());
            return null;
        }
    }
}
