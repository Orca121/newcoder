package com.newcoder.community.controller;

import com.newcoder.community.annotation.LoginRequired;
import com.newcoder.community.entity.User;
import com.newcoder.community.service.UserService;
import com.newcoder.community.util.CommunityUtil;
import com.newcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {

    public static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value ("${community.path.domain}")
    private String domain;

    @Value ("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @LoginRequired
    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    public String getSettingPage(){
        return "site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        // 空值处理
        if(headerImage == null){
            model.addAttribute("error","您还没选择头像图片！");
            return "/site/setting";
        }
        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","您还没选择头像图片！");
            return "/site/setting";
        }

        // 生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;

        // 确定文件存放的路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常！",e);
        }

        // 更新数据库中的user路径（web访问路径：http://localhost:8080/community/user/header/xxx.png）
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(),headerUrl);

        return "redirect:/index";
    }

    @RequestMapping(path = "/header/{fileName}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName")String fileName, HttpServletResponse response){
        // 服务器(本地)存放路径
        fileName = uploadPath + "/" + fileName;

        //文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));

        //响应图片
        response.setContentType("image/" + suffix);
        try (
                // 输入流不需要关闭，SpringMvc帮忙管理，但自己创建的输出流需要
                OutputStream os = response.getOutputStream();
                FileInputStream fis = new FileInputStream(fileName);
                ){
            byte[] buffer = new byte[1024];
            int b = 0;
            while((b = fis.read(buffer)) != -1){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("读取图片失败：" + e.getMessage());
        }
    }

    @RequestMapping(path="/updatePassword",method = RequestMethod.POST)
    public String changePassword(Model model, String oldPassword, String newPassword, String confirmPassword){

        //确认具有更改密码权限
        if(!StringUtils.equals(newPassword,confirmPassword)){
            model.addAttribute("confirmPasswordMsg","两次输入新密码不一致！");
            return "/site/setting";
        }

        User user = hostHolder.getUser();
        String password = user.getPassword();
        String checkPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if(!StringUtils.equals(password,checkPassword)){
            model.addAttribute("oldPasswordMsg","原始密码有误！");
            return "/site/setting";
        }

        //更改密码
        int id = user.getId();
        password = CommunityUtil.md5(newPassword + user.getSalt());
        userService.updatePassword(id,password);
        return "redirect:/logout";
    }

}
