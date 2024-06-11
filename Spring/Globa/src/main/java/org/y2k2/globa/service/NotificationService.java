package org.y2k2.globa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.y2k2.globa.dto.NotificationDto;
import org.y2k2.globa.dto.ResponseNotificationDto;
import org.y2k2.globa.entity.NotificationEntity;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.exception.BadRequestException;
import org.y2k2.globa.exception.NotFoundException;
import org.y2k2.globa.mapper.NotificationMapper;
import org.y2k2.globa.repository.NotificationRepository;
import org.y2k2.globa.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public ResponseNotificationDto getNotifications(long userId, int count, int page) {
        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new NotFoundException("User not found");
        }

        if (user.getDeleted()) throw new BadRequestException("User Deleted ! ");

        Pageable pageable = PageRequest.of(page - 1, count);
        Page<NotificationEntity> notificationEntityPage = notificationRepository.findAllByFromUserOrTypeIdIn(pageable, user, new char[]{'1'});
        List<NotificationEntity> notifications = notificationEntityPage.getContent();
        List<NotificationDto> dtos = notifications.stream()
                .map(NotificationMapper.INSTANCE::toResponseNotificationDto)
                .toList();
        long total = notificationEntityPage.getTotalElements();

        return new ResponseNotificationDto(dtos, total);
    }
}
