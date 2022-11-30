package com.yeahn.web.controller;

import com.yeahn.model.YeahnTable;
import com.yeahn.web.service.YeTableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.yeahn.common.CommonUtils.getTimestampToDate;

@Slf4j
@RequiredArgsConstructor
@Controller
public class YeController {

    @Autowired
    private YeTableService yeTableService;

    @RequestMapping("/yetable/list")
    public ModelAndView list(Model model){
        ModelAndView mv = new ModelAndView();

        List<YeahnTable> YeList = yeTableService.getYeahnTableList();

        mv.addObject("YeList", YeList);
        mv.setViewName("/yetable/list");
        return mv;
    }

    @RequestMapping("/yetable/list.json")
    @ResponseBody
    public List<YeahnTable> jsonList(Model model){
        List<YeahnTable> YeList = yeTableService.getYeahnTableList();

        return YeList;
    }

    @RequestMapping("/yetable/edit.json")
    public @ResponseBody String cellEdit(
    @RequestParam(value = "NO") Integer NO,
    @RequestParam(value = "TITLE") String TITLE) {

        String result = "";
        YeahnTable model = new YeahnTable();
        model.setNO(NO);
        model.setTITLE(TITLE);

        int resultQuery = yeTableService.editYeahnTable(model);
        if (resultQuery == 1) result = "SUCCESS";
        else result = "FAIL";
        return result;
    }

    @RequestMapping("/yetable/create")
    public ModelAndView index(Model model){
        ModelAndView mv = new ModelAndView();

        mv.setViewName("/yetable/create");
        return mv;
    }

    @RequestMapping("/ajax/yetable/insertYetable")
    @ResponseBody
    public int insertYetable(@RequestParam Map<String, Object> params){
        String unixTimestamp = getTimestampToDate(Instant.now().getEpochSecond());
        params.put("REG_DATE", unixTimestamp);
        params.put("REG_ID", "yeahn");

        int result = yeTableService.insertYetable(params);
        return result;
    }

    private String getServerIp() {
        InetAddress local = null;
        try {
            local = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        if (local == null) {
            return "";
        } else {
            String ip = local.getHostAddress();
            return ip;
        }
    }

    @Value("${image.upload.path}")
    private String uploadPath;

    @Value("${resource.handler}")
    private String resourceHandler;

    //CKEditor 이미지 업로드
    @RequestMapping("/api/image/imageUpload")
    @ResponseBody
    public Map image(@RequestParam Map<String, Object> map, MultipartHttpServletRequest request)
            throws Exception {
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

                File imageUpload = new File(uploadPath + imageName);
                try {
                    mf.transferTo(imageUpload);
                    map.put("FILE_NM", originalImagename);
                    map.put("FILE_NM_SAVED", imageName);
                    map.put("FILE_SIZE", String.valueOf(uploadFileSize));
                    map.put("FILE_URL", imageUpload);
                } catch (IllegalStateException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return map;
    }
}
