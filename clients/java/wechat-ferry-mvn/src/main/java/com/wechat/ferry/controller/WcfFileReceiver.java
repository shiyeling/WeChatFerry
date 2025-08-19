package com.wechat.ferry.controller;

import com.wechat.ferry.entity.TResponse;
import com.wechat.ferry.enums.ResponseCodeEnum;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@RestController
@RequestMapping("/wechat-ferry")
@Api(tags = "微信机器人管理服务之文件上传接口")
public class WcfFileReceiver {

    /**
     * Wechat Ferry的主目录，存放着各个微信机器人的数据目录
     */
    @Value("${wcf.home.dir:/home/wcf}")
    private String wcfFerryHomeDir;

    @Value("${wcf.bot.file.dir:files}")
    private String wechatBotFileDirName;
    @Value("${wcf.bot.file.docker:/tmp/files}")
    private String wechatBotFileDirInDocker;

    /**
     * 向机器人管理服务传输文件，以便后续执行文件消息发送，
     * 目前这个版本的服务暂时不管理机器人的元数据信息，因此需要调用者自行拼接具体的机器人的文件发送目录，并通过path 参数传入
     *
     * @param botHomeDir    文件在当前服务的环境中应存放的位置
     * @param multipartFile 文件
     * @return 写入结果对象
     */
    @PostMapping("/upload-file")
    public TResponse<String> saveFile(@RequestParam("bot-home-dir") String botHomeDir, @RequestPart("file") MultipartFile multipartFile) {
        String fileName = "";
        if (StringUtils.isBlank(botHomeDir)) {
            return TResponse.fail("传入的微信机器人主目录不可为空");
        }
        try {
            fileName = multipartFile.getOriginalFilename();
            log.info("Uploading file:{} to bot {}", fileName, botHomeDir);

            Path botHomePath = Path.of(wcfFerryHomeDir, botHomeDir);
            if (Files.exists(botHomePath) && Files.isDirectory(botHomePath)) {
                int seq = 1;
                Path targetPath = Path.of(wcfFerryHomeDir, botHomeDir, wechatBotFileDirName, fileName);
                while (Files.exists(targetPath)) {
                    // 如果目录中已经有同名文件，给他一个序号，不断尝试
                    log.info("Found existing file: {}", targetPath.toFile().getAbsolutePath());
                    targetPath = Path.of(wcfFerryHomeDir, botHomeDir, wechatBotFileDirName,
                            String.format("%s%d.%s", FilenameUtils.getBaseName(fileName), seq, FilenameUtils.getExtension(fileName)));
                    seq++;
                }
                File file = targetPath.toFile();
                IOUtils.copy(multipartFile.getInputStream(), FileUtils.openOutputStream(file));
                log.info("Finished copy with bot internal file botHomeDir: {}", file.getAbsolutePath());
                String filePathInDocker = Path.of(wechatBotFileDirInDocker, file.getName()).toString();
                return TResponse.ok(ResponseCodeEnum.SUCCESS, filePathInDocker);
            } else {
                return TResponse.fail(String.format("文件存储失败：指定的机器人的收件箱文件夹%s不存在", botHomePath));
            }
        } catch (Exception e) {
            log.error("save file[{}] has error", fileName, e);
            return TResponse.fail("文件存储失败：" + e.getMessage());
        }
    }
}
