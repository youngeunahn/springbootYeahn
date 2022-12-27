package com.yeahn.web.controller;

import com.yeahn.common.S3Uploader;
import com.yeahn.model.YeahnTable;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
public class S3Controller {
    private final S3Uploader s3Uploader;

    @Value("${image.upload.path}")
    private String uploadPath;

    @Value("${resource.handler}")
    private String resourceHandler;

    @PostMapping("/imageTest")
    public ResponseEntity<YeahnTable> updateImage(@RequestParam Map<String, Object> map, MultipartHttpServletRequest request) throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        String imageServerPath = null;    //서버경로
        String savePath = null;            //저장경로
        String originalImagename = null;    //원본이름
        String imageName = null;            //저장본이름
        String extension = null;            //확장자
        long fileSize = 0;
        long uploadFileSize = 0;

        List<MultipartFile> imageList = request.getFiles("upload");

        for (MultipartFile mf : imageList) {
            if (imageList.get(0).getSize() > 0) {

                originalImagename = mf.getOriginalFilename(); // 원본 파일 명
                extension = FilenameUtils.getExtension(originalImagename);
                imageName = "img_" + UUID.randomUUID() + "." + extension;
                fileSize = mf.getSize();
                uploadFileSize += fileSize;

//                File imageUpload = new File(s3Uploader.uploadFiles(mf, "images"));
                try {
//                    mf.transferTo(imageUpload);
//                    map.put("FILE_NM", originalImagename);
//                    map.put("FILE_NM_SAVED", imageName);
//                    map.put("FILE_SIZE", String.valueOf(uploadFileSize));
//                    map.put("FILE_URL", imageUpload);
                } catch (Exception e) {
                    return new ResponseEntity(HttpStatus.BAD_REQUEST);
                }
            }
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}