package org.y2k2.globa.util;

import org.springframework.http.HttpStatus;


public class Const {
    public enum CustomErrorCode {
        FOLDER_DELETE_BAD_REQUEST(40010,  HttpStatus.Series.CLIENT_ERROR, "Default Folder Cannot Be Deleted"),
        INVALID_TOKEN(40110,  HttpStatus.Series.CLIENT_ERROR, "Access Token Invalid"),
        EXPIRED_ACCESS_TOKEN(40120,  HttpStatus.Series.CLIENT_ERROR, "Access Token Expired ! "),
        EXPIRED_REFRESH_TOKEN(40130,  HttpStatus.Series.CLIENT_ERROR, "Refresh Token Expired ! "),
        RECORD_NAME_DUPLICATED(40910,  HttpStatus.Series.CLIENT_ERROR, "Record Name Duplicated"),
        FOLDER_NAME_DUPLICATED(40920,  HttpStatus.Series.CLIENT_ERROR, "Folder Name Duplicated"),
        // ... 추가적인 에러 코드
        ;

        private final int code;
        private final HttpStatus.Series series;
        private final String message;

        CustomErrorCode(int code, HttpStatus.Series series, String message) {
            this.code = code;
            this.series = series;
            this.message = message;
        }

        public int value() {
            return this.code;
        }

        // Getter 메소드
    }

}
