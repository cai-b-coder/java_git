package com.pjb.springbootjjwt.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IgtcoaFileProcess {
    String privary;
    String workflow_id ;
    String workflow_name ;
    String workflow_type ;
    @JsonFormat(pattern = "yyyy-MM-dd")
    Date created_time;
    String originator;
    String project_id;
    String project_name;
    String product_name;
    String project_codename;
    String project_type;
    String product_type;
    String werks;
    String workcode;
    String project_level;
    String product_level;
    String security_level;
    List<IgtcoaDocumentInfo> DocumentInfo ;
}
