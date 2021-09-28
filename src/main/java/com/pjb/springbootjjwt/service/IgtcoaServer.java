package com.pjb.springbootjjwt.service;

import com.pjb.springbootjjwt.entity.*;
import com.pjb.springbootjjwt.mapper.IgtcoaMapper;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service("IgoatcServer")
public class IgtcoaServer {
    @Autowired
    IgtcoaMapper igtcoaMapper;
    public List<IgtcoaProcess> listAllProcess(IgtcoaProcessQuery query){
        List<IgtcoaProcess> processList = igtcoaMapper.listAllProcess(query);
        //过滤保留workcode为null或所输入工号的流程
        String workcode = query.getWorkcode();
        Iterator it = processList.iterator();
        while (it.hasNext()){
            IgtcoaProcess list = (IgtcoaProcess) it.next();
            System.out.println(list.getWorkcode());
            if(workcode.equals(list.getWorkcode()) || list.getWorkcode() == null){
                continue;
            }
            else{
                it.remove();
            }
        }
        return processList;
    }


    public IgtcoaFileProcess getFileProcess(String processid){
        IgtcoaFileProcess fileProcess =  igtcoaMapper.getFileProcess(processid);
        if(fileProcess!=null){
            List<IgtcoaDocumentInfo> documentInfos = igtcoaMapper.listDocumentInfos(processid);
            //放入files和prInfos和qiInfo
            for (int i = 0 ; i < documentInfos.size();i++) {
                IgtcoaDocumentInfo single = documentInfos.get(i);
                List<IgtcoaFile> fileList = igtcoaMapper.listAllFile2(single.getDocument_id(),single.getDocument_rev());
                documentInfos.get(i).setFiles(fileList);
                List<IgtcoaPrInfo> prInfoList = igtcoaMapper.listAllPrInfos(single.getDocument_id(),single.getDocument_rev());
                documentInfos.get(i).setPrInfo(prInfoList);
                List<IgtcoaQiInfo> qiInfoList = igtcoaMapper.listAllQiInfos(single.getEcr_id(),single.getEcr_ver());
                for (int j = 0; j < qiInfoList.size();j++){
                    IgtcoaQiInfo qiInfo = qiInfoList.get(j);
                    //添加qi_file
                    List<IgtcoaQiFile> qiFileList = igtcoaMapper.listAllQiFiles(qiInfo.getQi_id(),qiInfo.getQi_ver());
                    qiInfoList.get(j).setQi_file(qiFileList);
                }
                documentInfos.get(i).setQiInfo(qiInfoList);
            }
            fileProcess.setDocumentInfo(documentInfos);
        }
        String level = fileProcess.getPrivary();
        if(level != null && level.contains("/")){
            level = level.split("/")[1];
        }
        fileProcess.setPrivary(level);
        return fileProcess;
    }

    public String getLovInnerValue(String displayValue,String lovName){
        String innerValue = igtcoaMapper.getLovInnerValue(displayValue,lovName);
        return innerValue;
    }
    public String getLovInnerValue2(String displayValue,String lovName){
        String innerValue = igtcoaMapper.getLovInnerValue2(displayValue,lovName);
        return innerValue;
    }

    public void setSignIfno(IgtcoaSignWorkflow signMsg){
        igtcoaMapper.updateProcessApproval(signMsg.getWorkflow_id(),""+signMsg.getApproval_results());
       for(IgtcoaSignInfo signinfo :signMsg.getSign_info()) {
           List<IgtcoaSignContent> signcontentList = signinfo.getContent();
           //igtcoaMapper.deleteFileSigninfo(signinfo.getFile_id());
           StringBuffer stringBuffer = new StringBuffer();
//           for(int i =0;i<signcontentList.size();i++) {
////               igtcoaMapper.insertFileSigninfo(signinfo.getFile_id(),
////                       signcontentList.get(i).getName(),
////                       signcontentList.get(i).getTime(),i+1);
//               if(i == 0){
//                   stringBuffer.append(signcontentList.get(i).getTime());
//                   stringBuffer.append(":");
//               }
//               stringBuffer.append(signcontentList.get(i).getName());
//               if(i != signcontentList.size() - 1){
//                   stringBuffer.append(";");
//               }
//           }
           for (int i = 0;i<signcontentList.size();i++){
               if(signcontentList.get(i).getName()==null
                    &&signcontentList.get(i).getTime()==null){
                   stringBuffer.append("null;");
                   continue;
               }
               stringBuffer.append(signcontentList.get(i).getName());
               stringBuffer.append(":");
               stringBuffer.append(signcontentList.get(i).getTime());
               stringBuffer.append(";");
           }
           igtcoaMapper.updateProcessSignInfo(stringBuffer.toString(),signMsg.getWorkflow_id(),signinfo.getDocument_id());
       }

    }

    public void insertOAFiles(String fileID,String fileName,String filePath){
        igtcoaMapper.insertOAFile(fileName,filePath,fileID);
    }
}
