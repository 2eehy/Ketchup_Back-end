package com.devsplan.ketchup.board.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Setter
@Getter
public class BoardDTO {

    private int boardNo;                        // 게시물 번호
    private int memberNo;                       // 사번
    private int departmentNo;                   // 부서번호
    private String boardTitle;                  // 게시물 제목
    private BoardFileDTO boardFiles;       // 게시물 파일
    private String boardContent;                // 게시물 내용
    private Timestamp boardCreateDttm;      // 게시글 등록일시
    private Timestamp boardUpdateDttm;      // 게시글 수정일시

    public BoardDTO(int boardNo, String boardTitle, String boardContent, Timestamp boardCreateDttm) {
        this.boardNo = boardNo;
        this.boardTitle = boardTitle;
        this.boardContent = boardContent;
        this.boardCreateDttm = boardCreateDttm;
    }
    public BoardDTO(int boardNo, String boardTitle, BoardFileDTO boardFiles, String boardContent, Timestamp boardCreateDttm) {
        this.boardNo = boardNo;
        this.boardTitle = boardTitle;
        this.boardFiles = boardFiles;
        this.boardContent = boardContent;
        this.boardCreateDttm = boardCreateDttm;
    }


    public BoardDTO(int boardNo, int memberNo, int departmentNo, String boardTitle, String boardContent, Timestamp boardCreateDttm) {
        this.boardNo = boardNo;
        this.memberNo = memberNo;
        this.departmentNo = departmentNo;
        this.boardTitle = boardTitle;
        this.boardContent = boardContent;
        this.boardCreateDttm = boardCreateDttm;
    }
    public BoardDTO(int boardNo, int memberNo, int departmentNo, String boardTitle, BoardFileDTO boardFiles, String boardContent, Timestamp boardCreateDttm) {
        this.boardNo = boardNo;
        this.memberNo = memberNo;
        this.departmentNo = departmentNo;
        this.boardTitle = boardTitle;
        this.boardFiles = boardFiles;
        this.boardContent = boardContent;
        this.boardCreateDttm = boardCreateDttm;
    }
}
