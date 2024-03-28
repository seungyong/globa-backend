package org.y2k2.globa.dto;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.y2k2.globa.entity.UserEntity;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class UserDTO implements Serializable {

    private Long userId;
    private String snsKind;
    private String snsId;
    private String code;
    private String name;
    private String primaryNofi;
    private String uploadNofi;
    private String shareNofi;
    private String eventNofi;
    private String profileSize;
    private String profileType;
    private String profilePath;
    private Boolean deleted;
    private LocalDateTime deletedTime;
    private LocalDateTime createdTime;

    public static UserDTO toUserDTO(UserEntity userEntity){
        UserDTO userDTO = new UserDTO();

        userDTO.setUserId(userEntity.getUserId());
        userDTO.setSnsKind(userEntity.getSnsKind());
        userDTO.setSnsId(userEntity.getSnsId());
        userDTO.setCode(userEntity.getCode());
        userDTO.setName(userEntity.getName());
        userDTO.setPrimaryNofi(userEntity.getPrimaryNofi());
        userDTO.setUploadNofi(userEntity.getUploadNofi());
        userDTO.setShareNofi(userEntity.getShareNofi());
        userDTO.setEventNofi(userEntity.getEventNofi());;
        userDTO.setProfilePath(userEntity.getProfilePath());;
        userDTO.setProfileSize(userEntity.getProfileSize());;
        userDTO.setProfileType(userEntity.getProfileType());;
        userDTO.setDeleted(userEntity.getDeleted());;
        userDTO.setDeletedTime(userEntity.getDeletedTime());
        userDTO.setCreatedTime(userEntity.getCreatedTime());

        return userDTO;
    }

}
