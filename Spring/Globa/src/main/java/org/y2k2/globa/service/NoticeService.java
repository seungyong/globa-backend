package org.y2k2.globa.service;

import com.google.cloud.storage.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.y2k2.globa.dto.*;
import org.y2k2.globa.entity.*;
import org.y2k2.globa.exception.*;
import org.y2k2.globa.mapper.NoticeMapper;
import org.y2k2.globa.mapper.NotificationMapper;
import org.y2k2.globa.repository.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final NoticeRepository noticeRepository;
    private final NoticeImageRepository noticeImageRepository;
    private final DummyImageRepository dummyImageRepository;
    private final NotificationRepository notificationRepository;

    @Autowired
    private Bucket bucket;

    public List<NoticeIntroResponseDto> getIntroNotices() {
        List<NoticeEntity> noticeEntities = noticeRepository.findByOrderByCreatedTimeDesc(Limit.of(3));

        return noticeEntities.stream()
                .map(NoticeMapper.INSTANCE::toIntroResponseDto)
                .collect(Collectors.toList());
    }

    public NoticeDetailResponseDto getNoticeDetail(Long noticeId) {
        NoticeEntity noticeEntity = noticeRepository.findByNoticeId(noticeId);

        if (noticeEntity == null) {
            throw new NotFoundException("Not found notice using noticeId : " + noticeId);
        }

        return NoticeMapper.INSTANCE.toDetailResponseDto(noticeEntity);
    }

    @Transactional
    public Long addNotice(Long userId, NoticeAddRequestDto dto) {
        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new InvalidTokenException("Invalid Token");
        }

        if (user.getDeleted()) throw new BadRequestException("User Deleted ! ");

        UserRoleEntity userRole = userRoleRepository.findByUser(user);
        String roleName = userRole.getRoleId().getName();
        boolean isAdminOrEditor = UserRole.ADMIN.getRoleName().equals(roleName) || UserRole.EDITOR.getRoleName().equals(roleName);
        if (!isAdminOrEditor) throw new ForbiddenException("Only admin or editor can write answers");

        NoticeEntity requestEntity = NoticeMapper.INSTANCE.toEntity(dto);
        requestEntity.setUser(user);

        try {
            uploadThumbnail(requestEntity, dto.getThumbnail());
            NoticeEntity noticeEntity = noticeRepository.save(requestEntity);

            if (dto.getImageIds() == null) {
                NotificationEntity notification = NotificationMapper.INSTANCE.toNotificationWithNotice(
                        new RequestNotificationWithNoticeDto(user, noticeEntity)
                );
                notification.setTypeId(NotificationTypeEnum.NOTICE.getTypeId());
                notificationRepository.save(notification);

                return noticeEntity.getNoticeId();
            }

            List<DummyImageEntity> dummyImageEntities = dummyImageRepository.findByImageIdIn(dto.getImageIds());
            List<NoticeImageEntity> noticeImageEntities = new ArrayList<>();

            if (dummyImageEntities.isEmpty()) return noticeEntity.getNoticeId();

            for (DummyImageEntity dummyImageEntity : dummyImageEntities) {
                noticeImageEntities.add(
                        NoticeImageEntity.create(
                                noticeEntity,
                                dummyImageEntity.getImagePath(),
                                dummyImageEntity.getImageSize(),
                                dummyImageEntity.getImageType()
                        )
                );
            }

            dummyImageRepository.deleteAll(dummyImageEntities);
            noticeImageRepository.saveAll(noticeImageEntities);

            NotificationEntity notification = NotificationMapper.INSTANCE.toNotificationWithNotice(
                    new RequestNotificationWithNoticeDto(user, noticeEntity)
            );
            notification.setTypeId(NotificationTypeEnum.NOTICE.getTypeId());
            notificationRepository.save(notification);

            return noticeEntity.getNoticeId();
        } catch (Exception e) {
            if (bucket.get(requestEntity.getThumbnailPath()) != null) {
                bucket.get(requestEntity.getThumbnailPath()).delete();
            }

            throw e;
        }
    }

    private void uploadThumbnail(NoticeEntity notice, MultipartFile file) {
        long current = new Date().getTime();
        long size = file.getSize();
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String mimeType = file.getContentType();

        String path = "notices/thumbnails/" + current + "." + extension;

        try {
            if (bucket.get(path) != null) {
                bucket.get(path).delete();
            }

            bucket.create(path, file.getBytes());

            notice.setThumbnailPath(path);
            notice.setThumbnailSize(size);
            notice.setThumbnailType(mimeType);
        } catch (Exception e) {
            if (bucket.get(path) != null) {
                bucket.get(path).delete();
            }

            throw new FileUploadException(e.getMessage());
        }
    }
}
