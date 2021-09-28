package com.pjb.springbootjjwt.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IgtcoaDocumentInfo {
    String document_id;
    String document_rev;
    String document_type1;
    String document_type;
    String dcc_type;
    String dcc_stage;
    String dcc_upper;
    String ecn_id;
    String ecn_rev;
    String ecn_owner;
    List<IgtcoaFile> files;
    String ecr_id;
    String ecr_ver;
    String ecr_owner;
    List<IgtcoaPrInfo> prInfo;
    List<IgtcoaQiInfo> qiInfo;
}
