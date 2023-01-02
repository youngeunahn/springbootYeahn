package com.yeahn.common;

import com.ibm.cloud.objectstorage.AmazonServiceException;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Uploader {

    // local, development 등 현재 프로파일

    @Value("${spring.environment}")
    private String environment;


    // 파일이 저장되는 경로

    @Value("${image.upload.path}")
    private String rootDir;
    private String fileDir;

    private final AmazonS3 amazonS3Client;

    @Value("${cos.bucket}")
    public String bucket;

    /**
     * 서버가 시작할 때 프로파일에 맞는 파일 경로를 설정해줌
     */

    @PostConstruct
    private void init(){
        if(environment.equals("local")){
            this.fileDir = System.getProperty("user.dir");
        }
        else if(environment.equals("development")){
            this.fileDir = this.rootDir;
        }

    }

    public String upload(List<MultipartFile> imageList) {

        try{
            Map uploadFile = convert(imageList);  // 파일 변환할 수 없으면 에러
            String uploadImageUrl = putS3(uploadFile); // s3로 업로드
            removeNewFile((File) uploadFile.get("FILE"));

            return uploadImageUrl;
        } catch (IOException | AmazonServiceException e){
            e.printStackTrace();
        }
        return null;
    }

    // S3로 업로드
    private String putS3(Map uploadFile) {
        try {

            File file = (File) uploadFile.get("FILE");

            ByteArrayOutputStream theBytes = new ByteArrayOutputStream();
            ObjectOutputStream serializer = new ObjectOutputStream(theBytes);
            serializer.writeObject(file); // serialize the object data
            serializer.flush();
            serializer.close();

            System.out.format("Uploading %s to S3 bucket %s...\n", uploadFile.get("FILE_NM").toString(), bucket);
            amazonS3Client.putObject(bucket, uploadFile.get("FILE_NM").toString(), file);

            return amazonS3Client.getUrl(bucket, uploadFile.get("FILE_NM").toString()).toString();
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    // 로컬에 저장된 이미지 지우기
    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            System.out.println("File delete success");
            return;
        }
        System.out.println("File delete fail");
    }

    @Value("${image.upload.path}")
    private String uploadPath;

    // 로컬에 파일 업로드 하기
    private Map convert(List<MultipartFile> imageList) throws IOException {
        if (imageList.isEmpty()) {
            return null;
        }

        String originalImagename = null;    //원본이름
        String imageName = null;            //저장본이름
        String extension = null;            //확장자
        Map<String, Object> map = new HashMap<>();

//        String originalFilename = multipartFile.getOriginalFilename();
//        String storeFileName = createStoreFileName(originalFilename);

        if (imageList.get(0).getSize() > 0) {
            for (MultipartFile mf : imageList) {
                try {
                    originalImagename = mf.getOriginalFilename(); // 원본 파일 명
                    extension = FilenameUtils.getExtension(originalImagename);
                    imageName = "img_" + UUID.randomUUID() + "." + extension;
                    File imageUpload = new File(uploadPath + imageName);
                    mf.transferTo(imageUpload);

                    map.put("FILE_NM", imageName);
                    map.put("FILE_NM_SAVED", originalImagename);
                    map.put("FILE_SIZE", String.valueOf(mf.getSize()));
                    map.put("FILE", imageUpload);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return map;
    }
}
