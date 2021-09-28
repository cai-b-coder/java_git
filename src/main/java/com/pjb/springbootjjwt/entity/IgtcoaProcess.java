package com.pjb.springbootjjwt.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IgtcoaProcess {
    String workflow_id ;
    String project_name ;
    String workflow_name ;
    String workcode ;
    String privary ;
    @JsonFormat(pattern = "yyyy-MM-dd")
    Date created_time ;
}
