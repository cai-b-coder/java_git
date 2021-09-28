package com.pjb.springbootjjwt.mapper;

import com.pjb.springbootjjwt.entity.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IgtcoaMapper {
    List<IgtcoaDocumentInfo> listDocumentInfos(@Param("processId") String processId);
    List<IgtcoaProcess> listAllProcess(IgtcoaProcessQuery query);
    List<IgtcoaFile> listAllFile(@Param("processId") String processId);
    List<IgtcoaFile> listAllFile2(@Param("DOCUMENT_ID") String DOCUMENT_ID,
                                  @Param("DOCUMENT_REV") String DOCUMENT_REV);
    List<IgtcoaPrInfo> listAllPrInfos(@Param("DOCUMENT_ID") String DOCUMENT_ID,
                                  @Param("DOCUMENT_REV") String DOCUMENT_REV);
    List<IgtcoaQiInfo> listAllQiInfos(@Param("ECR_ID") String ECR_ID,@Param("ECR_VER") String ECR_VER);
    List<IgtcoaQiFile> listAllQiFiles(@Param("QI_ID") String QI_ID,@Param("QI_VER") String QI_VER);
    String getLovInnerValue(@Param("displayValue")String displayValue,@Param("lovName")String lovName);
    String getLovInnerValue2(@Param("displayValue")String displayValue,@Param("lovName")String lovName);
    IgtcoaFileProcess getFileProcess(@Param("processId") String processId);
    void updateProcessApproval(@Param("processId") String processId,
                               @Param("approval") String approval);
    void updateProcessSignInfo(@Param("result")String result,
                               @Param("processId")String processId,
                               @Param("fileId")String fileId);
    void insertFileSigninfo(@Param("fileID") String fileID,
                            @Param("signName") String signName,
                            @Param("signDate") String signDate,
                            @Param("signNo") int signNo);
    void deleteFileSigninfo(@Param("fileID") String fileID);
    void insertOAFile(@Param("fileName") String fileName,
                      @Param("filePath") String filePath,
                      @Param("fileID") String fileID);
}
