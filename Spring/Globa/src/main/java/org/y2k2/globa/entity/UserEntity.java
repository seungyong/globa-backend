package org.y2k2.globa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.y2k2.globa.dto.UserDTO;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "app_user")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", columnDefinition = "INT UNSIGNED")
    private Long userId;
    @Column(name = "sns_kind")
    private String snsKind;
    @Column(name = "sns_id")
    private String snsId;
    @Column(name = "code")
    private String code;
    @Column(name = "name")
    private String name;
    @Column(name = "primary_nofi")
    private String primaryNofi;
    @Column(name = "upload_nofi")
    private String uploadNofi;
    @Column(name = "share_nofi")
    private String shareNofi;
    @Column(name = "event_nofi")
    private String eventNofi;
    @Column(name = "profile_size")
    private String profileSize;
    @Column(name = "profile_type")
    private String profileType;
    @Column(name = "profile_path")
    private String profilePath;
    @Column(name = "deleted")
    private Boolean deleted;
    @Column(name = "deleted_time")
    private LocalDateTime deletedTime;
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    public static UserEntity toUserEntity(UserDTO userDTO){
        UserEntity userEntity = new UserEntity();

        userEntity.setUserId(userDTO.getUserId());
        userEntity.setSnsKind(userDTO.getSnsKind());
        userEntity.setSnsId(userDTO.getSnsId());
        userEntity.setCode(userDTO.getCode());
        userEntity.setName(userDTO.getName());
        userEntity.setPrimaryNofi(userDTO.getPrimaryNofi());
        userEntity.setUploadNofi(userDTO.getUploadNofi());
        userEntity.setShareNofi(userDTO.getShareNofi());
        userEntity.setEventNofi(userDTO.getEventNofi());;
        userEntity.setProfilePath(userDTO.getProfilePath());;
        userEntity.setProfileSize(userDTO.getProfileSize());;
        userEntity.setProfileType(userDTO.getProfileType());;
        userEntity.setDeleted(userDTO.getDeleted());;
        userEntity.setDeletedTime(userDTO.getDeletedTime());
        userEntity.setCreatedTime(userDTO.getCreatedTime());

        return userEntity;
    }
}
