package org.y2k2.globa.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.google.cloud.storage.Bucket;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import org.y2k2.globa.Projection.KeywordProjection;
import org.y2k2.globa.Projection.QuizGradeProjection;
import org.y2k2.globa.dto.*;

import org.y2k2.globa.entity.*;

import org.y2k2.globa.exception.NotFoundException;
import org.y2k2.globa.exception.FileUploadException;
import org.y2k2.globa.exception.InvalidTokenException;
import org.y2k2.globa.exception.UnAuthorizedException;
import org.y2k2.globa.exception.BadRequestException;

import org.y2k2.globa.repository.*;
import org.y2k2.globa.util.JwtToken;
import org.y2k2.globa.util.JwtTokenProvider;
import org.y2k2.globa.util.JwtUtil;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private final Bucket bucket;

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtUtil jwtUtil;

    public final UserRepository userRepository;
    public final StudyRepository studyRepository;
    public final SurveyRepository surveyRepository;
    public final FolderRepository folderRepository;
    public final RecordRepository recordRepository;

    public final FolderService folderService;

    public JwtToken reloadRefreshToken(String refreshToken, String accessToken){
        try {
            Long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);
            Date expiredTime = jwtTokenProvider.getExpiredTimeByAccessTokenWithoutCheck(accessToken);
            String redisRefreshToken = jwtUtil.getRefreshToken(userId);

            if(new Date().before(expiredTime)) {
                jwtUtil.deleteValue(String.valueOf(userId));
                throw new BadRequestException("아직 토큰이 살아있습니다.");
            }

            if (!redisRefreshToken.equals(refreshToken)) {
                jwtUtil.deleteValue(String.valueOf(userId));
                throw new BadRequestException("Not equal RefreshToken");
            }

            jwtTokenProvider.getExpirationDateFromToken(redisRefreshToken);


            JwtToken jwtToken = jwtTokenProvider.generateToken(userId);

            jwtUtil.insertRedisRefreshToken(userId, jwtToken.getRefreshToken());

            return jwtToken;
        }
        catch(Exception e){
            throw e;
        }
    }

    public JwtToken postUser(RequestUserPostDTO requestUserPostDTO){

        UserEntity postUserEntity = userRepository.findBySnsId(requestUserPostDTO.getSnsId());

        if(postUserEntity == null) {
            String USER_CODE = generateRandomCode(6);
            UserEntity userEntity = new UserEntity();
            userEntity.setSnsKind(requestUserPostDTO.getSnsKind());
            userEntity.setSnsId(requestUserPostDTO.getSnsId());
            userEntity.setCode(USER_CODE);
            userEntity.setName(requestUserPostDTO.getName());
            userEntity.setProfilePath(requestUserPostDTO.getProfile());
            userEntity.setPrimaryNofi(requestUserPostDTO.getNotification());
            userEntity.setShareNofi(requestUserPostDTO.getNotification());
            userEntity.setUploadNofi(requestUserPostDTO.getNotification());
            userEntity.setEventNofi(requestUserPostDTO.getNotification());
            userEntity.setCreatedTime(LocalDateTime.now());
            userEntity.setDeleted(false);

            postUserEntity = userRepository.save(userEntity);

            folderService.postDefaultFolder(postUserEntity);
        }

        JwtToken jwtToken = jwtTokenProvider.generateToken(postUserEntity.getUserId());

        jwtUtil.insertRedisRefreshToken(postUserEntity.getUserId(), jwtToken.getRefreshToken());


        return jwtToken;

    }

    public ResponseUserDTO getUser(String accessToken){

        Long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken);

        UserEntity userEntity = userRepository.findOneByUserId(userId);
        FolderEntity folderEntity = folderRepository.findFirstByUserUserIdOrderByCreatedTimeAsc(userId);

        if(userEntity.getDeleted())
            throw new BadRequestException("User Deleted ! ");

        if(folderEntity == null)
            throw new NotFoundException("기본 폴더를 찾을 수 없습니다 ! ");
        if(userEntity == null)
            throw new NotFoundException("유저를 찾을 수 없습니다 !");

        ResponseUserDTO responseUserDTO = new ResponseUserDTO();

        responseUserDTO.setProfile(userEntity.getProfilePath());
        responseUserDTO.setName(userEntity.getName());
        responseUserDTO.setCode(userEntity.getCode());
        responseUserDTO.setPublicFolderId(folderEntity.getFolderId());

        return responseUserDTO;

    }

    public ResponseUserSearchDto getUser(String accessToken, String code){

        Long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken); // 사용하지 않아도, 작업을 거치며 토큰 유효성 검사함.

        UserEntity userEntity = userRepository.findOneByCode(code);

        if(userEntity.getDeleted())
            throw new BadRequestException("User Deleted ! ");

        if(userEntity == null)
            throw new NotFoundException("유저를 찾을 수 없습니다 !");

        ResponseUserSearchDto responseUserSearchDto = new ResponseUserSearchDto();

        responseUserSearchDto.setProfile(userEntity.getProfilePath());
        responseUserSearchDto.setName(userEntity.getName());
        responseUserSearchDto.setCode(userEntity.getCode());

        return responseUserSearchDto;

    }

    public NotificationSettingDto getNotification(String accessToken, Long pathUserId){

        Long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken); // 사용하지 않아도, 작업을 거치며 토큰 유효성 검사함.

        if (!Objects.equals(userId, pathUserId)){
            throw new UnAuthorizedException("Not Matched User !");
        }

        UserEntity userEntity = userRepository.findOneByUserId(userId);


        if(userEntity == null)
            throw new NotFoundException("유저를 찾을 수 없습니다 !");

        if(userEntity.getDeleted())
            throw new BadRequestException("User Deleted ! ");

        NotificationSettingDto responseUserNotificationSettingDto = new NotificationSettingDto();

        responseUserNotificationSettingDto.setUploadNofi(userEntity.getUploadNofi());
        responseUserNotificationSettingDto.setShareNofi(userEntity.getShareNofi());
        responseUserNotificationSettingDto.setEventNofi(userEntity.getEventNofi());

        return responseUserNotificationSettingDto;
    }

    @Transactional
    public void updateProfile(MultipartFile file, long userId) {
        UserEntity userEntity = userRepository.findOneByUserId(userId);

        if (userEntity == null) throw new BadRequestException("Not found user");

        if(userEntity.getDeleted())
            throw new BadRequestException("User Deleted ! ");

        long current = new Date().getTime();
        long size = file.getSize();
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String mimeType = file.getContentType();

        String oldPath = userEntity.getProfilePath();
        String newPath = "users/" + userId + "/profile/" + current + "." + extension;

        try {
            bucket.create(newPath, file.getBytes());

            userEntity.setProfilePath(newPath);
            userEntity.setProfileType(mimeType);
            userEntity.setProfileSize(size);
            userRepository.save(userEntity);

            if(oldPath != null && bucket.get(oldPath) != null) {
                try {
                    bucket.get(oldPath).delete();
                } catch (Exception e) {
                    log.error("Failed to delete old profile : " + e);
                }
            }
        } catch (Exception e) {
            if(bucket.get(newPath) != null) {
                bucket.get(newPath).delete();
            }

            log.error("Failed to update profile : " + e);
            throw new FileUploadException("Failed to update profile");
        }
    }

    public ResponseAnalysisDto getAnalysis(String accessToken, Long pathUserId){

        Long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken); // 사용하지 않아도, 작업을 거치며 토큰 유효성 검사함.

        UserEntity userEntity = userRepository.findOneByUserId(userId);

        if (userEntity == null) throw new BadRequestException("Not found user");

        if(userEntity.getDeleted()) throw new BadRequestException("User Deleted ! ");

        if (!Objects.equals(userId, pathUserId)){
            throw new UnAuthorizedException("Not Matched User !");
        }

        List<RecordEntity> recordEntities = recordRepository.findRecordEntitiesByUserUserId(userId);

        if(recordEntities == null)
            throw new NotFoundException("Not Founded Record");

        List<Long> recordIds = new ArrayList<>();

        for(RecordEntity recordEntity : recordEntities){
            recordIds.add(recordEntity.getRecordId());
        }

        List<StudyEntity> studyEntities = studyRepository.findAllByUserUserId(userId);
        List<QuizGradeProjection> quizGradeProjectionList = userRepository.findQuizGradeByUser(userId);
        List<KeywordProjection> keywordProjectionList = userRepository.findKeywordByRecordIds(recordIds);

        ResponseAnalysisDto responseAnalysisDto = new ResponseAnalysisDto();
        List<ResponseStudyTimesDto> studyTimes = new ArrayList<>();
        List<ResponseQuizGradeDto> quizGrades = new ArrayList<>();
        List<ResponseKeywordDto> keywords = new ArrayList<>();

        for( StudyEntity studyEntitiy : studyEntities ){
            ResponseStudyTimesDto responseStudyTimesDto = new ResponseStudyTimesDto();
            responseStudyTimesDto.setStudyTime(studyEntitiy.getStudyTime());
            responseStudyTimesDto.setCreatedTime(studyEntitiy.getCreatedTime());
            studyTimes.add(responseStudyTimesDto);
        }

        for( QuizGradeProjection quizGradeProjection : quizGradeProjectionList ){
            ResponseQuizGradeDto responseQuizGradeDto = new ResponseQuizGradeDto();
            responseQuizGradeDto.setQuizGrade(quizGradeProjection.getQuizGrade());
            responseQuizGradeDto.setCreatedTime(quizGradeProjection.getCreatedTime());
            quizGrades.add(responseQuizGradeDto);
        }

        for( KeywordProjection keywordProjection : keywordProjectionList ){
            ResponseKeywordDto responseKeywordDto = new ResponseKeywordDto();
            responseKeywordDto.setWord(keywordProjection.getWord());
            responseKeywordDto.setImportance(keywordProjection.getImportance());
            keywords.add(responseKeywordDto);
        }

        responseAnalysisDto.setStudyTimes(studyTimes);
        responseAnalysisDto.setQuizGrades(quizGrades);
        responseAnalysisDto.setKeywords(keywords);
        return responseAnalysisDto;
    }

    public NotificationSettingDto putNotification(String accessToken, Long putUserId, NotificationSettingDto NotificationSettingDto){

        Long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken); // 사용하지 않아도, 작업을 거치며 토큰 유효성 검사함.

        if (!Objects.equals(userId, putUserId)){
            throw new UnAuthorizedException("Not Matched User ");
        }

        UserEntity userEntity = userRepository.findOneByUserId(userId);

        if (userEntity == null) throw new BadRequestException("Not found user");
        if(userEntity.getDeleted()) throw new BadRequestException("User Deleted ! ");

        userEntity.setUploadNofi(NotificationSettingDto.getUploadNofi());
        userEntity.setShareNofi(NotificationSettingDto.getShareNofi());
        userEntity.setEventNofi(NotificationSettingDto.getEventNofi());

        UserEntity savedEntity = userRepository.save(userEntity);

        NotificationSettingDto responseUserNotificationSettingDto = new NotificationSettingDto();

        responseUserNotificationSettingDto.setUploadNofi(savedEntity.getUploadNofi());
        responseUserNotificationSettingDto.setShareNofi(savedEntity.getShareNofi());
        responseUserNotificationSettingDto.setEventNofi(savedEntity.getEventNofi());

        return responseUserNotificationSettingDto;
    }

    public HttpStatus patchUserName(String accessToken, Long putUserId, String name){
        Long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken); // 사용하지 않아도, 작업을 거치며 토큰 유효성 검사함.

        if (!Objects.equals(userId, putUserId)){
            throw new UnAuthorizedException("Not Matched User ");
        }

        UserEntity userEntity = userRepository.findOneByUserId(userId);

        if (userEntity == null) throw new BadRequestException("Not found user");
        if(userEntity.getDeleted()) throw new BadRequestException("User Deleted ! ");

        userEntity.setName(name);

        userRepository.save(userEntity);


        return HttpStatus.OK;
    }


    public HttpStatus deleteUser(String accessToken, RequestSurveyDto requestSurveyDto){
        Long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken); // 사용하지 않아도, 작업을 거치며 토큰 유효성 검사함.

        UserEntity userEntity = userRepository.findOneByUserId(userId);

        if (userEntity == null) throw new BadRequestException("Not found user");
        if(userEntity.getDeleted()) throw new BadRequestException("User Deleted ! ");

        userEntity.setDeleted(true);
        userEntity.setDeletedTime(LocalDateTime.now());

        SurveyEntity surveyEntity = new SurveyEntity();
        surveyEntity.setSurveyType(String.valueOf(requestSurveyDto.getSurveyType()));
        surveyEntity.setContent(requestSurveyDto.getContent());
        surveyEntity.setCreatedTime(LocalDateTime.now());

        userRepository.save(userEntity);
        surveyRepository.save(surveyEntity);

        folderService.deleteDefaultFolder(userEntity);

        return HttpStatus.OK;
    }
  
    public String generateRandomCode(int length){
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new SecureRandom();

        StringBuilder code = new StringBuilder();

        for(int i = 0 ; i < length; ++i ){
            int index = random.nextInt(characters.length());
            code.append(characters.charAt(index));

        }

        return code.toString();
    }

    public void addAndUpdateNotificationToken(RequestNotificationTokenDto requestNotificationTokenDto, Long userId){
        UserEntity userEntity = userRepository.findOneByUserId(userId);
        if (userEntity == null) throw new BadRequestException("Not found user");

        userEntity.setNotificationToken(requestNotificationTokenDto.getToken());
        userEntity.setNotificationTokenTime(LocalDateTime.now());
        userRepository.save(userEntity);
    }
}