package com.internhub.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * Service for handling Cloudinary file operations. Manages upload, download,
 * and deletion of documents in Cloudinary.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    private static final String LIBRARY_FOLDER = "internhub/library";
    private static final String CHAT_FOLDER = "internhub/chat";

    /**
     * Upload a document to the library folder in Cloudinary
     */
    public Map<String, Object> uploadToLibrary(MultipartFile file, String customPublicId) throws IOException {
        String publicId = customPublicId != null ? customPublicId : generatePublicId("lib");

        Map<String, Object> params = ObjectUtils.asMap(
                "public_id", LIBRARY_FOLDER + "/" + publicId,
                "resource_type", "raw", // For documents (PDF, DOCX, etc.)
                "overwrite", true,
                "invalidate", true,
                "tags", "library,internship-report"
        );

        Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), params);
        log.info("Document uploaded to library: {}", result.get("public_id"));
        return result;
    }

    /**
     * Upload a file for chat attachments
     */
    public Map<String, Object> uploadChatAttachment(MultipartFile file) throws IOException {
        String publicId = generatePublicId("chat");
        String resourceType = determineResourceType(file.getContentType());

        Map<String, Object> params = ObjectUtils.asMap(
                "public_id", CHAT_FOLDER + "/" + publicId,
                "resource_type", resourceType,
                "overwrite", false,
                "tags", "chat,attachment"
        );

        Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), params);
        log.info("Chat attachment uploaded: {}", result.get("public_id"));
        return result;
    }

    /**
     * Upload an image (for chat or profiles)
     */
    public Map<String, Object> uploadImage(MultipartFile file, String folder) throws IOException {
        String publicId = generatePublicId("img");

        Map<String, Object> params = ObjectUtils.asMap(
                "public_id", "internhub/" + folder + "/" + publicId,
                "resource_type", "image",
                "overwrite", true,
                "transformation", ObjectUtils.asMap(
                        "quality", "auto",
                        "fetch_format", "auto"
                )
        );

        return cloudinary.uploader().upload(file.getBytes(), params);
    }

    /**
     * Get a secure download URL for a document
     */
    public String getDownloadUrl(String publicId) {
        return cloudinary.url()
                .resourceType("raw")
                .secure(true)
                .generate(publicId);
    }

    /**
     * Get a secure URL with download attachment disposition
     */
    public String getDownloadUrlWithAttachment(String publicId, String originalFileName) {
        return cloudinary.url()
                .resourceType("raw")
                .secure(true)
                .transformation(new com.cloudinary.Transformation()
                        .flags("attachment:" + sanitizeFileName(originalFileName)))
                .generate(publicId);
    }

    /**
     * Get a preview URL for supported document types
     */
    public String getPreviewUrl(String publicId) {
        return cloudinary.url()
                .resourceType("raw")
                .secure(true)
                .generate(publicId);
    }

    /**
     * Delete a file from Cloudinary
     */
    public boolean deleteFile(String publicId, String resourceType) {
        try {
            Map<String, Object> result = cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap("resource_type", resourceType)
            );
            String status = (String) result.get("result");
            log.info("Delete result for {}: {}", publicId, status);
            return "ok".equals(status);
        } catch (IOException e) {
            log.error("Failed to delete file from Cloudinary: {}", publicId, e);
            return false;
        }
    }

    /**
     * Delete a library document
     */
    public boolean deleteLibraryDocument(String publicId) {
        return deleteFile(publicId, "raw");
    }

    /**
     * Delete a chat attachment
     */
    public boolean deleteChatAttachment(String publicId, String contentType) {
        String resourceType = determineResourceType(contentType);
        return deleteFile(publicId, resourceType);
    }

    /**
     * Generate a unique public ID
     */
    private String generatePublicId(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    /**
     * Determine Cloudinary resource type based on content type
     */
    private String determineResourceType(String contentType) {
        if (contentType == null) {
            return "raw";
        }
        if (contentType.startsWith("image/")) {
            return "image";
        }
        if (contentType.startsWith("video/")) {
            return "video";
        }
        return "raw";
    }

    /**
     * Sanitize filename for use in URLs
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "document";
        }
        return fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    /**
     * Check if Cloudinary is properly configured
     */
    public boolean isConfigured() {
        try {
            Map<String, Object> result = cloudinary.api().ping(ObjectUtils.emptyMap());
            return result != null && "ok".equals(result.get("status"));
        } catch (Exception e) {
            log.warn("Cloudinary not configured or unreachable: {}", e.getMessage());
            return false;
        }
    }
}
