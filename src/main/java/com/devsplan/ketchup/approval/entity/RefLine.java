package com.devsplan.ketchup.approval.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
@Entity
@Table(name = "TBL_REF_LINE")
public class RefLine {
    @Id
    @Column(name = "REF_LINE_NO" )
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int refLineNo;
    @Column(name = "APPROVAL_NO", nullable = false)
    private int approvalNo;
    @Column(name = "REF_MEMBER_NO", nullable = false)
    private int refMemberNo;

    protected RefLine(){}
}
