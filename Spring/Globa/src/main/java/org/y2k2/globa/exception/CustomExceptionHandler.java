package org.y2k2.globa.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.y2k2.globa.util.Const;

@RestControllerAdvice
@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<Object> handleGlobalException(GlobalException ex, WebRequest request) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode errorNode = objectMapper.createObjectNode();
        errorNode.put("errorCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorNode.put("message",ex.getMessage());

        return new ResponseEntity<>(errorNode, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequestException(BadRequestException ex, WebRequest request) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode errorNode = objectMapper.createObjectNode();
        errorNode.put("errorCode", HttpStatus.BAD_REQUEST.value());
        errorNode.put("message",ex.getMessage());

        return new ResponseEntity<>(errorNode, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(UpdateException.class)
    public ResponseEntity<Object> handleUpdateException(UpdateException ex, WebRequest request) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode errorNode = objectMapper.createObjectNode();
        errorNode.put("errorCode", HttpStatus.BAD_REQUEST.value());
        errorNode.put("message",ex.getMessage());

        return new ResponseEntity<>(errorNode, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Object> handleSQLException(SQLException ex) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode errorNode = objectMapper.createObjectNode();
        errorNode.put("errorCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorNode.put("message",ex.getMessage());

        return new ResponseEntity<>(errorNode, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RecordNameException.class)
    public ResponseEntity<Object> handleRecordNameException(RecordNameException ex, WebRequest request) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode errorNode = objectMapper.createObjectNode();
        errorNode.put("errorCode", Const.CustomErrorCode.RECORD_NAME_DUPLICATED.value());
        errorNode.put("message",ex.getMessage());

        return new ResponseEntity<>(errorNode, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(FolderNameException.class)
    public ResponseEntity<Object> handleFolderNameException(FolderNameException ex, WebRequest request) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode errorNode = objectMapper.createObjectNode();
        errorNode.put("errorCode", Const.CustomErrorCode.FOLDER_NAME_DUPLICATED.value());
        errorNode.put("message",ex.getMessage());

        return new ResponseEntity<>(errorNode, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AuthorizedException.class)
    public ResponseEntity<Object> handleAuthorizedException(AuthorizedException ex) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode errorNode = objectMapper.createObjectNode();
        errorNode.put("errorCode", HttpStatus.UNAUTHORIZED.value());
        errorNode.put("message",ex.getMessage());

        // 적절한 HTTP 상태 코드 선택, 예: HttpStatus.INTERNAL_SERVER_ERROR
        return new ResponseEntity<>(errorNode, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DuplicatedExcepiton.class)
    public ResponseEntity<Object> handleDuplicatedException(DuplicatedExcepiton ex) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode errorNode = objectMapper.createObjectNode();
        errorNode.put("errorCode", HttpStatus.CONFLICT.value());
        errorNode.put("message",ex.getMessage());

        // 적절한 HTTP 상태 코드 선택, 예: HttpStatus.INTERNAL_SERVER_ERROR
        return new ResponseEntity<>(errorNode, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFoundException(NotFoundException ex) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode errorNode = objectMapper.createObjectNode();
        errorNode.put("errorCode", HttpStatus.NOT_FOUND.value());
        errorNode.put("message",ex.getMessage());

        // 적절한 HTTP 상태 코드 선택, 예: HttpStatus.INTERNAL_SERVER_ERROR
        return new ResponseEntity<>(errorNode,HttpStatus.NOT_FOUND);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

    }

    @ExceptionHandler(CompileException.class)
    public ResponseEntity<Object> handleCompileException(CompileException ex) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode errorNode = objectMapper.createObjectNode();
        errorNode.put("errorCode", HttpStatus.BAD_REQUEST.value());
        errorNode.put("message",ex.getMessage());

        // 적절한 HTTP 상태 코드 선택, 예: HttpStatus.INTERNAL_SERVER_ERROR
        return new ResponseEntity<>(errorNode, HttpStatus.BAD_REQUEST);
    }



    // ErrorResponse 클래스 정의

}