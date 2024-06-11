package org.y2k2.globa.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.dto.*;
import org.y2k2.globa.entity.*;
import org.y2k2.globa.exception.*;
import org.y2k2.globa.mapper.NotificationMapper;
import org.y2k2.globa.repository.*;

@Service
@RequiredArgsConstructor
public class AnswerService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final FirebaseMessaging firebaseMessaging;
    private final AnswerRepository answerRepository;
    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    public void addAnswer(long userId, long inquiryId, RequestAnswerDto dto) {
        UserEntity user = validateUser(userId);
        validateRole(user);

        InquiryEntity inquiry = inquiryRepository.findByInquiryId(inquiryId);
        if (inquiry == null) throw new NotFoundException("Not found inquiry");
        if (inquiry.isSolved()) throw new DuplicatedExcepiton("Answer already exists");

        inquiry.setSolved(true);
        AnswerEntity answer = AnswerEntity.create(user, inquiry, dto.getTitle(), dto.getContent());

        inquiryRepository.save(inquiry);
        answerRepository.save(answer);

        NotificationEntity notification = NotificationMapper.INSTANCE.toNotificationWithInquiry(
                new RequestNotificationWithInquiryDto(user, inquiry.getUser(), inquiry)
        );
        notification.setTypeId(NotificationTypeEnum.INQUIRY.getTypeId());
        notificationRepository.save(notification);

        if (!user.getPrimaryNofi() || user.getNotificationToken() == null) return;

        try {
            Message message = Message.builder()
                    .setToken(user.getNotificationToken())
                    .setNotification(Notification.builder()
                            .setTitle("문의 답변 도착!")
                            .setBody(inquiry.getTitle() + "에 대한 문의 답변이 도착했어요!")
                            .build())
                    .build();

            firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            if (e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT || e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                user.setNotificationToken(null);
                user.setNotificationTokenTime(null);
                userRepository.save(user);
                log.debug("Delete Notification Token : " + user.getUserId());
            }

            log.debug("Failed to send answer notification : " + answer.getAnswerId() + " : " + e.getMessage());
        } catch (Exception e) {
            log.debug("Failed to send answer notification : " + answer.getAnswerId() + " : " + e.getMessage());
        }
    }

    @Transactional
    public void editAnswer(long userId, long inquiryId, long answerId, RequestAnswerDto dto) {
        UserEntity user = validateUser(userId);
        validateRole(user);

        InquiryEntity inquiry = inquiryRepository.findByInquiryId(inquiryId);
        if (inquiry == null) throw new NotFoundException("Not found inquiry");

        AnswerEntity answer = validateAnswer(answerId);

        if (!inquiry.isSolved()) inquiry.setSolved(true);
        answer.setUser(user);
        answer.setTitle(dto.getTitle());
        answer.setContent(dto.getContent());

        inquiryRepository.save(inquiry);
        answerRepository.save(answer);
    }

    @Transactional
    public void deleteAnswer(long userId, long inquiryId, long answerId) {
        UserEntity user = validateUser(userId);
        validateRole(user);

        InquiryEntity inquiry = inquiryRepository.findByInquiryId(inquiryId);
        if (inquiry == null) throw new NotFoundException("Not found inquiry");

        AnswerEntity answer = validateAnswer(answerId);

        if (inquiry.isSolved()) inquiry.setSolved(false);

        inquiryRepository.save(inquiry);
        answerRepository.delete(answer);
    }

    private UserEntity validateUser(long userId) {
        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) throw new InvalidTokenException("Not found user");
        if (user.getDeleted()) throw new BadRequestException("User Deleted ! ");

        return user;
    }

    private void validateRole(UserEntity user) {
        UserRoleEntity role = userRoleRepository.findByUser(user);
        if (role == null) throw new ForbiddenException("Wrong approach");

        String roleName = role.getRoleId().getName();
        boolean isValid = UserRole.ADMIN.getRoleName().equals(roleName) || UserRole.EDITOR.getRoleName().equals(roleName);
        if (!isValid) throw new ForbiddenException("Only admin or editor can write answers");

    }

    private AnswerEntity validateAnswer(long answerId) {
        AnswerEntity answer = answerRepository.findByAnswerId(answerId);
        if (answer == null) throw new NotFoundException("Not found answer");

        return answer;
    }
}
