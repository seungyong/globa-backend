package org.y2k2.globa.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.y2k2.globa.dto.UserDTO;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.exception.BadRequestException;
import org.y2k2.globa.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    public final UserRepository userRepository;

    public ObjectNode getRanking(int page, int count) {
        try {
            Pageable pageable = PageRequest.of(page-1, count);
            Page<UserEntity> rankingEntities =  userRepository.findAll(pageable);


            List<UserEntity> resultValue = rankingEntities.getContent();
            List<ObjectNode> responseList = new ArrayList<>();

            for (UserEntity userEntity : resultValue) {
                ObjectNode rankingNode = createRankingNode(userEntity);
                responseList.add(rankingNode);
            }

            return createResultNode(responseList);

        }catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void errorTestBadRequest() {
        try {

            throw new BadRequestException("Test BAD");

        }catch (BadRequestException e) {
            throw e;
        }
    }

    private ObjectNode createRankingNode(UserEntity userEntity) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode rankingNode = objectMapper.createObjectNode();


        System.out.println(UserDTO.toUserDTO(userEntity));
        rankingNode.put("user", userEntity.getName());

        return rankingNode;
    }

    private ObjectNode createResultNode(List<ObjectNode> responseList) {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode codesArrayNode = objectMapper.createArrayNode();

        for (ObjectNode node : responseList) {
            codesArrayNode.add(node);
        }

        ObjectNode resultNode = objectMapper.createObjectNode();
        resultNode.set("rankings", codesArrayNode);
        resultNode.put("total", responseList.size());

        return resultNode;
    }
}