package com.pjb.springbootjjwt.api;

import com.alibaba.fastjson.JSONObject;
import com.pjb.springbootjjwt.SpringbootJjwtApplication;
import com.pjb.springbootjjwt.annotation.UserLoginToken;
import com.pjb.springbootjjwt.entity.*;
import com.pjb.springbootjjwt.service.IgtcoaServer;
import com.pjb.springbootjjwt.service.TcsoaService;
import com.teamcenter.clientx.AppXSession;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.strong.core._2007_01.Session;
import com.teamcenter.soa.client.model.strong.User;
import org.apache.catalina.connector.RequestFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import springfox.documentation.spring.web.json.Json;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
//@RequestMapping("igtcoa")
public class IgtcoaApi {
    @Autowired
    IgtcoaServer igtcoaServer;

    private static AppXSession tcSession;
    private static User tcUser;

    @UserLoginToken
    @PostMapping(value = "/oaGetProcess")
    public Object listProcess(@RequestBody IgtcoaProcessQuery query)
    {
        JSONObject jsonObject=new JSONObject();
        String werk = query.getWerks();
        if(werk ==null || werk.equals("")){
            jsonObject.put("message","未输入必填字段werks");
            jsonObject.put("success",false);
            return jsonObject;
        }
        List<IgtcoaProcess> processList = igtcoaServer.listAllProcess(query);
        if(processList == null || processList.size()==0){
            jsonObject.put("message","没有找到对应的信息");
            jsonObject.put("success",false);
        }else{
            jsonObject.put("success",true);
            jsonObject.put("message","查询信息成功");
            jsonObject.put("result",processList);
        }

        return jsonObject;
    }


    @UserLoginToken
    @PostMapping(value = "/oaGetProcessInfo")
    public Object getFileProcess(String workflow_id)
    {
        JSONObject jsonObject=new JSONObject();
        if(workflow_id == null || workflow_id.equals("")){
            jsonObject.put("message","未输入流程ID");//CGnBwHULJRWNkA
            jsonObject.put("success",false);
            return jsonObject;
        }
        IgtcoaFileProcess processList = igtcoaServer.getFileProcess(workflow_id);
        if(processList == null ){
            jsonObject.put("message","没有找到对应的信息");
            jsonObject.put("success",false);
        }else{
            jsonObject.put("success",true);
            jsonObject.put("message","查询信息成功");
            jsonObject.put("result",processList);
        }

        return jsonObject;
    }

    @UserLoginToken
    @PostMapping(value = "/oaSign")
    public Object setSignInfo(@RequestBody IgtcoaSignWorkflow signMsg) {
        JSONObject jsonObject=new JSONObject();
        try {
            igtcoaServer.setSignIfno(signMsg);
            jsonObject.put("success",true);
            jsonObject.put("message","更新签名信息成功");
        }catch (Exception e){
            jsonObject.put("success",false);
            jsonObject.put("message",e.getMessage());
        }
        return jsonObject;
    }

    @UserLoginToken
    @PostMapping(value = "/oaUploadFile")
    public Object uploadOAFile(HttpServletRequest request) {
        JSONObject jsonObject=new JSONObject();

        String picturePath = "C:\\TCOA\\TEMP\\";
        String pictureName ="";
        String pictureLocalPath =null;
        String oaFileName = "";
        File dir = new File(picturePath);
        if (!dir.exists())
        {
            dir.mkdirs();
        }

        try {
            StandardMultipartHttpServletRequest req = (StandardMultipartHttpServletRequest) request;
            //获取formdata的值
            Iterator<String> iterator = req.getFileNames();
            oaFileName = req.getParameter("file_name");
            System.out.println("fileName="+oaFileName);
            while (iterator.hasNext()) {
                MultipartFile file=req.getFile(iterator.next());
                if(file == null){
                    continue;
                }
                //获取文件后缀名
                String fileSuffixName=file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
                //真正写到磁盘上
                //全球唯一id
                String uuid= UUID.randomUUID().toString().replace("-","");
                pictureName=uuid + fileSuffixName;
                pictureLocalPath = picturePath+pictureName;
                File file1=new File(picturePath+pictureName);
                OutputStream out=new FileOutputStream(file1);
                out.write(file.getBytes());
                out.close();
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        if(pictureLocalPath !=null){
            jsonObject.put("success",true);
            jsonObject.put("message","上传文件成功");
            jsonObject.put("result","");
            igtcoaServer.insertOAFiles(UUID.randomUUID().toString(),oaFileName,pictureLocalPath);
        }else{
            jsonObject.put("success",false);
            jsonObject.put("message",oaFileName+",上传文件失败");
            jsonObject.put("result","");
        }
        return jsonObject;
    }

    @UserLoginToken
    @PostMapping(value = "/createPrItem")
    public Object createPrItem(HttpServletRequest request){
        JSONObject jsonObject=new JSONObject();
        //登录TC
        if(tcSession == null){
            tcSession = new AppXSession(SpringbootJjwtApplication.TC_URL);
        }
        if(tcUser == null) {
            tcUser = tcSession.login(SpringbootJjwtApplication.TC_USER,SpringbootJjwtApplication.TC_PWD);
        }
        if(tcUser == null){
            jsonObject.put("success",false);
            jsonObject.put("message","TC登录失败！！！");
            return jsonObject;
        }

        StandardMultipartHttpServletRequest req = (StandardMultipartHttpServletRequest) request;
        //获取JSON信息
        String jsonInfo = req.getParameter("PrInfo");
        JSONObject requstJson = JSONObject.parseObject(jsonInfo);
        String itemName = requstJson.getString("item_name");
        JSONObject propJson = requstJson.getJSONObject("properties");

        Map<String,Object> innerMap = propJson.getInnerMap();
        Map<String,String> propertyMap = new HashMap<String,String>(16);
        Map<String,String> dstSettingMap = new HashMap<String,String>(16);
        Map<String,String> lovMap = new HashMap<String,String>(16);
        String needItemIds = "";
        //解析EXCEl
        TcsoaService.parseExcelSetting(propertyMap,innerMap,dstSettingMap,lovMap);
        if(propertyMap.containsKey("SET8_WTLZJ")){
            needItemIds = propertyMap.get("SET8_WTLZJ");
            propertyMap.remove("SET8_WTLZJ");
        }
        //放新建pr对象的ID或者lov属性传错的值
        StringBuffer buffer = new StringBuffer("");
        for (Map.Entry<String,String> entry : lovMap.entrySet()){
            String[] lovProps = entry.getKey().split(";");
            if(lovProps.length == 2){
//                String innerValue = igtcoaServer.getLovInnerValue(entry.getValue(), lovProps[1]);
//                if(innerValue == null){
//                    buffer.append(entry.getValue() + "在lov中不存在!");
//                    continue;
//                }
//                propertyMap.put(lovProps[0], innerValue);
                String innerValue = igtcoaServer.getLovInnerValue2(entry.getValue(), lovProps[1]);
                if(innerValue == null){
                    buffer.append(entry.getValue() + "在lov中不存在!");
                    continue;
                }
                propertyMap.put(lovProps[0], entry.getValue());
            }
        }
        //判断问题零组件是否全部存在
        String[] propValues = needItemIds.split(";");
        System.out.println("开始判断问题零组件");
        DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
        System.out.println(needItemIds);
        for (String item_id : propValues) {
            System.out.println("查询ID:" + item_id);
            com.teamcenter.services.strong.query._2007_06.SavedQuery.SavedQueryResults results
                    = TcsoaService.queryItemById(item_id, dmService);
            if (results != null && results.numOfObjects > 0) {
                System.out.println(item_id+"存在");
            }
            else{
                buffer.append(item_id + "在TC中不存在!!");
            }
        }
        System.out.println("结束");
        if(buffer.length() > 0){
            jsonObject.put("success",false);
            jsonObject.put("message",buffer.toString());
            return jsonObject;
        }

        System.out.println("数据集配置：" + dstSettingMap);
        System.out.println("属性配置:" + propertyMap);

        String tempPath = "C:\\TCOA\\TEMP\\";
        String tempName = "";
        String tempLocalPath = "";

        File dir = new File(tempPath);
        if (!dir.exists())
        {
            dir.mkdirs();
        }
        //获取上传文件
        Iterator<String> iterator = req.getFileNames();
        List<File> fileList = new ArrayList<File>();
        while(iterator.hasNext()){
            MultipartFile file=req.getFile(iterator.next());
            if(file == null){
                continue;
            }
            //获取文件后缀名
            String fileSuffixName=file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            //全球唯一id
            String uuid= UUID.randomUUID().toString().replace("-","");
            tempName = uuid + "_" + file.getOriginalFilename().substring(0,file.getOriginalFilename().lastIndexOf(".")) +fileSuffixName;
            tempLocalPath = tempPath + tempName;
            File tempFile = new File(tempLocalPath);
            OutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(tempFile);
                outputStream.write(file.getBytes());
                outputStream.flush();
                fileList.add(tempFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(outputStream != null){
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        String folderUid = "";
        try {
            Session.ReturnedPreferences[] returnedPreferences = TcsoaService.getTCPreferences("SET_PR");
            if(returnedPreferences.length > 0){
                String[] prefValues = returnedPreferences[0].values;
                if(prefValues.length > 0){
                    folderUid = prefValues[0];
                    System.out.println("文件夹UID："+folderUid);
                }
            }
        } catch (ServiceException e) {
            jsonObject.put("message","获取首选项UID失败！！！" + e.getMessage());
            e.printStackTrace();
        }
        String returnString = TcsoaService.createTcObject(itemName,needItemIds,propertyMap,folderUid,fileList,dstSettingMap,buffer);
        if(returnString.equals("COMPLETE")){
            jsonObject.put("success",true);
            jsonObject.put("message","创建PR对象成功！！！"+buffer.toString());
        }else{
            jsonObject.put("success",false);
            jsonObject.put("message","创建PR对象失败：" + returnString);
        }
        tcSession.logout();
        return jsonObject;
    }

    @UserLoginToken
   // @GetMapping(value = "/file/download")
    @PostMapping(value = "/file/download2")
    public byte[] downloadfile(HttpServletRequest request) {
        //StandardMultipartHttpServletRequest req = (StandardMultipartHttpServletRequest) request;
        //RequestFacade req = (RequestFacade)request;
        //request.getHeader("file_id");
        String file_id = request.getParameter("file_id");
        //  file_id = request.getHeader("file_id");
        if(file_id == null){
            System.out.println("file_id 为空");
            return null;
        }
        byte[] fileData = null;
        if(tcSession == null){
            tcSession = new AppXSession(SpringbootJjwtApplication.TC_URL);
        }
        try{
            if(tcUser == null) {
                tcUser = tcSession.login(SpringbootJjwtApplication.TC_USER,SpringbootJjwtApplication.TC_PWD);
            }
            if(tcUser != null){
                fileData =  TcsoaService.getTcfileByFileId(file_id);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        if(fileData == null){
            System.out.println("没有找到"+file_id+"对应的文件");
        }

        return fileData;
    }


    @UserLoginToken
    @GetMapping(value = "/file/download")
    //@PostMapping(value = "/file/download")
    public void fileDownload(HttpServletRequest request, HttpServletResponse response){
        //w1W9KBJZ5gRCGA";//
        String file_id = request.getParameter("file_id");

        if(file_id == null){
            throw new RuntimeException("file_id 为空");
        }
        if(tcSession == null){
            tcSession = new AppXSession(SpringbootJjwtApplication.TC_URL);
        }
        try{
            if(tcUser == null) {
                tcUser = tcSession.login(SpringbootJjwtApplication.TC_USER,SpringbootJjwtApplication.TC_PWD);
            }
            if(tcUser == null) {
                throw new RuntimeException("TC登录失败！！！");
            }
            File file = TcsoaService.getTcfileByFileId2(file_id);
            String fileName = TcsoaService.getDatasetFileName(file_id);
            if (file !=null && file.exists()) {
                // 设置强制下载不打开
                response.setContentType("application/force-download");
                Date currentTime = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                String dataTime=dateFormat.format(currentTime);
                //文件重新命名
                String fileNewName = dataTime+fileName.substring(fileName.indexOf("."));
                response.addHeader("Content-Disposition",
                        "attachment;fileName=" + fileNewName);// 设置文件名
                byte[] buffer = new byte[1024];
                FileInputStream fis = null;
                BufferedInputStream bis = null;
                try {
                    fis = new FileInputStream(file);
                    bis = new BufferedInputStream(fis);
                    OutputStream os = response.getOutputStream();
                    int i = bis.read(buffer);
                    while (i != -1) {
                        os.write(buffer, 0, i);
                        i = bis.read(buffer);
                    }
                    System.out.println(fileNewName+"下载成功！！！");
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(fileNewName+"下载失败！！！"+e);
                } finally {
                    if (bis != null) {
                        try {
                            bis.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
