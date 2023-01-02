package com.yeahn.web.controller;

import com.ibm.cloud.objectstorage.services.s3.model.ObjectMetadata;
import com.yeahn.common.CommonUtils;
import com.yeahn.common.S3Uploader;
import com.yeahn.model.YeahnTable;
import com.yeahn.web.service.YeTableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @RequestMapping("/yetable/detail")
    public ModelAndView detail(@RequestParam Map<String, Object> params){
        ModelAndView mv = new ModelAndView();

        params = CommonUtils.paramCleanXSS(params);
        YeahnTable data = yeTableService.getYeahnTableData(params);
        mv.addObject("data", data);

        mv.setViewName("/yetable/detail");
        return mv;
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
        params = CommonUtils.paramCleanXSS(params);

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

    private final S3Uploader s3Uploader;

    //CKEditor 이미지 업로드
    @RequestMapping("/api/image/imageUpload")
    @ResponseBody
    public Map image(@RequestParam Map<String, Object> map, MultipartHttpServletRequest request) {

        List<MultipartFile> imageList = request.getFiles("upload");
        map.put("FILE_NM_SAVED",s3Uploader.upload(imageList));

        return map;
    }
}
