package com.github.xiaotuwo.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.xiaotuwo.dto.FileDTO;
import com.github.xiaotuwo.provider.UCloudProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by codedrinker on 2020/12/20.
 */
@Controller
@Slf4j
public class FileController {
    @Autowired
    private UCloudProvider uCloudProvider;

    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    @PostMapping({"/api/files/upload"})
    @ResponseBody
    public FileDTO upload(HttpServletRequest request) {
        FileDTO resultFileDTO = new FileDTO();
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile file = multipartRequest.getFile("file");
        long start = System.currentTimeMillis();
        try {
            if (file == null) {
                resultFileDTO.setStatus("error");
            }
            FileDTO fileDTO = uCloudProvider.upload(file.getInputStream(), file.getContentType(), Objects.requireNonNull(file.getOriginalFilename()));
            log.info("UPLOAD_FILE_EXPIRED|ip:{}|referer:{}|url:{}|cost:{}", getIpAddress(request), request.getHeader("referer"), fileDTO.getUrl(), System.currentTimeMillis() - start);
            executorService.submit(new UCloudScanner(fileDTO, uCloudProvider, executorService, 1));
            resultFileDTO.setName(fileDTO.getName());
            resultFileDTO.setUrl(fileDTO.getUrl());
            resultFileDTO.setThumbUrl(fileDTO.getUrl());
            resultFileDTO.setStatus("done");
        } catch (Exception e) {
            log.error("UPLOAD_FILE_ERROR", e);
            resultFileDTO.setStatus("error");
        }
        return resultFileDTO;
    }

    public class UCloudScanner implements Runnable {
        private FileDTO fileDTO;
        private UCloudProvider uCloudProvider;
        private ExecutorService executorService;
        private Integer times;

        public UCloudScanner(FileDTO fileDTO, UCloudProvider uCloudProvider, ExecutorService executorService, Integer times) {
            this.fileDTO = fileDTO;
            this.uCloudProvider = uCloudProvider;
            this.executorService = executorService;
            this.times = times;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(5000L);
                String scan = uCloudProvider.scan(fileDTO.getUrl());
                JSONObject jsonObject = JSON.parseObject(scan);
                if (jsonObject.getInteger("RetCode") == 0) {
                    String res = jsonObject.getJSONObject("Result").getJSONObject("Porn").getString("Suggestion");
                    log.info("IMAGE_SCAN|url:{}|res:{},times:{}", fileDTO.getUrl(), res, times);
                    if (!StringUtils.equals(res, "pass")) {
                        log.info("IMAGE_SCAN_DELETE|url:{}", fileDTO.getUrl());
                        uCloudProvider.delete(fileDTO.getName());
                    }
                }
            } catch (Exception e) {
                log.error("IMAGE_SCAN_SCAN_ERROR|url:{}|times:{}", fileDTO.getUrl(), times, e);
                if (times++ < 10) {
                    executorService.submit(this);
                }
            }
        }
    }

    public String getIpAddress(HttpServletRequest request) {
        try {
            String ip = request.getHeader("x-forwarded-for");
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            return ip;
        } catch (Exception e) {
            return "";
        }
    }
}