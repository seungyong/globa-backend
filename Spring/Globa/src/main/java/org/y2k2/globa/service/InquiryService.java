package org.y2k2.globa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.y2k2.globa.dto.*;
import org.y2k2.globa.entity.AnswerEntity;
import org.y2k2.globa.entity.InquiryEntity;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.exception.BadRequestException;
import org.y2k2.globa.exception.ForbiddenException;
import org.y2k2.globa.exception.InvalidTokenException;
import org.y2k2.globa.exception.NotFoundException;
import org.y2k2.globa.mapper.InquiryMapper;
import org.y2k2.globa.repository.AnswerRepository;
import org.y2k2.globa.repository.InquiryRepository;
import org.y2k2.globa.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InquiryService {
    private final InquiryRepository inquiryRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;

    public ResponseInquiryDto getInquiries(long userId, PaginationDto pagination) {
        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) throw new InvalidTokenException("Not found user");
        if (user.getDeleted()) throw new BadRequestException("User Deleted ! ");

        Pageable pageable = PageRequest.of(pagination.getPage() - 1, pagination.getCount());
        Page<InquiryEntity> inquiryEntityPage;

        if (pagination.getSort().getValue().equals("s")) {
            inquiryEntityPage = inquiryRepository.findAllBySolvedIsTrueOrderByCreatedTimeDesc(pageable);
        } else if (pagination.getSort().getValue().equals("n")) {
            inquiryEntityPage = inquiryRepository.findAllBySolvedIsFalseOrderByCreatedTimeDesc(pageable);
        } else {
            inquiryEntityPage = inquiryRepository.findAllByOrderByCreatedTimeDesc(pageable);
        }

        List<InquiryEntity> inquiryEntities = inquiryEntityPage.getContent();
        List<InquiryDto> dtos = inquiryEntities.stream()
                .map(InquiryMapper.INSTANCE::toInquiryDto)
                .toList();
        long total = inquiryEntityPage.getTotalElements();

        return new ResponseInquiryDto(dtos, total);
    }

    public ResponseInquiryDetailDto getInquiry(long userId, long inquiryId) {
        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) throw new InvalidTokenException("Not found user");

        InquiryEntity inquiry = inquiryRepository.findByInquiryId(inquiryId);
        if (inquiry == null) throw new NotFoundException("Not found inquiry");
        else if (!inquiry.getUser().getUserId().equals(userId)) throw new ForbiddenException("This inquiry isn't your own");

        AnswerEntity answer = null;

        if (inquiry.isSolved()) {
            answer = answerRepository.findByInquiry(inquiry);
        }

        if (answer == null) {
            return InquiryMapper.INSTANCE.toResponseInquiryDetailDto(inquiry);
        } else {
            return InquiryMapper.INSTANCE.toResponseInquiryDetailDto(inquiry, answer);
        }
    }

    public long addInquiry(long userId, RequestInquiryDto dto) {
        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) throw new InvalidTokenException("Not found user");

        InquiryEntity inquiry = InquiryEntity.create(user, dto.getTitle(), dto.getContent());
        InquiryEntity response = inquiryRepository.save(inquiry);

        return response.getInquiryId();
    }
}
