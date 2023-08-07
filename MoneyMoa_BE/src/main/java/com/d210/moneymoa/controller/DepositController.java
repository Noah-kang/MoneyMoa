package com.d210.moneymoa.controller;


import com.d210.moneymoa.domain.oauth.AuthTokensGenerator;
import com.d210.moneymoa.dto.Deposit;
import com.d210.moneymoa.dto.DepositComment;
import com.d210.moneymoa.dto.LikedDeposit;
import com.d210.moneymoa.service.DepositCommentService;
import com.d210.moneymoa.service.DepositService;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deposit")
public class DepositController {

    @Autowired
    DepositService depositService;

    @Autowired
    private AuthTokensGenerator authTokensGenerator;

    @Autowired
    private DepositCommentService depositCommentService;

    // 예금상품 API 정보 저장
    @ApiOperation(value = "DB에 예금상품 API 정보 저장 *상품정보 조회가능할시 호출 금지")
    @GetMapping("/save")
    public ResponseEntity<Map<String,Object>> saveDepositProducts(String[] args) throws InterruptedException {

        Map<String,Object>resultMap = new HashMap<>();

        HttpStatus status;

        try{
            depositService.saveDepositProducts();
            status = HttpStatus.OK;
            resultMap.put("messasge","success");

        }catch (Exception e){
            e.printStackTrace();
            status = HttpStatus.BAD_REQUEST;
            resultMap.put("messasge","fail");
        }
        return new ResponseEntity<Map<String,Object>>(resultMap, status);
    }

    // 예금상품 API 정보 조회
    @ApiOperation(value = "DB에 저장된 모든 예금상품 정보 조회")
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getDepositProductsWithInterestDetails() {

        Map<String, Object> resultMap = new HashMap<>();

        HttpStatus status;

        try {
            List<Deposit> depositProducts = depositService.getAllDepositProducts();

            // spcl 문자열을 리스트로 변환
            for (Deposit deposit : depositProducts) {
                String spclStr = deposit.getSpcl();
                deposit.setSpclList(Arrays.asList(spclStr.split("\n")));

            }

            status = HttpStatus.OK;
            resultMap.put("products", depositProducts);
            resultMap.put("message", "success");
        } catch (Exception e) {
            e.printStackTrace();
            status = HttpStatus.BAD_REQUEST;
            resultMap.put("message", "fail");
        }
        return new ResponseEntity<>(resultMap, status);
    }

    // 예금상품 API 상세정보 조회
    @ApiOperation(value = "productCode에 해당하는 예금상품 상세정보 조회")
    @GetMapping("/{productCode}")
    public ResponseEntity<Map<String, Object>> getDepositProductsWithInterestDetails(@PathVariable String productCode) {

        Map<String, Object> resultMap = new HashMap<>();

        HttpStatus status;

        try {
            Deposit depositProduct = depositService.getDepositProductWithInterestDetails(productCode);

            // spcl 문자열을 목록으로 변환
            String spclStr = depositProduct.getSpcl();
            depositProduct.setSpclList(Arrays.asList(spclStr.split("\n")));

            // 같은 productCode를 가진 DepositComments 조회
            List<DepositComment> depositComments = depositCommentService.findByProductCode(productCode);

            status = HttpStatus.OK;
            resultMap.put("product", depositProduct);
            resultMap.put("comments", depositComments);
            resultMap.put("message", "success");
        } catch (Exception e) {
            e.printStackTrace();
            status = HttpStatus.BAD_REQUEST;
            resultMap.put("message", "fail");
        }
        return new ResponseEntity<>(resultMap, status);
    }

    @ApiOperation(value = "예금 상품을 '찜'으로 등록")
    @PostMapping("/like")
    public ResponseEntity<Map<String, Object>> saveLikedDeposit(@ApiParam(value = "MemberId와, productcode가 잘못되면 안들어갑니다.")
                                                                @RequestBody LikedDeposit likedDeposit,
                                                                @ApiParam(value = "Bearer ${jwt token} 형식으로 전송")
                                                                @RequestHeader("Authorization") String jwt) {
        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status;

        try {
            jwt = jwt.replace("Bearer ", "");
            Long memberId = authTokensGenerator.extractMemberId(jwt);

            likedDeposit.setMemberId(memberId);

            depositService.saveLikedDeposit(likedDeposit);
            status = HttpStatus.OK;
            resultMap.put("likedDeposit", likedDeposit);
            resultMap.put("message", "success");
        } catch (Exception e) {
            e.printStackTrace();
            status = HttpStatus.BAD_REQUEST;
            resultMap.put("message", "fail");
        }
        return new ResponseEntity<>(resultMap, status);
    }

    @ApiOperation(value = "예금상품에 댓글 작성")
    @PostMapping("/{productCode}/comment")
    public ResponseEntity<Map<String, Object>> createComment(@PathVariable String productCode,
                                                             @RequestBody DepositComment depositComment,
                                                             @ApiParam(value = "Bearer ${jwt token} 형식으로 전송")
                                                             @RequestHeader("Authorization") String jwt) {
        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status;

        try {
            jwt = jwt.replace("Bearer ", "");
            Long memberId = authTokensGenerator.extractMemberId(jwt);

            depositComment.setMemberId(memberId);

            depositCommentService.createDepositComment(productCode, depositComment);
            status = HttpStatus.CREATED;
            resultMap.put("depositComment", depositComment);
            resultMap.put("message", "success");
        } catch (Exception e) {
            e.printStackTrace();
            status = HttpStatus.BAD_REQUEST;
            resultMap.put("message", "fail");
        }
        return new ResponseEntity<>(resultMap, status);
    }

    @ApiOperation(value = "예금상품 댓글 삭제")
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<Map<String, Object>> deleteComment(@PathVariable Long commentId,
                                                             @ApiParam(value = "Bearer ${jwt token} 형식으로 전송")
                                                             @RequestHeader("Authorization") String jwt) {
        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status;

        try {
            jwt = jwt.replace("Bearer ", "");
            Long memberId = authTokensGenerator.extractMemberId(jwt);

            depositCommentService.deleteDepositComment(commentId, memberId);
            status = HttpStatus.OK;
            resultMap.put("message", "success");
            resultMap.put("message2", "삭제 되었습니다.");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            status = HttpStatus.FORBIDDEN;
            resultMap.put("message", "fail");
            resultMap.put("message2", "작성자가 아닙니다.");
        } catch (Exception e) {
            e.printStackTrace();
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            resultMap.put("message", "fail");
            resultMap.put("message2", "jwttoken이 잘못되었거나 commentId가 잘못되었습니다.");
        }

        return new ResponseEntity<>(resultMap, status);
    }

    // 클래스 내부에 다음 메서드를 추가
    @ApiOperation(value = "예금상품 댓글 수정")
    @PutMapping("/comment/{commentId}")
    public ResponseEntity<Map<String, Object>> updateComment(@PathVariable Long commentId,
                                                             @RequestBody DepositComment updateDepositComment,
                                                             @ApiParam(value = "Bearer ${jwt token} 형식으로 전송")
                                                             @RequestHeader("Authorization") String jwt) {
        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status;

        try {
            jwt = jwt.replace("Bearer ", "");
            Long memberId = authTokensGenerator.extractMemberId(jwt);

            depositCommentService.updateDepositComment(commentId, memberId, updateDepositComment);
            status = HttpStatus.OK;
            resultMap.put("message", "success");
            resultMap.put("updateDepositComment", updateDepositComment);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            status = HttpStatus.FORBIDDEN;
            resultMap.put("message", "fail");
            resultMap.put("message2", "작성자가 아닙니다.");
        } catch (Exception e) {
            e.printStackTrace();
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            resultMap.put("message", "fail");
            resultMap.put("message2", "jwttoken이 잘못되었거나 commentId가 잘못되었습니다.");
        }

        return new ResponseEntity<>(resultMap, status);
    }
}
