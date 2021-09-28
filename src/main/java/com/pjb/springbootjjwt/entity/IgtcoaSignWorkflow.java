package com.pjb.springbootjjwt.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IgtcoaSignWorkflow {
     String workflow_id ;
     String approval_results ;
     String approval_type ;
     List<String> attached_files ;
     List<IgtcoaSignInfo> sign_info ;
}
