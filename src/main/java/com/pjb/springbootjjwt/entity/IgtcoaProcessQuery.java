package com.pjb.springbootjjwt.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IgtcoaProcessQuery {
    String werks ;
    String workcode ;
    String workflow_type ;
    String privary ;
    String workflow_name ;
    String start_time ;
    String end_time ;
}
