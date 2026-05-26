package com.yeahn.common;

import com.ibm.cloud.objectstorage.services.s3.AmazonS3;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class S3UploaderTest {

    @TempDir
    Path tempDir;

    @Test
    void uploadStoresMultipartFileThroughAmazonS3Client() throws Exception {
        AmazonS3 amazonS3Client = mock(AmazonS3.class);
        when(amazonS3Client.getUrl(eq("test-bucket"), anyString()))
                .thenReturn(URI.create("https://example.com/uploaded.png").toURL());

        S3Uploader uploader = new S3Uploader(amazonS3Client);
        ReflectionTestUtils.setField(uploader, "bucket", "test-bucket");
        ReflectionTestUtils.setField(uploader, "uploadPath", tempDir.toString() + File.separator);

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "profile.png",
                "image/png",
                "image-bytes".getBytes()
        );

        String uploadedUrl = uploader.upload(Collections.singletonList(image));

        assertEquals("https://example.com/uploaded.png", uploadedUrl);
        verify(amazonS3Client).putObject(eq("test-bucket"), anyString(), any(File.class));
    }
}
