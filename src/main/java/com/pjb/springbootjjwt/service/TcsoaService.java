package com.pjb.springbootjjwt.service;

import com.pjb.springbootjjwt.SpringbootJjwtApplication;
import com.pjb.springbootjjwt.mapper.IgtcoaMapper;
import com.teamcenter.clientx.AppXSession;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.services.strong.core.FileManagementService;
import com.teamcenter.services.strong.core.LOVService;
import com.teamcenter.services.strong.core.SessionService;
import com.teamcenter.services.loose.core._2006_03.FileManagement;
import com.teamcenter.services.strong.core._2006_03.DataManagement;
import com.teamcenter.services.strong.core._2007_01.Session;
import com.teamcenter.services.strong.core._2013_05.LOV;
import com.teamcenter.services.strong.query.SavedQueryService;
import com.teamcenter.services.strong.query._2006_03.SavedQuery;
import com.teamcenter.services.strong.workflow.WorkflowService;
import com.teamcenter.services.strong.workflow._2007_06.Workflow;
import com.teamcenter.soa.client.FileManagementUtility;
import com.teamcenter.soa.client.GetFileResponse;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.strong.*;
import com.teamcenter.soa.exceptions.NotLoadedException;
import org.apache.poi.hpsf.Property;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TcsoaService {

    public static File getTcfileByFileId2(String fileId) {
        DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
        File fileData = null;
        ServiceData resultDate = dmService.loadObjects(new String[]{fileId});
        Dataset dataset = null;
        ModelObject modelObject = null;
        if (resultDate.sizeOfPlainObjects() > 0) {
            modelObject = resultDate.getPlainObject(0);
            if (modelObject instanceof Dataset) {
                dataset = (Dataset) modelObject;
            }
        }
        if (dataset == null) {
            return null;
        }
        fileData = TcsoaService.loadFileFromTc(dataset);
        return fileData;
    }

    public static String getDatasetFileName(String fileId) {
        String fileOrgName = null;
        DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
        ServiceData resultDate = dmService.loadObjects(new String[]{fileId});
        Dataset dataset = null;
        ModelObject modelObject = null;
        if (resultDate.sizeOfPlainObjects() > 0) {
            modelObject = resultDate.getPlainObject(0);
            if (modelObject instanceof Dataset) {
                dataset = (Dataset) modelObject;
            }
        }
        if (dataset == null) {
            return null;
        }
        fileOrgName = getDatasetFileName(dataset);
        return fileOrgName;
    }


    public static byte[] getTcfileByFileId(String fileId) {
        DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
        byte[] fileData = null;
        ServiceData resultDate = dmService.loadObjects(new String[]{fileId});
        Dataset dataset = null;
        ModelObject modelObject = null;
        if (resultDate.sizeOfPlainObjects() > 0) {
            modelObject = resultDate.getPlainObject(0);
            if (modelObject instanceof Dataset) {
                dataset = (Dataset) modelObject;
            }
        }
        if (dataset == null) {
            return null;
        }
        File tcFile = TcsoaService.loadFileFromTc(dataset);
        if (tcFile != null) {
            fileData = fileConvertToByteArray(tcFile);
        }

        return fileData;
    }

    private static String getDatasetFileName(Dataset dataset) {
        String orgName = null;
        try {
            DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());

            ModelObject[] objects = {dataset};
            dmService.refreshObjects(objects);
            dmService.getProperties(objects, new String[]{"ref_list"});
            ModelObject[] dsfilevec = dataset.get_ref_list();
            ImanFile dsfile = (ImanFile) dsfilevec[0];

            ModelObject[] objects1 = {dsfile};
            String[] attributes1 = {"relative_directory_path", "original_file_name"};
            dmService.refreshObjects(objects1);
            dmService.getProperties(objects1, attributes1);
            orgName = dsfile.get_original_file_name();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return orgName;
    }

    /***
     * 创建数据集并发布
     * @param uploadFiles
     * @param itemRevision
     */
    private static String createDataset(List<File> uploadFiles, ItemRevision itemRevision,Map<String,String> dstSettingMap) {
        DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
        String returnString = "COMPLETE";
        //数据集创建
        try {
            for (File uploadFile : uploadFiles) {
                if(uploadFile == null || !uploadFile.exists()){
                    continue;
                }
                String suffixName = uploadFile.getName().substring(uploadFile.getName().lastIndexOf(".") + 1);
                String[] dstSetting = dstSettingMap.get(suffixName).split(";");

                com.teamcenter.services.strong.core._2010_04.DataManagement.DatasetInfo datasetInfo = new com.teamcenter.services.strong.core._2010_04.DataManagement.DatasetInfo();
                datasetInfo.name = uploadFile.getName().substring(0,uploadFile.getName().lastIndexOf(".")).split("_")[1];
                datasetInfo.type = dstSetting[0];
                datasetInfo.description = "TCServerCreate";
                com.teamcenter.services.strong.core._2010_04.DataManagement.DatasetInfo[] datasetInfos = { datasetInfo };
                System.out.println("数据集名称：" + datasetInfo.name + " 数据集类型：" + datasetInfo.type);
                com.teamcenter.services.strong.core._2010_04.DataManagement.CreateDatasetsResponse createDatasetsResponse = dmService.createDatasets(datasetInfos);
                if (createDatasetsResponse.servData.sizeOfPartialErrors() > 0) {
                    returnString = "创建数据集出错！！！";
                    for(int errorIndex = 0;errorIndex < createDatasetsResponse.servData.sizeOfPartialErrors();errorIndex ++) {
                        System.out.println("数据集创建错误信息:" + String.join(",", createDatasetsResponse.servData.getPartialError(errorIndex).getMessages()));
                    }
                    return returnString;
                }
                if(createDatasetsResponse.datasetOutput.length == 0){
                    returnString = "创建数据集失败！！！，未能正确输出！！！";
                    return returnString;
                }
                Dataset dataset = createDatasetsResponse.datasetOutput[0].dataset;
                if (dataset == null) {
                    returnString = "创建数据集出错！！！数据集未成功创建！！！";
                    return returnString;
                }

                DataManagement.Relationship[] relationships = new DataManagement.Relationship[1];
                relationships[0] = new DataManagement.Relationship();
                relationships[0].clientId = "";
                relationships[0].primaryObject = itemRevision;
                relationships[0].secondaryObject = dataset;
                relationships[0].relationType = "IMAN_specification";
                relationships[0].userData = null;

                dmService.refreshObjects2(new ModelObject[]{itemRevision, dataset}, true);
                DataManagement.CreateRelationsResponse createRelationsResponse = dmService.createRelations(relationships);
                dmService.refreshObjects2(new ModelObject[]{itemRevision, dataset}, false);
                if(createRelationsResponse.serviceData.sizeOfPartialErrors() > 0){
                    for(int errorIndex = 0;errorIndex < createRelationsResponse.serviceData.sizeOfPartialErrors();errorIndex ++) {
                        System.out.println("挂载数据集错误信息:" + String.join(",", createRelationsResponse.serviceData.getPartialError(errorIndex).getMessages()));
                    }
                    return "挂载数据集到新建PR版本出错！！！";
                }

                //文件挂载
                byPass(true);
                FileManagement.DatasetFileInfo datasetFileInfo = new FileManagement.DatasetFileInfo();
                datasetFileInfo.fileName = uploadFile.getAbsolutePath();
                datasetFileInfo.allowReplace = true;
                datasetFileInfo.isText = false;
                datasetFileInfo.namedReferencedName = dstSetting[1];
                FileManagement.DatasetFileInfo[] datasetFileInfos = {datasetFileInfo};

                FileManagement.GetDatasetWriteTicketsInputData inputData = new FileManagement.GetDatasetWriteTicketsInputData();
                inputData.dataset = dataset;
                inputData.createNewVersion = true;
                inputData.datasetFileInfos = datasetFileInfos;
                FileManagement.GetDatasetWriteTicketsInputData[] inputDatas = {inputData};


                FileManagementUtility fileManagementUtility = new FileManagementUtility(AppXSession.getConnection(), null, new String[]{SpringbootJjwtApplication.TC_FMSURL}, new String[]{SpringbootJjwtApplication.TC_FMSURL}, SpringbootJjwtApplication.TC_FCCCACH);
                ServiceData serviceData = fileManagementUtility.putFiles(inputDatas);
                if (serviceData.sizeOfPartialErrors() > 0) {
                    returnString = "数据集挂载文件出错！！！";
                    return returnString;
                }
                //发布数据集
                WorkflowService workflowService = WorkflowService.getService(AppXSession.getConnection());
                Workflow.ReleaseStatusInput[] releaseStatusInputs = new Workflow.ReleaseStatusInput[1];
                releaseStatusInputs[0] = new Workflow.ReleaseStatusInput();
                releaseStatusInputs[0].objects = new WorkspaceObject[]{ dataset };
                releaseStatusInputs[0].operations = new Workflow.ReleaseStatusOption[1];
                releaseStatusInputs[0].operations[0] = new Workflow.ReleaseStatusOption();
                releaseStatusInputs[0].operations[0].existingreleaseStatusTypeName = "delete";
                releaseStatusInputs[0].operations[0].newReleaseStatusTypeName = "SET8_Release";
                releaseStatusInputs[0].operations[0].operation = "Append";

                Workflow.SetReleaseStatusResponse setReleaseStatusResponse = workflowService.setReleaseStatus(releaseStatusInputs);
            }
            //发布版本
            WorkflowService workflowService = WorkflowService.getService(AppXSession.getConnection());
            Workflow.ReleaseStatusInput[] releaseStatusInputs = new Workflow.ReleaseStatusInput[1];
            releaseStatusInputs[0] = new Workflow.ReleaseStatusInput();
            releaseStatusInputs[0].objects = new WorkspaceObject[]{ itemRevision };
            releaseStatusInputs[0].operations = new Workflow.ReleaseStatusOption[1];
            releaseStatusInputs[0].operations[0] = new Workflow.ReleaseStatusOption();
            releaseStatusInputs[0].operations[0].existingreleaseStatusTypeName = "delete";
            releaseStatusInputs[0].operations[0].newReleaseStatusTypeName = "SET8_Release";
            releaseStatusInputs[0].operations[0].operation = "Append";
            Workflow.SetReleaseStatusResponse setReleaseStatusResponse = workflowService.setReleaseStatus(releaseStatusInputs);
        } catch (FileNotFoundException e) {
            returnString = "挂载文件未找到！！！" + e.getMessage();
            e.printStackTrace();
        } catch (ServiceException e) {
            returnString = "发布数据集和版本出现异常！！！" + e.getMessage();
            e.printStackTrace();
        } finally {
            byPass(false);
        }
        return returnString;
    }

    /***
     * 创建PR对象
     * @param itemName
     * @param propertyMap
     * @param folderUid
     */
    public static String createTcObject(String itemName, String needItemIds, Map<String, String> propertyMap, String folderUid, List<File> uploadFiles, Map<String, String> dstSettingMap,StringBuffer buffer) {
        DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
        String returnString = "COMPLETE";
        //获取文件夹
        Folder folder = null;
        ModelObject[] modelObjects = loadModelobjects(new String[]{folderUid}, dmService);
        for (ModelObject modelObject : modelObjects) {
            if (modelObject instanceof Folder) {
                folder = (Folder) modelObject;
            }
        }
        //testLov();
        //创建对象
        if (folder != null) {
            DataManagement.ItemProperties itemProperties = new DataManagement.ItemProperties();
            itemProperties.name = itemName;
            itemProperties.type = "SET8_PR";
//            itemProperties.extendedAttributes = new DataManagement.ExtendedAttributes[1];
//            itemProperties.extendedAttributes[0] = new DataManagement.ExtendedAttributes();
//            itemProperties.extendedAttributes[0].attributes = propertyMap;
//            itemProperties.extendedAttributes[0].objectType = "SET8_PRRevision";
            DataManagement.ItemProperties[] itemPropertiesArray = {itemProperties};
            DataManagement.CreateItemsResponse itemsResponse = dmService.createItems(itemPropertiesArray, folder, "contents");
            if (itemsResponse.serviceData.sizeOfPartialErrors() > 0) {
                returnString = "创建PR对象出错！！！";
                System.out.println(itemsResponse.serviceData.getPartialError(0));
                return returnString;
            } else {
                ItemRevision itemRevision = itemsResponse.output[0].itemRev;
                //设置属性
                byPass(true);
                dmService.getProperties(new ModelObject[]{ itemRevision }, propertyMap.keySet().toArray(new String[propertyMap.keySet().size()]));
                dmService.setDisplayProperties(new ModelObject[]{ itemRevision },propertyMap);
                byPass(false);
                String[] propValues = needItemIds.split(";");
                if(itemRevision == null){
                    return "创建itemRevision失败";
                }
                for (String item_id : propValues) {
                    System.out.println("查询ID:" + item_id);
                    com.teamcenter.services.strong.query._2007_06.SavedQuery.SavedQueryResults results = queryItemById(item_id, dmService);
                    if (results != null && results.numOfObjects > 0) {
                        System.out.println("查询结果数量:" + results.numOfObjects);
                        ItemRevision latestItemRevisioin = (ItemRevision) results.objects[0];
                        //创建关系
                        DataManagement.Relationship[] relationships = new DataManagement.Relationship[1];
                        relationships[0] = new DataManagement.Relationship();
                        relationships[0].clientId = "";
                        relationships[0].primaryObject = itemRevision;
                        relationships[0].secondaryObject = latestItemRevisioin;
                        relationships[0].relationType = "SET8_WTLZJ";
                        relationships[0].userData = null;

                        byPass(true);
                        dmService.refreshObjects2(new ModelObject[]{itemRevision, latestItemRevisioin}, true);
                        DataManagement.CreateRelationsResponse createRelationsResponse = dmService.createRelations(relationships);
                        dmService.refreshObjects2(new ModelObject[]{itemRevision, latestItemRevisioin}, false);
                        byPass(false);
                    }
                }
                returnString = createDataset(uploadFiles, itemRevision,dstSettingMap);
                //把id放入buffer
                dmService.getProperties(new ModelObject[] { itemRevision }, new String[] { "item_id"});
                String id = "";
                try{
                    id = itemRevision.get_item_id();
                }catch (NotLoadedException e) {
                    e.printStackTrace();
                }
                buffer.append(id);
            }
        } else {
            returnString = "文件夹获取失败，请检查首选项UID配置是否正确！！！";
        }

        return returnString;
    }

    /***
     * 解析EXCEL配置
     * @param propertyMap
     * @param innerMap
     * @param dstSettingMap
     * @param lovMap
     */
    public static void parseExcelSetting(Map<String, String> propertyMap, Map<String, Object> innerMap, Map<String, String> dstSettingMap, Map<String, String> lovMap) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook("D:/Siemens/sign/PR属性配置表.xlsx");
            XSSFSheet sheet = workbook.getSheetAt(0);
            int rowNum = sheet.getPhysicalNumberOfRows();
            for (int i = 1; i < rowNum; i++) {
                Cell oaCell = sheet.getRow(i).getCell(0);
                Cell tcCell = sheet.getRow(i).getCell(2);
                Cell typeCell = sheet.getRow(i).getCell(3);
                Cell bakCell = sheet.getRow(i).getCell(4);
                Cell suffixCell = sheet.getRow(i).getCell(6);
                Cell dstTypeCell = sheet.getRow(i).getCell(7);
                Cell refCell = sheet.getRow(i).getCell(8);
                if (oaCell != null && tcCell != null) {
                    if(!"LOV".equals(typeCell.getStringCellValue().toUpperCase())) {
                        if (innerMap.get(oaCell.getStringCellValue()) != null) {
                            propertyMap.put(tcCell.getStringCellValue(), String.valueOf(innerMap.get(oaCell.getStringCellValue())));
                        }
                    }else{
                        if (innerMap.get(oaCell.getStringCellValue()) != null) {
                            lovMap.put(tcCell.getStringCellValue() + ";" + bakCell.getStringCellValue(), String.valueOf(innerMap.get(oaCell.getStringCellValue())));
                        }
                    }
                }
                if(suffixCell != null && dstTypeCell != null && refCell != null){
                    dstSettingMap.put(suffixCell.getStringCellValue(),dstTypeCell.getStringCellValue() + ";" + refCell.getStringCellValue());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /***
     * 开启旁路
     * @param bypass
     */
    public static void byPass(boolean bypass) {
        com.teamcenter.services.loose.core.SessionService sessionservice = com.teamcenter.services.loose.core.SessionService
                .getService(AppXSession.getConnection());
        com.teamcenter.services.loose.core._2007_12.Session.StateNameValue
                astatenamevalue[] = new com.teamcenter.services.loose.core._2007_12.Session.StateNameValue[1];
        astatenamevalue[0] = new com.teamcenter.services.loose.core._2007_12.Session.StateNameValue();
        astatenamevalue[0].name = "bypassFlag";
        astatenamevalue[0].value = Boolean.toString(bypass);
        ServiceData servicedata = sessionservice
                .setUserSessionState(astatenamevalue);
        if(servicedata.sizeOfPartialErrors() > 0){
            System.out.println("开启旁路失败！！！");
        }
    }

    /***
     * 将UID转换为对象
     *
     * @param puids
     * @return
     */
    public static ModelObject[] loadModelobjects(String[] puids, DataManagementService dmService) {
        List<ModelObject> modelObjects = new ArrayList<ModelObject>();

        ServiceData resultDate = dmService.loadObjects(puids);

        for (int i = 0; i < resultDate.sizeOfPlainObjects(); i++) {
            ModelObject mobj = resultDate.getPlainObject(i);
            modelObjects.add(mobj);
        }

        return modelObjects.toArray(new ModelObject[modelObjects.size()]);
    }

    /***
     * 获取首选项
     * @param prefername
     * @return
     */
    public static Session.ReturnedPreferences[] getTCPreferences(String prefername) throws ServiceException {
        SessionService sessionservice = SessionService.getService(AppXSession.getConnection());
        Session.ScopedPreferenceNames[] prefNames = new Session.ScopedPreferenceNames[1];
        Session.ScopedPreferenceNames scopedPref = new Session.ScopedPreferenceNames();
        scopedPref.names = new String[]{prefername};
        scopedPref.scope = "site";
        prefNames[0] = scopedPref;

        Session.MultiPreferencesResponse resp = null;
        resp = sessionservice.getPreferences(prefNames);
        Session.ReturnedPreferences[] preferenceResp = resp.preferences;
        return preferenceResp;
    }

    /***
     * 获取属性值
     * @param properyName
     * @param dmService
     * @param modelObject
     * @return
     */
    private static String getTcPropertyDisplayValue(String properyName, DataManagementService dmService, ModelObject modelObject) {
        String returnValue = "";
        ModelObject[] modelObjects = new ModelObject[]{modelObject};
        dmService.refreshObjects(modelObjects);
        dmService.getProperties(modelObjects, new String[]{properyName});
        try {
            if (modelObjects[0].getPropertyObject(properyName) != null) {
                returnValue = modelObjects[0].getPropertyObject(properyName).getDisplayValue();
            }
        } catch (NotLoadedException e) {
            e.printStackTrace();
        }
        return returnValue;
    }

    /***
     * 查询最新零组件版本
     * @param itemId
     * @param dmService
     * @return
     */
    public static com.teamcenter.services.strong.query._2007_06.SavedQuery.SavedQueryResults queryItemById(String itemId, DataManagementService dmService) {
        ImanQuery imanQuery = getImanQuery("Latest Item Revision...");
        if(imanQuery == null){
            System.out.println("未找到该查询！！！");
            return null;
        }
        SavedQueryService queryService = SavedQueryService.getService(AppXSession.getConnection());
        com.teamcenter.services.strong.query._2007_06.SavedQuery.SavedQueryInput[] savedQueryInputs = new com.teamcenter.services.strong.query._2007_06.SavedQuery.SavedQueryInput[1];
        savedQueryInputs[0] = new com.teamcenter.services.strong.query._2007_06.SavedQuery.SavedQueryInput();
        savedQueryInputs[0].query = imanQuery;
        savedQueryInputs[0].entries = new String[]{"Item ID"};
        savedQueryInputs[0].values = new String[]{ itemId };
        com.teamcenter.services.strong.query._2007_06.SavedQuery.ExecuteSavedQueriesResponse executeSavedQueryResponse = queryService.executeSavedQueries(savedQueryInputs);
        if(executeSavedQueryResponse.serviceData.sizeOfPartialErrors() > 0){
            System.out.println("查询出错：" + executeSavedQueryResponse.serviceData.getPartialError(0).getMessages());
        }
        com.teamcenter.services.strong.query._2007_06.SavedQuery.SavedQueryResults results = executeSavedQueryResponse.arrayOfResults[0];
        return results;
    }

    /**
     * 获取查询构建器
     *
     * @param name
     * @return
     */
    public static ImanQuery getImanQuery(String name) {
        ImanQuery query = null;
        SavedQueryService queryService = SavedQueryService.getService(AppXSession.getConnection());
        try {
            SavedQuery.GetSavedQueriesResponse savedQueriesResponse = queryService.getSavedQueries();
            for (int i = 0; i < savedQueriesResponse.queries.length; i++) {
                //name 查询构建器的名称
                if (savedQueriesResponse.queries[i].name.equals(name)) {
                    query = savedQueriesResponse.queries[i].query;
                    break;
                }
            }
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        return query;
    }

    /**
     * 从TC下载数据集
     *
     * @param dataset
     * @return
     */
    private static File loadFileFromTc(Dataset dataset) {
        if (dataset == null) {
            return null;
        }
        File outFile = null;
        try {
            DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
            FileManagementService fmService = FileManagementService.getService(AppXSession.getConnection());
            FileManagementUtility fmu = new FileManagementUtility(AppXSession.getConnection(),
                    null,
                    new String[]{SpringbootJjwtApplication.TC_FMSURL},
                    new String[]{SpringbootJjwtApplication.TC_FMSURL},
                    SpringbootJjwtApplication.TC_FCCCACH);
            ModelObject[] objects = {dataset};
            dmService.refreshObjects(objects);
            dmService.getProperties(objects, new String[]{"ref_list"});
            objects = dataset.get_ref_list();

            GetFileResponse getFileResponse = fmu.getFiles(objects);
            File[] fileinfovec = getFileResponse.getFiles();
            outFile = fileinfovec[0];
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return outFile;
    }

    /**
     * 把文件转换为BYTE数组
     *
     * @param file
     * @return
     */
    private static byte[] fileConvertToByteArray(File file) {
        byte[] data = null;

        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            int len;
            byte[] buffer = new byte[1024];
            while ((len = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }

            data = baos.toByteArray();

            fis.close();
            baos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }


}
