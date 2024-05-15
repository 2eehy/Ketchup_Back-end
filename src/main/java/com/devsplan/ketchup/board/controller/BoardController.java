package com.devsplan.ketchup.board.controller;

import com.devsplan.ketchup.board.dto.BoardDTO;
import com.devsplan.ketchup.board.service.BoardService;
import com.devsplan.ketchup.common.*;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.devsplan.ketchup.util.TokenUtils.decryptToken;

@Slf4j
@RestController
@RequestMapping("/boards")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService){
        this.boardService = boardService;
    }

    /* 게시물 목록 조회(부서, 페이징, 검색) */
    @GetMapping
    public ResponseEntity<ResponseDTO> selectBoardList(@RequestParam(name = "offset", defaultValue = "1") String offset
                                                        , @RequestParam(required = false) String title
                                                        , @RequestHeader("Authorization") String token) {
        try {
            Criteria cri = new Criteria(Integer.parseInt(offset),10);
            PagingResponseDTO pagingResponseDTO = new PagingResponseDTO();

            // 클레임에서 depNo 추출
            Integer depNo = decryptToken(token).get("depNo", Integer.class);
            System.out.println("depNo : " + depNo);

            Page<BoardDTO> boardList;

            if (depNo == null) {
                // 부서 번호가 없는 경우 모든 부서의 자료실 정보를 조회합니다.
                boardList = boardService.selectAllBoards(cri, title);
                pagingResponseDTO.setData(boardList);
            } else {
                // 특정 부서의 자료실 정보를 조회합니다.
                boardList = boardService.selectBoardList(depNo, cri, title);
                pagingResponseDTO.setData(boardList);
                System.out.println("depNo : " + depNo);
            }

            pagingResponseDTO.setPageInfo(new PageDTO(cri, (int) boardList.getTotalElements()));
            return ResponseEntity.ok().body(new ResponseDTO(HttpStatus.OK, "목록 조회 성공", pagingResponseDTO));

        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDTO("토큰이 만료되었습니다."));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDTO("유효하지 않은 토큰입니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류", null));
        }
    }

    /* 게시물 상세 조회 */
    @GetMapping("/{boardNo}")
    public ResponseEntity<ResponseDTO> selectBoardDetail(@PathVariable("boardNo") int boardNo, @RequestHeader("Authorization") String token) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
            Integer depNo = decryptToken(token).get("depNo", Integer.class);

            // 게시물 상세 정보 조회
            BoardDTO boardDTO = boardService.selectBoardDetail(boardNo);

            if (boardDTO == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO(HttpStatus.NOT_FOUND, "게시물을 찾을 수 없습니다.", null));
            }

            // 해당 게시물을 볼 수 있는지 확인
            if (boardDTO.getDepartmentNo() != depNo) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ResponseDTO(HttpStatus.FORBIDDEN, "해당 게시물에 접근할 수 있는 권한이 없습니다.", null));
            }

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new ResponseDTO(HttpStatus.OK, "상세 조회 성공", boardDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류", null));
        }
    }

    /* 게시물 등록 */
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ResponseDTO> insertBoard(@RequestPart("boardDTO") BoardDTO boardDTO
                                                   , @RequestPart(required = false, value="files") List<MultipartFile> files
                                                   , @RequestHeader("Authorization") String token) {
        try {
            String memberNo = decryptToken(token).get("memberNo", String.class);
            Integer depNo = decryptToken(token).get("depNo", Integer.class);

            System.out.println("memberNo : " + memberNo);
            System.out.println("depNo : " + depNo);
//            System.out.println("File Content: " + new String(files.toString().getBytes(), StandardCharsets.UTF_8));

//            boardDTO.setMemberNo(memberNo);
//            boardDTO.setDepartmentNo(depNo);

            // 게시물 등록 요청한 회원의 부서 번호와 게시물에 등록하려는 부서 번호가 일치하는지 확인
            if (!depNo.equals(boardDTO.getDepartmentNo())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ResponseDTO(HttpStatus.FORBIDDEN, "해당 부서의 게시물만 등록할 수 있습니다.", null));
            }

            Object data;
            if (files != null && !files.isEmpty()) {
                data = boardService.insertBoardWithFile(boardDTO, files, memberNo, depNo);
            } else {
                data = boardService.insertBoard(boardDTO, memberNo, depNo);
            }

            return ResponseEntity.ok().body(new ResponseDTO(HttpStatus.OK, "게시물 등록 성공", data));
        } catch (ExpiredJwtException e) {
            log.error("토큰이 만료되었습니다.", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDTO("토큰이 만료되었습니다."));
        } catch (JwtException e) {
            log.error("유효하지 않은 토큰입니다.", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDTO("유효하지 않은 토큰입니다."));
        } catch (Exception e) {
            log.error("서버 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류", null));
        }
    }

    /* 게시물 수정 */
    @PutMapping("/{boardNo}")
    public ResponseEntity<ResponseDTO> updateBoard(@PathVariable int boardNo
                                                    , @RequestPart("boardDTO") BoardDTO boardDTO
                                                    , @RequestPart("files") List<MultipartFile> files
                                                    , @RequestHeader("Authorization") String token) {
        try {
            String memberNo = decryptToken(token).get("memberNo", String.class);
            int positionNo = decryptToken(token).get("positionNo", Integer.class);

            System.out.println("memberNo : " + memberNo);

            // 작성자인 경우 또는 LV2 또는 LV3 권한을 가진 부서원인 경우에만 삭제 가능하도록 제한
            if (!memberNo.equals(boardDTO.getMemberNo()) &&
                    positionNo != 2 &&
                    positionNo != 3) {
                log.error("수정 권한이 없습니다.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseDTO(HttpStatus.FORBIDDEN, "수정 권한이 없습니다.", null));
            }

            Object data;
            // 파일이 첨부되었는지 여부에 따라 서비스 메서드 호출 방식을 변경
            if (files != null && !files.isEmpty()) {
                data = boardService.updateBoardWithFile(boardNo, boardDTO, files, memberNo);
            } else {
                data = boardService.updateBoard(boardNo, boardDTO, memberNo);
            }

            return ResponseEntity.ok().body(new ResponseDTO(HttpStatus.OK, "게시물 수정 성공", data));
        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDTO("토큰이 만료되었습니다."));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDTO("유효하지 않은 토큰입니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류", null));
        }
    }


    /* 게시물 삭제 */
    @DeleteMapping("/{boardNo}")
    public ResponseEntity<ResponseDTO> deleteBoard(@PathVariable int boardNo, @RequestHeader("Authorization") String token) {

        try {
            String memberNo = decryptToken(token).get("memberNo", String.class);
            int positionNo = decryptToken(token).get("positionNo", Integer.class);

            BoardDTO boardDTO = boardService.getBoardById(boardNo);

            // 작성자인 경우 또는 LV2 또는 LV3 권한을 가진 부서원인 경우에만 삭제 가능하도록 제한
            if (boardDTO != null) {
                String writerMemberNo = boardDTO.getMemberNo();

                // 작성자인 경우 또는 LV2 또는 LV3 권한을 가진 부서원인 경우에만 삭제 가능
                if (writerMemberNo.equals(memberNo) || positionNo == 2 || positionNo == 3) {
                    Object data = boardService.deleteBoard(boardNo, memberNo);
                    return ResponseEntity.ok().body(new ResponseDTO(HttpStatus.OK, "게시물 삭제 성공", data));
                } else {
                    log.error("삭제 권한이 없습니다.");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseDTO(HttpStatus.FORBIDDEN, "삭제 권한이 없습니다.", null));
                }
            } else {
                log.error("게시물을 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO(HttpStatus.NOT_FOUND, "게시물을 찾을 수 없습니다.", null));
            }

        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDTO("토큰이 만료되었습니다."));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDTO("유효하지 않은 토큰입니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류", null));
        }
    }
}
