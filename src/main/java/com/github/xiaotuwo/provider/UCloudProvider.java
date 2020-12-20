package com.github.xiaotuwo.provider;

import cn.ucloud.ufile.UfileClient;
import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.auth.ObjectAuthorization;
import cn.ucloud.ufile.auth.UfileObjectLocalAuthorization;
import cn.ucloud.ufile.bean.PutObjectResultBean;
import cn.ucloud.ufile.bean.base.BaseResponseBean;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.exception.UfileServerException;
import com.github.xiaotuwo.dto.FileDTO;
import com.github.xiaotuwo.exception.ErrorCode;
import com.github.xiaotuwo.exception.ErrorCodeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Created by codedrinker on 2020/12/20.
 */
@Service
@Slf4j
public class UCloudProvider {
    @Value("${ucloud.ufile.public-key}")
    private String publicKey;

    @Value("${ucloud.ufile.private-key}")
    private String privateKey;

    @Value("${ucloud.ufile.bucket-name-private}")
    private String bucketNamePrivate;

    @Value("${ucloud.ufile.upload-domain-private}")
    private String uploadDomainPrivate;

    @Value("${ucloud.ufile.download-domain-private}")
    private String downloadDomainPrivate;

    @Value("${ucloud.ufile.expires}")
    private Integer expires;

    @Value("${ucloud.uaicensor.resourceId}")
    private String resourceId;

    @Value("${ucloud.uaicensor.publicKey}")
    private String uaicensorPublicKey;

    @Value("${ucloud.uaicensor.privateKey}")
    private String uaicensorPrivateKey;

    @Value("${ucloud.uaicensor.url}")
    private String ucloudUrl;

    public FileDTO upload(InputStream fileStream, String mimeType, String fileName) {
        String generatedFileName;
        String[] filePaths = fileName.split("\\.");
        if (filePaths.length > 1) {
            generatedFileName = UUID.randomUUID().toString() + "." + filePaths[filePaths.length - 1];
        } else {
            throw new ErrorCodeException(ErrorCode.FILE_UPLOAD_FAIL);
        }
        long start = System.currentTimeMillis();
        try {
            log.debug("UCloudProvider start upload file, filename : {}, time : {}", fileName, new Date());
            ObjectAuthorization objectAuthorization = new UfileObjectLocalAuthorization(publicKey, privateKey);
            ObjectConfig config = new ObjectConfig(uploadDomainPrivate);
            PutObjectResultBean response = UfileClient.object(objectAuthorization, config)
                    .putObject(fileStream, mimeType)
                    .nameAs(generatedFileName)
                    .toBucket(bucketNamePrivate)
                    .setOnProgressListener((bytesWritten, contentLength) -> {
                    })
                    .execute();
            log.debug("UCloudProvider end upload file, filename : {}, time : {}, cost : {}", fileName, new Date(), System.currentTimeMillis() - start);
            if (response != null && response.getRetCode() == 0) {
                long start2 = System.currentTimeMillis();
                log.debug("UCloudProvider start get url, filename : {}, time : {}", fileName, new Date());

                String url = UfileClient.object(objectAuthorization, new ObjectConfig(downloadDomainPrivate))
                        .getDownloadUrlFromPrivateBucket(generatedFileName, bucketNamePrivate, expires)
                        .createUrl();

                log.debug("UCloudProvider end get url, filename : {}, time : {}, cost : {}", fileName, new Date(), System.currentTimeMillis() - start2);

                FileDTO fileDTO = new FileDTO();
                fileDTO.setUrl(url.replace("http", "https"));
                fileDTO.setName(generatedFileName);
                return fileDTO;
            } else {
                log.debug("UCloudProvider end upload file, filename : {}, time : {}, cost : {}", fileName, new Date(), System.currentTimeMillis() - start);
                log.error("upload error,{}", response);
                throw new ErrorCodeException(ErrorCode.FILE_UPLOAD_FAIL);
            }
        } catch (UfileClientException | UfileServerException e) {
            log.debug("UCloudProvider end upload file, filename : {}, time : {}, cost : {}", fileName, new Date(), System.currentTimeMillis() - start);
            log.error("upload error,{}", fileName, e);
            throw new ErrorCodeException(ErrorCode.FILE_UPLOAD_FAIL);
        }
    }

    public void delete(String fileName) {
        try {
            log.debug("start deleting file : {}", fileName);
            ObjectAuthorization objectAuthorization = new UfileObjectLocalAuthorization(publicKey, privateKey);
            ObjectConfig config = new ObjectConfig(uploadDomainPrivate);
            BaseResponseBean response = UfileClient.object(objectAuthorization, config)
                    .deleteObject(fileName, bucketNamePrivate)
                    .execute();
            if (response != null && response.getRetCode() == 0) {
                log.debug("deleting file success : {}", fileName);
            } else {
                log.debug("deleting file error : {}, {}", fileName, response);
                throw new ErrorCodeException(ErrorCode.FILE_UPLOAD_FAIL);
            }
            log.debug("end deleting file : {}", fileName);
        } catch (UfileClientException | UfileServerException e) {
            log.error("delete file error : {}", fileName, e);
            throw new ErrorCodeException(ErrorCode.FILE_UPLOAD_FAIL);
        }
    }

    /**
     * ucloud 鉴黄
     *
     * @param imageUrl
     * @return 返回值
     * RetCode 0 标识正常 其余一律异常
     * Suggestion 建议， pass-放行， forbid-封禁， check-人工审核
     */
    public String scan(String imageUrl) throws Exception {
        //图片绝对路径
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        /**
         * 生成signature，首字母排序
         */
        String timestamp = System.currentTimeMillis() + "";
        SortedMap<Object, Object> packageParams = new TreeMap<>();
        packageParams.put("PublicKey", uaicensorPublicKey);
        packageParams.put("ResourceId", resourceId);
        packageParams.put("Timestamp", timestamp);
        packageParams.put("Url", imageUrl);
        String signature = UCloudSigner.createSign(packageParams, uaicensorPrivateKey);
        /**
         * 参数
         */
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
        param.add("Scenes", "porn");
        param.add("Method", "url");
        param.add("Url", imageUrl);
        /**
         * headers 参数
         */
        headers.setContentType(MediaType.parseMediaType("multipart/form-data; charset=UTF-8"));
        headers.set("PublicKey", uaicensorPublicKey);
        headers.set("Signature", signature);
        headers.set("ResourceId", resourceId);
        headers.set("Timestamp", timestamp);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(param, headers);
        ResponseEntity<String> responseEntity = rest.exchange(ucloudUrl, HttpMethod.POST, httpEntity, String.class);
        return responseEntity.getBody();
    }
}
