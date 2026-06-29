package com.raulsanchez.erp_lite.persistence.aws.adapters;

import com.raulsanchez.erp_lite.domain.exceptions.MyBusinessException;
import com.raulsanchez.erp_lite.domain.ports.services.ImageStorageServicePort;
import com.raulsanchez.erp_lite.domain.entities.product.ProductImage;
import com.raulsanchez.erp_lite.persistence.aws.models.AwsConfigModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Service
@RequiredArgsConstructor
public class AwsImageStorageService implements ImageStorageServicePort {

    private final S3Client s3Client;
    private final AwsConfigModel awsConfig;

    @Override
    public ProductImage upload(String imageName, byte[] imageData) {
        try {
            final var key = "products/" +  imageName;

            final var putObjectRequest = PutObjectRequest.builder().bucket(awsConfig.bucketName()).key(key).contentType(this.determinateContentType(imageName)).contentLength((long) imageData.length).build();

            this.s3Client.putObject(putObjectRequest, RequestBody.fromBytes(imageData));

            final var imgUrl  =this.buildUrlImg(key);

            log.info(" Image Uploaded Successfully in {}.", imgUrl);

            return new ProductImage(imgUrl);

        } catch (S3Exception S3e) {
            log.error("Error uploading image", S3e);
            throw new MyBusinessException("Error uploading image " + S3e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error uploading image", e);
            throw new MyBusinessException("Unexpected error uploading image " + e.getMessage());
        }
    }

    @Override
    public void delete(ProductImage img) {
        try {
            final var key = this.getKeyFromUrl(img.imageUrl());

            final var deleteObjectRequest = DeleteObjectRequest.builder().bucket(awsConfig.bucketName()).key(key).build();

            this.s3Client.deleteObject(deleteObjectRequest);

            log.info("Delete image success {}", img.imageUrl());

        } catch (S3Exception S3e) {
            log.error("Error deleting image", S3e);
            throw new MyBusinessException("Error deleting image " + S3e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error deleting image", e);
            throw new MyBusinessException("Unexpected error deleting image " + e.getMessage());
        }
    }

    @Override
    public byte[] download(ProductImage img) {
        try {
            final var key = this.getKeyFromUrl(img.imageUrl());

            final var getObjectRequest = GetObjectRequest.builder().bucket(awsConfig.bucketName()).key(key).build();

            final var byres = this.s3Client.getObjectAsBytes(getObjectRequest).asByteArray();

            log.info("Downloading image: {} bytes", byres.length);

            return byres;

        } catch (S3Exception S3e) {
            log.error("Error downloading image", S3e);
            throw new MyBusinessException("Error downloading image " + S3e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error downloading image", e);
            throw new MyBusinessException("Unexpected error downloading image " + e.getMessage());
        }
    }

    /**
     *
     * @param url https://amazonaws/erp-products/products/mac-01.png
     * @return product/mac-01.png
     */
    private String getKeyFromUrl(String url) {
        var bucketName = awsConfig.bucketName();
        var parts = url.split("/" + bucketName + "/");

        if (parts.length > 1) {
            return parts[1];
        }

        log.warn("No bucket name found for url: " + url);
        return null;
    }

    private String buildUrlImg(String key) {
        final var placeholder = "%s/%s/%s";
        return String.format(placeholder, this.awsConfig.endpoint(), this.awsConfig.bucketName(), key);
    }

    private String determinateContentType(String fileName) {
        final var extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

        return switch (extension) {
            case "jpg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }
}
